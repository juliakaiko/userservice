package com.mymicroservice.userservice.integration.service;

import com.mymicroservice.userservice.configuration.TestContainersConfig;
import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.impl.UserServiceImpl;
import com.mymicroservice.userservice.util.UserDtoGenerator;
import com.mymicroservice.userservice.util.UserGenerator;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.mymicroservice.userservice.util.data.TestConstants.BCRYPT_PREFIX;
import static com.mymicroservice.userservice.util.data.TestConstants.BORN_AFTER_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_NAME;
import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.PAGINATION_PAGE_SIZE;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_NAME;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_PASSWORD;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_SURNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class UserServiceImplIT extends TestContainersConfig {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUserDto = UserDtoGenerator.generateUserDto();
        cacheManager.getCache("userCache").clear();
    }

    @Test
    void createUser_ShouldSaveUserToDatabase_WhenDtoIsValid() {
        UserDto createdUser = userService.createUser(testUserDto);

        assertNotNull(createdUser.getUserId());
        assertEquals(USER_NAME, createdUser.getName());
        assertEquals(USER_SURNAME, createdUser.getSurname());
        assertEquals(testUserDto.getBirthDate(), createdUser.getBirthDate());
        assertEquals(USER_EMAIL, createdUser.getEmail());
        assertEquals(Role.USER, createdUser.getRole());
        assertTrue(createdUser.getPassword().startsWith(BCRYPT_PREFIX));
        assertTrue(passwordEncoder.matches(USER_PASSWORD, createdUser.getPassword()));

        User userFromDb = userRepository.findById(createdUser.getUserId()).orElseThrow();
        assertEquals(USER_NAME, userFromDb.getName());
        assertTrue(passwordEncoder.matches(USER_PASSWORD, userFromDb.getPassword()));
    }

    @Test
    void getUserById_ShouldReturnUser_WhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);
        UserDto foundUser = userService.getUserById(createdUser.getUserId());

        assertNotNull(foundUser);
        assertEquals(createdUser.getUserId(), foundUser.getUserId());
        assertEquals(createdUser.getEmail(), foundUser.getEmail());
    }

    @Test
    void getUserById_ShouldThrowException_WhenUserNotExists() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(NON_EXISTENT_ID));
    }

    @Test
    void getUserById_ShouldCacheUser_WhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);

        userService.getUserById(createdUser.getUserId());

        UserDto cachedUser = cacheManager.getCache("userCache")
                .get(createdUser.getUserId(), UserDto.class);
        assertNotNull(cachedUser);
        assertEquals(createdUser.getUserId(), cachedUser.getUserId());
        assertEquals(USER_NAME, cachedUser.getName());
        assertEquals(USER_EMAIL, cachedUser.getEmail());
    }

    @Test
    void updateUser_ShouldUpdateUserData_WhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);
        UserDto updatedDto = UserDtoGenerator.generateUpdateDto();

        UserDto updatedUser = userService.updateUser(createdUser.getUserId(), updatedDto);

        assertEquals(createdUser.getUserId(), updatedUser.getUserId());
        assertEquals(NEW_USER_NAME, updatedUser.getName());
        assertEquals(NEW_USER_EMAIL, updatedUser.getEmail());
    }

    @Test
    void updateUser_ShouldUpdateUserDataInCache_WhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);
        UserDto updatedDto = UserDtoGenerator.generateUpdateDto();

        userService.updateUser(createdUser.getUserId(), updatedDto);

        UserDto cachedUser = cacheManager.getCache("userCache")
                .get(createdUser.getUserId(), UserDto.class);
        assertNotNull(cachedUser);
        assertEquals(NEW_USER_NAME, cachedUser.getName());
        assertEquals(NEW_USER_EMAIL, cachedUser.getEmail());
    }

    @Test
    void deleteUser_ShouldRemoveUserFromDatabase_WhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);
        userService.deleteUser(createdUser.getUserId());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(createdUser.getUserId()));
    }

    @Test
    void deleteUser_ShouldRemoveUserFromCache_WhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);
        userService.getUserById(createdUser.getUserId());
        userService.deleteUser(createdUser.getUserId());

        assertNull(cacheManager.getCache("userCache").get(createdUser.getUserId(), UserDto.class));
    }

    @Test
    void deleteUser_ShouldThrowException_WhenUserNotExists() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(NON_EXISTENT_ID));
    }

    @Test
    void getUsersByEmail_ShouldReturnUser_WhenEmailExists() {
        UserDto createdUser = userService.createUser(testUserDto);
        UserDto foundUser = userService.getUsersByEmail(createdUser.getEmail());

        assertEquals(createdUser.getUserId(), foundUser.getUserId());
    }

    @Test
    void getUsersByEmail_ShouldThrowException_WhenEmailNotExists() {
        assertThrows(UserNotFoundException.class,
                () -> userService.getUsersByEmail(NON_EXISTENT_EMAIL));
    }

    @Test
    void getUsersIdIn_ShouldReturnUsersWithGivenIds_WhenUsersExist() {
        List<UserDto> users = LongStream.rangeClosed(1, 5)
                .mapToObj(i -> userService.createUser(UserDtoGenerator.generateUserDtoForBatch(i)))
                .collect(Collectors.toList());

        Set<Long> idsToFind = Set.of(
                users.get(0).getUserId(),
                users.get(2).getUserId(),
                users.get(4).getUserId()
        );

        List<UserDto> foundUsers = userService.getUsersIdIn(idsToFind);

        assertEquals(3, foundUsers.size());
        assertTrue(foundUsers.stream().allMatch(u -> idsToFind.contains(u.getUserId())));
    }

    @Test
    void getUsersByRole_ShouldReturnUsersWithGivenRole_WhenUsersExist() {
        userService.createUser(testUserDto);
        userService.createUser(UserDtoGenerator.generateAdminUserDto());

        List<UserDto> adminUsers = userService.getUsersByRole(Role.ADMIN);
        List<UserDto> users = userService.getUsersByRole(Role.USER);

        assertEquals(1, adminUsers.size());
        assertEquals(1, users.size());
        assertEquals(Role.ADMIN, adminUsers.get(0).getRole());
        assertEquals(Role.USER, users.get(0).getRole());
    }

    @Test
    void getUsersBornAfter_ShouldReturnUsersBornAfterGivenDate_WhenUsersExist() {
        userService.createUser(testUserDto);

        List<UserDto> youngUsers = userService.getUsersBornAfter(BORN_AFTER_DATE);

        assertEquals(1, youngUsers.size());
        assertTrue(youngUsers.get(0).getBirthDate().isAfter(BORN_AFTER_DATE));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers_WhenUsersExist() {
        userService.createUser(testUserDto);
        userService.createUser(UserDtoGenerator.generatePartialUpdateDto());

        List<UserDto> allUsers = userService.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void getAllUsersNativeWithPagination_ShouldReturnPageOfUsers_WhenUsersExist() {
        LongStream.rangeClosed(1, 4)
                .forEach(i -> userService.createUser(UserDtoGenerator.generateUserDtoForBatch(i)));

        Page<UserDto> firstPage = userService.getAllUsersNativeWithPagination(0, PAGINATION_PAGE_SIZE);
        assertEquals(PAGINATION_PAGE_SIZE, firstPage.getContent().size());
        assertTrue(firstPage.hasNext());

        Page<UserDto> secondPage = userService.getAllUsersNativeWithPagination(1, PAGINATION_PAGE_SIZE);
        assertEquals(PAGINATION_PAGE_SIZE, secondPage.getContent().size());
    }
}
