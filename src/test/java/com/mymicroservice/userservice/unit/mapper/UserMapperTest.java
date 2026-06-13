package com.mymicroservice.userservice.unit.mapper;

import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.mapper.UserMapper;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.util.UserGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserMapperTest {

    @Test
    void toDto_ShouldMapFieldsCorrectly_WhenUserIsValid() {
        User user = UserGenerator.generateUser();
        UserDto userDto = UserMapper.INSTANSE.toDto(user);

        assertEquals(user.getUserId(), userDto.getUserId());
        assertEquals(user.getName(), userDto.getName());
        assertEquals(user.getSurname(), userDto.getSurname());
        assertEquals(user.getBirthDate(), userDto.getBirthDate());
        assertEquals(user.getEmail(), userDto.getEmail());
        assertEquals(user.getPassword(), userDto.getPassword());
        assertEquals(user.getRole(), userDto.getRole());
    }

    @Test
    void toEntity_ShouldMapFieldsCorrectly_WhenUserDtoIsValid() {
        User user = UserGenerator.generateUser();
        UserDto userDto = UserMapper.INSTANSE.toDto(user);
        User mappedUser = UserMapper.INSTANSE.toEntity(userDto);

        assertEquals(userDto.getUserId(), mappedUser.getUserId());
        assertEquals(userDto.getName(), mappedUser.getName());
        assertEquals(userDto.getSurname(), mappedUser.getSurname());
        assertEquals(userDto.getBirthDate(), mappedUser.getBirthDate());
        assertEquals(userDto.getEmail(), mappedUser.getEmail());
        assertEquals(userDto.getPassword(), mappedUser.getPassword());
        assertEquals(userDto.getRole(), mappedUser.getRole());
    }
}
