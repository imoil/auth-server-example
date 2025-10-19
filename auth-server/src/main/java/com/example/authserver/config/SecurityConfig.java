package com.example.authserver.config;

import com.example.authserver.model.Role;
import com.example.authserver.repository.UserRepository;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.authorization.OAuth2TokenType;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserRepository userRepository;

    public SecurityConfig(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authorizationServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authorizationServerConfigurer = new OAuth2AuthorizationServerConfigurer();
        RequestMatcher endpointsMatcher = authorizationServerConfigurer.getEndpointsMatcher();

        http
                .securityMatcher(endpointsMatcher)
                .authorizeHttpRequests((authorize) -> authorize.anyRequest().authenticated())
                .csrf(csrf -> csrf.ignoringRequestMatchers(endpointsMatcher))
                .exceptionHandling(exceptions ->
                        exceptions.defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )
                .oauth2ResourceServer(resourceServer -> resourceServer.jwt(Customizer.withDefaults()))
                // Configure OIDC and apply the configurer using the new 'with' method
                .with(authorizationServerConfigurer, configurer -> configurer.oidc(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers("/login", "/login/**", "/error").permitAll()
                    .anyRequest().authenticated()
                )
                .formLogin(formLogin ->
                    formLogin.loginPage("/login")
                )
                .oauth2Login(oauth2Login ->
                    oauth2Login.loginPage("/login")
                );

        return http.build();
    }

    /**
     * This bean is a workaround for the .oauth2Login() dependency when no external IdPs are configured.
     * Using fully qualified names to avoid unused import warnings from the IDE analyzer.
     */
    @Bean
    public org.springframework.security.oauth2.client.registration.ClientRegistrationRepository clientRegistrationRepository() {
        org.springframework.security.oauth2.client.registration.ClientRegistration dummyRegistration = 
            org.springframework.security.oauth2.client.registration.ClientRegistration.withRegistrationId("dummy")
                .clientId("dummy")
                .clientSecret("dummy")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("{baseUrl}/login/oauth2/code/{registrationId}")
                .authorizationUri("/dummy/auth")
                .tokenUri("/dummy/token")
                .build();
        return new org.springframework.security.oauth2.client.registration.InMemoryClientRegistrationRepository(dummyRegistration);
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient nuxtClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("nuxt-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://localhost:3000/api/auth/callback")
                .postLogoutRedirectUri("http://localhost:3000/")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("read.user")
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();

        RegisteredClient winformsClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("winforms-client")
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("http://127.0.0.1:7890/callback")
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .clientSettings(ClientSettings.builder().requireProofKey(true).build())
                .build();

        return new InMemoryRegisteredClientRepository(nuxtClient, winformsClient);
    }

    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            if (OAuth2TokenType.ACCESS_TOKEN.equals(context.getTokenType())) {
                Authentication principal = context.getPrincipal();
                String email = null;

                if (principal.getPrincipal() instanceof OidcUser oidcUser) {
                    email = oidcUser.getEmail();
                } else if (principal.getPrincipal() instanceof com.example.authserver.model.User user) {
                    email = user.getEmail();
                }

                if (email != null) {
                    userRepository.findByEmail(email).ifPresent(user -> {
                        Set<String> roles = user.getRoles().stream()
                                .map(Role::getName)
                                .collect(Collectors.toSet());
                        context.getClaims().claim("roles", roles);
                    });
                }
                
                context.getClaims().audience(List.of("api://resource-server"));
            }
        };
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }

    @Bean
    public JwtDecoder jwtDecoder(JWKSource<SecurityContext> jwkSource) {
        return org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder().build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        return email -> userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
