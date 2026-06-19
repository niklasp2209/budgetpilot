package de.budgetpilot.finance.backend.me;

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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Niklas Petermeier
 * @since 06.05.2026
 */
@SpringBootTest
@AutoConfigureMockMvc
class MeControllerTest extends AbstractPostgresIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuthUserStore authUserStore;

    @BeforeEach
    void setUp() {
        authUserStore.clear();
    }

    @Test
    void meAndMyOrganizationsWork() throws Exception {
        String accessToken = registerAndGetAccessToken("me@example.com");

        mockMvc.perform(get("/api/v1/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("me@example.com"))
                .andExpect(jsonPath("$.id").isNotEmpty());

        createOrganization(accessToken, "Team A", "team-a");

        mockMvc.perform(get("/api/v1/me/organizations")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].slug").value("team-a"))
                .andExpect(jsonPath("$[0].role").value("OWNER"));
    }

    @Test
    void changePasswordWorks() throws Exception {
        String accessToken = registerAndGetAccessToken("password@example.com");

        mockMvc.perform(put("/api/v1/me/password")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "currentPassword": "Password123!",
                                  "newPassword": "NewPassword456!"
                                }
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "password@example.com",
                                  "password": "NewPassword456!"
                                }
                                """))
                .andExpect(status().isOk());
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

    private void createOrganization(String accessToken, String name, String slug) throws Exception {
        mockMvc.perform(post("/api/v1/organizations")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "slug": "%s"
                                }
                                """.formatted(name, slug)))
                .andExpect(status().isCreated());
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

