package com.mymicroservice.userservice.unit.controller;

import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mymicroservice.userservice.configuration.SecurityConfig;
import com.mymicroservice.userservice.controller.UserController;
import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.service.UserService;
import com.mymicroservice.userservice.util.UserDtoGenerator;
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

import static com.mymicroservice.userservice.util.data.TestConstants.ADMIN_USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.BORN_AFTER_QUERY_DATE;
import static com.mymicroservice.userservice.util.data.TestConstants.DEFAULT_PAGE;
import static com.mymicroservice.userservice.util.data.TestConstants.DEFAULT_PAGE_SIZE;
import static com.mymicroservice.userservice.util.data.TestConstants.PAGINATION_PAGE_SIZE;
import static com.mymicroservice.userservice.util.data.TestConstants.SECOND_ENTITY_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.THIRD_ENTITY_ID;
import static com.mymicroservice.userservice.util.data.TestConstants.UPDATED_USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_EMAIL;
import static com.mymicroservice.userservice.util.data.TestConstants.USER_ID;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(SecurityConfig.class)
class UserControllerTest {

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
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getUserById_ShouldReturnUserDto_WhenUserExists() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).getUserById(USER_ID);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getUserById_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.getUserById(USER_ID)).thenReturn(null);

        mockMvc.perform(get("/api/users/{id}", USER_ID))
                .andExpect(status().isNotFound());

        verify(userService).getUserById(USER_ID);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getUserByEmail_ShouldReturnUserDto_WhenUserExists() throws Exception {
        when(userService.getUsersByEmail(USER_EMAIL)).thenReturn(userDto);

        mockMvc.perform(get("/api/users/find-by-email")
                        .param("email", USER_EMAIL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(USER_EMAIL));

        verify(userService).getUsersByEmail(USER_EMAIL);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getUserByEmail_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.getUsersByEmail(USER_EMAIL)).thenReturn(null);

        mockMvc.perform(get("/api/users/find-by-email")
                        .param("email", USER_EMAIL))
                .andExpect(status().isNotFound());

        verify(userService).getUsersByEmail(USER_EMAIL);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getUsersByIds_ShouldReturnList_WhenUsersExist() throws Exception {
        List<UserDto> userDtos = List.of(
                UserDtoGenerator.generateUserDtoForBatch(1),
                UserDtoGenerator.generateUserDtoForBatch(2)
        );
        Set<Long> ids = Set.of(USER_ID, SECOND_ENTITY_ID, THIRD_ENTITY_ID);

        when(userService.getUsersIdIn(ids)).thenReturn(userDtos);

        mockMvc.perform(get("/api/users/find-by-ids")
                        .param("ids", USER_ID.toString(), SECOND_ENTITY_ID.toString(), THIRD_ENTITY_ID.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(PAGINATION_PAGE_SIZE));

        verify(userService).getUsersIdIn(Set.of(USER_ID, SECOND_ENTITY_ID, THIRD_ENTITY_ID));
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getUserByRole_ShouldReturnList_WhenUsersExist() throws Exception {
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
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getUsersBornAfter_ShouldReturnList_WhenUsersExist() throws Exception {
        List<UserDto> users = LongStream.rangeClosed(USER_ID, THIRD_ENTITY_ID)
                .mapToObj(UserDtoGenerator::generateUserDtoForBatch)
                .collect(Collectors.toList());

        when(userService.getUsersBornAfter(BORN_AFTER_QUERY_DATE)).thenReturn(users);

        mockMvc.perform(get("/api/users/born-after")
                        .param("date", BORN_AFTER_QUERY_DATE.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(THIRD_ENTITY_ID.intValue()));

        verify(userService).getUsersBornAfter(BORN_AFTER_QUERY_DATE);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getAllUsers_ShouldReturnList_WhenUsersExist() throws Exception {
        List<UserDto> userDtos = List.of(userDto);

        when(userService.getAllUsers()).thenReturn(userDtos);

        mockMvc.perform(get("/api/users/all"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1));

        verify(userService).getAllUsers();
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void getAllUsersWithPagination_ShouldReturnPage_WhenUsersExist() throws Exception {
        Page<UserDto> page = new PageImpl<>(List.of(
                UserDtoGenerator.generateUserDtoForBatch(1),
                UserDtoGenerator.generateUserDtoForBatch(2)
        ));

        when(userService.getAllUsersNativeWithPagination(DEFAULT_PAGE, DEFAULT_PAGE_SIZE)).thenReturn(page);

        mockMvc.perform(get("/api/users/paginated")
                        .param("page", String.valueOf(DEFAULT_PAGE))
                        .param("size", String.valueOf(DEFAULT_PAGE_SIZE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(PAGINATION_PAGE_SIZE));

        verify(userService).getAllUsersNativeWithPagination(DEFAULT_PAGE, DEFAULT_PAGE_SIZE);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void updateUser_ShouldReturnUpdatedUserDto_WhenUserExists() throws Exception {
        UserDto responseDto = UserDtoGenerator.generateUserDtoWithId();
        responseDto.setEmail(UPDATED_USER_EMAIL);

        when(userService.updateUser(USER_ID, userDto)).thenReturn(responseDto);

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.email").value(UPDATED_USER_EMAIL));

        verify(userService).updateUser(USER_ID, userDto);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void updateUser_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        userDto.setEmail(UPDATED_USER_EMAIL);

        when(userService.updateUser(USER_ID, userDto))
                .thenThrow(new UserNotFoundException("User wasn't found with id " + USER_ID));

        mockMvc.perform(put("/api/users/{id}", USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userDto)))
                .andExpect(status().isNotFound());

        verify(userService).updateUser(USER_ID, userDto);
    }

    @Test
    @WithMockUser(username = USER_EMAIL, roles = {"USER"})
    void deleteUser_ShouldReturnForbidden_WhenNonAdmin() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(userDto);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = ADMIN_USER_EMAIL, authorities = {"ROLE_ADMIN"})
    void deleteUser_ShouldReturnDeletedUserDto_WhenUserExists() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(userDto);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(USER_ID));

        verify(userService).deleteUser(USER_ID);
    }

    @Test
    @WithMockUser(username = ADMIN_USER_EMAIL, authorities = {"ROLE_ADMIN"})
    void deleteUser_ShouldReturnNotFound_WhenUserNotFound() throws Exception {
        when(userService.deleteUser(USER_ID)).thenReturn(null);

        mockMvc.perform(delete("/api/users/{id}", USER_ID))
                .andExpect(status().isNotFound());

        verify(userService).deleteUser(USER_ID);
    }
}
