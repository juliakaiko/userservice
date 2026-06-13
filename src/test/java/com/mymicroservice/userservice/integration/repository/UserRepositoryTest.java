package com.mymicroservice.userservice.integration.repository;

import com.mymicroservice.userservice.configuration.PostgresTestContainersConfig;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.util.UserGenerator;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.REPOSITORY_BORN_AFTER_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.REPOSITORY_NO_RESULTS_DATE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class UserRepositoryTest extends PostgresTestContainersConfig {

    @Autowired
    private UserRepository userRepository;

    private static User expectedUser;

    @BeforeAll
    static void setUpAll() {
        expectedUser = UserGenerator.generateUser();
    }

    @BeforeEach
    void init() {
        userRepository.deleteAll();
        expectedUser = userRepository.save(UserGenerator.generateUser());
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnUser_WhenEmailExists() {
        Optional<User> actualUser = userRepository.findByEmailIgnoreCase(expectedUser.getEmail());

        assertNotNull(actualUser.orElse(null));
        assertEquals(expectedUser, actualUser.get());
        assertThat(actualUser).isPresent().contains(expectedUser);
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnEmpty_WhenEmailNotExists() {
        Optional<User> actualUser = userRepository.findByEmailIgnoreCase(NON_EXISTENT_EMAIL);

        assertFalse(actualUser.isPresent());
    }

    @Test
    void findByUserIdIn_ShouldReturnUsers_WhenUsersExist() {
        Set<Long> ids = Set.of(expectedUser.getUserId());

        List<User> actualUsers = userRepository.findByUserIdIn(ids);

        assertThat(actualUsers).hasSize(1);
        assertEquals(expectedUser.getUserId(), actualUsers.get(0).getUserId());
    }

    @Test
    void findByUserIdIn_ShouldReturnEmptyList_WhenUsersNotExist() {
        List<User> actualUsers = userRepository.findByUserIdIn(Set.of(NON_EXISTENT_ID));

        assertThat(actualUsers).isEmpty();
    }

    @Test
    void findUsersByRole_ShouldReturnUsers_WhenRoleExists() {
        List<User> actualUsers = userRepository.findUsersByRole(Role.USER);

        assertThat(actualUsers).isNotEmpty();
        assertEquals(expectedUser, actualUsers.get(0));
    }

    @Test
    void findUsersByRole_ShouldReturnEmptyList_WhenRoleNotExists() {
        List<User> actualUsers = userRepository.findUsersByRole(Role.ADMIN);

        assertTrue(actualUsers.isEmpty());
    }

    @Test
    void findUsersBornAfter_ShouldReturnUsers_WhenUsersBornAfterDate() {
        List<User> bornAfterUsers = userRepository.findUsersBornAfter(REPOSITORY_BORN_AFTER_DATE);

        assertThat(bornAfterUsers).isNotEmpty();
        assertTrue(bornAfterUsers.stream().allMatch(user -> user.getBirthDate().isAfter(REPOSITORY_BORN_AFTER_DATE)));
    }

    @Test
    void findUsersBornAfter_ShouldReturnEmptyList_WhenNoUsersBornAfterDate() {
        List<User> bornAfterUsers = userRepository.findUsersBornAfter(REPOSITORY_NO_RESULTS_DATE);

        assertThat(bornAfterUsers).isEmpty();
    }

    @Test
    void findAllUsersNative_ShouldReturnPaginatedResults_WhenUsersExist() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<User> page = userRepository.findAllUsersNative(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    void findAllUsersNative_ShouldReturnEmptyPage_WhenNoUsersExist() {
        userRepository.deleteAll();
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = userRepository.findAllUsersNative(pageable);

        assertThat(page.getContent()).isEmpty();
    }
}
