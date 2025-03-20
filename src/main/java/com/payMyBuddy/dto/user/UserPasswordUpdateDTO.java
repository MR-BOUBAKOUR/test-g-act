package com.payMyBuddy.dto.user;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserPasswordUpdateDTO {

    @NotBlank(message = "Le mot de passe est obligatoire.")
    private String actualPassword;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères.")
    private String newPassword;

    @NotBlank(message = "Veuillez confirmer votre mot de passe.")
    private String confirmNewPassword;

    @AssertTrue(message = "Les mots de passe ne correspondent pas.")
    public boolean isPasswordMatching() {
        return newPassword != null && newPassword.equals(confirmNewPassword);
    }

}
