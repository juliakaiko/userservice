package com.mymicroservice.userservice.repository;

import com.mymicroservice.userservice.configuration.TestContainersConfig;
import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.util.CardInfoGenerator;
import com.mymicroservice.userservice.util.UserGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@Slf4j
@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE) // Disabling DataSource Replacement
public class CardInfoRepositoryTest extends TestContainersConfig {

    @Autowired
    private CardInfoRepository cardInfoRepository;

    @Autowired
    private UserRepository userRepository;

    private User expectedUser;

    private CardInfo expectedCardInfo;

    @BeforeEach
    public void init() {
        cardInfoRepository.deleteAll();
        userRepository.deleteAll();

        expectedUser = UserGenerator.generateUser();
        expectedUser = userRepository.save(expectedUser);

        expectedCardInfo = CardInfoGenerator.generateCardInfo();
        expectedCardInfo.setUserId(expectedUser);
        expectedCardInfo = cardInfoRepository.save(expectedCardInfo);
    }

    @Test
    void findByNumber_shouldReturnCardInfoWhenExists() {
        Optional<CardInfo> actualCardInfo = cardInfoRepository.findByNumber(expectedCardInfo.getNumber());
        log.info("Test to find the CardInfo with number: {} "+expectedCardInfo.getNumber());

        assertNotNull(actualCardInfo.get());
        assertEquals(expectedCardInfo, actualCardInfo.get());
        assertThat(actualCardInfo).isPresent().contains(expectedCardInfo);
    }

    @Test
    public void findByNumber_shouldReturnEmptyWhenNotExists() {
        Optional<CardInfo> actualCardInfo = cardInfoRepository.findByNumber("non-existing-number");

        assertFalse(actualCardInfo.isPresent());
    }

    @Test
    public void findByCardIdIn_shouldReturnCardsWhenExist() {
        Set<Long> ids = Set.of(expectedCardInfo.getCardId());

        List<CardInfo> actualCards = cardInfoRepository.findByCardIdIn(ids);

        assertThat(actualCards).hasSize(1);
        assertEquals(expectedCardInfo.getCardId(), actualCards.get(0).getCardId());
    }

    @Test
    public void findByCardIdIn_shouldReturnEmptyListWhenNotExist() {
        Set<Long> ids = Set.of(999L);

        List<CardInfo> actualCards = cardInfoRepository.findByCardIdIn(ids);

        assertThat(actualCards).isEmpty();
    }

    @Test
    public void findByUserId_shouldReturnCardsWhenExist() {
        List<CardInfo> actualCards = cardInfoRepository.findByUserId(expectedCardInfo.getUserId().getUserId());

        assertThat(actualCards).hasSize(1);
        assertEquals(expectedCardInfo.getUserId().getUserId(), actualCards.get(0).getUserId().getUserId());
    }

    @Test
    public void findByUserId_shouldReturnEmptyListWhenNotExist() {
        List<CardInfo> actualCards = cardInfoRepository.findByUserId(999L);

        assertThat(actualCards).isEmpty();
    }

    @Test
    public void findExpiredCards_shouldReturnExpiredCards() {
        CardInfo expiredCard = CardInfoGenerator.generateCardInfo();
        expiredCard.setExpirationDate(LocalDate.now().minusDays(1));
        expiredCard.setNumber("1111222233335555");
        cardInfoRepository.save(expiredCard);

        List<CardInfo> expiredCards = cardInfoRepository.findExpiredCards();

        assertThat(expiredCards).isNotEmpty();
        assertTrue(expiredCards.stream().allMatch(card ->
                card.getExpirationDate().isBefore(LocalDate.now())));
    }

    @Test
    public void findExpiredCards_shouldReturnEmptyListWhenNoExpiredCards() {
        CardInfo validCard = CardInfoGenerator.generateCardInfo();
        validCard.setExpirationDate(LocalDate.now().plusYears(1));
        validCard.setNumber("1111222233335555");
        cardInfoRepository.save(validCard);

        List<CardInfo> expiredCards = cardInfoRepository.findExpiredCards();

        assertThat(expiredCards).isEmpty();
    }

    @Test
    public void findAllCardInfosNative_shouldReturnPaginatedResults() {
        Pageable pageable = PageRequest.of(0, 2);

        Page<CardInfo> page = cardInfoRepository.findAllCardInfoNative(pageable);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getTotalElements()).isGreaterThanOrEqualTo(1);
        assertThat(page.getNumber()).isEqualTo(0);
    }

    @Test
    public void findAllCardInfosNative_shouldReturnEmptyPageWhenNoCards() {
        cardInfoRepository.deleteAll();
        Pageable pageable = PageRequest.of(0, 10);

        Page<CardInfo> page = cardInfoRepository.findAllCardInfoNative(pageable);

        assertThat(page.getContent()).isEmpty();
    }
}
