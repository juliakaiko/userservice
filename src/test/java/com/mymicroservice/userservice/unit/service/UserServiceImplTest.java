package com.mymicroservice.userservice.unit.service;

import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.mapper.UserMapper;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.impl.UserServiceImpl;
import com.mymicroservice.userservice.util.UserDtoGenerator;
import com.mymicroservice.userservice.util.UserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Page;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mymicroservice.userservice.util.data.TestConstants.DEFAULT_PAGE;
import static com.mymicroservice.userservice.util.data.TestConstants.DEFAULT_PAGE_SIZE;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_ENCODED_PASSWORD;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_NAME;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_PASSWORD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = UserGenerator.generateUserWithId();
        testUserDto = UserMapper.INSTANSE.toDto(testUser);
    }

    @Test
    void createUser_ShouldReturnUserDto_WhenDtoIsValid() {
        when(passwordEncoder.encode(anyString())).thenAnswer(inv -> "encoded_" + inv.getArgument(0));
        testUser.setPassword("encoded_" + USER_PASSWORD);
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        UserDto result = userService.createUser(testUserDto);

        assertNotNull(result);
        assertEquals(USER_ID, result.getUserId());
        assertEquals("encoded_" + USER_PASSWORD, result.getPassword());
        verify(passwordEncoder, times(1)).encode(USER_PASSWORD);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserById_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        UserDto result = userService.getUserById(USER_ID);

        assertNotNull(result);
        assertEquals(testUserDto, result);
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void getUserById_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(USER_ID));
        verify(userRepository, times(1)).findById(USER_ID);
    }

    @Test
    void updateUser_ShouldReturnUpdatedUserDto_WhenUserExists() {
        UserDto updatedDto = new UserDto();
        updatedDto.setName(NEW_USER_NAME);
        updatedDto.setEmail(NEW_USER_EMAIL);
        updatedDto.setPassword(NEW_ENCODED_PASSWORD);

        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(USER_ID, updatedDto);

        assertNotNull(result);
        assertEquals(NEW_USER_NAME, result.getName());
        assertEquals(NEW_USER_EMAIL, result.getEmail());
        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updateUser_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(USER_ID, testUserDto));
        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUser_ShouldReturnDeletedUserDto_WhenUserExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        UserDto result = userService.deleteUser(USER_ID);

        assertNotNull(result);
        assertEquals(testUserDto, result);
        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, times(1)).deleteById(USER_ID);
    }

    @Test
    void deleteUser_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        when(userRepository.findById(USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(USER_ID));
        verify(userRepository, times(1)).findById(USER_ID);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    void getUsersByEmail_ShouldReturnUserDto_WhenUserExists() {
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.of(testUser));

        UserDto result = userService.getUsersByEmail(USER_EMAIL);

        assertNotNull(result);
        assertEquals(testUserDto, result);
        verify(userRepository, times(1)).findByEmailIgnoreCase(USER_EMAIL);
    }

    @Test
    void getUsersByEmail_ShouldThrowUserNotFoundException_WhenUserNotExists() {
        when(userRepository.findByEmailIgnoreCase(USER_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUsersByEmail(USER_EMAIL));
        verify(userRepository, times(1)).findByEmailIgnoreCase(USER_EMAIL);
    }

    @Test
    void getUsersIdIn_ShouldReturnUserDtoList_WhenUsersExist() {
        Set<Long> ids = Set.of(USER_ID);
        when(userRepository.findByUserIdIn(ids)).thenReturn(List.of(testUser));

        List<UserDto> result = userService.getUsersIdIn(ids);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository, times(1)).findByUserIdIn(ids);
    }

    @Test
    void getUsersByRole_ShouldReturnUserDtoList_WhenUsersExist() {
        Role testRole = testUser.getRole();
        when(userRepository.findUsersByRole(testRole)).thenReturn(List.of(testUser));

        List<UserDto> result = userService.getUsersByRole(testRole);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository, times(1)).findUsersByRole(testRole);
    }

    @Test
    void getUsersBornAfter_ShouldReturnUserDtoList_WhenUsersExist() {
        var date = testUser.getBirthDate().minusYears(1);
        when(userRepository.findUsersBornAfter(date)).thenReturn(List.of(testUser));

        List<UserDto> result = userService.getUsersBornAfter(date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository, times(1)).findUsersBornAfter(date);
    }

    @Test
    void getAllUsers_ShouldReturnUserDtoList_WhenUsersExist() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    void getAllUsersNativeWithPagination_ShouldReturnPageOfUserDto_WhenCalled() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, Sort.by("id"));
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAllUsersNative(pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getAllUsersNativeWithPagination(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUserDto, result.getContent().get(0));
        verify(userRepository, times(1)).findAllUsersNative(pageable);
    }
}
