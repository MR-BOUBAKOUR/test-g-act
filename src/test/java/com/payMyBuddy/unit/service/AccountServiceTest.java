package com.payMyBuddy.unit.service;

import com.payMyBuddy.dto.account.AccountCreateDTO;
import com.payMyBuddy.dto.account.AccountResponseDTO;
import com.payMyBuddy.dto.account.BalanceUpdateDTO;
import com.payMyBuddy.dto.account.ReceiversAccountsResponseDTO;
import com.payMyBuddy.dto.user.ContactResponseDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.exception.ConflictException;
import com.payMyBuddy.mapper.AccountMapper;
import com.payMyBuddy.model.Account;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.AccountRepository;
import com.payMyBuddy.service.AccountService;
import com.payMyBuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@SpringBootTest
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private UserService userService;

    @InjectMocks
    private AccountService accountService;

    private User user;
    private Account account;
    private AccountCreateDTO accountCreateDTO;
    private BalanceUpdateDTO balanceUpdateDTO;

    @BeforeEach
    void setUp() {

        user = new User();
        user.setId(1);
        user.setUsername("Utilisateur");

        account = new Account();
        account.setId(1);
        account.setBalance(BigDecimal.valueOf(100));
        account.setName("Compte Test");
        account.setCreatedAt(Instant.now());
        account.setReceivedTransactions(Set.of());
        account.setSentTransactions(Set.of());
        account.setUser(user);

        accountCreateDTO = new AccountCreateDTO();
        accountCreateDTO.setName("Nouveau Compte");

        balanceUpdateDTO = new BalanceUpdateDTO();
        balanceUpdateDTO.setAccountId(1);
        balanceUpdateDTO.setAmount(BigDecimal.valueOf(50));

    }

    @Test
    void findAccountByIdInternalUse_shouldReturnAccount_whenAccountExists() {

        // Given
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account));

        // When
        Account result = accountService.findAccountByIdInternalUse(1);

        // Then
        assertNotNull(result);
        assertEquals(account.getId(), result.getId());
        assertEquals(account.getBalance(), result.getBalance());
        assertEquals(account.getName(), result.getName());

        verify(accountRepository, times(1)).findById(1);

    }

    @Test
    void createAccount_whenValidData_shouldCreateAccount() {

        // Given
        when(userService.findByUserIdInternalUse(anyInt())).thenReturn(user);
        when(accountRepository.existsByNameAndUser_Id(anyString(), anyInt())).thenReturn(false);
        when(accountMapper.toEntityFromCreateDTO(accountCreateDTO)).thenReturn(account);

        // When
        accountService.createAccount(accountCreateDTO, 1);

        // Then
        verify(userService, times(1)).findByUserIdInternalUse(1);
        verify(accountRepository, times(1)).existsByNameAndUser_Id("Nouveau Compte", 1);
        verify(accountMapper, times(1)).toEntityFromCreateDTO(accountCreateDTO);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void createAccount_whenNameAlreadyExists_shouldThrowException () {

        // Given
        when(userService.findByUserIdInternalUse(anyInt())).thenReturn(user);
        when(accountRepository.existsByNameAndUser_Id(anyString(), anyInt())).thenReturn(true);

        // When
        ConflictException exception = assertThrows(
                ConflictException.class,
                () -> accountService.createAccount(accountCreateDTO, user.getId())
        );

        // Then
        assertEquals("Vous avez déjà un compte avec ce nom. Veuillez en choisir un autre.", exception.getMessage());
        verify(userService, times(1)).findByUserIdInternalUse(1);
        verify(accountRepository, times(1)).existsByNameAndUser_Id("Nouveau Compte", 1);
        verify(accountMapper, never()).toEntityFromCreateDTO(any(AccountCreateDTO.class));
        verify(accountRepository, never()).save(any(Account.class));

    }

    @Test
    void deleteAccount_whenAccountExists_shouldDeleteAccount() {

        // Given
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account));

        // When
        accountService.deleteAccount(1);

        // Then
        verify(accountRepository, times(1)).findById(1);
        verify(accountRepository, times(1)).delete(account);
    }

    @Test
    void updateBalanceAccount_whenAccountExists_shouldUpdateBalance() {

        // Given
        when(accountRepository.findById(anyInt())).thenReturn(Optional.of(account));

        // When
        accountService.updateBalanceAccount(balanceUpdateDTO);

        // Then
        assertEquals(new BigDecimal("150"), account.getBalance());
        verify(accountRepository, times(1)).findById(1);
        verify(accountRepository, times(1)).save(account);
    }

    @Test
    void findAccountsForCurrentUserAndHisContacts_shouldReturnAllAccounts() {

        // Arrange

            // user & contacts DTOs
        UserResponseDTO user = new UserResponseDTO();
        user.setId(1);
        user.setUsername("user");
        ContactResponseDTO contact = new ContactResponseDTO();
        contact.setContactId(2);
        contact.setUsername("contactUser");

        Set<ContactResponseDTO> contacts = new HashSet<>();
        user.setContacts(contacts);
        contacts.add(contact);

            // accounts
        Account userAccount = new Account();
        userAccount.setId(1);
        userAccount.setName("Compte Personnel");
        Account contactAccount = new Account();
        contactAccount.setId(2);
        contactAccount.setName("Compte Contact");

        Set<Account> contactAccounts = new HashSet<>();
        contactAccounts.add(contactAccount);

            // accounts DTOs
        AccountResponseDTO userAccountDTO = new AccountResponseDTO();
        userAccountDTO.setId(1);
        userAccountDTO.setName("Compte Personnel");
        AccountResponseDTO contactAccountDTO = new AccountResponseDTO();
        contactAccountDTO.setId(2);
        contactAccountDTO.setName("Compte Contact");

        when(userService.findByUserId(anyInt())).thenReturn(user);

        when(accountRepository.findByUserId(1)).thenReturn(Set.of(userAccount));
        when(accountRepository.findByUserId(2)).thenReturn(contactAccounts);

        when(accountMapper.toAccountResponseDTO(userAccount)).thenReturn(userAccountDTO);
        when(accountMapper.toAccountResponseDTO(contactAccount)).thenReturn(contactAccountDTO);

        // Act
        List<ReceiversAccountsResponseDTO> result = accountService.findAccountsForCurrentUserAndHisContacts(1);

        // Assert

        assertEquals(2, result.size());

        assertEquals("user", result.get(0).getOwnerName());
        assertEquals("contactUser", result.get(1).getOwnerName());

        verify(userService, times(1)).findByUserId(1);
        verify(accountRepository, times(1)).findByUserId(1);
        verify(accountRepository, times(1)).findByUserId(2);

    }

    @Test
    void saveAccount_shouldSaveAccount() {

        // When
        accountService.saveAccount(account);

        // Then
        verify(accountRepository, times(1)).save(account);
    }

}