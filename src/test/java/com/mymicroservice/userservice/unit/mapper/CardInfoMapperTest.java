package com.mymicroservice.userservice.unit.mapper;

import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.mapper.CardInfoMapper;
import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.util.CardInfoGenerator;
import com.mymicroservice.userservice.util.UserGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CardInfoMapperTest {

    @Test
    void toDto_ShouldMapFieldsCorrectly_WhenCardInfoIsValid() {
        User user = UserGenerator.generateUser();
        CardInfo cardInfo = CardInfoGenerator.generateCardInfo();
        cardInfo.setUserId(user);
        CardInfoDto cardInfoDto = CardInfoMapper.INSTANSE.toDto(cardInfo);

        assertEquals(cardInfo.getCardId(), cardInfoDto.getCardId());
        assertEquals(cardInfo.getNumber(), cardInfoDto.getNumber());
        assertEquals(cardInfo.getHolder(), cardInfoDto.getHolder());
        assertEquals(cardInfo.getExpirationDate(), cardInfoDto.getExpirationDate());
        assertEquals(cardInfo.getUserId().getUserId(), cardInfoDto.getUserId());
    }

    @Test
    void toEntity_ShouldMapFieldsCorrectly_WhenCardInfoDtoIsValid() {
        CardInfo cardInfo = CardInfoGenerator.generateCardInfo();
        CardInfoDto cardInfoDto = CardInfoMapper.INSTANSE.toDto(cardInfo);
        CardInfo mappedCardInfo = CardInfoMapper.INSTANSE.toEntity(cardInfoDto);

        assertEquals(cardInfoDto.getCardId(), mappedCardInfo.getCardId());
        assertEquals(cardInfoDto.getNumber(), mappedCardInfo.getNumber());
        assertEquals(cardInfoDto.getHolder(), mappedCardInfo.getHolder());
        assertEquals(cardInfoDto.getExpirationDate(), mappedCardInfo.getExpirationDate());
        assertEquals(cardInfoDto.getUserId(), mappedCardInfo.getUserId().getUserId());
    }
}
