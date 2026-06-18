package de.budgetpilot.finance.backend.organization;

import de.budgetpilot.finance.backend.auth.service.AuthUserStore;
import de.budgetpilot.finance.backend.auth.repository.AuthUserRepository;
import de.budgetpilot.finance.backend.auth.domain.AuthUserEntity;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationRepository;
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class OrganizationControllerTest extends de.budgetpilot.finance.backend.auth.AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    @Autowired
    private OrganizationMembershipRepository organizationMembershipRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

    @Autowired
    private AuthUserRepository authUserRepository;

    @BeforeEach
    void setUp() {
        organizationMembershipRepository.deleteAll();
        organizationRepository.deleteAll();
        authUserStore.clear();
    }

    @Test
    void createAndGetOrganizationWorksForOwner() throws Exception {
        String accessToken = registerAndGetAccessToken("owner@example.com");
        String createResponse = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Budget Team",
                                  "slug": "budget-team"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Budget Team"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        String organizationId = extractJsonValue(createResponse, "id");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}", organizationId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("budget-team"));

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].role").value("OWNER"))
                .andExpect(jsonPath("$[0].email").value("owner@example.com"));
    }

    @Test
    void ownerCanAddMemberDirectly() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-add@example.com");
        String organizationId = createOrganization(ownerToken, "Add Org", "add-org");

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new-member@example.com",
                                  "password": "Password123!",
                                  "role": "MEMBER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new-member@example.com"))
                .andExpect(jsonPath("$.role").value("MEMBER"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new-member@example.com",
                                  "password": "Password123!"
                                }
                                """))
                .andExpect(status().isOk());
    }

    @Test
    void ownerCanAddExistingUserWithoutPassword() throws Exception {
        registerAndGetAccessToken("existing-user@example.com");
        String ownerToken = registerAndGetAccessToken("owner-existing@example.com");
        String organizationId = createOrganization(ownerToken, "Existing Org", "existing-org");

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "existing-user@example.com",
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("existing-user@example.com"))
                .andExpect(jsonPath("$.role").value("VIEWER"));
    }

    @Test
    void nonMemberCannotAccessOrganization() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner2@example.com");
        String outsiderToken = registerAndGetAccessToken("outsider@example.com");

        String createResponse = mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Finance Guild",
                                  "slug": "finance-guild"
                                }
                                """))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String organizationId = extractJsonValue(createResponse, "id");

        mockMvc.perform(get("/api/v1/organizations/{organizationId}", organizationId)
                        .header("Authorization", "Bearer " + outsiderToken))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value("ORGANIZATION_FORBIDDEN"));
    }

    @Test
    void ownerCanChangeMemberRoleAndRemoveMember() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner3@example.com");
        String memberToken = registerAndGetAccessToken("member1@example.com");

        String organizationId = createOrganization(ownerToken, "Org", "org-1");
        String inviteToken = createInvite(ownerToken, organizationId, "member1@example.com", "MEMBER");
        acceptInvite(memberToken, inviteToken);

        UUID memberId = findUserIdByEmail("member1@example.com");

        mockMvc.perform(patch("/api/v1/organizations/{organizationId}/members/{userId}/role", organizationId, memberId)
                        .header("Authorization", "Bearer " + ownerToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/v1/organizations/{organizationId}/members/{userId}", organizationId, memberId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void memberCannotChangeRoles() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner4@example.com");
        String memberToken = registerAndGetAccessToken("member2@example.com");

        String organizationId = createOrganization(ownerToken, "Org2", "org-2");
        String inviteToken = createInvite(ownerToken, organizationId, "member2@example.com", "MEMBER");
        acceptInvite(memberToken, inviteToken);

        UUID ownerId = findUserIdByEmail("owner4@example.com");

        mockMvc.perform(patch("/api/v1/organizations/{organizationId}/members/{userId}/role", organizationId, ownerId)
                        .header("Authorization", "Bearer " + memberToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "role": "VIEWER"
                                }
                                """))
                .andExpect(status().isForbidden());
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

    private String createInvite(String accessToken, String organizationId, String email, String role) throws Exception {
        String inviteResponse = mockMvc.perform(post("/api/v1/organizations/{organizationId}/invites", organizationId)
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
        return extractJsonValue(inviteResponse, "token");
    }

    private void acceptInvite(String accessToken, String token) throws Exception {
        mockMvc.perform(post("/api/v1/invites/{token}/accept", token)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }

    private @NonNull UUID findUserIdByEmail(@NonNull String email) {
        AuthUserEntity entity = authUserRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalStateException("User not found in database: " + email));
        return entity.getId();
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
