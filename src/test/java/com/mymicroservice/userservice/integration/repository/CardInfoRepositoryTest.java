package com.mymicroservice.userservice.integration.repository;

import com.mymicroservice.userservice.configuration.PostgresTestContainersConfig;
import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.CardInfoRepository;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.util.CardInfoGenerator;
import com.mymicroservice.userservice.util.UserGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_CARD_NUMBER;
import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_CARD_NUMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers(disabledWithoutDocker = true)
class CardInfoRepositoryTest extends PostgresTestContainersConfig {

    @Autowired
    private CardInfoRepository cardInfoRepository;

    @Autowired
    private UserRepository userRepository;

    private User expectedUser;
    private CardInfo expectedCardInfo;

    @BeforeEach
    void init() {
        cardInfoRepository.deleteAll();
        userRepository.deleteAll();

        expectedUser = userRepository.save(UserGenerator.generateUser());
        expectedCardInfo = CardInfoGenerator.generateCardInfo();
        expectedCardInfo.setUserId(expectedUser);
        expectedCardInfo = cardInfoRepository.save(expectedCardInfo);
    }

    @Test
    void findByNumber_ShouldReturnCardInfo_WhenNumberExists() {
        Optional<CardInfo> actualCardInfo = cardInfoRepository.findByNumber(expectedCardInfo.getNumber());

        assertNotNull(actualCardInfo.orElse(null));
        assertEquals(expectedCardInfo, actualCardInfo.get());
    }

    @Test
    void findByNumber_ShouldReturnEmpty_WhenNumberNotExists() {
        Optional<CardInfo> actualCardInfo = cardInfoRepository.findByNumber(NON_EXISTENT_CARD_NUMBER);

        assertFalse(actualCardInfo.isPresent());
    }

    @Test
    void findByCardIdIn_ShouldReturnCards_WhenCardsExist() {
        List<CardInfo> actualCards = cardInfoRepository.findByCardIdIn(Set.of(expectedCardInfo.getCardId()));

        assertThat(actualCards).hasSize(1);
        assertEquals(expectedCardInfo.getCardId(), actualCards.get(0).getCardId());
    }

    @Test
    void findByCardIdIn_ShouldReturnEmptyList_WhenCardsNotExist() {
        List<CardInfo> actualCards = cardInfoRepository.findByCardIdIn(Set.of(NON_EXISTENT_ID));

        assertThat(actualCards).isEmpty();
    }

    @Test
    void findByUserId_ShouldReturnCards_WhenUserHasCards() {
        List<CardInfo> actualCards = cardInfoRepository.findByUserId(expectedUser.getUserId());

        assertThat(actualCards).hasSize(1);
        assertEquals(expectedUser.getUserId(), actualCards.get(0).getUserId().getUserId());
    }

    @Test
    void findByUserId_ShouldReturnEmptyList_WhenUserHasNoCards() {
        List<CardInfo> actualCards = cardInfoRepository.findByUserId(NON_EXISTENT_ID);

        assertThat(actualCards).isEmpty();
    }

    @Test
    void findExpiredCards_ShouldReturnExpiredCards_WhenExpiredCardsExist() {
        CardInfo expiredCard = CardInfoGenerator.generateCardInfo();
        expiredCard.setExpirationDate(LocalDate.now().minusDays(1));
        expiredCard.setNumber(SECOND_CARD_NUMBER);
        cardInfoRepository.save(expiredCard);

        List<CardInfo> expiredCards = cardInfoRepository.findExpiredCards();

        assertThat(expiredCards).isNotEmpty();
        assertTrue(expiredCards.stream().allMatch(card -> card.getExpirationDate().isBefore(LocalDate.now())));
    }

    @Test
    void findExpiredCards_ShouldReturnEmptyList_WhenNoExpiredCardsExist() {
        CardInfo validCard = CardInfoGenerator.generateCardInfo();
        validCard.setExpirationDate(LocalDate.now().plusYears(1));
        validCard.setNumber(SECOND_CARD_NUMBER);
        cardInfoRepository.save(validCard);

        List<CardInfo> expiredCards = cardInfoRepository.findExpiredCards();

        assertThat(expiredCards).isEmpty();
    }

    @Test
    void findAllCardInfoNative_ShouldReturnPaginatedResults_WhenCardsExist() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<CardInfo> page = cardInfoRepository.findAllCardInfoNative(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    void findAllCardInfoNative_ShouldReturnEmptyPage_WhenNoCardsExist() {
        cardInfoRepository.deleteAll();
        Pageable pageable = PageRequest.of(0, 10);

        Page<CardInfo> page = cardInfoRepository.findAllCardInfoNative(pageable);

        assertThat(page.getContent()).isEmpty();
    }
}
