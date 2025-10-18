package com.example.authserver.runner;

import com.example.authserver.model.Role;
import com.example.authserver.model.User;
import com.example.authserver.repository.RoleRepository;
import com.example.authserver.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        Role userRole = roleRepository.findByName("ROLE_USER").orElseGet(() -> {
            Role newUserRole = new Role();
            newUserRole.setName("ROLE_USER");
            return roleRepository.save(newUserRole);
        });

        userRepository.findByEmail("user@example.com").ifPresentOrElse(
                user -> {},
                () -> {
                    User testUser = new User();
                    testUser.setUsername("Test User");
                    testUser.setEmail("user@example.com");
                    testUser.setPassword(passwordEncoder.encode("password"));
                    testUser.setRoles(Set.of(userRole));
                    userRepository.save(testUser);
                }
        );
    }
}
