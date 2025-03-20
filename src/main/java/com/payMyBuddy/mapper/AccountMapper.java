package com.payMyBuddy.mapper;

import com.payMyBuddy.dto.account.AccountCreateDTO;
import com.payMyBuddy.dto.account.AccountResponseDTO;
import com.payMyBuddy.model.Account;
import org.mapstruct.Mapper;

/**
 * The interface Account mapper.
 */
@Mapper(componentModel = "spring")
public interface AccountMapper {

    /**
     * From entity account to account response dto.
     *
     * @param account the account
     * @return the account response dto
     */
    AccountResponseDTO toAccountResponseDTO(Account account);

    /**
     * From account create dto to entity account.
     *
     * @param accountCreateDTO the account create dto
     * @return the account
     */
    Account toEntityFromCreateDTO(AccountCreateDTO accountCreateDTO);
}