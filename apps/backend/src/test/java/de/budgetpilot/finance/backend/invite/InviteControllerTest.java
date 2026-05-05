package de.budgetpilot.finance.backend.invite;

import de.budgetpilot.finance.backend.auth.AbstractPostgresIntegrationTest;
import de.budgetpilot.finance.backend.auth.service.AuthUserStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class InviteControllerTest extends AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    /**
     * Clears persisted auth state before each test.
     */
    @BeforeEach
    void setUp() {
        authUserStore.clear();
    }

    /**
     * Verifies owner invite creation and successful acceptance flow.
     *
     * @throws Exception on MockMvc failures
     */
    @Test
    void ownerCanCreateAndInviteeCanAccept() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner-invite@example.com");
        String inviteeToken = registerAndGetAccessToken("invitee@example.com");
        String organizationId = createOrganization(ownerToken, "Invite Org", "invite-org");
        String inviteToken = createInvite(ownerToken, organizationId, "invitee@example.com", "MEMBER");

        mockMvc.perform(post("/api/v1/invites/{token}/accept", inviteToken)
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/organizations/{organizationId}/members", organizationId)
                        .header("Authorization", "Bearer " + ownerToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[1].role").value("MEMBER"));
    }

    /**
     * Verifies that members cannot create invites.
     *
     * @throws Exception on MockMvc failures
     */
    @Test
    void memberCannotCreateInvite() throws Exception {
        String ownerToken = registerAndGetAccessToken("owner2-invite@example.com");
        String inviteeToken = registerAndGetAccessToken("invitee2@example.com");
        String organizationId = createOrganization(ownerToken, "Invite Org 2", "invite-org-2");
        String inviteToken = createInvite(ownerToken, organizationId, "invitee2@example.com", "MEMBER");

        mockMvc.perform(post("/api/v1/invites/{token}/accept", inviteToken)
                        .header("Authorization", "Bearer " + inviteeToken))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/organizations/{organizationId}/invites", organizationId)
                        .header("Authorization", "Bearer " + inviteeToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "new-member@example.com",
                                  "role": "MEMBER"
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    /**
     * Creates an organization via API and returns its ID.
     *
     * @param accessToken authenticated access token
     * @param name organization name
     * @param slug organization slug
     * @return created organization ID
     * @throws Exception on MockMvc failures
     */
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

    /**
     * Creates an invitation via API and returns token.
     *
     * @param accessToken authenticated access token
     * @param organizationId organization identifier
     * @param email invitee email
     * @param role invited role
     * @return created invite token
     * @throws Exception on MockMvc failures
     */
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

    /**
     * Registers a user and returns access token.
     *
     * @param email user email
     * @return access token
     * @throws Exception on MockMvc failures
     */
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

    /**
     * Extracts a flat JSON string value by key.
     *
     * @param json JSON payload
     * @param key target key
     * @return extracted value
     */
    private String extractJsonValue(String json, String key) {
        String pattern = "\"%s\":\"([^\"]+)\"".formatted(key);
        Matcher matcher = Pattern.compile(pattern).matcher(json);
        if (matcher.find()) {
            return matcher.group(1);
        }
        throw new IllegalStateException("JSON key not found: " + key);
    }
}
