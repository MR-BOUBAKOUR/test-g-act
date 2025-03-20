package com.payMyBuddy.dto.account;

import lombok.*;

import java.util.List;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ReceiversAccountsResponseDTO {

    private String ownerName;
    private List<AccountResponseDTO> accounts;

}
