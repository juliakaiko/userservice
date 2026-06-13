package com.mymicroservice.userservice.unit.controller;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.userservice.configuration.SecurityConfig;
import com.mymicroservice.userservice.controller.InternalController;
import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.service.UserService;
import com.mymicroservice.userservice.util.UserDtoGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.mymicroservice.userservice.util.data.TestConstants.INTERNAL_CALL_FALSE;
import static com.mymicroservice.userservice.util.data.TestConstants.INTERNAL_CALL_HEADER;
import static com.mymicroservice.userservice.util.data.TestConstants.INTERNAL_CALL_TRUE;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_ID;
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
class InternalControllerTest {

    @MockBean
    private UserService userService;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userDto = UserDtoGenerator.generateUserDtoWithId();
        disableAnnotationsInObjectMapper();
    }

    private void disableAnnotationsInObjectMapper() {
        objectMapper.disable(MapperFeature.USE_ANNOTATIONS);
    }

    @Test
    void createUser_ShouldReturnCreatedUserDto_WhenHeaderIsTrue() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/internal/users/")
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_TRUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.email").value(USER_EMAIL));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    void createUser_ShouldReturnNotFound_WhenCreatedUserIsNull() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(null);

        mockMvc.perform(post("/api/internal/users/")
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_TRUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    void createUser_ShouldReturnForbidden_WhenHeaderMissing() throws Exception {
        mockMvc.perform(post("/api/internal/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void createUser_ShouldReturnForbidden_WhenHeaderIsFalse() throws Exception {
        mockMvc.perform(post("/api/internal/users/")
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_FALSE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_ShouldReturnDeletedUserDto_WhenHeaderIsTrue() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(userDto);

        mockMvc.perform(delete("/api/internal/users/{id}", USER_ID)
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_TRUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).deleteUser(USER_ID);
    }

    @Test
    void deleteUser_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(null);

        mockMvc.perform(delete("/api/internal/users/{id}", USER_ID)
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_TRUE))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(USER_ID);
    }

    @Test
    void deleteUser_ShouldReturnForbidden_WhenHeaderMissing() throws Exception {
        mockMvc.perform(delete("/api/internal/users/{id}", USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteUser_ShouldReturnForbidden_WhenHeaderIsFalse() throws Exception {
        mockMvc.perform(delete("/api/internal/users/{id}", USER_ID)
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_FALSE))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldReturnUserDto_WhenHeaderIsTrue() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(userDto);

        mockMvc.perform(get("/api/internal/users/{id}", USER_ID)
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_TRUE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).getUserById(USER_ID);
    }

    @Test
    void getUserById_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/internal/users/{id}", USER_ID)
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_TRUE))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(USER_ID);
    }

    @Test
    void getUserById_ShouldReturnForbidden_WhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/internal/users/{id}", USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserById_ShouldReturnForbidden_WhenHeaderIsFalse() throws Exception {
        mockMvc.perform(get("/api/internal/users/{id}", USER_ID)
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_FALSE))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserByEmail_ShouldReturnUserDto_WhenHeaderIsTrue() throws Exception {
        when(userService.getUsersByEmail(USER_EMAIL)).thenReturn(userDto);

        mockMvc.perform(get("/api/internal/users/find-by-email")
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_TRUE)
                        .param("email", USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL));

        verify(userService).getUsersByEmail(USER_EMAIL);
    }

    @Test
    void getUserByEmail_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.getUsersByEmail(USER_EMAIL)).thenReturn(null);

        mockMvc.perform(get("/api/internal/users/find-by-email")
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_TRUE)
                        .param("email", USER_EMAIL))
                .andExpect(status().isNotFound());

        verify(userService).getUsersByEmail(USER_EMAIL);
    }

    @Test
    void getUserByEmail_ShouldReturnForbidden_WhenHeaderMissing() throws Exception {
        mockMvc.perform(get("/api/internal/users/find-by-email")
                        .param("email", USER_EMAIL))
                .andExpect(status().isForbidden());
    }

    @Test
    void getUserByEmail_ShouldReturnForbidden_WhenHeaderIsFalse() throws Exception {
        mockMvc.perform(get("/api/internal/users/find-by-email")
                        .header(INTERNAL_CALL_HEADER, INTERNAL_CALL_FALSE)
                        .param("email", USER_EMAIL))
                .andExpect(status().isForbidden());
    }
}
