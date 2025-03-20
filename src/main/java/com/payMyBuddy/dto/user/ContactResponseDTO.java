package com.payMyBuddy.dto.user;

import com.payMyBuddy.dto.account.AccountResponseDTO;
import lombok.*;

import java.util.Set;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ContactResponseDTO {

    private Integer contactId;
    private String username;
    private String email;
    private Set<AccountResponseDTO> accounts;

}
