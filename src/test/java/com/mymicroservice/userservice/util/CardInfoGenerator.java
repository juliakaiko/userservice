package com.mymicroservice.userservice.util;

import com.mymicroservice.userservice.model.CardInfo;

import static com.mymicroservice.userservice.util.data.TestConstants.CARD_NUMBER;
import static com.mymicroservice.userservice.util.data.TestConstants.ENTITY_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.EXPIRED_CARD_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.FUTURE_EXPIRATION_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.HOLDER;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_CARD_NUMBER;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_CARD_EXPIRATION_DATE;

public class CardInfoGenerator {

    public static CardInfo generateCardInfo() {
        return CardInfo.builder()
                .number(CARD_NUMBER)
                .holder(HOLDER)
                .expirationDate(FUTURE_EXPIRATION_DATE)
                .build();
    }

    public static CardInfo generateCardInfoWithId() {
        CardInfo cardInfo = generateCardInfo();
        cardInfo.setCardId(ENTITY_ID);
        return cardInfo;
    }

    public static CardInfo generateExpiredCardInfo() {
        return CardInfo.builder()
                .number(CARD_NUMBER)
                .holder(HOLDER)
                .expirationDate(EXPIRED_CARD_DATE)
                .build();
    }

    public static CardInfo generateSecondCardInfo() {
        return CardInfo.builder()
                .number(SECOND_CARD_NUMBER)
                .holder(HOLDER)
                .expirationDate(SECOND_CARD_EXPIRATION_DATE)
                .build();
    }

    public static CardInfo generateCardInfoForBatch(long index) {
        return CardInfo.builder()
                .number("111122223333444" + index)
                .holder(HOLDER)
                .expirationDate(FUTURE_EXPIRATION_DATE)
                .build();
    }
}
