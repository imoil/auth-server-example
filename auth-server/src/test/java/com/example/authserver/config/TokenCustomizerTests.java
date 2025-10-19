package com.example.authserver.config;

import com.example.authserver.model.Role;
import com.example.authserver.model.User;
import com.example.authserver.repository.RoleRepository;
import com.example.authserver.repository.UserRepository;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.server.authorization.OAuth2Authorization;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.test.annotation.DirtiesContext;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class TokenCustomizerTests {

    @Autowired
    private OAuth2TokenCustomizer<JwtEncodingContext> customizer;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;

    private static final String TEST_EMAIL = "user@example.com";

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        roleRepository.deleteAll();
        Role userRole = roleRepository.save(new Role("USER"));
        Role customRole = roleRepository.save(new Role("CUSTOM_ROLE"));
        User user = new User(TEST_EMAIL, "Test User", "password");
        user.setRoles(Set.of(userRole, customRole));
        userRepository.save(user);
    }

    @Test
    void whenPrincipalIsOidcUser_thenRolesAreAdded() {
        OidcIdToken idToken = OidcIdToken.withTokenValue("id-token")
                .issuer("https://idp.example.com").subject("subject").claim("email", TEST_EMAIL)
                .issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(60)).build();
        OidcUser oidcUser = new DefaultOidcUser(Set.of(), idToken);
        Authentication principal = new UsernamePasswordAuthenticationToken(oidcUser, null, oidcUser.getAuthorities());

        JwtEncodingContext context = createJwtEncodingContext(principal);
        customizer.customize(context);

        JwtClaimsSet claims = context.getClaims().build();

        assertThat(claims.getClaimAsStringList("roles"))
                .containsExactlyInAnyOrder("USER", "CUSTOM_ROLE");
        assertThat(claims.getAudience())
                .contains("api://resource-server");
    }

    @Test
    void whenPrincipalIsUserDetails_thenRolesAreAdded() {
        User user = userRepository.findByEmail(TEST_EMAIL).orElseThrow();
        Authentication principal = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

        JwtEncodingContext context = createJwtEncodingContext(principal);
        customizer.customize(context);

        JwtClaimsSet claims = context.getClaims().build();

        assertThat(claims.getClaimAsStringList("roles"))
                .containsExactlyInAnyOrder("USER", "CUSTOM_ROLE");
        assertThat(claims.getAudience())
                .contains("api://resource-server");
    }

    private JwtEncodingContext createJwtEncodingContext(Authentication principal) {
        RegisteredClient registeredClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("test-client").clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://example.com").scope(OidcScopes.OPENID).build();

        OAuth2Authorization authorization = OAuth2Authorization.withRegisteredClient(registeredClient)
                .principalName(principal.getName()).authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE).build();

        // Correctly build the JwtEncodingContext using its public builder
        return JwtEncodingContext.with(
                        JwsHeader.with(SignatureAlgorithm.RS256).build(),
                        JwtClaimsSet.builder()
                                .issuer("http://auth-server:9000") // Add issuer
                                .subject(principal.getName()) // Add subject
                                .audience(List.of("api://resource-server")) // Add audience
                                .issuedAt(Instant.now())
                                .expiresAt(Instant.now().plusSeconds(3600))
                                .build()
                )
                .tokenType(OAuth2TokenType.ACCESS_TOKEN)
                .principal(principal)
                .authorization(authorization)
                .authorizedScopes(Set.of(OidcScopes.OPENID, OidcScopes.PROFILE)) // Add profile scope
                .registeredClient(registeredClient)
                .build();
    }
}
