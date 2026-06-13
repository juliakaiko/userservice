package com.mymicroservice.userservice.util;

import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.mapper.UserMapper;
import com.mymicroservice.userservice.model.Role;

import java.time.LocalDate;

import static com.mymicroservice.userservice.util.data.TestConstants.ADMIN_BIRTH_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_NAME;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_SURNAME;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_PASSWORD;

public class UserDtoGenerator {

    public static UserDto generateUserDto() {
        return UserMapper.INSTANSE.toDto(UserGenerator.generateUser());
    }

    public static UserDto generateUserDtoWithId() {
        UserDto userDto = generateUserDto();
        userDto.setUserId(USER_ID);
        return userDto;
    }

    public static UserDto generateAdminUserDto() {
        return UserMapper.INSTANSE.toDto(UserGenerator.generateAdminUser());
    }

    public static UserDto generateUserDtoForBatch(long index) {
        return UserMapper.INSTANSE.toDto(UserGenerator.generateUserForBatch(index));
    }

    public static UserDto generateUpdateDto() {
        UserDto updateDto = new UserDto();
        updateDto.setName(NEW_USER_NAME);
        updateDto.setEmail(NEW_USER_EMAIL);
        return updateDto;
    }

    public static UserDto generatePartialUpdateDto() {
        UserDto updateDto = new UserDto();
        updateDto.setName(NEW_USER_NAME);
        updateDto.setSurname(NEW_USER_SURNAME);
        updateDto.setBirthDate(ADMIN_BIRTH_DATE);
        updateDto.setEmail(NEW_USER_EMAIL);
        updateDto.setPassword(USER_PASSWORD);
        updateDto.setRole(Role.ADMIN);
        return updateDto;
    }

    public static UserDto generateUserDtoWithBirthDate(LocalDate birthDate) {
        UserDto userDto = generateUserDto();
        userDto.setBirthDate(birthDate);
        return userDto;
    }
}
