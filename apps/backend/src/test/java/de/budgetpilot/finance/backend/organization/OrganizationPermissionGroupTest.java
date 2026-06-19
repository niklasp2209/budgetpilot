package de.budgetpilot.finance.backend.organization;

import de.budgetpilot.finance.backend.auth.AbstractPostgresIntegrationTest;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.auth.service.AuthUserStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Niklas Petermeier
 * @since 18.06.2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrganizationPermissionGroupTest extends AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    @Autowired
    private AuthUserRepository authUserRepository;

    @BeforeEach
    void setUp() {
        authUserStore.clear();
    }

    @Test
    void adminCanCreateGroupAndViewerGainsWritePermissionViaAssignment() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-pg@example.com");
        String viewerToken = registerAndGetAccessToken("viewer-pg@example.com");
        String organizationId = createOrganization(ownerToken, "PG Org", "pg-org");

        String inviteToken = createInvite(ownerToken, organizationId, "viewer-pg@example.com", "VIEWER");
        acceptInvite(viewerToken, inviteToken);

        String groupId = createPermissionGroup(ownerToken, organizationId);
        UUID viewerId = findUserIdByEmail("viewer-pg@example.com");
        assignGroups(ownerToken, organizationId, viewerId, groupId);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Viewer Account"
                                }
                                """))
                .andExpect(status().isCreated());
    }

    @Test
    void viewerWithoutGroupCannotCreateAccount() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-pg-2@example.com");
        String viewerToken = registerAndGetAccessToken("viewer-pg-2@example.com");
        String organizationId = createOrganization(ownerToken, "PG Org 2", "pg-org-2");

        String inviteToken = createInvite(ownerToken, organizationId, "viewer-pg-2@example.com", "VIEWER");
        acceptInvite(viewerToken, inviteToken);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/accounts", organizationId)
                        .header("Authorization", "Bearer " + viewerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Blocked Account"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void memberCannotManagePermissionGroups() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-pg-3@example.com");
        String memberToken = registerAndGetAccessToken("member-pg-3@example.com");
        String organizationId = createOrganization(ownerToken, "PG Org 3", "pg-org-3");

        String inviteToken = createInvite(ownerToken, organizationId, "member-pg-3@example.com", "MEMBER");
        acceptInvite(memberToken, inviteToken);

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/permission-groups", organizationId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Blocked Group",
                                  "permissions": ["ACCOUNTING_WRITE"]
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanListAndUpdatePermissionGroups() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-pg-4@example.com");
        String organizationId = createOrganization(ownerToken, "PG Org 4", "pg-org-4");
        String groupId = createPermissionGroup(ownerToken, organizationId);

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/permission-groups", organizationId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(groupId))
                .andExpect(jsonPath("$[0].permissions[0]").value("ACCOUNTING_WRITE"));

        mockMvc.perform(put("/api/v1/organizations/{organizationId}/permission-groups/{groupId}", organizationId, groupId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Writers",
                                  "permissions": ["ACCOUNTING_WRITE", "BUDGET_WRITE"]
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Writers"))
                .andExpect(jsonPath("$.permissions.length()").value(2));
    }

    private String createPermissionGroup(String accessToken, String organizationId) throws Exception {
        String response = mockMvc.perform(post("/api/v1/organizations/{organizationId}/permission-groups", organizationId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Account Writers",
                                  "permissions": ["ACCOUNTING_WRITE"]
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return extractJsonValue(response, "id");
    }

    private void assignGroups(String accessToken, String organizationId, UUID userId, String groupId) throws Exception {
        mockMvc.perform(put("/api/v1/organizations/{organizationId}/members/{userId}/permission-groups", organizationId, userId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "groupIds": ["%s"]
                                }
                                """.formatted(groupId)))
                .andExpect(status().isNoContent());
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

    private @NonNull UUID findUserIdByEmail(@NonNull String email) {
        return authUserRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("User not found: " + email))
                .getId();
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
