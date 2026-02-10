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

@RestControllerAdvice(annotations = GlobalExceptionHandler.class)
public class GlobalAdvice {

    /**
     * Handles validation exceptions for DTO fields when controller method parameters
     * annotated with @Valid fail validation, such as @NotNull, @NotBlank, @Size, @Email, etc.
     *
     * <p>This method extracts field-specific error messages and returns them
     * in the `fieldErrors` map, where keys are field names and values are messages.
     * It also returns a general message, timestamp, URL, and HTTP 400 status code.
     *
     * @param e the MethodArgumentNotValidException containing validation error information
     * @return ResponseEntity containing an ErrorItem object with:
     *         - general message ("Validation failed")
     *         - map of fieldErrors (field name → validation message)
     *         - timestamp
     *         - request URL
     *         - HTTP 400 status code (BAD_REQUEST)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity<ErrorItem> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorItem error = ErrorItem.fromMethodArgumentNotValid(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
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
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
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
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    /**
     * Handles authorization denied exceptions when a user lacks required permissions.
     *
     * @param e AuthorizationDeniedException containing authorization failure details
     * @return ResponseEntity with ErrorItem containing error details and HTTP 403 status (FORBIDDEN)
     */
    @ExceptionHandler({AuthorizationDeniedException.class})
    public ResponseEntity<ErrorItem> handleAuthorizationDeniedException(AuthorizationDeniedException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.FORBIDDEN);
        return ResponseEntity.status(error.getStatusCode()).body(error);
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
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.BAD_REQUEST);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }

    /**
     * Handles general JWT authentication failures (invalid/malformed tokens).
     *
     * @param e JwtException containing the authentication failure details
     * @return ResponseEntity with ErrorItem containing error details and HTTP 401 status (UNAUTHORIZED)
     */
    @ExceptionHandler({JwtException.class})
    public ResponseEntity<ErrorItem> handleJwtException(JwtException e) {
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.UNAUTHORIZED);
        return ResponseEntity.status(error.getStatusCode()).body(error);
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
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.NOT_FOUND);
        return ResponseEntity.status(error.getStatusCode()).body(error);
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
        ErrorItem error = ErrorItem.generateMessage(e, HttpStatus.NOT_FOUND);
        return ResponseEntity.status(error.getStatusCode()).body(error);
    }
}