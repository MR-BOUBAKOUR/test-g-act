package com.payMyBuddy.dto.transaction;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.math.BigDecimal;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TransactionCreateDTO {

    @NotNull(message = "L'identifiant du compte émetteur est obligatoire.")
    private Integer senderAccountId;

    @NotNull(message = "L'identifiant du compte destinataire est obligatoire.")
    private Integer receiverAccountId;

    @NotNull(message = "Le montant est obligatoire.")
    @Positive(message = "Le montant doit être positif.")
    private BigDecimal amount;

    @Size(max = 255, message = "La description ne peut dépasser 255 caractères.")
    private String description;

}
