package com.mymicroservice.userservice.repository;

import com.mymicroservice.userservice.configuration.TestContainersConfig;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.util.UserGenerator;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Disabling DataSource Replacement
@Import(TestContainersConfig.class)
public class UserRepositoryTest extends TestContainersConfig {

    @Autowired
    private UserRepository userRepository;

    private static User expectedUser;

    @BeforeAll
    static void setUp(){
        expectedUser = UserGenerator.generateUser();
    }

    @BeforeEach
    void init() {
        userRepository.deleteAll();
        expectedUser = userRepository.save(expectedUser);
    }

    @Test
    void findByEmail_shouldReturnUserWhenExists() {
        Optional<User> actualUser = userRepository.findByEmailIgnoreCase(expectedUser.getEmail());
        log.info("Test to find the User with email: {} "+ expectedUser.getEmail());

        assertNotNull(actualUser.get());
        assertEquals(expectedUser, actualUser.get());
        assertThat(actualUser).isPresent().contains(expectedUser);
    }

    @Test
    public void findByEmail_shouldReturnEmptyWhenNotExists() {
        Optional<User> actualUser = userRepository.findByEmailIgnoreCase("non-existing-email");

        assertFalse(actualUser.isPresent());
    }

    @Test
    public void findByUserIdIn_shouldReturnUsersWhenExist() {
        Set<Long> ids = Set.of(expectedUser.getUserId());

        List<User> actualUsers = userRepository.findByUserIdIn(ids);

        assertThat(actualUsers).hasSize(1);
        assertEquals(expectedUser.getUserId(), actualUsers.get(0).getUserId());
    }

    @Test
    public void findByUserIdIn_shouldReturnEmptyListWhenNotExist() {
        Set<Long> ids = Set.of(999L);

        List<User> actualUsers = userRepository.findByUserIdIn(ids);

        assertThat(actualUsers).isEmpty();
    }

    @Test
    void findByRole_shouldReturnUserWhenExists() {
        List<User> actualUsers = userRepository.findUsersByRole(Role.USER);

        assertThat(actualUsers).isNotEmpty();
        assertEquals(expectedUser, actualUsers.get(0));
        assertThat(actualUsers).contains(expectedUser);
    }

    @Test
    public void findByRole_shouldReturnEmptyWhenNotExists() {
        List<User> actualUsers = userRepository.findUsersByRole(Role.ADMIN);

        assertTrue(actualUsers.isEmpty());
    }

    @Test
    public void findUsersBornAfter_shouldReturnBornAfterUsers() {
        LocalDate date = LocalDate.of(2000,1,1);
        List<User> bornAfterUsers = userRepository.findUsersBornAfter(date);

        assertThat(bornAfterUsers).isNotEmpty();
        assertTrue(bornAfterUsers.stream().allMatch(user ->
                user.getBirthDate().isAfter(date)));
    }

    @Test
    public void findUsersBornAfter_shouldReturnEmptyList() {
        LocalDate date = LocalDate.of(2020,1,1);

        List<User> bornAfterUsers = userRepository.findUsersBornAfter(date);

        assertThat(bornAfterUsers).isEmpty();
    }

    @Test
    public void findAllUsersNative_shouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<User> page = userRepository.findAllUsersNative(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    public void findAllUsersNative_shouldReturnEmptyPageWhenNoCards() {
        userRepository.deleteAll();
        Pageable pageable = PageRequest.of(0, 10);

        Page<User> page = userRepository.findAllUsersNative(pageable);

        assertThat(page.getContent()).isEmpty();
    }
}
