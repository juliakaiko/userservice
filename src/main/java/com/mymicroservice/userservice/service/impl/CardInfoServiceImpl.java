package com.mymicroservice.userservice.service.impl;

import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.exception.CardInfoNotFoundException;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.mapper.CardInfoMapper;
import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.repository.CardInfoRepository;
import com.mymicroservice.userservice.repository.UserRepository;
import com.mymicroservice.userservice.service.CardInfoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheConfig;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
@CacheConfig(cacheNames = "cardInfoCache")
public class CardInfoServiceImpl implements CardInfoService {

    private final CardInfoRepository cardInfoRepository;
    private final UserRepository userRepository;

    /**
     * Creates a new CardInfo based on the provided DTO.
     *
     * If userId is provided, adds the CardInfo to the list of cards for the User with specified ID.
     *
     * @param cardInfoDto DTO containing CardInfo data. Must not be {@code null}.
     * @return DTO of the created CardInfo.
     * @throws UserNotFoundException if userId is provided but no user with such ID is found
     * @throws jakarta.validation.ConstraintViolationException if card data is invalid
     */
    @Override
    @Transactional
    public CardInfoDto createCardInfo(@Valid CardInfoDto cardInfoDto) {
        CardInfo cardInfo = CardInfoMapper.INSTANSE.toEntity(cardInfoDto);
        log.info("createCardInfo(): {}",cardInfo);

        if (cardInfoDto.getUserId() != null) {
            User user = userRepository.findById(cardInfoDto.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + cardInfoDto.getUserId()));
            cardInfo.setUserId(user);
        }
        cardInfo = cardInfoRepository.save(cardInfo);
        return CardInfoMapper.INSTANSE.toDto(cardInfo);
    }

    /**
     * Returns the CardInfo by its ID.
     *
     * The method result is cached in "cardInfoCache" using the card ID as the key.
     * Subsequent calls with the same CardInfo ID will return the value from cache without database access
     *
     * @param cardId ID of the CardInfo
     * @return DTO of the found CardInfo
     * @throws CardInfoNotFoundException if no CardInfo with the specified ID is found
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#cardId")
    public CardInfoDto getCardInfoById(Long cardId) {
        Optional<CardInfo> cardInfo = Optional.ofNullable(cardInfoRepository.findById(cardId)
                .orElseThrow(() -> new CardInfoNotFoundException("CardInfo wasn't found with id " + cardId)));
        log.info("getCardInfoById(): {}",cardId);
        return  CardInfoMapper.INSTANSE.toDto(cardInfo.get());
    }

    /**
     * Updates CardInfo data (number, holder, expiration_date, userId) and updates the corresponding cache entry.
     * If no CardInfo with the specified ID is found, throws CardInfoNotFoundException.
     * After successful update, the updated CardInfo data is saved in "cardInfoCache" with the CardInfo ID as the key.
     *
     * @param cardId ID of the CardInfo to update
     * @param cardInfoDto DTO containing updated CardInfo data
     * @return DTO of the updated CardInfo
     * @throws CardInfoNotFoundException if no CardInfo with the specified ID is found
     * @throws UserNotFoundException if no User with ID cardInfoDto.getUserId() exists
     * @see org.springframework.cache.annotation.CachePut
     */
    @Override
    @Transactional
    @CachePut(key = "#cardId") // update in cache
    public CardInfoDto updateCardInfo(Long cardId, CardInfoDto cardInfoDto) {
        Optional<CardInfo> cardInfoFromDb = Optional.ofNullable(cardInfoRepository.findById(cardId)
                .orElseThrow(() -> new CardInfoNotFoundException("CardInfo wasn't found with id " + cardId)));
        CardInfo cardInfo = cardInfoFromDb.get();

        if (cardInfoDto.getUserId() != null) {
            User user = userRepository.findById(cardInfoDto.getUserId())
                    .orElseThrow(() -> new UserNotFoundException("User not found with id: " + cardInfoDto.getUserId()));
            cardInfo.setUserId(user);
        }
        cardInfo.setNumber(cardInfoDto.getNumber());
        cardInfo.setHolder(cardInfoDto.getHolder());
        cardInfo.setExpirationDate(cardInfoDto.getExpirationDate());
        log.info("updateCardInfo: {}",cardInfo);
        cardInfoRepository.save(cardInfo);
        return CardInfoMapper.INSTANSE.toDto(cardInfo);
    }

    /**
     * Deletes the CardInfo by its ID and removes the corresponding cache entry.
     * Throws CardInfoNotFoundException if no CardInfo with the specified ID is found.
     * After successful deletion, the entry with the card ID as key is removed from "cardInfoCache".
     *
     * @param cardId ID of the CardInfo to delete
     * @return DTO of the deleted CardInfo
     * @throws CardInfoNotFoundException if no CardInfo with the specified ID is found
     * @see org.springframework.cache.annotation.CacheEvict
     */
    @Override
    @Transactional
    @CacheEvict(key = "#cardId")
    public CardInfoDto deleteCardInfo(Long cardId) {
        Optional<CardInfo> cardInfo = Optional.ofNullable(cardInfoRepository.findById(cardId)
                .orElseThrow(() -> new CardInfoNotFoundException("CardInfo wasn't found with id " + cardId)));
        cardInfoRepository.deleteById(cardId);
        log.info("deleteCardInfo(): {}",cardInfo);
        return CardInfoMapper.INSTANSE.toDto(cardInfo.get());
    }

    /**
     * Returns the CardInfo by its number.
     *
     * @param number CardInfo number
     * @return DTO of the found CardInfo
     * @throws CardInfoNotFoundException if no CardInfo with the specified number is found
     */
    @Override
    @Transactional(readOnly = true)
    public CardInfoDto getCardInfoByNumber(String number) {
        Optional<CardInfo> cardInfo = Optional.ofNullable(cardInfoRepository.findByNumber(number)
                .orElseThrow(() -> new CardInfoNotFoundException("CardInfo wasn't found with number " + number)));
        log.info("getCardInfoByNumber(): {}",number);
        return  CardInfoMapper.INSTANSE.toDto(cardInfo.get());
    }

    /**
     * Returns a list of CardInfos belonging to the User with the specified userId.
     *
     * @param userId ID of the User whose CardInfos should be retrieved
     * @return List of card CardInfoDtos for the specified User
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getByUserId(Long userId){
        List <CardInfo> cardInfosList = cardInfoRepository.findByUserId(userId);
        log.info("getByUserId()");
        return cardInfosList.stream().map(CardInfoMapper.INSTANSE::toDto).toList();
    }

    /**
     * Returns a list of CardInfos by the specified set of IDs.
     *
     * @param ids Set of CardInfos IDs to search for
     * @return List of CardInfoDtos with the specified IDs
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getCardInfoIdIn(Set<Long> ids) {
        List <CardInfo> cardInfosList = cardInfoRepository.findByCardIdIn(ids);
        log.info("getCardInfoIdIn()");
        return cardInfosList.stream().map(CardInfoMapper.INSTANSE::toDto).toList();
    }

    /**
     * Returns a list of expired CardInfos.
     *
     * @return List of expired CardInfoDtos
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getExpiredCards() {
        List <CardInfo> cardInfosList = cardInfoRepository.findExpiredCards();
        log.info("getExpiredCards()");
        return cardInfosList.stream().map(CardInfoMapper.INSTANSE::toDto).toList();
    }

    /**
     * Returns a list of all CardInfos.
     *
     * @return List of all CardInfoDtos
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getAllCardInfos() {
        List <CardInfo> cardInfosList = cardInfoRepository.findAll();
        log.info("getAllCardInfos()");
        return cardInfosList.stream().map(CardInfoMapper.INSTANSE::toDto).toList();
    }

    /**
     * Returns a page of CardInfos using native pagination sorted by ID.
     *
     * @param page Page number (0-based index)
     * @param size Number of CardInfos per page
     * @return Page of card CardInfoDtos
     */
    @Override
    @Transactional(readOnly = true)
    public Page<CardInfoDto> getAllCardInfosNativeWithPagination(Integer page, Integer size) {
        var pageable  = PageRequest.of(page,size, Sort.by("id"));
        Page<CardInfo> cardInfos = cardInfoRepository.findAllCardInfoNative(pageable);
        log.info("getAllCardInfosNativeWithPagination()");
        return cardInfos.map(CardInfoMapper.INSTANSE::toDto);
    }
}
