package com.mymicroservice.userservice.util;

import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;

import java.time.LocalDate;

public class UserGenerator {

    public static User generateUser() {

        return  User.builder()
                //.userId(1l)
                .name("TestName")
                .surname("TestSurName")
                .birthDate(LocalDate.of(2000, 2, 2))
                .email("test@test.by")
                .password("pass_test")
                .role(Role.USER)
                .build();
    }
}
