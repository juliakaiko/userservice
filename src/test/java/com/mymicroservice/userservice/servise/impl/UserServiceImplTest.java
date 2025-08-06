package com.mymicroservice.userservice.servise.impl;

import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.mapper.UserMapper;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.impl.UserServiceImpl;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
public class UserServiceImplTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private UserRepository userRepository;

    private final Long TEST_USER_ID = 1L;
    private final String TEST_EMAIL = "test@test.by";
    private User testUser;
    private UserDto testUserDto;

    @BeforeEach
    void setUp() {
        testUser = UserGenerator.generateUser();
        testUser.setUserId(1l);
        testUserDto = UserMapper.INSTANSE.toDto(testUser);
    }

    @Test
    public void createUser_whenCorrect_thenReturnUserDto() {
        User mappedUser = UserMapper.INSTANSE.toEntity(testUserDto);
        when(userRepository.save(mappedUser)).thenReturn(testUser);

        UserDto result = userService.createUser(testUserDto);

        assertNotNull(result);
        assertEquals(testUserDto, result);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void getUsersById_whenUserExists_thenReturnUserDto() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        UserDto result = userService.getUserById(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(testUserDto, result);
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }

    @Test
    public void getUsersById_whenUserNotExists_thenThrowUserNotFoundException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUserById(TEST_USER_ID));
        verify(userRepository, times(1)).findById(TEST_USER_ID);
    }

    @Test
    public void updateUser_whenUserExists_thenReturnUpdatedUserDto() {
        UserDto updatedDto = new UserDto();
        updatedDto.setName("New Name");
        updatedDto.setEmail("new@email.com");

        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto result = userService.updateUser(TEST_USER_ID, updatedDto);

        assertNotNull(result);
        assertEquals(updatedDto.getName(), result.getName());
        assertEquals(updatedDto.getEmail(), result.getEmail());
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    public void updateUser_whenUserNotExists_thenThrowUserNotFoundException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.updateUser(TEST_USER_ID, testUserDto));
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).save(any());
    }

    @Test
    public void deleteUser_whenUserExists_thenReturnDeletedUserDto() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.of(testUser));

        UserDto result = userService.deleteUser(TEST_USER_ID);

        assertNotNull(result);
        assertEquals(testUserDto, result);
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, times(1)).deleteById(TEST_USER_ID);
    }

    @Test
    public void deleteUser_whenUserNotExists_thenThrowUserNotFoundException() {
        when(userRepository.findById(TEST_USER_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(TEST_USER_ID));
        verify(userRepository, times(1)).findById(TEST_USER_ID);
        verify(userRepository, never()).deleteById(any());
    }

    @Test
    public void getUsersByEmail_whenUserExists_thenReturnUserDto() {
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.of(testUser));

        UserDto result = userService.getUsersByEmail(TEST_EMAIL);

        assertNotNull(result);
        assertEquals(testUserDto, result);
        verify(userRepository, times(1)).findByEmailIgnoreCase(TEST_EMAIL);
    }

    @Test
    public void getUsersByEmail_whenUserNotExists_thenThrowUserNotFoundException() {
        when(userRepository.findByEmailIgnoreCase(TEST_EMAIL)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getUsersByEmail(TEST_EMAIL));
        verify(userRepository, times(1)).findByEmailIgnoreCase(TEST_EMAIL);
    }

    @Test
    public void getUsersIdIn_whenUsersExist_thenReturnUserDtoList() {
        Set<Long> ids = Set.of(TEST_USER_ID);
        when(userRepository.findByUserIdIn(ids)).thenReturn(List.of(testUser));

        List<UserDto> result = userService.getUsersIdIn(ids);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository, times(1)).findByUserIdIn(ids);
    }

    @Test
    public void getUsersByRole_whenUsersExist_thenReturnUserDtoList() {
        Role testRole = testUser.getRole();
        when(userRepository.findUsersByRole(testRole)).thenReturn(List.of(testUser));

        List<UserDto> result = userService.getUsersByRole(testRole);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository, times(1)).findUsersByRole(testRole);
    }

    @Test
    public void getUsersBornAfter_whenUsersExist_thenReturnUserDtoList() {
        LocalDate date = testUser.getBirthDate().minusYears(1);
        when(userRepository.findUsersBornAfter(date)).thenReturn(List.of(testUser));

        List<UserDto> result = userService.getUsersBornAfter(date);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository, times(1)).findUsersBornAfter(date);
    }

    @Test
    public void getAllUsers_whenUsersExist_thenReturnUserDtoList() {
        when(userRepository.findAll()).thenReturn(List.of(testUser));

        List<UserDto> result = userService.getAllUsers();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testUserDto, result.get(0));
        verify(userRepository, times(1)).findAll();
    }

    @Test
    public void getAllUsersNativeWithPagination_whenCalled_thenReturnPageOfUserDto() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<User> userPage = new PageImpl<>(List.of(testUser), pageable, 1);

        when(userRepository.findAllUsersNative(pageable)).thenReturn(userPage);

        Page<UserDto> result = userService.getAllUsersNativeWithPagination(page, size);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testUserDto, result.getContent().get(0));
        verify(userRepository, times(1)).findAllUsersNative(pageable);
    }
}
