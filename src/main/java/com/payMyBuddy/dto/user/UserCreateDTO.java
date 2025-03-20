package com.payMyBuddy.dto.user;

import lombok.*;
import jakarta.validation.constraints.*;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserCreateDTO {

    @Null(message = "L'ID doit être nul lors de la création de l'utilisateur.")
    private Integer id;

    @NotBlank(message = "Le username est obligatoire.")
    @Size(min = 3, max = 50, message = "Le username doit contenir entre 3 et 50 caractères.")
    private String username;

    @NotBlank(message = "L'email est obligatoire.")
    @Email(message = "L'email doit être valide.")
    private String email;

    @NotBlank(message = "Le mot de passe est obligatoire.")
    @Size(min = 6, message = "Le mot de passe doit contenir au moins 6 caractères.")
    private String password;

    @NotBlank(message = "Veuillez confirmer votre mot de passe.")
    private String confirmPassword;

    @AssertTrue(message = "Les mots de passe ne correspondent pas.")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }

}
