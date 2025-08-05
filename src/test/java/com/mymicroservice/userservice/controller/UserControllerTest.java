package com.mymicroservice.userservice.controller;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.userservice.configuration.SecurityConfig;
import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.mapper.UserMapper;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.model.User;
import com.mymicroservice.userservice.service.UserService;
import com.mymicroservice.userservice.util.UserGenerator;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
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

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc(addFilters = true)
@Import(SecurityConfig.class)
@Slf4j
public class UserControllerTest {

    @InjectMocks
    private UserController userController;

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
        user.setUserId(1l);
        userDto = UserMapper.INSTANSE.toDto(user);
        disableAnnotationsInObjectMapper();
    }

    private void disableAnnotationsInObjectMapper() {
        objectMapper.disable(MapperFeature.USE_ANNOTATIONS); // Disable @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getUserById_ShouldReturnUserDto() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).getUserById(USER_ID);
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getUserById_ShouldReturnNotFound() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(USER_ID);
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getUserByEmail_ShouldReturnUserDto() throws Exception {
        when(userService.getUsersByEmail(USER_EMAIL)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/find-by-email")
                        .param("email", USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL));

        verify(userService).getUsersByEmail(USER_EMAIL);
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getUserByEmail_ShouldReturnNotFound() throws Exception {
        when(userService.getUsersByEmail(USER_EMAIL)).thenReturn(null);

        mockMvc.perform(get("/api/users/find-by-email")
                        .param("email", USER_EMAIL))
                .andExpect(status().isNotFound());

        verify(userService).getUsersByEmail(USER_EMAIL);
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getUsersByIds_ShouldReturnList() throws Exception {
        List<UserDto> userDtos = List.of(new UserDto(), new UserDto());
        Set<Long> ids = Set.of(1L, 2L, 3L);

        when(userService.getUsersIdIn(ids)).thenReturn(userDtos);

        mockMvc.perform(get("/api/users/find-by-ids")
                        .param("ids", "1", "2", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));

        verify(userService).getUsersIdIn(Set.of(1L, 2L, 3L));
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getUserByRole_ShouldReturnList() throws Exception {
        List<UserDto> userDtos = List.of(userDto);

        when(userService.getUsersByRole(Role.USER)).thenReturn(userDtos);

        mockMvc.perform(get("/api/users/find-by-role")
                        .param("role", Role.USER.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userService).getUsersByRole(Role.USER);
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getUsersBornAfter_ShouldReturnList() throws Exception {
        when(userService.createUser(any(UserDto.class)))  // Mock for createUser()
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<UserDto> users = LongStream.range(1, 4) // stream:  1,2,3
                .mapToObj(i -> userService.createUser(
                        UserDto.builder()
                                .userId(i)
                                .name("User" + i)
                                .surname("Surname" + i)
                                .birthDate(LocalDate.of((int) (2000+i), 1, 1))
                                .email("user" + i + "@example.com")
                                .password("pass" + i)
                                .role(Role.USER)
                                .build()
                )).collect(Collectors.toList());

        LocalDate testDate = LocalDate.of(1990, 1, 1);
        when(userService.getUsersBornAfter(testDate)).thenReturn(users);

        mockMvc.perform(get("/api/users/born-after")
                        .param("date", testDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(3));

        verify(userService).getUsersBornAfter(LocalDate.of(1990, 1, 1));
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getAllUsers_ShouldReturnList() throws Exception {
        List<UserDto> userDtos = List.of(userDto);

        when(userService.getAllUsers()).thenReturn(userDtos);

        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void getAllUsersWithPagination_ShouldReturnPage() throws Exception {
        Page<UserDto> page = new PageImpl<>(List.of(new UserDto(), new UserDto()));

        when(userService.getAllUsersNativeWithPagination(0, 10)).thenReturn(page);

        mockMvc.perform(get("/api/users/paginated")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(2));

        verify(userService).getAllUsersNativeWithPagination(0, 10);
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void createUser_ShouldReturnCreatedUserDto() throws Exception {
        when(userService.createUser(any(UserDto.class))).thenReturn(userDto);

        mockMvc.perform(post("/api/users/")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.email").value(USER_EMAIL));

        verify(userService).createUser(any(UserDto.class));
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void updateUser_ShouldReturnUpdatedUserDto() throws Exception {
        UserDto responseDto = UserMapper.INSTANSE.toDto(UserGenerator.generateUser());
        responseDto.setUserId(1l);
        responseDto.setEmail("updated_email@test.by");

        when(userService.updateUser(USER_ID, userDto)).thenReturn(responseDto);

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.email").value("updated_email@test.by"));

        verify(userService).updateUser(USER_ID, userDto);
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void updateUser_ShouldReturnNotFound() throws Exception {
        userDto.setEmail("updated_email@test.by");

        when(userService.updateUser(USER_ID, userDto))
                .thenThrow(new UserNotFoundException("User wasn't found with id " + USER_ID));

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(USER_ID, userDto);
    }

    @Test
    @WithMockUser(username = "test@test.by", roles = {"USER"})
    public void deleteUser_ShouldReturnForbiddenForNonAdmin() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(userDto);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin@test.by", roles = {"ADMIN"})
    public void deleteUser_ShouldReturnDeletedUserDto() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(userDto);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).deleteUser(USER_ID);
    }

    @Test
    @WithMockUser(username = "admin@test.by", roles = {"ADMIN"})
    public void deleteUser_ShouldReturnNotFound() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(null);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(USER_ID);
    }
}
