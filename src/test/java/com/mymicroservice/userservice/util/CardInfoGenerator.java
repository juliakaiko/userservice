package com.mymicroservice.userservice.util;

import com.mymicroservice.userservice.model.CardInfo;

import java.time.LocalDate;

public class CardInfoGenerator {

    public static CardInfo generateCardInfo() {
        return  CardInfo.builder()
                .number("1111222233334444")
                .holder("TestUser")
                .expirationDate(LocalDate.of(2030, 3, 3))
                .build();
    }
}
