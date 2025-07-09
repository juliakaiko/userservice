package com.mymicroservice.userservice.advice;

import com.mymicroservice.userservice.annotation.UserExceptionHandler;
import com.mymicroservice.userservice.exception.NotFoundException;
import com.mymicroservice.userservice.util.ErrorItem;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.stream.Collectors;

@RestControllerAdvice(annotations = UserExceptionHandler.class)
public class UserAdvice {

    /**
     * Обрабатывает исключения валидации для полей DTO, когда данные не проходят аннотации валидации
     * такие как @Valid, @NotNull, @Size, @Pattern и другие.
     *
     * @param e исключение MethodArgumentNotValidException, содержащее информацию об ошибках валидации
     * @return ResponseEntity с объектом ErrorItem, содержащим список сообщений об ошибках
     *         и временную метку, а также статус HTTP 400 (BAD_REQUEST)
     */
    @ExceptionHandler({MethodArgumentNotValidException.class})
    public ResponseEntity <ErrorItem> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        ErrorItem error = new ErrorItem();
        String errors = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(x -> x.getDefaultMessage())
                .collect(Collectors.toList())
                .toString();
        error.setMessage(errors);
        error.setTimestamp(formatDate());
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения валидации для параметров методов контроллера,
     * таких как @Email, @NotBlank и других.
     *
     * @param e исключение ConstraintViolationException, содержащее информацию об ошибках валидации
     * @return ResponseEntity с объектом ErrorItem, содержащим сообщение об ошибке
     *         и временную метку, а также статус HTTP 400 (BAD_REQUEST)
     */
    @ExceptionHandler({ConstraintViolationException.class})
    public ResponseEntity <ErrorItem> handleValidationException(ConstraintViolationException e) {
        ErrorItem error = generateMessage(e);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    /**
     * Обрабатывает исключения нарушения целостности данных, например,
     * при попытке сохранить дубликат уникального поля (например, email).
     *
     * @param e исключение DataIntegrityViolationException, содержащее информацию о нарушении целостности
     * @return ResponseEntity с объектом ErrorItem, содержащим сообщение об ошибке
     *         и временную метку, а также статус HTTP 400 (BAD_REQUEST)
     */
    @ExceptionHandler({DataIntegrityViolationException.class})
    public ResponseEntity <ErrorItem> handleDataIntegrityViolationException(DataIntegrityViolationException e) {
        ErrorItem error = generateMessage(e);
        return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity <ErrorItem> handleNotFoundException(NotFoundException e) {
        ErrorItem error = generateMessage(e);
        return new ResponseEntity<>(error, HttpStatus.NOT_FOUND);
    }

    public ErrorItem generateMessage(Exception e){
        ErrorItem error = new ErrorItem();
        error.setTimestamp(formatDate());
        error.setMessage(e.getMessage());
        return error;
    }

    public String formatDate(){
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        String data = dateTimeFormatter.format( LocalDateTime.now() );
        return data;
    }
}