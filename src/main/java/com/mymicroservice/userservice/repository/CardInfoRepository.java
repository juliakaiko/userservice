package com.mymicroservice.userservice.repository;

import com.mymicroservice.userservice.model.CardInfo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CardInfoRepository extends JpaRepository<CardInfo, Long> {

    /**
     * Finds a card by its number using "named method"
     *
     * @param number the card number to search for
     * @return an {@link Optional} containing the card if found, empty otherwise
     * @throws IllegalArgumentException if the number is null
     */
    Optional<CardInfo> findByNumber(String number);

    /**
     * Finds all cards with specified IDs using "named method".
     *
     * @param ids set of card IDs to search for
     * @return a list of cards matching the provided IDs (may be empty)
     * @throws IllegalArgumentException if the ids set is null
     */
    List<CardInfo> findByCardIdIn(Set<Long> ids);

    /**
     * Finds all cards by userId using JPQL query.
     *
     * @param userId user id to search for
     * @return a list of cards matching the provided userId (may be empty)
     * @throws IllegalArgumentException if the userId is null
     */
    @Query("SELECT c FROM CardInfo c WHERE c.userId.userId  = :userId")
    List<CardInfo> findByUserId(@Param("userId") Long userId);

    /**
     * Finds all expired cards (where expiration date is before current date) using JPQL query.
     *
     * @return a list of expired cards ordered by ID (may be empty)
     */
    @Query("SELECT c FROM CardInfo c WHERE c.expirationDate < CURRENT_DATE")
    List<CardInfo> findExpiredCards();

    /**
     * Retrieves all card information with pagination support using native SQL.
     *
     * @param pageable pagination information (page number, size, etc.)
     * @return a {@link Page} of cards ordered by ID in ascending order
     * @throws IllegalArgumentException if pageable is null
     */
    @Query(value = "select * from card_info order by card_info.id asc", nativeQuery = true)
    Page<CardInfo> findAllCardInfoNative(Pageable pageable);
}
