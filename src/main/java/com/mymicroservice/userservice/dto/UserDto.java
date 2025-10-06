package com.mymicroservice.userservice.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true) // ignore unknown fields
public class UserDto implements Serializable {

    private Long userId;

    @NotBlank(message = "Name cannot be blank")
    @Size(max = 50, message = "Name must be less than 50 characters")
    private String name;

    @NotBlank(message = "Surname cannot be blank")
    @Size(max = 50, message = "Surname must be less than 50 characters")
    private String surname;

    @NotNull(message = "Birth date cannot be null")
    @Past(message = "Birth date must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) //ISO.DATE = yyyy-MM-dd
    private LocalDate birthDate;

    @Email(regexp="[\\w.]+@\\w+\\.\\w+", message="Please provide a valid email address") // [a-zA-Z0-9_] _ .
    @NotBlank(message = "Email address may not be blank")
    private String email;

    /**
     * User password. Must meet the following requirements:
     * <ul>
     *     <li>Cannot be empty or contain only whitespace ({@code @NotBlank})</li>
     *     <li>Length must be between 5 and 255 characters ({@code @Size})</li>
     * </ul>
     *
     * <p>When serialized to JSON, the password is ignored ({@code @JsonProperty(access = WRITE_ONLY)})
     * to prevent accidental exposure in API responses. However, the password remains available
     * for deserialization (e.g., when receiving data from client) and is saved in cache/database.
     *
     * <p>Example of valid password: {@code "secure123"}.
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
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Role role;
}
