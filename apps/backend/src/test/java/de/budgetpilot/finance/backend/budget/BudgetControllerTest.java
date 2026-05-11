package de.budgetpilot.finance.backend.budget;

import de.budgetpilot.finance.backend.auth.AbstractPostgresIntegrationTest;
import de.budgetpilot.finance.backend.auth.service.AuthUserStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class BudgetControllerTest extends AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    @BeforeEach
    void setUp() {
        authUserStore.clear();
    }

    @Test
    void memberCanCreateBudgetSetItemAndReadSummary() throws Exception {
        String accessToken = registerAndGetAccessToken("budget@example.com");
        String organizationId = createOrganization(accessToken, "Budget Org", "budget-org");

        String categoryId = createCategory(accessToken, organizationId);
        String accountId = createAccount(accessToken, organizationId);

        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        String budgetId = createBudget(accessToken, organizationId, periodStart);
        upsertItem(accessToken, organizationId, budgetId, categoryId, 5000);

        createExpenseTransaction(accessToken, organizationId, accountId, categoryId, 1234);

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/budgets/{budgetId}/summary", organizationId, budgetId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalBudgetCents").value(5000))
                .andExpect(jsonPath("$.totalExpenseCents").value(1234));
    }

    @Test
    void nonMemberCannotAccessBudgets() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-budget@example.com");
        String outsiderToken = registerAndGetAccessToken("outsider-budget@example.com");
        String organizationId = createOrganization(ownerToken, "Budget Org 2", "budget-org-2");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/budgets", organizationId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
    }

    private String createBudget(String accessToken, String organizationId, LocalDate periodStart) throws Exception {
        String response = mockMvc.perform(post("/api/v1/organizations/{organizationId}/budgets", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "May Budget",
                                  "periodStart": "%s",
                                  "currency": "EUR"
                                }
                                """.formatted(periodStart)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractJsonValue(response, "id");
    }

    private void upsertItem(String accessToken, String organizationId, String budgetId, String categoryId, long amountCents) throws Exception {
        mockMvc.perform(put("/api/v1/organizations/{organizationId}/budgets/{budgetId}/items", organizationId, budgetId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": "%s",
                                  "amountCents": %d
                                }
                                """.formatted(categoryId, amountCents)))
                .andExpect(status().isOk());
    }

    private void createExpenseTransaction(
            String accessToken,
            String organizationId,
            String accountId,
            String categoryId,
            long amountCents
    ) throws Exception {
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/transactions", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "categoryId": "%s",
                                  "amountCents": %d,
                                  "currency": "EUR",
                                  "bookedAt": "%s",
                                  "description": "Budget Expense"
                                }
                                """.formatted(accountId, categoryId, amountCents, OffsetDateTime.now())))
                .andExpect(status().isCreated());
    }

    private String createAccount(String accessToken, String organizationId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Main Account",
                                  "currency": "EUR"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractJsonValue(response, "id");
    }

    private String createCategory(String accessToken, String organizationId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/organizations/{organizationId}/categories", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Food",
                                  "type": "EXPENSE"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractJsonValue(response, "id");
    }

    private String createOrganization(String accessToken, String name, String slug) throws Exception {
        String response = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "slug": "%s"
                                }
                                """.formatted(name, slug)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractJsonValue(response, "id");
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        String response = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractJsonValue(response, "accessToken");
    }

    private String extractJsonValue(String json, String key) {
        String pattern = "\"%s\":\"([^\"]+)\"".formatted(key);
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("JSON key not found: " + key);
    }
}

