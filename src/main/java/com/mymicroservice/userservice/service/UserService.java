package com.mymicroservice.userservice.service;

import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.model.Role;
import org.springframework.data.domain.Page;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

public interface UserService {

    UserDto createUser(UserDto userDto);
    UserDto getUserById(Long userId);
    UserDto getUsersByEmail(String email);
    List<UserDto> getUsersIdIn(Set<Long> ids);
    List<UserDto> getUsersByRole(Role role);
    List<UserDto> getUsersBornAfter(LocalDate date);
    List<UserDto> getAllUsers();
    Page<UserDto> getAllUsersNativeWithPagination(Integer page, Integer size);
    UserDto updateUser(Long userId, UserDto userDetails);
    UserDto deleteUser(Long userId) ;
}
