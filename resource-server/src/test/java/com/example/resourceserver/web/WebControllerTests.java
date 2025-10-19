package com.example.resourceserver.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.example.resourceserver.config.SecurityConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(WebController.class)
@Import(SecurityConfig.class)
class WebControllerTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "USER")
    void whenUserAccessesUserPage_thenReturnsUserPageView() throws Exception {
        mockMvc.perform(get("/user"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-page"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAdminAccessesAdminPage_thenReturnsAdminPageView() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin-page"));
    }

    @Test
    @WithMockUser
    void whenNormalUserAccessesAdminPage_thenIsForbidden() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    void whenUnauthenticatedUserAccessesUserPage_thenIsUnauthorized() throws Exception {
        // For WebMvcTest with Spring Security, unauthenticated access to a secured
        // endpoint typically results in a 3xx redirect to a login page, but since
        // this is a resource server, we expect a 401 Unauthorized.
        // The default behavior of oauth2ResourceServer() correctly returns 401.
        mockMvc.perform(get("/user"))
                .andExpect(status().isUnauthorized());
    }
}
