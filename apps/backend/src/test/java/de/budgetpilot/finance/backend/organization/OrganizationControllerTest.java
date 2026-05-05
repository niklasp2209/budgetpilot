package de.budgetpilot.finance.backend.organization;

import de.budgetpilot.finance.backend.auth.service.AuthUserStore;
import de.budgetpilot.finance.backend.organization.repository.OrganizationMembershipRepository;
import de.budgetpilot.finance.backend.organization.repository.OrganizationRepository;
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
class OrganizationControllerTest extends de.budgetpilot.finance.backend.auth.AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    @Autowired
    private OrganizationMembershipRepository organizationMembershipRepository;

    @Autowired
    private OrganizationRepository organizationRepository;

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
                .andExpect(jsonPath("$[0].role").value("OWNER"));
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
