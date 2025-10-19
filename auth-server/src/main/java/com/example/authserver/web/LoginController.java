package com.example.authserver.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Arrays;

/**
 * Controller to handle custom login logic, such as redirecting to a default
 * identity provider or showing a login page with multiple options.
 */
@Controller
public class LoginController {

    private final String defaultProvider;
    private final boolean isProd;

    /**
     * Injects configuration properties to determine login behavior.
     *
     * @param defaultProvider The default login provider from application.yml (app.login.default-provider).
     * @param environment The Spring environment, used to check for the active profile (e.g., "prod").
     */
    public LoginController(
            @Value("${app.login.default-provider:form}") String defaultProvider,
            Environment environment) {
        this.defaultProvider = defaultProvider;
        this.isProd = Arrays.asList(environment.getActiveProfiles()).contains("prod");
    }

    /**
     * Handles GET requests for the /login page.
     *
     * BEHAVIOR:
     * - In the "prod" profile, if the default provider is "azure", this controller
     *   will automatically redirect the user to start the Azure AD login flow.
     * - In all other scenarios (e.g., "dev" profile, or if the default is "form"),
     *   it will render the custom login.html template, which shows all available
     *   login methods.
     *
     * @return A redirect view name for Azure AD flow, or the "login" template name.
     */
    @GetMapping("/login")
    public String login() {
        // For production environment, if the default provider is set to Azure, redirect immediately.
        if (isProd && "azure".equalsIgnoreCase(defaultProvider)) {
            // This forwards to the standard Spring Security endpoint for initiating OAuth2 login.
            return "redirect:/oauth2/authorization/azure";
        }

        // Otherwise, show the custom login page that offers choices.
        return "login";
    }

    /**
     * This is a placeholder endpoint for the Local SSO login flow.
     *
     * TODO: The implementation of this endpoint is pending technical details for the
     * Local SSO API call. It needs to be defined how to programmatically obtain a token.
     * For now, it returns a simple message.
     *
     * @return A placeholder message.
     */
    @GetMapping("/login/local-sso")
    @ResponseBody
    public String handleLocalSsoLogin() {
        return "Login with Local SSO is not yet implemented. Awaiting technical specifications.";
    }
}
