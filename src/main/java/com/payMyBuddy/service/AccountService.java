package com.payMyBuddy.service;

import com.payMyBuddy.dto.account.AccountCreateDTO;
import com.payMyBuddy.dto.account.AccountResponseDTO;
import com.payMyBuddy.dto.account.BalanceUpdateDTO;
import com.payMyBuddy.dto.account.ReceiversAccountsResponseDTO;
import com.payMyBuddy.dto.user.ContactResponseDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.exception.ConflictException;
import com.payMyBuddy.exception.ResourceNotFoundException;
import com.payMyBuddy.mapper.AccountMapper;
import com.payMyBuddy.model.Account;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.AccountRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * The type Account service.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    private final UserService userService;

    /**
     * Find account by id - internal use.
     *
     * @param accountId the account id
     * @return the account
     */
    public Account findAccountByIdInternalUse(Integer accountId) {
        return accountRepository.findById(accountId)
            .orElseThrow(() -> new ResourceNotFoundException("Compte non trouvé."));
    }

    /**
     * Create account.
     *
     * @param accountCreateDTO the account create dto
     * @param userId           the user id
     */
    public void createAccount(AccountCreateDTO accountCreateDTO, Integer userId) {

        User user = userService.findByUserIdInternalUse(userId);

        if (accountRepository.existsByNameAndUser_Id(accountCreateDTO.getName(), userId)) {
            throw new ConflictException("Vous avez déjà un compte avec ce nom. Veuillez en choisir un autre.");
        }

        Account account = accountMapper.toEntityFromCreateDTO(accountCreateDTO);
        account.setUser(user);
        account.setBalance(BigDecimal.ZERO);
        account.setCreatedAt(Instant.now());

        accountRepository.save(account);
    }

    /**
     * Delete account.
     *
     * @param accountId the account id
     */
    public void deleteAccount(Integer accountId) {

        Account account = findAccountByIdInternalUse(accountId);
        accountRepository.delete(account);
    }

    /**
     * Update balance account.
     *
     * @param balanceUpdateDTO the balance update dto
     */
    public void updateBalanceAccount(BalanceUpdateDTO balanceUpdateDTO) {

        Account account = findAccountByIdInternalUse(balanceUpdateDTO.getAccountId());
        account.setBalance(
            account.getBalance()
                .add(balanceUpdateDTO.getAmount())
        );

        accountRepository.save(account);
    }

    /**
     * Find accounts for current user and his contacts list.
     *
     * @param userId the user id
     * @return the list
     */
    public List<ReceiversAccountsResponseDTO> findAccountsForCurrentUserAndHisContacts(Integer userId) {

        List<ReceiversAccountsResponseDTO> allAccountsDTO = new ArrayList<>();

        UserResponseDTO currentUser = userService.findByUserId(userId);
        List<AccountResponseDTO> selfAccounts =
            accountRepository.findByUserId(userId).stream()
                .map(accountMapper::toAccountResponseDTO)
                .toList();

        allAccountsDTO.add(
            new ReceiversAccountsResponseDTO(
                currentUser.getUsername(),
                selfAccounts
        ));

        Set<ContactResponseDTO> contacts = currentUser.getContacts();

        allAccountsDTO.addAll(
            contacts.stream()
                .map(contact -> {

                    Set<Account> accounts = accountRepository.findByUserId(contact.getContactId());
                    List<AccountResponseDTO> beneficiaryAccounts = accounts.stream()
                        .map(accountMapper::toAccountResponseDTO)
                        .toList();

                    return new ReceiversAccountsResponseDTO(
                        contact.getUsername(),
                        beneficiaryAccounts
                    );

                }).toList()
        );

        return allAccountsDTO;
    }

    /**
     * Save account.
     *
     * @param account the account
     */
    public void saveAccount(Account account) {
        accountRepository.save(account);
    }
}
