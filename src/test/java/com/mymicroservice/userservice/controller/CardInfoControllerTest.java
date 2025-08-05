package com.mymicroservice.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.userservice.configuration.SecurityConfig;
import com.mymicroservice.userservice.dto.CardInfoDto;
import com.mymicroservice.userservice.mapper.CardInfoMapper;
import com.mymicroservice.userservice.model.CardInfo;
import com.mymicroservice.userservice.service.CardInfoService;
import com.mymicroservice.userservice.util.CardInfoGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.mockito.Mockito.any;

@WebMvcTest(controllers = CardInfoController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
@WithMockUser(roles = {"ADMIN", "USER"})
@Slf4j
public class CardInfoControllerTest {

    @InjectMocks
    private CardInfoController cardInfoController;

    @MockBean
    private CardInfoService cardInfoService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final static Long ENTITY_ID = 1L;
    private final static String CARD_NUMBER = "1111222233334444";
    private final static Long USER_ID = 1L;
    private CardInfoDto cardInfoDto;

    @BeforeEach
    void setUp() {
        CardInfo card = CardInfoGenerator.generateCardInfo();
        card.setCardId(1l);
        cardInfoDto = CardInfoMapper.INSTANSE.toDto(card);
    }

    @Test
    public void getCardInfoById_ShouldReturnCardInfoDto() throws Exception {
        when(cardInfoService.getCardInfoById(ENTITY_ID)).thenReturn(cardInfoDto);

        mockMvc.perform(get("/api/cards/{id}", ENTITY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ENTITY_ID));

        verify(cardInfoService).getCardInfoById(ENTITY_ID);
    }

    @Test
    public void getCardInfoById_ShouldReturnNotFound() throws Exception {
        when(cardInfoService.getCardInfoById(ENTITY_ID)).thenReturn(null);

        mockMvc.perform(get("/api/cards/{id}", ENTITY_ID))
                .andExpect(status().isNotFound());

        verify(cardInfoService).getCardInfoById(ENTITY_ID);
    }

    @Test
    public void getCardInfoByNumber_ShouldReturnCardInfoDto() throws Exception {
        when(cardInfoService.getCardInfoByNumber(CARD_NUMBER)).thenReturn(cardInfoDto);

        mockMvc.perform(get("/api/cards/find-by-number")
                        .param("number", CARD_NUMBER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.number").value(CARD_NUMBER));

        verify(cardInfoService).getCardInfoByNumber(CARD_NUMBER);
    }

    @Test
    public void getCardInfoByNumber_ShouldReturnNotFound() throws Exception {
        when(cardInfoService.getCardInfoByNumber(CARD_NUMBER)).thenReturn(null);

        mockMvc.perform(get("/api/cards/find-by-number")
                        .param("number", CARD_NUMBER))
                .andExpect(status().isNotFound());

        verify(cardInfoService).getCardInfoByNumber(CARD_NUMBER);
    }

    @Test
    public void getCardInfoByIds_ShouldReturnList() throws Exception {
        List<CardInfoDto> cardInfoDtos = List.of(new CardInfoDto(), new CardInfoDto());
        Set<Long> ids = Set.of(1L, 2L, 3L);

        when(cardInfoService.getCardInfoIdIn(ids)).thenReturn(cardInfoDtos);

        mockMvc.perform(get("/api/cards/find-by-ids")
                        .param("ids", "1", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(cardInfoService).getCardInfoIdIn(Set.of(1L, 2L, 3L));
    }

    @Test
    public void getCardInfoByUserId_ShouldReturnList() throws Exception {
        List<CardInfoDto> cardInfoDtos = List.of(cardInfoDto);

        when(cardInfoService.getByUserId(USER_ID)).thenReturn(cardInfoDtos);

        mockMvc.perform(get("/api/cards/user/{user_id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(cardInfoService).getByUserId(USER_ID);
    }

    @Test
    public void getExpiredCards_ShouldReturnList() throws Exception {
        when(cardInfoService.createCardInfo(any(CardInfoDto.class)))  // Mock for createCardInfo()
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<CardInfoDto> cardInfos = LongStream.range(1, 4) // stream:  1,2,3
                .mapToObj(i -> cardInfoService.createCardInfo(
                        CardInfoDto.builder()
                                .cardId(i)
                                .number("111122223333444"+i)
                                .holder("TestUser")
                                .expirationDate(LocalDate.of(2020, 3, 3))
                                .userId(USER_ID)
                                .build()
                )).collect(Collectors.toList());

        when(cardInfoService.getExpiredCards()).thenReturn(cardInfos);

        mockMvc.perform(get("/api/cards/expired", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(cardInfoService).getExpiredCards();
    }

    @Test
    public void getAllCardInfos_ShouldReturnList() throws Exception {
        List<CardInfoDto> cardInfoDtos = List.of(cardInfoDto);

        when(cardInfoService.getAllCardInfos()).thenReturn(cardInfoDtos);

        mockMvc.perform(get("/api/cards/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(cardInfoService).getAllCardInfos();
    }

    @Test
    public void getAllCardInfosWithPagination_ShouldReturnPage() throws Exception {
        Page<CardInfoDto> page = new PageImpl<>(List.of(new CardInfoDto(), new CardInfoDto()));

        when(cardInfoService.getAllCardInfosNativeWithPagination(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/cards/paginated")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(cardInfoService).getAllCardInfosNativeWithPagination(0, 10);
    }

    @Test
    public void createCardInfo_ShouldReturnCreatedCardDto() throws Exception {
        when(cardInfoService.createCardInfo(any(CardInfoDto.class))).thenReturn(cardInfoDto);

        mockMvc.perform(post("/api/cards/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ENTITY_ID))
                .andExpect(jsonPath("$.number").value(CARD_NUMBER));

        verify(cardInfoService).createCardInfo(any(CardInfoDto.class));
    }

    @Test
    public void updateCardInfo_ShouldReturnUpdatedCardDto() throws Exception {
        CardInfoDto responseDto = CardInfoMapper.INSTANSE.toDto(CardInfoGenerator.generateCardInfo());
        responseDto.setCardId(1l);
        responseDto.setNumber("1111555577779999");

        when(cardInfoService.updateCardInfo(ENTITY_ID, cardInfoDto)).thenReturn(responseDto);

        mockMvc.perform(put("/api/cards/{id}", ENTITY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ENTITY_ID))
                .andExpect(jsonPath("$.number").value("1111555577779999"));

        verify(cardInfoService).updateCardInfo(ENTITY_ID, cardInfoDto);
    }

    @Test
    public void updateCardInfo_ShouldReturnNotFound() throws Exception {
        CardInfoDto responseDto = CardInfoMapper.INSTANSE.toDto(CardInfoGenerator.generateCardInfo());
        responseDto.setNumber("1111555577779999");

        when(cardInfoService.updateCardInfo(ENTITY_ID, cardInfoDto)).thenReturn(null);

        mockMvc.perform(put("/api/cards/{id}", ENTITY_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cardInfoDto)))
                .andExpect(status().isNotFound());

        verify(cardInfoService).updateCardInfo(ENTITY_ID, cardInfoDto);
    }

    @Test
    public void deleteCardInfo_ShouldReturnDeletedCardDto() throws Exception {
        when(cardInfoService.deleteCardInfo(ENTITY_ID)).thenReturn(cardInfoDto);

        mockMvc.perform(delete("/api/cards/{id}", ENTITY_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(ENTITY_ID));

        verify(cardInfoService).deleteCardInfo(ENTITY_ID);
    }

    @Test
    public void deleteCardInfo_ShouldReturnNotFound() throws Exception {
        when(cardInfoService.deleteCardInfo(ENTITY_ID)).thenReturn(null);

        mockMvc.perform(delete("/api/cards/{id}", ENTITY_ID))
                .andExpect(status().isNotFound());

        verify(cardInfoService).deleteCardInfo(ENTITY_ID);
    }
}
