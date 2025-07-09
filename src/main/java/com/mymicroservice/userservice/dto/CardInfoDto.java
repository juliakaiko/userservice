package com.mymicroservice.userservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode (exclude = {"userId"})
@ToString (exclude = {"userId"})
@Builder
public class CardInfoDto implements Serializable {

    private Long cardId;

    @NotBlank(message = "Card number cannot be blank")
    @Size(min = 16, max = 16, message = "Card number must be exactly 16 characters")
    @Pattern(regexp = "^[0-9]+$", message = "Card number must contain only digits")
    private String number;

    @NotBlank(message = "Card holder cannot be blank")
    @Size(max = 100, message = "Card holder must be less than 100 characters")
    private String holder;

    @Future(message = "Expiration date must be in the future")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate expirationDate;

/*    @JsonIgnore
    private User user;*/

  // @JsonProperty("userId")
    private Long userId;

    @JsonProperty("userId") //поле должно участвовать в сериализации/десериализации
    public Long getUserId() {
        return userId; // в Mapping берем ID связанного User
        //return user != null ? user.getUserId() : userId;
    }
}
