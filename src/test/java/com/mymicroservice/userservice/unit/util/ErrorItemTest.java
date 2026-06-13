package com.mymicroservice.userservice.unit.util;

import com.mymicroservice.userservice.util.ErrorItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ErrorItemTest {

    private static final String TEST_URI = "/api/users/find-by-email";

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
    void fromMethodArgumentNotValid_ShouldReturnErrorItemWithFieldErrors_WhenValidationFails() {
        BeanPropertyBindingResult bindingResult = new BeanPropertyBindingResult(new Object(), "userDto");
        bindingResult.addError(new FieldError("userDto", "email", "Please provide a valid email address"));
        bindingResult.addError(new FieldError("userDto", "email", "Email address may not be blank"));
        MethodArgumentNotValidException exception = new MethodArgumentNotValidException(null, bindingResult);

        ErrorItem error = ErrorItem.fromMethodArgumentNotValid(exception, HttpStatus.BAD_REQUEST);

        assertEquals("Validation failed", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode());
        assertTrue(error.getUrl().endsWith(TEST_URI));
        assertNotNull(error.getTimestamp());
        assertTrue(error.getFieldErrors().containsKey("email"));
        assertTrue(error.getFieldErrors().get("email").contains("Please provide a valid email address"));
    }

    @Test
    void generateMessage_ShouldReturnErrorItemWithMessage_WhenExceptionProvided() {
        Exception exception = new RuntimeException("Something went wrong");

        ErrorItem error = ErrorItem.generateMessage(exception, HttpStatus.BAD_REQUEST);

        assertEquals("Something went wrong", error.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST.value(), error.getStatusCode());
        assertTrue(error.getUrl().endsWith(TEST_URI));
        assertNotNull(error.getTimestamp());
    }

    @Test
    void formatDate_ShouldReturnFormattedDateTime_WhenCalled() {
        String formattedDate = ErrorItem.formatDate();

        assertNotNull(formattedDate);
        assertTrue(formattedDate.matches("\\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}"));
    }

    @Test
    void settersAndGetters_ShouldWork_WhenErrorItemIsCreated() {
        ErrorItem error = new ErrorItem();
        error.setMessage("Error");
        error.setTimestamp("2020-01-01 12:00");
        error.setUrl(TEST_URI);
        error.setStatusCode(HttpStatus.NOT_FOUND.value());

        assertEquals("Error", error.getMessage());
        assertEquals("2020-01-01 12:00", error.getTimestamp());
        assertEquals(TEST_URI, error.getUrl());
        assertEquals(HttpStatus.NOT_FOUND.value(), error.getStatusCode());
    }

    @Test
    void allArgsConstructor_ShouldPopulateFields_WhenValuesProvided() {
        ErrorItem error = new ErrorItem("Not found", "2020-01-01 12:00", TEST_URI, 404, null);

        assertEquals("Not found", error.getMessage());
        assertEquals("2020-01-01 12:00", error.getTimestamp());
        assertEquals(TEST_URI, error.getUrl());
        assertEquals(404, error.getStatusCode());
    }
}
