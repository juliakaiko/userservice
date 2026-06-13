package com.mymicroservice.userservice.util;

import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.mapper.CardInfoMapper;
import com.mymicroservice.userservice.model.User;

import static com.mymicroservice.userservice.util.data.TestConstants.ENTITY_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_HOLDER;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_CARD_NUMBER;

public class CardInfoDtoGenerator {

    public static CardInfoDto generateCardInfoDto() {
        return CardInfoMapper.INSTANSE.toDto(CardInfoGenerator.generateCardInfo());
    }

    public static CardInfoDto generateCardInfoDtoWithId() {
        CardInfoDto cardInfoDto = generateCardInfoDto();
        cardInfoDto.setCardId(ENTITY_ID);
        return cardInfoDto;
    }

    public static CardInfoDto generateCardInfoDtoForUser(User user) {
        CardInfoDto cardInfoDto = generateCardInfoDto();
        cardInfoDto.setUserId(user.getUserId());
        return cardInfoDto;
    }

    public static CardInfoDto generateCardInfoDtoForUserId(Long userId) {
        CardInfoDto cardInfoDto = generateCardInfoDto();
        cardInfoDto.setUserId(userId);
        return cardInfoDto;
    }

    public static CardInfoDto generateExpiredCardInfoDto(Long userId) {
        CardInfoDto cardInfoDto = CardInfoMapper.INSTANSE.toDto(CardInfoGenerator.generateExpiredCardInfo());
        cardInfoDto.setUserId(userId);
        return cardInfoDto;
    }

    public static CardInfoDto generateSecondCardInfoDto(Long userId) {
        CardInfoDto cardInfoDto = CardInfoMapper.INSTANSE.toDto(CardInfoGenerator.generateSecondCardInfo());
        cardInfoDto.setUserId(userId);
        return cardInfoDto;
    }

    public static CardInfoDto generateCardInfoDtoForBatch(long index, Long userId) {
        CardInfoDto cardInfoDto = CardInfoMapper.INSTANSE.toDto(CardInfoGenerator.generateCardInfoForBatch(index));
        cardInfoDto.setUserId(userId);
        return cardInfoDto;
    }

    public static CardInfoDto generateUpdateDto() {
        CardInfoDto updateDto = new CardInfoDto();
        updateDto.setNumber(SECOND_CARD_NUMBER);
        updateDto.setHolder(NEW_HOLDER);
        return updateDto;
    }
}
