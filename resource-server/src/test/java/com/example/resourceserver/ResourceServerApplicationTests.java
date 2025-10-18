package com.example.resourceserver;

import com.example.resourceserver.web.ApiController;
import com.example.resourceserver.web.WebController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ComponentScan(basePackages = "com.example.resourceserver")
public class ResourceServerApplicationTests {

    @Autowired
    private MockMvc mvc;

    @Test
    void whenUnauthenticated_thenReturns401() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void whenValidJwt_thenReturns200() throws Exception {
        mvc.perform(get("/").with(jwt()))
                .andExpect(status().isOk());
    }

    @Test
    void whenPublicEndpoint_thenReturns200() throws Exception {
        mvc.perform(get("/api/public"))
                .andExpect(status().isOk());
    }

    @Test
    void whenUserEndpointWithJwt_thenReturns200() throws Exception {
        mvc.perform(get("/api/user").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void whenAdminEndpointWithJwt_thenReturns200() throws Exception {
        mvc.perform(get("/api/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void whenUserEndpointWithUserRole_thenReturns200() throws Exception {
        mvc.perform(get("/api/user").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void whenAdminEndpointWithAdminRole_thenReturns200() throws Exception {
        mvc.perform(get("/api/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }

    @Test
    void whenAdminEndpointWithUserRole_thenReturns403() throws Exception {
        mvc.perform(get("/api/admin").with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Configuration
    static class TestConfig {
        @Bean
        public JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test-user")
                    .build();
        }
    }
}
