package com.mymicroservice.userservice.unit.advice;

import com.mymicroservice.userservice.advice.GlobalAdvice;
import com.mymicroservice.userservice.exception.CardInfoNotFoundException;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.util.ErrorItem;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Set;

import static com.mymicroservice.userservice.util.data.TestConstants.NON_EXISTENT_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class GlobalAdviceTest {

    private static final String TEST_URI = "/api/users/1";

    private final GlobalAdvice globalAdvice = new GlobalAdvice();

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", TEST_URI);
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @AfterEach
    void tearDown() {
        RequestContextHolder.resetRequestAttributes();
    }

    @Test
    void handleMethodArgumentNotValidException_ShouldReturnBadRequest_WhenValidationFails() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "userDto");
        bindingResult.addError(new FieldError("userDto", "name", "Name cannot be blank"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ResponseEntity<ErrorItem> response = globalAdvice.handleMethodArgumentNotValidException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Validation failed", response.getBody().getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleValidationException_ShouldReturnBadRequest_WhenConstraintViolationOccurs() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("must not be empty");
        ConstraintViolationException exception = new ConstraintViolationException(Set.of(violation));

        ResponseEntity<ErrorItem> response = globalAdvice.handleValidationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getMessage().contains("must not be empty"));
    }

    @Test
    void handleDataIntegrityViolationException_ShouldReturnBadRequest_WhenDuplicateKeyOccurs() {
        DataIntegrityViolationException exception = new DataIntegrityViolationException("duplicate email");

        ResponseEntity<ErrorItem> response = globalAdvice.handleDataIntegrityViolationException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("duplicate email", response.getBody().getMessage());
    }

    @Test
    void handleAuthorizationDeniedException_ShouldReturnForbidden_WhenAccessDenied() {
        AuthorizationDeniedException exception = new AuthorizationDeniedException(
                "Access Denied", mock(AuthorizationResult.class));

        ResponseEntity<ErrorItem> response = globalAdvice.handleAuthorizationDeniedException(exception);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.FORBIDDEN.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleHttpMessageNotReadableException_ShouldReturnBadRequest_WhenJsonIsMalformed() {
        HttpMessageNotReadableException exception = new HttpMessageNotReadableException("Malformed JSON", null, null);

        ResponseEntity<ErrorItem> response = globalAdvice.handleHttpMessageNotReadableException(exception);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Malformed JSON", response.getBody().getMessage());
    }

    @Test
    void handleJwtException_ShouldReturnUnauthorized_WhenTokenIsInvalid() {
        JwtException exception = new JwtException("Invalid JWT");

        ResponseEntity<ErrorItem> response = globalAdvice.handleJwtException(exception);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getBody().getStatusCode());
    }

    @Test
    void handleUserNotFoundException_ShouldReturnNotFound_WhenUserNotFound() {
        UserNotFoundException exception = new UserNotFoundException("User wasn't found with id " + NON_EXISTENT_ID);

        ResponseEntity<ErrorItem> response = globalAdvice.handleUserNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatusCode());
        assertEquals("User wasn't found with id " + NON_EXISTENT_ID, response.getBody().getMessage());
    }

    @Test
    void handleCardInfoNotFoundException_ShouldReturnNotFound_WhenCardInfoNotFound() {
        CardInfoNotFoundException exception = new CardInfoNotFoundException("CardInfo wasn't found with id " + NON_EXISTENT_ID);

        ResponseEntity<ErrorItem> response = globalAdvice.handleCardInfoNotFoundException(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(HttpStatus.NOT_FOUND.value(), response.getBody().getStatusCode());
        assertEquals("CardInfo wasn't found with id " + NON_EXISTENT_ID, response.getBody().getMessage());
    }
}
