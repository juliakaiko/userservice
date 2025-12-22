package com.mymicroservice.userservice.controller;

import com.mymicroservice.userservice.annotation.GlobalExceptionHandler;
import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.model.Role;
import com.mymicroservice.userservice.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name="UserController")
@GlobalExceptionHandler
@Slf4j
@Validated // for @NotEmpty
public class UserController {

    private final UserService userService;

    public UserDto getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentPrincipalName = authentication.getName();
        UserDto userDto = userService.getUsersByEmail(currentPrincipalName);
        return userDto;
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, String>> sayHello() {
        log.info("Request to welcome the User ");
        UserDto userDto = getAuthenticatedUser();
        String greeting = "Welcome, " + userDto.getName() + " " + userDto.getSurname();
        return ResponseEntity.ok(Map.of("message", greeting));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById (@PathVariable("id") Long id) {
        log.info("Request to find the User by id: {}", id);
        UserDto userDto = userService.getUserById(id);
        return ObjectUtils.isEmpty(userDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(userDto);
    }

    @GetMapping("/find-by-email") //http://localhost:8080/api/users/find-by-email?email=user1%40yandex.ru
    public ResponseEntity<?> getUserByEmail (@RequestParam("email") String email) {
        log.info("Request to find the User by email: {}", email);
        UserDto userDto = userService.getUsersByEmail(email);
        return ObjectUtils.isEmpty(userDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(userDto);
    }

    @GetMapping("/find-by-ids") // /find-by-ids?ids=1&ids=2&ids=3
    public ResponseEntity<List<UserDto>> getUsersByIds(@RequestParam @NotEmpty Set<Long> ids) {
        log.info("Request to find Users by IDs: {}", ids);
        List<UserDto> userDtos = userService.getUsersIdIn(ids);
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/find-by-role") // /find-by-role?role=USER
    public ResponseEntity<List<UserDto>> getUsersByRole(@RequestParam Role role) {
        log.info("Request to find Users by role: {}", role);
        List<UserDto> userDtos = userService.getUsersByRole(role);
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/born-after") // /born-after?date=1990-01-01
    public ResponseEntity<List<UserDto>> getUsersBornAfter(@RequestParam LocalDate date) {
        log.info("Request to find Users born after: {}", date);
        List<UserDto> userDtos = userService.getUsersBornAfter(date);
        log.debug("Found {} users born after {}", userDtos.size(), date);
        return ResponseEntity.ok(userDtos);
    }

    @GetMapping("/all")
    public ResponseEntity<List<UserDto>> getAllUsers() {
        log.info("Request to find all Users");
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/paginated") //http://localhost:8080/api/users/paginated?page=0&size=2
    public ResponseEntity<Page<UserDto>> getAllUsersWithPagination(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Request to find all Users with pagination");
        return ResponseEntity.ok(userService.getAllUsersNativeWithPagination(page, size));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser (@PathVariable("id") Long id,
                                         @RequestBody @Valid UserDto userDto){
        log.info("Request to update the User: {}", userDto);

        UserDto updatedUserDto =  userService.updateUser(id, userDto);

        return ObjectUtils.isEmpty(updatedUserDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(updatedUserDto);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser (@PathVariable("id") Long id){
        log.info("Request to delete the User by id: {}", id);

        UserDto deletedUserDto = userService.deleteUser(id);

        return ObjectUtils.isEmpty(deletedUserDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(deletedUserDto);
    }
}
