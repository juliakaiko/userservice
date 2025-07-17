package com.mymicroservice.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mymicroservice.userservice.model.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

/*@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString*/
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto implements Serializable {

    private Long userId;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name must be less than 50 characters")
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    @Size(max = 50, message = "Surname must be less than 50 characters")
    private String surname;

    @NotNull
    @Past(message = "Birth date must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) //ISO.DATE = yyyy-MM-dd
    private LocalDate birthDate;

    @Email(regexp="\\w+@\\w+\\.\\w+", message="Please provide a valid email address")
    @NotBlank(message = "Email address may not be blank")
    private String email;


    /**
     * Пароль пользователя. Должен соответствовать следующим требованиям:
     * <ul>
     *     <li>Не может быть пустым или состоять только из пробелов ({@code @NotBlank})</li>
     *     <li>Длина должна быть от 5 до 255 символов ({@code @Size})</li>
     * </ul>
     *
     * <p>При сериализации в JSON пароль игнорируется ({@code @JsonProperty(access = WRITE_ONLY)}),
     * чтобы избежать его случайного раскрытия в API-ответах. Однако пароль остается доступным
     * для десериализации (например, при получении данных от клиента) и сохраняется в кэше/БД.
     *
     * <p>Пример корректного пароля: {@code "secure123"}.
     *
     * @see NotBlank
     * @see Size
     * @see com.fasterxml.jackson.annotation.JsonProperty
     */
    @NotBlank (message = "Password may not be blank")
    @Size(min=5, max=255, message = "Password size must be between 5 and 255")
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @NotNull
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)  // Пароль принимается в JSON, но не выводится
    private Role role;
}
