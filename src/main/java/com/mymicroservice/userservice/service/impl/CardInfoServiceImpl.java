package com.mymicroservice.userservice.service.impl;

import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.exception.NotFoundException;
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
     * Создает новую банковскую карточку на основе переданного DTO.
     *
     * Если userId предоставлен, добавляет карту в список карт пользователя с указанным ID.
     *
     * @param cardInfoDto DTO с данными карты. Не должен быть {@code null}.
     * @return DTO созданной карты.
     * @throws NotFoundException если передан userId, но пользователь с таким ID не найден
     * @throws jakarta.validation.ConstraintViolationException если данные карты невалидны
     */
    @Override
    @Transactional
    public CardInfoDto createCardInfo(@Valid CardInfoDto cardInfoDto) {
        CardInfo cardInfo = CardInfoMapper.INSTANSE.toEntity(cardInfoDto);
        log.info("createCardInfo(): {}",cardInfo);

        if (cardInfoDto.getUserId() != null) {
            User user = userRepository.findById(cardInfoDto.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + cardInfoDto.getUserId()));
            cardInfo.setUserId(user);
        }
        cardInfo = cardInfoRepository.save(cardInfo);
        return CardInfoMapper.INSTANSE.toDto(cardInfo);
    }

    /**
     * Возвращает банковскую карточку по ее ID.
     *
     * Результат метода кэшируется в кэше "cardInfoCache" с использованием ID карты в качестве ключа.
     * При повторных вызовах с тем же ID карты будет возвращаться значение из кэша без обращения к базе данных,
     * пока кэш не станет недействительным.
     *
     * @param cardId ID карты.
     * @return DTO найденной карты.
     * @throws NotFoundException если карта с указанным ID не найдена.
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(key = "#cardId")
    public CardInfoDto getCardInfoById(Long cardId) {
        Optional<CardInfo> cardInfo = Optional.ofNullable(cardInfoRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("CardInfo wasn't found with id " + cardId)));
        log.info("getCardInfoById(): {}",cardId);
        return  CardInfoMapper.INSTANSE.toDto(cardInfo.get());
    }

    /**
     * Обновляет данные карты (number, holder, expiration_date, userId) и обновляет соответствующую запись в кэше.
     * Если карта с указанным ID не найдена, выбрасывается исключение NotFoundException.
     * После успешного обновления обновлённые данные карты сохраняются в кэш "cardInfoCache" с ключом, равным ID карты.
     *
     * @param cardId ID карты для обновления.
     * @param cardInfoDto DTO с обновлёнными данными карты.
     * @return DTO обновлённой карты.
     * @throws NotFoundException если карта с указанным ID не найдена или если пользователь с указанным ID не существует.
     * @see org.springframework.cache.annotation.CachePut
     */
    @Override
    @Transactional
    @CachePut(key = "#cardId") // update in cache
    public CardInfoDto updateCardInfo(Long cardId, CardInfoDto cardInfoDto) {
        Optional<CardInfo> cardInfoFromDb = Optional.ofNullable(cardInfoRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("CardInfo wasn't found with id " + cardId)));
        CardInfo cardInfo = cardInfoFromDb.get();

        if (cardInfoDto.getUserId() != null) {
            User user = userRepository.findById(cardInfoDto.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found with id: " + cardInfoDto.getUserId()));
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
     * Удаляет карту по её ID и удаляет соответствующую запись из кэша.
     * Если карта с указанным ID не найдена, выбрасывается исключение NotFoundException.
     * После успешного удаления запись с ключом, равным ID карты, удаляется из кэша "cardInfoCache".
     *
     * @param cardId ID карты для удаления.
     * @return DTO удалённой карты.
     * @throws NotFoundException если карта с указанным ID не найдена.
     * @see org.springframework.cache.annotation.CacheEvict
     */
    @Override
    @Transactional
    @CacheEvict(key = "#cardId") // delete from cache
    public CardInfoDto deleteCardInfo(Long cardId) {
        Optional<CardInfo> cardInfo = Optional.ofNullable(cardInfoRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException("CardInfo wasn't found with id " + cardId)));
        cardInfoRepository.deleteById(cardId);
        log.info("deleteCardInfo(): {}",cardInfo);
        return CardInfoMapper.INSTANSE.toDto(cardInfo.get());
    }

    /**
     * Возвращает банковскую карту по ее number.
     *
     * @param number номер карты.
     * @return DTO найденной карты.
     * @throws NotFoundException если карта с указанным number не найдена.
     */
    @Override
    @Transactional(readOnly = true)
    public CardInfoDto getCardInfoByNumber(String number) {
        Optional<CardInfo> cardInfo = Optional.ofNullable(cardInfoRepository.findByNumber(number)
                .orElseThrow(() -> new NotFoundException("CardInfo wasn't found with number " + number)));
        log.info("getCardInfoByNumber(): {}",number);
        return  CardInfoMapper.INSTANSE.toDto(cardInfo.get());
    }

    /**
     *Возвращает список карт, принадлежащих пользователю с заданным userId.
     *
     * @param userId Id пользователя, чьи карты нужно найти
     * @return Список DTO карт с указанного пользователя
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getByUserId(Long userId){
        List <CardInfo> cardInfosList = cardInfoRepository.findByUserId(userId);
        log.info("getByUserId()");
        return cardInfosList.stream().map(CardInfoMapper.INSTANSE::toDto).toList();
    }

    /**
     *Возвращает список карт по заданному набору идентификаторов.
     *
     * @param ids Набор идентификаторов карт для поиска
     * @return Список DTO карт с указанными идентификаторами
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getCardInfoIdIn(Set<Long> ids) {
        List <CardInfo> cardInfosList = cardInfoRepository.findByCardIdIn(ids);
        log.info("getCardInfoIdIn()");
        return cardInfosList.stream().map(CardInfoMapper.INSTANSE::toDto).toList();
    }

    /**
     *Возвращает список карт, срок которых истек.
     *
     * @return Список DTO карт с истекшим сроком
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getExpiredCards() {
        List <CardInfo> cardInfosList = cardInfoRepository.findExpiredCards();
        log.info("getExpiredCards()");
        return cardInfosList.stream().map(CardInfoMapper.INSTANSE::toDto).toList();
    }

    /**
     * Возвращает список всех карт.
     *
     * @return Список DTO всех карт.
     */
    @Override
    @Transactional(readOnly = true)
    public List<CardInfoDto> getAllCardInfos() {
        List <CardInfo> cardInfosList = cardInfoRepository.findAll();
        log.info("getAllCardInfos()");
        return cardInfosList.stream().map(CardInfoMapper.INSTANSE::toDto).toList();
    }

    /**
     * Возвращает страницу с картами, используя нативную пагинацию и сортировку по ID.
     *
     * @param page Номер страницы (начиная с 0).
     * @param size Количество карт на странице.
     * @return Страница с DTO карт.
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
