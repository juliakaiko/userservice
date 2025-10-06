package com.mymicroservice.userservice.controller;

import com.mymicroservice.userservice.annotation.GlobalExceptionHandler;
import com.mymicroservice.userservice.dto.UserDto;
import com.mymicroservice.userservice.service.UserService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/internal/users")
@Tag(name="InternalController")
@GlobalExceptionHandler
@Slf4j
public class InternalController {

    private final UserService userService;

    @PostMapping ("/")
    public ResponseEntity<?> createUser (@RequestBody @Valid UserDto userDto,
                                         @RequestHeader(value = "X-Internal-Call", required = false) String internalCall){
        log.info("Request to add a new User: {}", userDto);

        if (!"true".equals(internalCall)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDto savedUserDto =  userService.createUser(userDto);
        return ObjectUtils.isEmpty(savedUserDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(savedUserDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser (@PathVariable("id") Long id,
                                         @RequestHeader(value = "X-Internal-Call", required = false) String internalCall){

        log.info("Request to delete the User by id: {}", id);

        if (!"true".equals(internalCall)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDto deletedUserDto = userService.deleteUser(id);

        return ObjectUtils.isEmpty(deletedUserDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(deletedUserDto);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById (@PathVariable("id") Long id,
                                          @RequestHeader(value = "X-Internal-Call", required = false) String internalCall) {
        log.info("Request to find the User by id: {}", id);

        if (!"true".equals(internalCall)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDto userDto = userService.getUserById(id);
        return ObjectUtils.isEmpty(userDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(userDto);
    }

    @GetMapping("/find-by-email")
    public ResponseEntity<?> getUserByEmail (@RequestParam("email")  String email,
                                             @RequestHeader(value = "X-Internal-Call", required = false) String internalCall) {
        log.info("Request to find the User by email: {}", email);

        if (!"true".equals(internalCall)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        UserDto userDto = userService.getUsersByEmail(email);
        return ObjectUtils.isEmpty(userDto)
                ? ResponseEntity.notFound().build()
                : ResponseEntity.ok(userDto);
    }
}
