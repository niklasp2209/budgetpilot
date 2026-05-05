package de.budgetpilot.finance.backend.auth;

import de.budgetpilot.finance.backend.auth.domain.TokenPair;
import de.budgetpilot.finance.backend.auth.exception.InvalidTokenException;
import de.budgetpilot.finance.backend.auth.service.TokenService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * @author Niklas Petermeier
 * @since 05.05.2026
 */
@SpringBootTest
class TokenServiceTest {
    @Autowired
    private TokenService tokenService;

    @Test
    void refreshTokenContainsSubject() {
        TokenPair tokenPair = tokenService.issueTokenPair("service@example.com");
        String subject = tokenService.validateAndExtractRefreshSubject(tokenPair.refreshToken());

        assertThat(subject).isEqualTo("service@example.com");
    }

    @Test
    void accessTokenIsRejectedInRefreshValidation() {
        TokenPair tokenPair = tokenService.issueTokenPair("service@example.com");

        assertThatThrownBy(() -> tokenService.validateAndExtractRefreshSubject(tokenPair.accessToken()))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void malformedTokenIsRejected() {
        assertThatThrownBy(() -> tokenService.validateAndExtractRefreshSubject("not-a-token"))
                .isInstanceOf(InvalidTokenException.class);
    }
}
