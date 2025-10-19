package com.example.resourceserver.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ApiController {

    @GetMapping("/me")
    public Map<String, Object> getMe(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        return Map.of(
                "name", authentication.getName(),
                "roles", authentication.getAuthorities().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList())
        );
    }

    @GetMapping("/public")
    public String publicEndpoint() {
        return "This is a public endpoint.";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userEndpoint() {
        return "Hello, User!";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminEndpoint() {
        return "Hello, Admin!";
    }
}