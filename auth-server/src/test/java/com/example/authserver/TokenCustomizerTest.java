package com.example.authserver;

import com.example.authserver.config.SecurityConfig;
import com.example.authserver.model.Role;
import com.example.authserver.model.User;
import com.example.authserver.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TokenCustomizerTest {

    @Mock
    private UserRepository userRepository;

    private OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityConfig securityConfig = new SecurityConfig(userRepository);
        tokenCustomizer = securityConfig.tokenCustomizer();
    }

    @Test
    void whenCustomizeToken_thenRolesAreAddedToClaims() {
        // given
        String userEmail = "test@example.com";
        User user = new User(userEmail, "testuser", "password");
        Role userRole = new Role("ROLE_USER");
        Role adminRole = new Role("ROLE_ADMIN");
        user.setRoles(Set.of(userRole, adminRole));

        Authentication principal = mock(Authentication.class);
        when(principal.getName()).thenReturn(userEmail);

        JwsHeader.Builder headers = JwsHeader.with(() -> "RS256");
        JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder();
        JwtEncodingContext context = JwtEncodingContext.with(headers, claimsBuilder)
                .principal(principal)
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .build();

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(user));

        // when
        tokenCustomizer.customize(context);

        // then
        Set<String> roles = context.getClaims().build().getClaim("roles");
        assertThat(roles).containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
