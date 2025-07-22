package com.mymicroservice.userservice.mapper;

import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.util.CardInfoGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.Assert.assertEquals;

public class CardInfoMapperTest {

    @Test
    public void cardInfoToDto_whenOk_thenMapFieldsCorrectly() {
        CardInfo cardInfo = CardInfoGenerator.generateCardInfo();
        CardInfoDto cardInfoDto = CardInfoMapper.INSTANSE.toDto(cardInfo);
        assertEquals(cardInfo.getCardId(), cardInfoDto.getCardId());
        assertEquals(cardInfo.getNumber(), cardInfoDto.getNumber());
        assertEquals(cardInfo.getHolder(), cardInfoDto.getHolder());
        assertEquals(cardInfo.getExpirationDate(), cardInfoDto.getExpirationDate());
        assertEquals(cardInfo.getUserId().getUserId(), cardInfoDto.getUserId());
    }

    @Test
    public void cardInfoDtoToEntity_whenOk_thenMapFieldsCorrectly() {
        CardInfo cardInfo = CardInfoGenerator.generateCardInfo();
        CardInfoDto cardInfoDto = CardInfoMapper.INSTANSE.toDto(cardInfo);
        cardInfo = CardInfoMapper.INSTANSE.toEntity(cardInfoDto);
        assertEquals(cardInfoDto.getCardId(), cardInfo.getCardId());
        assertEquals(cardInfoDto.getNumber(), cardInfo.getNumber());
        assertEquals(cardInfoDto.getHolder(), cardInfo.getHolder());
        assertEquals(cardInfoDto.getExpirationDate(), cardInfo.getExpirationDate());
        assertEquals(cardInfoDto.getUserId(), cardInfo.getUserId().getUserId());
    }
}
