package com.mymicroservice.userservice.util;

import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.model.User;

import java.time.LocalDate;

public class CardInfoGenerator {

    public static CardInfo generateCardInfo() {
        User user = UserGenerator.generateUser();
        return  CardInfo.builder()
                .cardId(1l)
                .number("1111222233334444")
                .holder("TestUser")
                .expirationDate(LocalDate.of(2030, 3, 3))
                .userId(user)
                .build();
    }
}
