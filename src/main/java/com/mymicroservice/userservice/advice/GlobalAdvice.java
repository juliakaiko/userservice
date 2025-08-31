package com.mymicroservice.userservice.advice;

import com.mymicroservice.userservice.annotation.GlobalExceptionHandler;
import com.mymicroservice.userservice.exception.CardInfoNotFoundException;
import com.mymicroservice.userservice.exception.UserNotFoundException;
import com.mymicroservice.userservice.util.ErrorItem;
import io.jsonwebtoken.JwtException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestControllerAdvice(annotations = GlobalExceptionHandler.class)
public class GlobalAdvice {

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
     * Handles authorization denied exceptions when a user lacks required permissions.
     *
     * @param e AuthorizationDeniedException containing authorization failure details
     * @return ResponseEntity with ErrorItem containing error details and HTTP 403 status (FORBIDDEN)
     */
    @ExceptionHandler({AuthorizationDeniedException.class})
    public ResponseEntity<ErrorItem> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        ErrorItem error = generateMessage(e, HttpStatus.FORBIDDEN);
        return new ResponseEntity<>(error, HttpStatus.FORBIDDEN);
    }

    /**
     * Handles {@link HttpMessageNotReadableException} which occurs when HTTP request body
     * cannot be properly parsed or converted to the expected Java object.
     *
     * <p>This typically happens when:
     * <ul>
     *   <li>Malformed JSON syntax in request body</li>
     *   <li>Type mismatch between JSON values and target Java types</li>
     *   <li>Invalid enum values that cannot be converted to the target enum type</li>
     *   <li>Missing required fields in JSON payload</li>
     * </ul>
     *
     * @param e the HttpMessageNotReadableException that was thrown during request processing
     * @return ResponseEntity containing ErrorItem with details about the parsing error
     * @see org.springframework.http.converter.HttpMessageNotReadableException
     * @see org.springframework.http.HttpStatus#BAD_REQUEST
     * @since 1.0
     */
    @ExceptionHandler({HttpMessageNotReadableException.class})
    public ResponseEntity<ErrorItem> handleHttpMessageNotReadableException(HttpMessageNotReadableException e) {
        ErrorItem error = generateMessage(e, HttpStatus.BAD_REQUEST);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Handles general JWT authentication failures (invalid/malformed tokens).
     *
     * @param e JwtException containing the authentication failure details
     * @return ResponseEntity with ErrorItem containing error details and HTTP 401 status (UNAUTHORIZED)
     */
    @ExceptionHandler({JwtException.class})
    public ResponseEntity<ErrorItem> handleJwtException(JwtException e) {
        ErrorItem error = generateMessage(e, HttpStatus.UNAUTHORIZED);
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
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