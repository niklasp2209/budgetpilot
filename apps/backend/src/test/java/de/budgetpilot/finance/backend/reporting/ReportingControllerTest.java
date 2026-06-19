package de.budgetpilot.finance.backend.reporting;

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
class ReportingControllerTest extends AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    @BeforeEach
    void setUp() {
        authUserStore.clear();
    }

    @Test
    void memberCanReadCashflowByCategoryAndBudgetVsActual() throws Exception {
        String accessToken = registerAndGetAccessToken("reporting@example.com");
        String organizationId = createOrganization(accessToken, "Reporting Org", "reporting-org");

        String expenseCategoryId = createCategory(accessToken, organizationId, "Food", "EXPENSE");
        String incomeCategoryId = createCategory(accessToken, organizationId, "Salary", "INCOME");
        String accountId = createAccount(accessToken, organizationId);

        createTransaction(accessToken, organizationId, accountId, expenseCategoryId, 1500, "EXPENSE");
        createTransaction(accessToken, organizationId, accountId, incomeCategoryId, 5000, "INCOME");

        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        String budgetId = createBudget(accessToken, organizationId, periodStart);
        upsertItem(accessToken, organizationId, budgetId, expenseCategoryId, 2000);

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reports/cashflow", organizationId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.incomeCents").value(5000))
                .andExpect(jsonPath("$.expenseCents").value(1500))
                .andExpect(jsonPath("$.netCents").value(3500));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reports/by-category", organizationId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].categoryName").value("Food"))
                .andExpect(jsonPath("$[0].amountCents").value(1500));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reports/budget-vs-actual", organizationId)
                        .param("budgetId", budgetId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.budgetId").value(budgetId))
                .andExpect(jsonPath("$.items.length()").value(1))
                .andExpect(jsonPath("$.items[0].budgetCents").value(2000))
                .andExpect(jsonPath("$.items[0].actualCents").value(1500));
    }

    @Test
    void nonMemberCannotAccessReports() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-reporting@example.com");
        String outsiderToken = registerAndGetAccessToken("outsider-reporting@example.com");
        String organizationId = createOrganization(ownerToken, "Reporting Org 2", "reporting-org-2");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reports/cashflow", organizationId)
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
                                  "periodStart": "%s"
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

    private void createTransaction(
            String accessToken,
            String organizationId,
            String accountId,
            String categoryId,
            long amountCents,
            String description
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
                                  "description": "%s"
                                }
                                """.formatted(accountId, categoryId, amountCents, OffsetDateTime.now(), description)))
                .andExpect(status().isCreated());
    }

    private String createAccount(String accessToken, String organizationId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Main Account"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractJsonValue(response, "id");
    }

    private String createCategory(String accessToken, String organizationId, String name, String type) throws Exception {
        String response = mockMvc.perform(post("/api/v1/organizations/{organizationId}/categories", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "type": "%s"
                                }
                                """.formatted(name, type)))
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
