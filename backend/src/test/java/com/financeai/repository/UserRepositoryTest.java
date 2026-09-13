package com.financeai.repository;

import com.financeai.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void findByEmail_existingEmail_returnsUser() {
        User user = User.builder()
                .name("Alice").email("alice@example.com").password("hashed").build();
        userRepository.save(user);

        Optional<User> found = userRepository.findByEmail("alice@example.com");

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Alice");
    }

    @Test
    void findByEmail_nonExistingEmail_returnsEmpty() {
        Optional<User> found = userRepository.findByEmail("nobody@example.com");
        assertThat(found).isEmpty();
    }

    @Test
    void existsByEmail_returnsCorrectBoolean() {
        User user = User.builder()
                .name("Bob").email("bob@example.com").password("hashed").build();
        userRepository.save(user);

        assertThat(userRepository.existsByEmail("bob@example.com")).isTrue();
        assertThat(userRepository.existsByEmail("unknown@example.com")).isFalse();
    }

    @Test
    void save_duplicateEmail_throwsDataIntegrityViolation() {
        User user1 = User.builder()
                .name("Charlie").email("charlie@example.com").password("hashed").build();
        User user2 = User.builder()
                .name("Charlie2").email("charlie@example.com").password("hashed2").build();
        userRepository.save(user1);

        assertThatThrownBy(() -> {
            userRepository.save(user2);
            userRepository.flush();
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}
