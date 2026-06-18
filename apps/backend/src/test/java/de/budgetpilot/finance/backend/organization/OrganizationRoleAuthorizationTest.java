package de.budgetpilot.finance.backend.organization;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Niklas Petermeier
 * @since 11.05.2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrganizationRoleAuthorizationTest extends AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    @BeforeEach
    void setUp() {
        authUserStore.clear();
    }

    @Test
    void viewerCanReadButCannotWriteOrganizationData() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-roles@example.com");
        String viewerToken = registerAndGetAccessToken("viewer-roles@example.com");
        String organizationId = createOrganization(ownerToken, "Role Org", "role-org");

        String accountId = createAccount(ownerToken, organizationId);
        String categoryId = createCategory(ownerToken, organizationId);
        createTransaction(ownerToken, organizationId, accountId, categoryId);

        String inviteToken = createInvite(ownerToken, organizationId, "viewer-roles@example.com", "VIEWER");
        acceptInvite(viewerToken, inviteToken);

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/reports/cashflow", organizationId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Viewer Account",
                                  "currency": "EUR"
                                }
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/transactions", organizationId)
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "categoryId": "%s",
                                  "amountCents": 500,
                                  "currency": "EUR",
                                  "bookedAt": "%s",
                                  "description": "Blocked"
                                }
                                """.formatted(accountId, categoryId, OffsetDateTime.now())))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanWriteAccountingButCannotManageInvites() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-roles-2@example.com");
        String memberToken = registerAndGetAccessToken("member-roles@example.com");
        String organizationId = createOrganization(ownerToken, "Role Org 2", "role-org-2");

        String inviteToken = createInvite(ownerToken, organizationId, "member-roles@example.com", "MEMBER");
        acceptInvite(memberToken, inviteToken);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Member Account",
                                  "currency": "EUR"
                                }
                                """))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/invites", organizationId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "blocked@example.com",
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void viewerCannotCreateBudgets() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-roles-3@example.com");
        String viewerToken = registerAndGetAccessToken("viewer-roles-3@example.com");
        String organizationId = createOrganization(ownerToken, "Role Org 3", "role-org-3");

        String inviteToken = createInvite(ownerToken, organizationId, "viewer-roles-3@example.com", "VIEWER");
        acceptInvite(viewerToken, inviteToken);

        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/budgets", organizationId)
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Viewer Budget",
                                  "periodStart": "%s",
                                  "currency": "EUR"
                                }
                                """.formatted(periodStart)))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCanCreateBudgetAndViewerCanReadSummary() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-roles-4@example.com");
        String memberToken = registerAndGetAccessToken("member-roles-4@example.com");
        String viewerToken = registerAndGetAccessToken("viewer-roles-4@example.com");
        String organizationId = createOrganization(ownerToken, "Role Org 4", "role-org-4");

        String memberInvite = createInvite(ownerToken, organizationId, "member-roles-4@example.com", "MEMBER");
        acceptInvite(memberToken, memberInvite);

        String viewerInvite = createInvite(ownerToken, organizationId, "viewer-roles-4@example.com", "VIEWER");
        acceptInvite(viewerToken, viewerInvite);

        LocalDate periodStart = LocalDate.now().withDayOfMonth(1);
        String budgetId = createBudget(memberToken, organizationId, periodStart);

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/budgets", organizationId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/budgets/{budgetId}/summary", organizationId, budgetId)
                        .header("Authorization", "Bearer " + viewerToken))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/v1/organizations/{organizationId}/budgets/{budgetId}/items", organizationId, budgetId)
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "categoryId": "%s",
                                  "amountCents": 1000
                                }
                                """.formatted(createCategory(memberToken, organizationId))))
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

    private void createTransaction(
            String accessToken,
            String organizationId,
            String accountId,
            String categoryId
    ) throws Exception {
        mockMvc.perform(post("/api/v1/organizations/{organizationId}/transactions", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "accountId": "%s",
                                  "categoryId": "%s",
                                  "amountCents": 1000,
                                  "currency": "EUR",
                                  "bookedAt": "%s",
                                  "description": "Setup"
                                }
                                """.formatted(accountId, categoryId, OffsetDateTime.now())))
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

    private String createInvite(String accessToken, String organizationId, String email, String role) throws Exception {
        String response = mockMvc.perform(post("/api/v1/organizations/{organizationId}/invites", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "role": "%s"
                                }
                                """.formatted(email, role)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractJsonValue(response, "token");
    }

    private void acceptInvite(String accessToken, String token) throws Exception {
        mockMvc.perform(post("/api/v1/invites/{token}/accept", token)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
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
