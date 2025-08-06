package com.mymicroservice.userservice.mapper;


import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.util.UserGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class UserMapperTest {

    @Test
    public void userToDto_whenOk_thenMapFieldsCorrectly() {
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
    public void userDtoToEntity_whenOk_thenMapFieldsCorrectly() {
        User user = UserGenerator.generateUser();
        UserDto userDto = UserMapper.INSTANSE.toDto(user);
        user = UserMapper.INSTANSE.toEntity(userDto);
        assertEquals(userDto.getUserId(), user.getUserId());
        assertEquals(userDto.getName(), user.getName());
        assertEquals(userDto.getSurname(), user.getSurname());
        assertEquals(userDto.getBirthDate(), user.getBirthDate());
        assertEquals(userDto.getEmail(), user.getEmail());
        assertEquals(userDto.getPassword(), user.getPassword());
        assertEquals(userDto.getRole(), user.getRole());
    }
}
