package com.mymicroservice.userservice.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.userservice.configuration.SecurityConfig;
import com.mymicroservice.userservice.controller.CardInfoController;
import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.service.CardInfoService;
import com.mymicroservice.userservice.util.CardInfoDtoGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static com.mymicroservice.userservice.util.data.TestConstants.CARD_NUMBER;
import static com.mymicroservice.userservice.util.data.TestConstants.DEFAULT_PAGE;
import static com.mymicroservice.userservice.util.data.TestConstants.DEFAULT_PAGE_SIZE;
import static com.mymicroservice.userservice.util.data.TestConstants.ENTITY_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.PAGINATION_PAGE_SIZE;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_CARD_NUMBER;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_ENTITY_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.THIRD_ENTITY_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CardInfoController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
@WithMockUser(roles = {"ADMIN", "USER"})
class CardInfoControllerTest {

    @MockBean
    private CardInfoService cardInfoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private CardInfoDto cardInfoDto;

    @BeforeEach
    void setUp() {
        cardInfoDto = CardInfoDtoGenerator.generateCardInfoDtoWithId();
    }

    @Test
    void getCardInfoById_ShouldReturnCardInfoDto_WhenCardInfoExists() throws Exception {
        when(cardInfoService.getCardInfoById(ENTITY_ID)).thenReturn(cardInfoDto);

        mockMvc.perform(get("/api/cards/{id}", ENTITY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ENTITY_ID));

        verify(cardInfoService).getCardInfoById(ENTITY_ID);
    }

    @Test
    void getCardInfoById_ShouldReturnNotFound_WhenCardInfoNotFound() throws Exception {
        when(cardInfoService.getCardInfoById(ENTITY_ID)).thenReturn(null);

        mockMvc.perform(get("/api/cards/{id}", ENTITY_ID))
                .andExpect(status().isNotFound());

        verify(cardInfoService).getCardInfoById(ENTITY_ID);
    }

    @Test
    void getCardInfoByNumber_ShouldReturnCardInfoDto_WhenCardInfoExists() throws Exception {
        when(cardInfoService.getCardInfoByNumber(CARD_NUMBER)).thenReturn(cardInfoDto);

        mockMvc.perform(get("/api/cards/find-by-number")
                        .param("number", CARD_NUMBER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(CARD_NUMBER));

        verify(cardInfoService).getCardInfoByNumber(CARD_NUMBER);
    }

    @Test
    void getCardInfoByNumber_ShouldReturnNotFound_WhenCardInfoNotFound() throws Exception {
        when(cardInfoService.getCardInfoByNumber(CARD_NUMBER)).thenReturn(null);

        mockMvc.perform(get("/api/cards/find-by-number")
                        .param("number", CARD_NUMBER))
                .andExpect(status().isNotFound());

        verify(cardInfoService).getCardInfoByNumber(CARD_NUMBER);
    }

    @Test
    void getCardInfoByIds_ShouldReturnList_WhenCardInfosExist() throws Exception {
        List<CardInfoDto> cardInfoDtos = List.of(
                CardInfoDtoGenerator.generateCardInfoDtoForBatch(1, USER_ID),
                CardInfoDtoGenerator.generateCardInfoDtoForBatch(2, USER_ID)
        );
        Set<Long> ids = Set.of(ENTITY_ID, SECOND_ENTITY_ID, THIRD_ENTITY_ID);

        when(cardInfoService.getCardInfoIdIn(ids)).thenReturn(cardInfoDtos);

        mockMvc.perform(get("/api/cards/find-by-ids")
                        .param("ids", ENTITY_ID.toString(), SECOND_ENTITY_ID.toString(), THIRD_ENTITY_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(PAGINATION_PAGE_SIZE));

        verify(cardInfoService).getCardInfoIdIn(Set.of(ENTITY_ID, SECOND_ENTITY_ID, THIRD_ENTITY_ID));
    }

    @Test
    void getCardInfoByUserId_ShouldReturnList_WhenCardInfosExist() throws Exception {
        List<CardInfoDto> cardInfoDtos = List.of(cardInfoDto);

        when(cardInfoService.getByUserId(USER_ID)).thenReturn(cardInfoDtos);

        mockMvc.perform(get("/api/cards/user/{user_id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(cardInfoService).getByUserId(USER_ID);
    }

    @Test
    void getExpiredCards_ShouldReturnList_WhenExpiredCardsExist() throws Exception {
        List<CardInfoDto> cardInfos = LongStream.rangeClosed(ENTITY_ID, THIRD_ENTITY_ID)
                .mapToObj(i -> CardInfoDtoGenerator.generateCardInfoDtoForBatch(i, USER_ID))
                .collect(Collectors.toList());

        when(cardInfoService.getExpiredCards()).thenReturn(cardInfos);

        mockMvc.perform(get("/api/cards/expired", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(THIRD_ENTITY_ID.intValue()));

        verify(cardInfoService).getExpiredCards();
    }

    @Test
    void getAllCardInfos_ShouldReturnList_WhenCardInfosExist() throws Exception {
        List<CardInfoDto> cardInfoDtos = List.of(cardInfoDto);

        when(cardInfoService.getAllCardInfos()).thenReturn(cardInfoDtos);

        mockMvc.perform(get("/api/cards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(cardInfoService).getAllCardInfos();
    }

    @Test
    void getAllCardInfosWithPagination_ShouldReturnPage_WhenCardInfosExist() throws Exception {
        Page<CardInfoDto> page = new PageImpl<>(List.of(
                CardInfoDtoGenerator.generateCardInfoDtoForBatch(1, USER_ID),
                CardInfoDtoGenerator.generateCardInfoDtoForBatch(2, USER_ID)
        ));

        when(cardInfoService.getAllCardInfosNativeWithPagination(DEFAULT_PAGE, DEFAULT_PAGE_SIZE)).thenReturn(page);

        mockMvc.perform(get("/api/cards/paginated")
                        .param("page", String.valueOf(DEFAULT_PAGE))
                        .param("size", String.valueOf(DEFAULT_PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(PAGINATION_PAGE_SIZE));

        verify(cardInfoService).getAllCardInfosNativeWithPagination(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    @Test
    void createCardInfo_ShouldReturnCreatedCardDto_WhenDtoIsValid() throws Exception {
        when(cardInfoService.createCardInfo(any(CardInfoDto.class))).thenReturn(cardInfoDto);

        mockMvc.perform(post("/api/cards/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ENTITY_ID))
                .andExpect(jsonPath("$.number").value(CARD_NUMBER));

        verify(cardInfoService).createCardInfo(any(CardInfoDto.class));
    }

    @Test
    void updateCardInfo_ShouldReturnUpdatedCardDto_WhenCardInfoExists() throws Exception {
        CardInfoDto responseDto = CardInfoDtoGenerator.generateCardInfoDtoWithId();
        responseDto.setNumber(SECOND_CARD_NUMBER);

        when(cardInfoService.updateCardInfo(ENTITY_ID, cardInfoDto)).thenReturn(responseDto);

        mockMvc.perform(put("/api/cards/{id}", ENTITY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ENTITY_ID))
                .andExpect(jsonPath("$.number").value(SECOND_CARD_NUMBER));

        verify(cardInfoService).updateCardInfo(ENTITY_ID, cardInfoDto);
    }

    @Test
    void updateCardInfo_ShouldReturnNotFound_WhenCardInfoNotFound() throws Exception {
        when(cardInfoService.updateCardInfo(ENTITY_ID, cardInfoDto)).thenReturn(null);

        mockMvc.perform(put("/api/cards/{id}", ENTITY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoDto)))
                .andExpect(status().isNotFound());

        verify(cardInfoService).updateCardInfo(ENTITY_ID, cardInfoDto);
    }

    @Test
    void deleteCardInfo_ShouldReturnDeletedCardDto_WhenCardInfoExists() throws Exception {
        when(cardInfoService.deleteCardInfo(ENTITY_ID)).thenReturn(cardInfoDto);

        mockMvc.perform(delete("/api/cards/{id}", ENTITY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ENTITY_ID));

        verify(cardInfoService).deleteCardInfo(ENTITY_ID);
    }

    @Test
    void deleteCardInfo_ShouldReturnNotFound_WhenCardInfoNotFound() throws Exception {
        when(cardInfoService.deleteCardInfo(ENTITY_ID)).thenReturn(null);

        mockMvc.perform(delete("/api/cards/{id}", ENTITY_ID))
                .andExpect(status().isNotFound());

        verify(cardInfoService).deleteCardInfo(ENTITY_ID);
    }
}
