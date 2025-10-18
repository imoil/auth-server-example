package com.example.authserver;

import com.example.authserver.model.User;
import com.example.authserver.model.Role;
import com.example.authserver.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void whenFindByEmail_thenReturnUser() {
        // given
        Role adminRole = new Role("ROLE_ADMIN");
        entityManager.persist(adminRole);

        User user = new User("test@example.com", "testuser", "password");
        user.setRoles(Set.of(adminRole));
        entityManager.persist(user);
        entityManager.flush();

        // when
        User found = userRepository.findByEmail(user.getEmail()).orElse(null);

        // then
        assertThat(found).isNotNull();
        assertThat(found.getEmail()).isEqualTo(user.getEmail());
        assertThat(found.getRoles()).contains(adminRole);
    }

    @Test
    public void whenSaveUser_thenUserIsPersisted() {
        // given
        User newUser = new User("new@example.com", "newuser", "password");

        // when
        User savedUser = userRepository.save(newUser);

        // then
        User foundUser = entityManager.find(User.class, savedUser.getId());
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getEmail()).isEqualTo(newUser.getEmail());
    }

    @Test
    public void whenUpdateUser_thenUserIsUpdated() {
        // given
        User user = new User("update@example.com", "updateuser", "password");
        entityManager.persist(user);
        entityManager.flush();

        // when
        user.setUsername("updatedUsername");
        userRepository.save(user);

        // then
        User updatedUser = entityManager.find(User.class, user.getId());
        assertThat(updatedUser.getUsername()).isEqualTo("updatedUsername");
    }

    @Test
    public void whenDeleteUser_thenUserIsRemoved() {
        // given
        User user = new User("delete@example.com", "deleteuser", "password");
        entityManager.persist(user);
        entityManager.flush();

        // when
        userRepository.delete(user);

        // then
        User deletedUser = entityManager.find(User.class, user.getId());
        assertThat(deletedUser).isNull();
    }
}
