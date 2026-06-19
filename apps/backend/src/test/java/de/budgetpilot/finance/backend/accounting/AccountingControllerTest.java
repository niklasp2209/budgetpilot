package de.budgetpilot.finance.backend.accounting;

import de.budgetpilot.finance.backend.auth.AbstractPostgresIntegrationTest;
import de.budgetpilot.finance.backend.auth.service.AuthUserStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.OffsetDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class AccountingControllerTest extends AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    @BeforeEach
    void setUp() {
        authUserStore.clear();
    }

    @Test
    void memberCanCreateAndListAccountingResources() throws Exception {
        String accessToken = registerAndGetAccessToken("acc@example.com");
        String organizationId = createOrganization(accessToken, "Acc Org", "acc-org");

        String accountId = createAccount(accessToken, organizationId);
        String categoryId = createCategory(accessToken, organizationId);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/transactions", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "categoryId": "%s",
                                  "amountCents": 1234,
                                  "currency": "EUR",
                                  "bookedAt": "%s",
                                  "description": "Lunch"
                                }
                                """.formatted(accountId, categoryId, OffsetDateTime.now())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amountCents").value(1234));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].currency").value("EUR"));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/categories", organizationId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("EXPENSE"));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/transactions", organizationId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].description").value("Lunch"));
    }

    @Test
    void memberCanDeleteAccountingResources() throws Exception {
        String accessToken = registerAndGetAccessToken("delete-acc@example.com");
        String organizationId = createOrganization(accessToken, "Delete Org", "delete-org");

        String accountId = createAccount(accessToken, organizationId);
        String categoryId = createCategory(accessToken, organizationId);

        String transactionResponse = mockMvc.perform(post("/api/v1/organizations/{organizationId}/transactions", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "categoryId": "%s",
                                  "amountCents": 500,
                                  "currency": "EUR",
                                  "description": "Coffee"
                                }
                                """.formatted(accountId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String transactionId = extractJsonValue(transactionResponse, "id");

        mockMvc.perform(delete("/api/v1/organizations/{organizationId}/transactions/{transactionId}", organizationId, transactionId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/organizations/{organizationId}/accounts/{accountId}", organizationId, accountId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/organizations/{organizationId}/categories/{categoryId}", organizationId, categoryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    @Test
    void cannotDeleteAccountWithTransactions() throws Exception {
        String accessToken = registerAndGetAccessToken("conflict-acc@example.com");
        String organizationId = createOrganization(accessToken, "Conflict Org", "conflict-org");

        String accountId = createAccount(accessToken, organizationId);
        String categoryId = createCategory(accessToken, organizationId);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/transactions", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "categoryId": "%s",
                                  "amountCents": 100,
                                  "currency": "EUR"
                                }
                                """.formatted(accountId, categoryId)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/organizations/{organizationId}/accounts/{accountId}", organizationId, accountId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    void memberCanUpdateTransaction() throws Exception {
        String accessToken = registerAndGetAccessToken("update-acc@example.com");
        String organizationId = createOrganization(accessToken, "Update Org", "update-org");

        String accountId = createAccount(accessToken, organizationId);
        String categoryId = createCategory(accessToken, organizationId);

        String transactionResponse = mockMvc.perform(post("/api/v1/organizations/{organizationId}/transactions", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "categoryId": "%s",
                                  "amountCents": 1000,
                                  "currency": "EUR",
                                  "description": "Old"
                                }
                                """.formatted(accountId, categoryId)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        String transactionId = extractJsonValue(transactionResponse, "id");

        mockMvc.perform(put("/api/v1/organizations/{organizationId}/transactions/{transactionId}", organizationId, transactionId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "categoryId": "%s",
                                  "amountCents": 2500,
                                  "bookedAt": "%s",
                                  "description": "Updated"
                                }
                                """.formatted(accountId, categoryId, OffsetDateTime.now())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amountCents").value(2500))
                .andExpect(jsonPath("$.description").value("Updated"));
    }

    @Test
    void nonMemberCannotAccessAccountingEndpoints() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-acc@example.com");
        String outsiderToken = registerAndGetAccessToken("outsider-acc@example.com");
        String organizationId = createOrganization(ownerToken, "Acc Org 2", "acc-org-2");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden());
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
        String createResponse = mockMvc.perform(post("/api/v1/organizations")
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
        return extractJsonValue(createResponse, "id");
    }

    private String registerAndGetAccessToken(String email) throws Exception {
        String registerResponse = mockMvc.perform(post("/api/v1/auth/register")
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
        return extractJsonValue(registerResponse, "accessToken");
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

