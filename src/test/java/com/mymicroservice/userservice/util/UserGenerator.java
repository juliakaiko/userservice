package com.mymicroservice.userservice.util;

import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;

import static com.mymicroservice.userservice.util.data.TestConstants.ADMIN_BIRTH_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_NAME;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_USER_SURNAME;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_BIRTH_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_NAME;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_PASSWORD;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_SURNAME;

public class UserGenerator {

    public static User generateUser() {
        return User.builder()
                .name(USER_NAME)
                .surname(USER_SURNAME)
                .birthDate(USER_BIRTH_DATE)
                .email(USER_EMAIL)
                .password(USER_PASSWORD)
                .role(Role.USER)
                .build();
    }

    public static User generateUserWithId() {
        User user = generateUser();
        user.setUserId(USER_ID);
        return user;
    }

    public static User generateAdminUser() {
        return User.builder()
                .name(NEW_USER_NAME)
                .surname(NEW_USER_SURNAME)
                .birthDate(ADMIN_BIRTH_DATE)
                .email(NEW_USER_EMAIL)
                .password(USER_PASSWORD)
                .role(Role.ADMIN)
                .build();
    }

    public static User generateUserForBatch(long index) {
        return User.builder()
                .name("User" + index)
                .surname("Surname" + index)
                .birthDate(java.time.LocalDate.of((int) (1990 + index), 1, 1))
                .email("user" + index + "@example.com")
                .password("pass" + index)
                .role(Role.USER)
                .build();
    }
}
