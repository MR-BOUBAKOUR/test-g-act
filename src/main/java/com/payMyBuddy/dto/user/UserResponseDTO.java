package com.payMyBuddy.dto.user;

import com.payMyBuddy.dto.account.AccountResponseDTO;

import lombok.*;

import java.util.Set;

@Setter @Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserResponseDTO {

    private Integer id;
    private String username;
    private String email;
    private Set<AccountResponseDTO> accounts;
    private Set<ContactResponseDTO> contacts;

}
