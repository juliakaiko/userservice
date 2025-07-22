package com.mymicroservice.userservice.service;

import com.mymicroservice.userservice.dto.CardInfoDto;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface CardInfoService {

    CardInfoDto createCardInfo(CardInfoDto cardInfoDto);
    CardInfoDto getCardInfoById(Long cardId);
    CardInfoDto getCardInfoByNumber(String number);
    List<CardInfoDto> getByUserId(Long userId);
    List<CardInfoDto> getCardInfoIdIn(Set<Long> ids);
    List<CardInfoDto> getExpiredCards();
    List<CardInfoDto> getAllCardInfos();
    Page<CardInfoDto> getAllCardInfosNativeWithPagination(Integer page, Integer size);
    CardInfoDto updateCardInfo(Long cardId, CardInfoDto cardInfoDetails);
    CardInfoDto deleteCardInfo(Long cardId);
}
