package com.mymicroservice.userservice.integration.service;

import com.mymicroservice.userservice.configuration.TestContainersConfig;
import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.exception.CardInfoNotFoundException;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.CardInfoRepository;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.impl.CardInfoServiceImpl;
import com.mymicroservice.userservice.util.CardInfoDtoGenerator;
import com.mymicroservice.userservice.util.UserGenerator;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.mymicroservice.userservice.util.data.TestConstants.CARD_NUMBER;
import static com.mymicroservice.userservice.util.data.TestConstants.NEW_HOLDER;
import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.PAGINATION_PAGE_SIZE;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_CARD_NUMBER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@Transactional
class CardInfoServiceImplIT extends TestContainersConfig {

    @Autowired
    private CardInfoServiceImpl cardInfoService;

    @Autowired
    private CardInfoRepository cardInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private CardInfoDto testCardInfoDto;
    private User user;

    @BeforeEach
    void setUp() {
        cardInfoRepository.deleteAll();
        userRepository.deleteAll();

        user = UserGenerator.generateUser();
        user.setUserCards(new HashSet<>());
        user = userRepository.save(user);

        testCardInfoDto = CardInfoDtoGenerator.generateCardInfoDtoForUser(user);
        cacheManager.getCache("cardInfoCache").clear();
    }

    @Test
    void createCardInfo_ShouldSaveCardInfoToDatabase_WhenDtoIsValid() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);

        assertNotNull(createdCardInfo.getCardId());
        assertEquals(CARD_NUMBER, createdCardInfo.getNumber());
        assertEquals(testCardInfoDto.getHolder(), createdCardInfo.getHolder());
        assertEquals(testCardInfoDto.getExpirationDate(), createdCardInfo.getExpirationDate());
        assertEquals(user.getUserId(), createdCardInfo.getUserId());

        var cardInfoFromDb = cardInfoRepository.findById(createdCardInfo.getCardId()).orElseThrow();
        assertEquals(CARD_NUMBER, cardInfoFromDb.getNumber());
    }

    @Test
    void createCardInfo_ShouldThrowUserNotFoundException_WhenUserIdNotExists() {
        CardInfoDto cardInfoDto = CardInfoDtoGenerator.generateCardInfoDtoForUserId(NON_EXISTENT_ID);

        assertThrows(UserNotFoundException.class, () -> cardInfoService.createCardInfo(cardInfoDto));
    }

    @Test
    void getCardInfoById_ShouldReturnCardInfo_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);
        CardInfoDto foundCardInfo = cardInfoService.getCardInfoById(createdCardInfo.getCardId());

        assertNotNull(foundCardInfo);
        assertEquals(createdCardInfo.getCardId(), foundCardInfo.getCardId());
        assertEquals(createdCardInfo.getNumber(), foundCardInfo.getNumber());
    }

    @Test
    void getCardInfoById_ShouldThrowException_WhenCardInfoNotExists() {
        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.getCardInfoById(NON_EXISTENT_ID));
    }

    @Test
    void getCardInfoById_ShouldCacheCardInfo_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);

        cardInfoService.getCardInfoById(createdCardInfo.getCardId());

        CardInfoDto cachedCardInfo = cacheManager.getCache("cardInfoCache")
                .get(createdCardInfo.getCardId(), CardInfoDto.class);
        assertNotNull(cachedCardInfo);
        assertEquals(createdCardInfo.getCardId(), cachedCardInfo.getCardId());
        assertEquals(CARD_NUMBER, cachedCardInfo.getNumber());
    }

    @Test
    void updateCardInfo_ShouldUpdateCardInfoData_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);
        CardInfoDto updatedDto = CardInfoDtoGenerator.generateUpdateDto();

        CardInfoDto updatedCardInfo = cardInfoService.updateCardInfo(createdCardInfo.getCardId(), updatedDto);

        assertEquals(createdCardInfo.getCardId(), updatedCardInfo.getCardId());
        assertEquals(SECOND_CARD_NUMBER, updatedCardInfo.getNumber());
        assertEquals(NEW_HOLDER, updatedCardInfo.getHolder());
    }

    @Test
    void updateCardInfo_ShouldUpdateCardInfoDataInCache_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);
        CardInfoDto updatedDto = CardInfoDtoGenerator.generateUpdateDto();

        cardInfoService.updateCardInfo(createdCardInfo.getCardId(), updatedDto);

        CardInfoDto cachedCardInfo = cacheManager.getCache("cardInfoCache")
                .get(createdCardInfo.getCardId(), CardInfoDto.class);

        assertNotNull(cachedCardInfo);
        assertEquals(SECOND_CARD_NUMBER, cachedCardInfo.getNumber());
        assertEquals(NEW_HOLDER, cachedCardInfo.getHolder());
    }

    @Test
    void deleteCardInfo_ShouldRemoveCardInfoFromDatabase_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);
        cardInfoService.deleteCardInfo(createdCardInfo.getCardId());

        assertThrows(CardInfoNotFoundException.class,
                () -> cardInfoService.getCardInfoById(createdCardInfo.getCardId()));
    }

    @Test
    void deleteCardInfo_ShouldRemoveCardInfoFromCache_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);
        cardInfoService.getCardInfoById(createdCardInfo.getCardId());
        cardInfoService.deleteCardInfo(createdCardInfo.getCardId());

        assertNull(cacheManager.getCache("cardInfoCache").get(createdCardInfo.getCardId(), CardInfoDto.class));
    }

    @Test
    void deleteCardInfo_ShouldThrowException_WhenCardInfoNotExists() {
        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.deleteCardInfo(NON_EXISTENT_ID));
    }

    @Test
    void getCardInfoByNumber_ShouldReturnCardInfo_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);
        CardInfoDto foundCardInfo = cardInfoService.getCardInfoByNumber(createdCardInfo.getNumber());

        assertNotNull(foundCardInfo);
        assertEquals(createdCardInfo.getNumber(), foundCardInfo.getNumber());
    }

    @Test
    void getCardInfoIdIn_ShouldReturnCardInfosWithGivenIds_WhenCardInfosExist() {
        List<CardInfoDto> cardInfos = LongStream.rangeClosed(1, 5)
                .mapToObj(i -> cardInfoService.createCardInfo(
                        CardInfoDtoGenerator.generateCardInfoDtoForBatch(i, user.getUserId())))
                .collect(Collectors.toList());

        Set<Long> idsToFind = Set.of(
                cardInfos.get(0).getCardId(),
                cardInfos.get(2).getCardId(),
                cardInfos.get(4).getCardId()
        );

        List<CardInfoDto> foundCardInfos = cardInfoService.getCardInfoIdIn(idsToFind);

        assertEquals(3, foundCardInfos.size());
        assertTrue(foundCardInfos.stream().allMatch(c -> idsToFind.contains(c.getCardId())));
    }

    @Test
    void getExpiredCards_ShouldReturnExpiredCards_WhenExpiredCardsExist() {
        cardInfoService.createCardInfo(CardInfoDtoGenerator.generateExpiredCardInfoDto(user.getUserId()));

        List<CardInfoDto> expiredCards = cardInfoService.getExpiredCards();

        assertEquals(1, expiredCards.size());
        assertTrue(expiredCards.get(0).getExpirationDate().isBefore(LocalDate.now()));
        assertEquals(CARD_NUMBER, expiredCards.get(0).getNumber());
    }

    @Test
    void getByUserId_ShouldReturnCardInfos_WhenUserHasCards() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);

        List<CardInfoDto> userCards = cardInfoService.getByUserId(createdCardInfo.getUserId());

        assertNotNull(userCards);
        assertEquals(1, userCards.size());
        assertTrue(userCards.stream().anyMatch(dto -> dto.getNumber().equals(CARD_NUMBER)));
    }

    @Test
    void getAllCardInfos_ShouldReturnAllCardInfos_WhenCardInfosExist() {
        cardInfoService.createCardInfo(testCardInfoDto);
        cardInfoService.createCardInfo(CardInfoDtoGenerator.generateSecondCardInfoDto(user.getUserId()));

        List<CardInfoDto> allCardInfos = cardInfoService.getAllCardInfos();
        assertEquals(2, allCardInfos.size());
    }

    @Test
    void getAllCardInfosNativeWithPagination_ShouldReturnPageOfCardInfos_WhenCardInfosExist() {
        LongStream.rangeClosed(1, 4)
                .forEach(i -> cardInfoService.createCardInfo(
                        CardInfoDtoGenerator.generateCardInfoDtoForBatch(i, user.getUserId())));

        Page<CardInfoDto> firstPage = cardInfoService.getAllCardInfosNativeWithPagination(0, PAGINATION_PAGE_SIZE);
        assertEquals(PAGINATION_PAGE_SIZE, firstPage.getContent().size());
        assertTrue(firstPage.hasNext());

        Page<CardInfoDto> secondPage = cardInfoService.getAllCardInfosNativeWithPagination(1, PAGINATION_PAGE_SIZE);
        assertEquals(PAGINATION_PAGE_SIZE, secondPage.getContent().size());
    }
}
