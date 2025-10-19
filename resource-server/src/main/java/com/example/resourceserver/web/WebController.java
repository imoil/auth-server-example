package com.example.resourceserver.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index(Model model, Authentication authentication) {
        // In a real app, you'd want to null-check authentication
        if (authentication != null) {
            model.addAttribute("username", authentication.getName());
        }
        return "index";
    }

    @GetMapping("/user")
    @PreAuthorize("hasRole('USER')")
    public String userPage() {
        return "user-page";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPage() {
        return "admin-page";
    }
}
