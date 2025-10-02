package com.mymicroservice.userservice.controller;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.userservice.configuration.SecurityConfig;
import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.mapper.UserMapper;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.service.UserService;
import com.mymicroservice.userservice.util.UserGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InternalController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
@Slf4j
public class InternalControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private final static Long USER_ID = 1L;
    private final static String USER_EMAIL = "test@test.by";
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        User user  = UserGenerator.generateUser();
        user.setUserId(USER_ID);
        userDto = UserMapper.INSTANSE.toDto(user);
        disableAnnotationsInObjectMapper();
    }

    private void disableAnnotationsInObjectMapper() {
        objectMapper.disable(MapperFeature.USE_ANNOTATIONS); // Disable @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    }

    // ------------------- CREATE USER -------------------
    @Test
    public void createUser_ShouldReturnCreatedUserDto_WhenHeaderIsTrue() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/internal/users/")
                        .header("X-Internal-Call", "true")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.email").value(USER_EMAIL));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    public void createUser_ShouldReturnForbidden_WhenHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/internal/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    public void createUser_ShouldReturnForbidden_WhenHeaderIsFalse() throws Exception {
        mockMvc.perform(post("/api/internal/users/")
                        .header("X-Internal-Call", "false")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isForbidden());
    }

    // ------------------- DELETE USER -------------------
    @Test
    public void deleteUser_ShouldReturnDeletedUserDto_WhenHeaderIsTrue() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(userDto);

        mockMvc.perform(delete("/api/internal/users/{id}", USER_ID)
                        .header("X-Internal-Call", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).deleteUser(USER_ID);
    }

    @Test
    public void deleteUser_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(null);

        mockMvc.perform(delete("/api/internal/users/{id}", USER_ID)
                        .header("X-Internal-Call", "true"))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(USER_ID);
    }

    @Test
    public void deleteUser_ShouldReturnForbidden_WhenHeaderMissing() throws Exception {
        mockMvc.perform(delete("/api/internal/users/{id}", USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    public void deleteUser_ShouldReturnForbidden_WhenHeaderIsFalse() throws Exception {
        mockMvc.perform(delete("/api/internal/users/{id}", USER_ID)
                        .header("X-Internal-Call", "false"))
                .andExpect(status().isForbidden());
    }

    // ------------------- GET USER BY ID -------------------
    @Test
    public void getUserById_ShouldReturnUserDto_WhenHeaderIsTrue() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(userDto);

        mockMvc.perform(get("/api/internal/users/{id}", USER_ID)
                        .header("X-Internal-Call", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).getUserById(USER_ID);
    }

    @Test
    public void getUserById_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/internal/users/{id}", USER_ID)
                        .header("X-Internal-Call", "true"))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(USER_ID);
    }

    @Test
    public void getUserById_ShouldReturnForbidden_WhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/internal/users/{id}", USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getUserById_ShouldReturnForbidden_WhenHeaderIsFalse() throws Exception {
        mockMvc.perform(get("/api/internal/users/{id}", USER_ID)
                        .header("X-Internal-Call", "false"))
                .andExpect(status().isForbidden());
    }

    // ------------------- GET USER BY EMAIL -------------------
    @Test
    public void getUserByEmail_ShouldReturnUserDto_WhenHeaderIsTrue() throws Exception {
        when(userService.getUsersByEmail(USER_EMAIL)).thenReturn(userDto);

        mockMvc.perform(get("/api/internal/users/find-by-email")
                        .header("X-Internal-Call", "true")
                        .param("email", USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL));

        verify(userService).getUsersByEmail(USER_EMAIL);
    }

    @Test
    public void getUserByEmail_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.getUsersByEmail(USER_EMAIL)).thenReturn(null);

        mockMvc.perform(get("/api/internal/users/find-by-email")
                        .header("X-Internal-Call", "true")
                        .param("email", USER_EMAIL))
                .andExpect(status().isNotFound());

        verify(userService).getUsersByEmail(USER_EMAIL);
    }

    @Test
    public void getUserByEmail_ShouldReturnForbidden_WhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/internal/users/find-by-email")
                        .param("email", USER_EMAIL))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getUserByEmail_ShouldReturnForbidden_WhenHeaderIsFalse() throws Exception {
        mockMvc.perform(get("/api/internal/users/find-by-email")
                        .header("X-Internal-Call", "false")
                        .param("email", USER_EMAIL))
                .andExpect(status().isForbidden());
    }


}
