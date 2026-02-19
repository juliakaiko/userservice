package com.mymicroservice.userservice.servise.testcontainer;

import com.mymicroservice.userservice.configuration.TestContainersConfig;
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
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CardInfoServiceImplIT extends TestContainersConfig{

    @Autowired
    private CardInfoServiceImpl cardInfoService;

    @Autowired
    private CardInfoRepository cardInfoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CacheManager cacheManager;

    private CardInfo testCardInfo;
    private CardInfoDto testCardInfoDto;
    private User user;

    @BeforeEach
    void setUp() {
        cardInfoRepository.deleteAll();
        userRepository.deleteAll();

        user = UserGenerator.generateUser();
        user.setUserCards(new HashSet<>());
        user = userRepository.save(user);

        testCardInfo = CardInfoGenerator.generateCardInfo();
        testCardInfo.setUserId(user);
        testCardInfoDto = CardInfoMapper.INSTANSE.toDto(testCardInfo);
        testCardInfoDto.setUserId(user.getUserId());
        cacheManager.getCache("cardInfoCache").clear();
    }

    @Test
    void createCardInfo_ShouldSaveCardInfoToDatabase() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);

        assertNotNull(createdCardInfo.getCardId());
        assertEquals(testCardInfoDto.getNumber(), createdCardInfo.getNumber());
        assertEquals(testCardInfoDto.getHolder(), createdCardInfo.getHolder());
        assertEquals(testCardInfoDto.getExpirationDate(), createdCardInfo.getExpirationDate());
        assertEquals(testCardInfoDto.getUserId(), createdCardInfo.getUserId());

        CardInfo cardInfoFromDb = cardInfoRepository.findById(createdCardInfo.getCardId()).orElseThrow();
        assertEquals(createdCardInfo.getNumber(), cardInfoFromDb.getNumber());
    }

    @Test
    void getCardInfosById_ShouldReturnCardInfo_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);
        CardInfoDto foundCardInfo = cardInfoService.getCardInfoById(createdCardInfo.getCardId());

        assertNotNull(foundCardInfo);
        assertEquals(createdCardInfo.getCardId(), foundCardInfo.getCardId());
        assertEquals(createdCardInfo, foundCardInfo);
    }

    @Test
    void getCardInfosById_ShouldThrowException_WhenCardInfoNotExists() {
        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.getCardInfoById(999L));
    }

    /**
     * Tests that CardInfo is properly cached when retrieved by ID.
     *
     * <p>The test performs the following steps:
     * <ol>
     *   <li>Creates a test CardInfo entity using the service</li>
     *   <li>Makes the first call to {@code getCardInfoById()} which should cache the result</li>
     *   <li>Verifies that the CardInfo exists in the cache</li>
     *   <li>Validates that all cached fields match the original values</li>
     * </ol>
     */
    @Test
    void getCardInfosById_ShouldCacheCardInfo() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);

        cardInfoService.getCardInfoById(createdCardInfo.getCardId()); //first call - must save to cache

        CardInfoDto cachedCardInfo = cacheManager.getCache("cardInfoCache") //Check if the CardInfo is in the cache
                .get(createdCardInfo.getCardId(), CardInfoDto.class);
        assertNotNull(cachedCardInfo);
        assertEquals(createdCardInfo.getCardId(), cachedCardInfo.getCardId());
        assertEquals(createdCardInfo.getNumber(), cachedCardInfo.getNumber());
        assertEquals(createdCardInfo.getHolder(), cachedCardInfo.getHolder());
        assertEquals(createdCardInfo.getExpirationDate(), cachedCardInfo.getExpirationDate());
    }

    @Test
    void updateCardInfo_ShouldUpdateCardInfoData() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);

        CardInfoDto updatedDto = new CardInfoDto();
        updatedDto.setNumber("9999888877776666");
        updatedDto.setHolder("New Holder");

        CardInfoDto updatedCardInfo = cardInfoService.updateCardInfo(createdCardInfo.getCardId(), updatedDto);

        assertEquals(createdCardInfo.getCardId(), updatedCardInfo.getCardId());
        assertEquals("9999888877776666", updatedCardInfo.getNumber());
        assertEquals("New Holder", updatedCardInfo.getHolder());
    }

    @Test
    void updateCardInfo_ShouldUpdateCardInfoDataInCache() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);

        CardInfoDto updatedDto = new CardInfoDto();
        updatedDto.setNumber("9999888877776666");
        updatedDto.setHolder("New Holder");

        cardInfoService.updateCardInfo(createdCardInfo.getCardId(), updatedDto);

        CardInfoDto cachedCardInfo = cacheManager.getCache("cardInfoCache")
                .get(createdCardInfo.getCardId(), CardInfoDto.class);

        assertNotNull(createdCardInfo.getCardId());
        assertNotNull(cachedCardInfo);
        assertEquals("9999888877776666", cachedCardInfo.getNumber());
        assertEquals("New Holder", cachedCardInfo.getHolder());
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
        cardInfoService.deleteCardInfo(createdCardInfo.getCardId());

        assertNull(cacheManager.getCache("cardInfoCache").get(createdCardInfo.getCardId(), CardInfoDto.class));
    }

    @Test
    void deleteCardInfosById_ShouldThrowException_WhenCardInfoNotExists() {
        assertThrows(CardInfoNotFoundException.class, () -> cardInfoService.deleteCardInfo(999L));
    }

    @Test
    void getCardInfosByNumber_ShouldReturnCardInfo_WhenCardInfoExists() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);
        CardInfoDto foundCardInfo = cardInfoService.getCardInfoByNumber(createdCardInfo.getNumber());

        assertNotNull(foundCardInfo);
        assertEquals(createdCardInfo.getNumber(), foundCardInfo.getNumber());
        assertEquals(createdCardInfo, foundCardInfo);
    }

    @Test
    void getCardInfosIdIn_ShouldReturnCardInfosWithGivenIds() {
        List<CardInfoDto> cardInfos = LongStream.range(1, 6) // stream:  1,2,3,4,5
                .mapToObj(i -> cardInfoService.createCardInfo(
                        CardInfoDto.builder()
                                .cardId(null)
                                .number("111122223333444"+i)
                                .holder("TestUser")
                                .expirationDate(LocalDate.of(2030, 3, 3))
                                .userId(testCardInfoDto.getUserId())
                                .build()
                )).collect(Collectors.toList());

        Set<Long> idsToFind = Set.of(cardInfos.get(0).getCardId(), cardInfos.get(2).getCardId(), cardInfos.get(4).getCardId());

        List<CardInfoDto> foundCardInfos = cardInfoService.getCardInfoIdIn(idsToFind);

        assertEquals(3, foundCardInfos.size());
        assertTrue(foundCardInfos.stream().allMatch(u -> idsToFind.contains(u.getCardId())));
    }

    @Test
    void getExpiredCards_ShouldReturnCardsExpiredAfterGivenDate() {
        CardInfoDto cardInfoDto = CardInfoDto.builder()
                .number("1111222233334444")
                .holder("TestUser")
                .expirationDate(LocalDate.of(2024, 3, 3))
                .userId(user.getUserId())
                .build();

        cardInfoService.createCardInfo(cardInfoDto);

        List<CardInfoDto> expiredCards = cardInfoService.getExpiredCards();
        LocalDate date = LocalDate.now();

        assertEquals(1, expiredCards.size());
        assertTrue(expiredCards.get(0).getExpirationDate().isBefore(date));
        assertEquals("1111222233334444", expiredCards.get(0).getNumber());
    }

    @Test
    void getCardsByUserId_ShouldReturnCardInfos() {
        CardInfoDto createdCardInfo = cardInfoService.createCardInfo(testCardInfoDto);

        List<CardInfoDto> userCards = cardInfoService.getByUserId(createdCardInfo.getUserId());

        assertNotNull(userCards);
        assertEquals(1, userCards.size());
        assertTrue(userCards.stream().anyMatch(dto -> dto.getNumber().equals("1111222233334444")));
    }

    @Test
    void getAllCardInfos_ShouldReturnAllCardInfos() {
        cardInfoService.createCardInfo(testCardInfoDto);

        cardInfoService.createCardInfo(
                CardInfoDto.builder()
                        .number("1111222233330000")
                        .holder("TestUser")
                        .expirationDate(LocalDate.of(2025, 3, 3))
                        .userId(user.getUserId())
                        .build()
        );

        List<CardInfoDto> allCardInfos = cardInfoService.getAllCardInfos();
        assertEquals(2, allCardInfos.size());
    }

    @Test
    void getAllCardInfosNativeWithPagination_ShouldReturnPageOfCardInfos() {
        List<CardInfoDto> cardInfos = LongStream.range(1, 5) // поток Stream: 1,2,3,4
                .mapToObj(i -> cardInfoService.createCardInfo(
                        CardInfoDto.builder()
                                .cardId(null)
                                .number("111122223333444"+i)
                                .holder("TestUser")
                                .expirationDate(LocalDate.of(2030, 3, 3))
                                .userId(user.getUserId())
                                .build()
                )).collect(Collectors.toList());

        Page<CardInfoDto> firstPage = cardInfoService.getAllCardInfosNativeWithPagination(0, 2);
        assertEquals(2, firstPage.getContent().size());
        assertTrue(firstPage.hasNext());

        Page<CardInfoDto> secondPage = cardInfoService.getAllCardInfosNativeWithPagination(1, 2);
        assertEquals(2, secondPage.getContent().size());
    }
}
