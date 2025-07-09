package com.mymicroservice.userservice.controller;

import com.mymicroservice.userservice.annotation.UserExceptionHandler;
import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.service.CardInfoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
@Tag(name="CardInfoController")
@UserExceptionHandler
@Slf4j
@Validated // for @NotEmpty
public class CardInfoController {

    private final CardInfoService cardInfoService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getCardInfoById (@PathVariable("id") Long id) {
        log.info("Request to find the CardInfo by id: {}",id);
        CardInfoDto cardInfoDto = cardInfoService.getCardInfoById(id);
        return ObjectUtils.isEmpty(cardInfoDto) //возвращает true, если объект null или пустой (например, пустая строка, пустая коллекция и т.д.)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(cardInfoDto);
    }

    @GetMapping("/find-by-number")
    //http://localhost:8080/api/cards/find_by_number?number=4111111111119999
    public ResponseEntity<?> getCardInfoByNumber (@RequestParam String number) {  //@RequestParam извлекает значения из строки запроса,строка запроса начинается ?
        log.info("Request to find the CardInfo by number: {}",number);
        CardInfoDto cardInfoDto = cardInfoService.getCardInfoByNumber(number);
        return ObjectUtils.isEmpty(cardInfoDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(cardInfoDto);
    }

    @GetMapping("/find-by-ids") // /find-by-ids?ids=1&ids=2&ids=3
    public ResponseEntity<List<CardInfoDto>> getCardInfoByIds(@RequestParam @NotEmpty Set<Long> ids) {
        log.info("Request to find CardInfos by IDs: {}", ids);
        return ResponseEntity.ok(cardInfoService.getCardInfoIdIn(ids));
    }

    @GetMapping("/user/{user_id}") // user/1
    public ResponseEntity<List<CardInfoDto>> getCardInfoByUserId(@PathVariable("user_id") Long user_id) {
        log.info("Request to find CardInfos by userId: {}", user_id);
        return ResponseEntity.ok(cardInfoService.getByUserId(user_id));
    }

    @GetMapping("/expired")
    public ResponseEntity<List<CardInfoDto>> getExpiredCards() {
        log.info("Request to find expired CardInfos");
        return ResponseEntity.ok(cardInfoService.getExpiredCards());
    }

    @GetMapping("/all")
    public ResponseEntity<List<CardInfoDto>> getAllCardInfos() {
        log.info("Request to find all CardInfos");
        return ResponseEntity.ok(cardInfoService.getAllCardInfos());
    }

    @GetMapping("/paginated")
    //http://localhost:8080/api/cards/paginated?page=0&size=2
    public ResponseEntity<Page<CardInfoDto>> getAllCardInfosWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request to find all CardInfos with pagination");
        return ResponseEntity.ok(cardInfoService.getAllCardInfosNativeWithPagination(page, size));
    }

    @PostMapping("/")
    public ResponseEntity<?> createCardInfo (@RequestBody @Valid CardInfoDto cardInfoDto){
        log.info("Request to add new CardInfo: {}",cardInfoDto);
        CardInfoDto savedCardInfoDto =  cardInfoService.createCardInfo(cardInfoDto);
        return ObjectUtils.isEmpty(savedCardInfoDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(savedCardInfoDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity <?> updateCardInfo (@PathVariable("id") Long id,
                                              @RequestBody @Valid CardInfoDto cardInfoDto){
        log.info("Request to update the CardInfo: {}",cardInfoDto);

        CardInfoDto savedCardInfoDto =  cardInfoService.updateCardInfo(id,cardInfoDto);

        return ObjectUtils.isEmpty(savedCardInfoDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(savedCardInfoDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity <?> deleteCardInfo (@PathVariable("id") Long id){
        log.info("Request to delete the CardInfo by id: {}",id);

        CardInfoDto deletedCardInfoDto = cardInfoService.deleteCardInfo(id);

        return ObjectUtils.isEmpty(deletedCardInfoDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(deletedCardInfoDto);
    }
}
