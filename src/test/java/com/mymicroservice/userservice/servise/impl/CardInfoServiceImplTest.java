package com.mymicroservice.userservice.servise.impl;

import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.exception.CardInfoNotFoundException;
import com.mymicroservice.userservice.mapper.CardInfoMapper;
import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.CardInfoRepository;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.impl.CardInfoServiceImpl;
import com.mymicroservice.userservice.util.CardInfoGenerator;
import com.mymicroservice.userservice.util.UserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CardInfoServiceImplTest {

    @InjectMocks
    private CardInfoServiceImpl cardInfoService;

    @Mock
    private UserRepository userRepository;

    @Mock 
    private CardInfoRepository cardInfoRepository;

    private final Long TEST_CARD_INFO_ID = 1L;
    private final String TEST_CARD_INFO_NUMBER = "1111222233334444";
    private CardInfo testCardInfo;
    private CardInfoDto testCardInfoDto;

    @BeforeEach
    void setUp() {
        testCardInfo = CardInfoGenerator.generateCardInfo();
        User user = UserGenerator.generateUser();
        user.setUserId(1l);
        testCardInfo.setUserId(user);
        testCardInfoDto = CardInfoMapper.INSTANSE.toDto(testCardInfo);
    }

    @Test
    public void createCardInfo_whenCorrect_thenReturnCardInfoDto() {
        CardInfo mappedCardInfo = CardInfoMapper.INSTANSE.toEntity(testCardInfoDto);
        User user = UserGenerator.generateUser();

        when(userRepository.findById(1l)).thenReturn(Optional.of(user));
        when(cardInfoRepository.save(mappedCardInfo)).thenReturn(testCardInfo);

        CardInfoDto result = cardInfoService.createCardInfo(testCardInfoDto);

        assertNotNull(result);
        assertEquals(testCardInfoDto, result);

        verify(cardInfoRepository, times(1)).save(any(CardInfo.class));
    }

    @Test
    public void getCardInfoById_whenCardInfoExists_thenReturnCardInfoDto() {
        when(cardInfoRepository.findById(TEST_CARD_INFO_ID)).thenReturn(Optional.of(testCardInfo));

        CardInfoDto result = cardInfoService.getCardInfoById(TEST_CARD_INFO_ID);

        assertNotNull(result);
        assertEquals(testCardInfoDto, result);
        verify(cardInfoRepository, times(1)).findById(TEST_CARD_INFO_ID);
    }

    @Test
    public void getCardInfoById_whenCardInfoNotExists_thenThrowCardInfoNotFoundException() {
        when(cardInfoRepository.findById(TEST_CARD_INFO_ID)).thenReturn(Optional.empty());

        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.getCardInfoById(TEST_CARD_INFO_ID));
        verify(cardInfoRepository, times(1)).findById(TEST_CARD_INFO_ID);
    }

    @Test
    public void updateCardInfo_whenCardInfoExists_thenReturnUpdatedCardInfoDto() {
        CardInfoDto updatedDto = new CardInfoDto();
        updatedDto.setHolder("New Holder");
        updatedDto.setNumber("1111222233334444");

        when(cardInfoRepository.findById(TEST_CARD_INFO_ID)).thenReturn(Optional.of(testCardInfo));
        when(cardInfoRepository.save(any(CardInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardInfoDto result = cardInfoService.updateCardInfo(TEST_CARD_INFO_ID, updatedDto);

        assertNotNull(result);
        assertEquals(updatedDto.getHolder(), result.getHolder());
        assertEquals(updatedDto.getNumber(), result.getNumber());
        verify(cardInfoRepository, times(1)).findById(TEST_CARD_INFO_ID);
        verify(cardInfoRepository, times(1)).save(any(CardInfo.class));
    }

    @Test
    public void updateCardInfo_whenCardInfoNotExists_thenThrowCardInfoNotFoundException() {
        when(cardInfoRepository.findById(TEST_CARD_INFO_ID)).thenReturn(Optional.empty());

        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.updateCardInfo(TEST_CARD_INFO_ID, testCardInfoDto));
        verify(cardInfoRepository, times(1)).findById(TEST_CARD_INFO_ID);
        verify(cardInfoRepository, never()).save(any());
    }

    @Test
    public void deleteCardInfo_whenCardInfoExists_thenReturnDeletedCardInfoDto() {
        when(cardInfoRepository.findById(TEST_CARD_INFO_ID)).thenReturn(Optional.of(testCardInfo));

        CardInfoDto result = cardInfoService.deleteCardInfo(TEST_CARD_INFO_ID);

        assertNotNull(result);
        assertEquals(testCardInfoDto, result);
        verify(cardInfoRepository, times(1)).findById(TEST_CARD_INFO_ID);
        verify(cardInfoRepository, times(1)).deleteById(TEST_CARD_INFO_ID);
    }

    @Test
    public void deleteCardInfo_whenCardInfoNotExists_thenThrowCardInfoNotFoundException() {
        when(cardInfoRepository.findById(TEST_CARD_INFO_ID)).thenReturn(Optional.empty());

        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.deleteCardInfo(TEST_CARD_INFO_ID));
        verify(cardInfoRepository, times(1)).findById(TEST_CARD_INFO_ID);
        verify(cardInfoRepository, never()).deleteById(any());
    }

    @Test
    public void getCardInfoByNumber_whenCardInfoExists_thenReturnCardInfoDto() {
        when(cardInfoRepository.findByNumber(TEST_CARD_INFO_NUMBER)).thenReturn(Optional.of(testCardInfo));

        CardInfoDto result = cardInfoService.getCardInfoByNumber(TEST_CARD_INFO_NUMBER);

        assertNotNull(result);
        assertEquals(testCardInfoDto, result);
        verify(cardInfoRepository, times(1)).findByNumber(TEST_CARD_INFO_NUMBER);
    }

    @Test
    public void getCardInfoByNumber_whenCardInfoNotExists_thenThrowCardInfoNotFoundException() {
        when(cardInfoRepository.findByNumber(TEST_CARD_INFO_NUMBER)).thenReturn(Optional.empty());

        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.getCardInfoByNumber(TEST_CARD_INFO_NUMBER));
        verify(cardInfoRepository, times(1)).findByNumber(TEST_CARD_INFO_NUMBER);
    }

    @Test
    public void getAllCardInfosByUserId_whenCardInfosExist_thenReturnCardInfoDtoList() {
        when(cardInfoRepository.findByUserId(1L)).thenReturn(List.of(testCardInfo));

        List<CardInfoDto> result = cardInfoService.getByUserId(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCardInfoDto,result.get(0));
        verify(cardInfoRepository, times(1)).findByUserId(1l);
    }

    @Test
    public void getCardInfosIdIn_whenCardInfosExist_thenReturnCardInfoDtoList() {
        Set<Long> ids = Set.of(TEST_CARD_INFO_ID);
        when(cardInfoRepository.findByCardIdIn(ids)).thenReturn(List.of(testCardInfo));

        List<CardInfoDto> result = cardInfoService.getCardInfoIdIn(ids);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCardInfoDto, result.get(0));
        verify(cardInfoRepository, times(1)).findByCardIdIn(ids);
    }

    @Test
    void getByUserId_ShouldReturnListOfCardInfoDto() {
        Long userId = testCardInfo.getUserId().getUserId();
        List<CardInfo> mockCards = List.of(testCardInfo);

        when(cardInfoRepository.findByUserId(userId)).thenReturn(mockCards);

        List<CardInfoDto> result = cardInfoService.getByUserId(userId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("1111222233334444", result.get(0).getNumber());
        verify(cardInfoRepository, times(1)).findByUserId(userId);
    }

    @Test
    void getExpiredCards_ShouldReturnListOfExpiredCards() {
        CardInfo expiredCard =  CardInfo.builder()
                .cardId(2l)
                .number("1111222233330000")
                .holder("TestUser")
                .expirationDate(LocalDate.of(2025, 3, 3))
                .build();
        List<CardInfo> mockCards = List.of(expiredCard);

        when(cardInfoRepository.findExpiredCards()).thenReturn(mockCards);

        List<CardInfoDto> result = cardInfoService.getExpiredCards();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardInfoRepository, times(1)).findExpiredCards();
    }


    @Test
    public void getAllCardInfos_whenCardInfosExist_thenReturnCardInfoDtoList() {
        when(cardInfoRepository.findAll()).thenReturn(List.of(testCardInfo));

        List<CardInfoDto> result = cardInfoService.getAllCardInfos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCardInfoDto, result.get(0));
        verify(cardInfoRepository, times(1)).findAll();
    }

    @Test
    public void getAllCardInfosNativeWithPagination_whenCalled_thenReturnPageOfCardInfoDto() {
        int page = 0;
        int size = 10;
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));
        Page<CardInfo> cardInfoPage = new PageImpl<>(List.of(testCardInfo), pageable, 1); // 1 - общее количество элементов во всех страницах

        when(cardInfoRepository.findAllCardInfoNative(pageable)).thenReturn(cardInfoPage);

        Page<CardInfoDto> result = cardInfoService.getAllCardInfosNativeWithPagination(page, size);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCardInfoDto, result.getContent().get(0));
        verify(cardInfoRepository, times(1)).findAllCardInfoNative(pageable);
    }
}
