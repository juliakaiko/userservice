package com.mymicroservice.userservice.integration.testcontainers;

import com.mymicroservice.userservice.configuration.TestContainersConfig;
import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.mapper.UserMapper;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.impl.UserServiceImpl;
import com.mymicroservice.userservice.util.UserGenerator;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class UserServiceImplIT extends TestContainersConfig  {

    @Autowired
    private UserServiceImpl userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        testUser = UserGenerator.generateUser();
        testUserDto = UserMapper.INSTANSE.toDto(testUser);
        cacheManager.getCache("userCache").clear();
    }

    @Test
    void createUser_ShouldSaveUserToDatabase() {
        UserDto createdUser = userService.createUser(testUserDto);

        assertNotNull(createdUser.getUserId());
        assertEquals(testUserDto.getName(), createdUser.getName());
        assertEquals(testUserDto.getSurname(), createdUser.getSurname());
        assertEquals(testUserDto.getBirthDate(), createdUser.getBirthDate());
        assertEquals(testUserDto.getEmail(), createdUser.getEmail());
        assertEquals(testUserDto.getPassword(), createdUser.getPassword());
        assertEquals(testUserDto.getRole(), createdUser.getRole());

        User userFromDb = userRepository.findById(createdUser.getUserId()).orElseThrow();
        assertEquals(createdUser.getName(), userFromDb.getName());
    }

    @Test
    void getUsersById_ShouldReturnUser_WhenUserExists() {
        UserDto createdUser = userService.createUser(testUserDto);
        UserDto foundUser = userService.getUserById(createdUser.getUserId());

        assertNotNull(foundUser);
        assertEquals(createdUser.getUserId(), foundUser.getUserId());
        assertEquals(createdUser, foundUser);
    }

    @Test
    void getUsersById_ShouldThrowException_WhenUserNotExists() {
        assertThrows(UserNotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getUsersById_ShouldCacheUser() {
        UserDto createdUser = userService.createUser(testUserDto);

        userService.getUserById(createdUser.getUserId()); //first call - must save to cache

        UserDto cachedUser = cacheManager.getCache("userCache") //Check if the User is in the cache
                .get(createdUser.getUserId(), UserDto.class);
        assertNotNull(cachedUser);
        assertEquals(createdUser.getUserId(), cachedUser.getUserId());
        assertEquals(createdUser.getName(), cachedUser.getName());
        assertEquals(createdUser.getSurname(), cachedUser.getSurname());
        assertEquals(createdUser.getBirthDate(), cachedUser.getBirthDate());
        assertEquals(createdUser.getEmail(), cachedUser.getEmail());
    }

    @Test
    void updateUser_ShouldUpdateUserData() {
        UserDto createdUser = userService.createUser(testUserDto);

        UserDto updatedDto = new UserDto();
        updatedDto.setName("New Name");
        updatedDto.setEmail("new@email.com");

        UserDto updatedUser = userService.updateUser(createdUser.getUserId(), updatedDto);

        assertEquals(createdUser.getUserId(), updatedUser.getUserId());
        assertEquals("New Name", updatedUser.getName());
        assertEquals("new@email.com", updatedUser.getEmail());
    }

    @Test
    void updateUser_ShouldUpdateUserDataInCache() {
        UserDto createdUser = userService.createUser(testUserDto);

        UserDto updatedDto = new UserDto();
        updatedDto.setName("New Name");
        updatedDto.setEmail("new@email.com");

        userService.updateUser(createdUser.getUserId(), updatedDto);

        UserDto cachedUser = cacheManager.getCache("userCache")
                .get(createdUser.getUserId(), UserDto.class);
        assertNotNull(cachedUser);
        assertEquals("New Name", cachedUser.getName());
        assertEquals("new@email.com", cachedUser.getEmail());
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
        userService.deleteUser(createdUser.getUserId());

        assertNull(cacheManager.getCache("userCache").get(createdUser.getUserId(), UserDto.class));
    }

    @Test
    void deleteUsersById_ShouldThrowException_WhenUserNotExists() {
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(999L));
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
                () -> userService.getUsersByEmail("nonexistent@example.com"));
    }

    @Test
    void getUsersIdIn_ShouldReturnUsersWithGivenIds() {
        List<UserDto> users = LongStream.range(1, 6) // stream: 1,2,3,4,5
                .mapToObj(i -> userService.createUser(new UserDto(
                        null,
                        "User" + i,
                        "Surname" + i,
                        LocalDate.of((int) (1990+i), 1, 1),
                        "user" + i + "@example.com",
                        "pass" + i,
                        Role.USER
                )))
                .collect(Collectors.toList());

        Set<Long> idsToFind = Set.of(users.get(0).getUserId(), users.get(2).getUserId(), users.get(4).getUserId());

        List<UserDto> foundUsers = userService.getUsersIdIn(idsToFind);

        assertEquals(3, foundUsers.size());
        assertTrue(foundUsers.stream().allMatch(u -> idsToFind.contains(u.getUserId())));
    }

    @Test
    void getUsersByRole_ShouldReturnUsersWithGivenRole() {
        userService.createUser(testUserDto);
        userService.createUser(new UserDto(
                2L,
                "Admin",
                "User",
                LocalDate.of(1985, 5, 5),
                "admin@example.com",
                "adminpass",
                Role.ADMIN
        ));

        List<UserDto> adminUsers = userService.getUsersByRole(Role.ADMIN);
        List<UserDto> users = userService.getUsersByRole(Role.USER);

        assertEquals(1, adminUsers.size());
        assertEquals(1, users.size());
        assertEquals(Role.ADMIN, adminUsers.get(0).getRole());
        assertEquals(Role.USER, users.get(0).getRole());
    }

    @Test
    void getUsersBornAfter_ShouldReturnUsersBornAfterGivenDate() {
        userService.createUser(testUserDto);
        LocalDate date = LocalDate.of(1995, 1, 1);

        List<UserDto> youngUsers = userService.getUsersBornAfter(date);

        assertEquals(1, youngUsers.size());
        assertTrue(youngUsers.get(0).getBirthDate().isAfter(date));
    }

    @Test
    void getAllUsers_ShouldReturnAllUsers() {
        userService.createUser(testUserDto);
        userService.createUser(new UserDto(
                2L,
                "Another",
                "User",
                LocalDate.of(1995, 5, 5),
                "another@example.com",
                "anotherpass",
                Role.USER
        ));

        List<UserDto> allUsers = userService.getAllUsers();

        assertEquals(2, allUsers.size());
    }

    @Test
    void getAllUsersNativeWithPagination_ShouldReturnPageOfUsers() {
        List<UserDto> users = LongStream.range(1, 5) //stream: 1,2,3,4
                .mapToObj(i -> userService.createUser(new UserDto(
                        null,
                        "User" + i,
                        "Surname" + i,
                        LocalDate.of((int) (1990+i), 1, 1),
                        "user" + i + "@example.com",
                        "pass" + i,
                        Role.USER
                )))
                .collect(Collectors.toList());

        Page<UserDto> firstPage = userService.getAllUsersNativeWithPagination(0, 2);
        assertEquals(2, firstPage.getContent().size());
        assertTrue(firstPage.hasNext());

        Page<UserDto> secondPage = userService.getAllUsersNativeWithPagination(1, 2);
        assertEquals(2, secondPage.getContent().size());
    }
}
