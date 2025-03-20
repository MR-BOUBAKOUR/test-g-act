package com.payMyBuddy.dto.account;

import jakarta.validation.constraints.*;
import lombok.*;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class AccountCreateDTO {

    @NotBlank(message = "Le nom est obligatoire.")
    @Size(min = 2, message = "Le nom doit contenir au moins 2 caractères.")
    private String name;

}
