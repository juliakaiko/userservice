package com.mymicroservice.userservice.unit.service;

import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.exception.CardInfoNotFoundException;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.mapper.CardInfoMapper;
import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.CardInfoRepository;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.impl.CardInfoServiceImpl;
import com.mymicroservice.userservice.util.CardInfoDtoGenerator;
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

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mymicroservice.userservice.util.data.TestConstants.CARD_NUMBER;
import static com.mymicroservice.userservice.util.data.TestConstants.DEFAULT_PAGE;
import static com.mymicroservice.userservice.util.data.TestConstants.DEFAULT_PAGE_SIZE;
import static com.mymicroservice.userservice.util.data.TestConstants.ENTITY_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.EXPIRED_CARD_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_HOLDER;
import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_CARD_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardInfoServiceImplTest {

    @InjectMocks
    private CardInfoServiceImpl cardInfoService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CardInfoRepository cardInfoRepository;

    private CardInfo testCardInfo;
    private CardInfoDto testCardInfoDto;
    private User testUser;

    @BeforeEach
    void setUp() {
        testCardInfo = CardInfoGenerator.generateCardInfoWithId();
        testUser = UserGenerator.generateUserWithId();
        testCardInfo.setUserId(testUser);
        testCardInfoDto = CardInfoMapper.INSTANSE.toDto(testCardInfo);
        testCardInfoDto.setUserId(testUser.getUserId());
    }

    @Test
    void createCardInfo_ShouldReturnCardInfoDto_WhenDtoIsValid() {
        when(userRepository.findById(ENTITY_ID)).thenReturn(Optional.of(testUser));
        when(cardInfoRepository.save(any(CardInfo.class))).thenReturn(testCardInfo);

        CardInfoDto result = cardInfoService.createCardInfo(testCardInfoDto);

        assertNotNull(result);
        assertEquals(testCardInfoDto.getNumber(), result.getNumber());
        verify(cardInfoRepository, times(1)).save(any(CardInfo.class));
    }

    @Test
    void createCardInfo_ShouldThrowUserNotFoundException_WhenUserIdNotExists() {
        testCardInfoDto.setUserId(NON_EXISTENT_ID);
        when(userRepository.findById(NON_EXISTENT_ID)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> cardInfoService.createCardInfo(testCardInfoDto));
        verify(cardInfoRepository, never()).save(any());
    }

    @Test
    void getCardInfoById_ShouldReturnCardInfoDto_WhenCardInfoExists() {
        when(cardInfoRepository.findById(ENTITY_ID)).thenReturn(Optional.of(testCardInfo));

        CardInfoDto result = cardInfoService.getCardInfoById(ENTITY_ID);

        assertNotNull(result);
        assertEquals(testCardInfoDto, result);
        verify(cardInfoRepository, times(1)).findById(ENTITY_ID);
    }

    @Test
    void getCardInfoById_ShouldThrowCardInfoNotFoundException_WhenCardInfoNotExists() {
        when(cardInfoRepository.findById(ENTITY_ID)).thenReturn(Optional.empty());

        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.getCardInfoById(ENTITY_ID));
        verify(cardInfoRepository, times(1)).findById(ENTITY_ID);
    }

    @Test
    void updateCardInfo_ShouldReturnUpdatedCardInfoDto_WhenCardInfoExists() {
        CardInfoDto updatedDto = CardInfoDtoGenerator.generateUpdateDto();
        when(cardInfoRepository.findById(ENTITY_ID)).thenReturn(Optional.of(testCardInfo));
        when(cardInfoRepository.save(any(CardInfo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CardInfoDto result = cardInfoService.updateCardInfo(ENTITY_ID, updatedDto);

        assertNotNull(result);
        assertEquals(NEW_HOLDER, result.getHolder());
        assertEquals(SECOND_CARD_NUMBER, result.getNumber());
        verify(cardInfoRepository, times(1)).save(any(CardInfo.class));
    }

    @Test
    void updateCardInfo_ShouldThrowCardInfoNotFoundException_WhenCardInfoNotExists() {
        when(cardInfoRepository.findById(ENTITY_ID)).thenReturn(Optional.empty());

        assertThrows(CardInfoNotFoundException.class,
                () -> cardInfoService.updateCardInfo(ENTITY_ID, testCardInfoDto));
        verify(cardInfoRepository, never()).save(any());
    }

    @Test
    void deleteCardInfo_ShouldReturnDeletedCardInfoDto_WhenCardInfoExists() {
        when(cardInfoRepository.findById(ENTITY_ID)).thenReturn(Optional.of(testCardInfo));

        CardInfoDto result = cardInfoService.deleteCardInfo(ENTITY_ID);

        assertNotNull(result);
        assertEquals(testCardInfoDto, result);
        verify(cardInfoRepository, times(1)).deleteById(ENTITY_ID);
    }

    @Test
    void deleteCardInfo_ShouldThrowCardInfoNotFoundException_WhenCardInfoNotExists() {
        when(cardInfoRepository.findById(ENTITY_ID)).thenReturn(Optional.empty());

        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.deleteCardInfo(ENTITY_ID));
        verify(cardInfoRepository, never()).deleteById(any());
    }

    @Test
    void getCardInfoByNumber_ShouldReturnCardInfoDto_WhenCardInfoExists() {
        when(cardInfoRepository.findByNumber(CARD_NUMBER)).thenReturn(Optional.of(testCardInfo));

        CardInfoDto result = cardInfoService.getCardInfoByNumber(CARD_NUMBER);

        assertNotNull(result);
        assertEquals(testCardInfoDto, result);
        verify(cardInfoRepository, times(1)).findByNumber(CARD_NUMBER);
    }

    @Test
    void getCardInfoByNumber_ShouldThrowCardInfoNotFoundException_WhenCardInfoNotExists() {
        when(cardInfoRepository.findByNumber(CARD_NUMBER)).thenReturn(Optional.empty());

        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.getCardInfoByNumber(CARD_NUMBER));
    }

    @Test
    void getByUserId_ShouldReturnCardInfoDtoList_WhenCardInfosExist() {
        when(cardInfoRepository.findByUserId(ENTITY_ID)).thenReturn(List.of(testCardInfo));

        List<CardInfoDto> result = cardInfoService.getByUserId(ENTITY_ID);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(CARD_NUMBER, result.get(0).getNumber());
    }

    @Test
    void getCardInfoIdIn_ShouldReturnCardInfoDtoList_WhenCardInfosExist() {
        Set<Long> ids = Set.of(ENTITY_ID);
        when(cardInfoRepository.findByCardIdIn(ids)).thenReturn(List.of(testCardInfo));

        List<CardInfoDto> result = cardInfoService.getCardInfoIdIn(ids);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCardInfoDto, result.get(0));
    }

    @Test
    void getExpiredCards_ShouldReturnExpiredCardDtoList_WhenExpiredCardsExist() {
        CardInfo expiredCard = CardInfo.builder()
                .cardId(2L)
                .number(SECOND_CARD_NUMBER)
                .holder(NEW_HOLDER)
                .expirationDate(EXPIRED_CARD_DATE)
                .build();
        when(cardInfoRepository.findExpiredCards()).thenReturn(List.of(expiredCard));

        List<CardInfoDto> result = cardInfoService.getExpiredCards();

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(cardInfoRepository, times(1)).findExpiredCards();
    }

    @Test
    void getAllCardInfos_ShouldReturnCardInfoDtoList_WhenCardInfosExist() {
        when(cardInfoRepository.findAll()).thenReturn(List.of(testCardInfo));

        List<CardInfoDto> result = cardInfoService.getAllCardInfos();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testCardInfoDto, result.get(0));
    }

    @Test
    void getAllCardInfosNativeWithPagination_ShouldReturnPageOfCardInfoDto_WhenCalled() {
        Pageable pageable = PageRequest.of(DEFAULT_PAGE, DEFAULT_PAGE_SIZE, Sort.by("id"));
        Page<CardInfo> cardInfoPage = new PageImpl<>(List.of(testCardInfo), pageable, 1);

        when(cardInfoRepository.findAllCardInfoNative(pageable)).thenReturn(cardInfoPage);

        Page<CardInfoDto> result = cardInfoService.getAllCardInfosNativeWithPagination(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(testCardInfoDto, result.getContent().get(0));
    }
}
