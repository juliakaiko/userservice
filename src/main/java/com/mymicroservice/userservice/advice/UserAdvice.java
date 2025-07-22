package com.mymicroservice.userservice.advice;

import com.mymicroservice.userservice.annotation.UserExceptionHandler;
import com.mymicroservice.userservice.exception.CardInfoNotFoundException;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.util.ErrorItem;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestControllerAdvice(annotations = UserExceptionHandler.class)
public class UserAdvice {

    /**
     * Handles validation exceptions for DTO fields when data fails validation annotations
     * such as @Valid, @NotNull, @Size, @Pattern and others.
     *
     * @param e MethodArgumentNotValidException containing validation error information
     * @return ResponseEntity with an ErrorItem object containing:
     *         - List of error messages
     *         - URL
     *         - Status code
     *         - Timestamp
     *         - HTTP 400 status (BAD_REQUEST)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorItem> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorItem error = new ErrorItem();
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList())
                .toString();
        error.setMessage(errors);
        error.setTimestamp(formatDate());
        error.setUrl(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString());
        error.setStatusCode(HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles validation exceptions for controller method parameters,
     * such as @NotEmpty, @NotBlank and others.
     *
     * @param e ConstraintViolationException containing validation error information
     * @return ResponseEntity with an ErrorItem object containing:
     *         - Error message
     *         - URL
     *         - Status code
     *         - Timestamp
     *         - HTTP 400 status (BAD_REQUEST)
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity<ErrorItem> handleValidationException(ConstraintViolationException e) {
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles data integrity violation exceptions, for example,
     * when attempting to save a duplicate unique field (such as email).
     *
     * @param e DataIntegrityViolationException containing integrity violation information
     * @return ResponseEntity with an ErrorItem object containing:
     *         - Error message
     *         - URL
     *         - Status code
     *         - Timestamp
     *         - HTTP 400 status (BAD_REQUEST)
     */
    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity<ErrorItem> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles exceptions when the User is not found.
     *
     * @param e UserNotFoundException
     * @return ResponseEntity with an ErrorItem object containing:
     *         - Error message
     *         - URL
     *         - Status code
     *         - Timestamp
     *         - HTTP 404 status (NOT_FOUND)
     */
    @ExceptionHandler({UserNotFoundException.class})
    public ResponseEntity<ErrorItem> handleUserNotFoundException(UserNotFoundException e) {
        ErrorItem error = generateMessage(e, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Handles exceptions when card information is not found.
     *
     * @param e CardInfoNotFoundException
     * @return ResponseEntity with an ErrorItem object containing:
     *         - Error message
     *         - URL
     *         - Status code
     *         - Timestamp
     *         - HTTP 404 status (NOT_FOUND)
     */
    @ExceptionHandler({CardInfoNotFoundException.class})
    public ResponseEntity<ErrorItem> handleCardInfoNotFoundException(CardInfoNotFoundException e) {
        ErrorItem error = generateMessage(e, HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    /**
     * Generates an ErrorItem object with error message, URL, status code and timestamp.
     *
     * @param e Exception
     * @param status HTTP status
     * @return ErrorItem with populated fields
     */
    public ErrorItem generateMessage(Exception e, HttpStatus status) {
        ErrorItem error = new ErrorItem();
        error.setTimestamp(formatDate());
        error.setMessage(e.getMessage());
        error.setUrl(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString());
        error.setStatusCode(status.value());
        return error;
    }

    /**
     * Formats the current date and time into a string with pattern "yyyy-MM-dd HH:mm".
     *
     * @return formatted date-time string
     */
    public String formatDate() {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        return dateTimeFormatter.format(LocalDateTime.now());
    }
}