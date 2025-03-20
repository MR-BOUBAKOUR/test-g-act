package com.payMyBuddy.integration;

import com.payMyBuddy.dto.account.AccountCreateDTO;
import com.payMyBuddy.dto.account.BalanceUpdateDTO;
import com.payMyBuddy.dto.user.UserCreateDTO;
import com.payMyBuddy.exception.ResourceNotFoundException;
import com.payMyBuddy.model.Account;
import com.payMyBuddy.model.Transaction;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.AccountRepository;
import com.payMyBuddy.repository.TransactionRepository;
import com.payMyBuddy.security.CustomUserDetailsService;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.service.AccountService;
import com.payMyBuddy.service.TransactionService;
import com.payMyBuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@WithMockUser
public class TransactionIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionService transactionService;

    @Autowired
    private AccountRepository accountRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @MockitoBean
    private SecurityUtils securityUtils;

    private User user;
    private Account senderAccount;
    private Account receiverAccount;

    @BeforeEach
    void setUp() throws Exception {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Given

            // Creating User
        UserCreateDTO createdUser = new UserCreateDTO();
        createdUser.setEmail("user@example.com");
        createdUser.setUsername("user");
        createdUser.setPassword("123123");
        createdUser.setConfirmPassword("123123");

        userService.createUser(createdUser);
        user = userService.findByUserEmailInternalUse(createdUser.getEmail());


            // Creating Sender Account & Receiver Account
        AccountCreateDTO senderAccountDTO = new AccountCreateDTO("Sender Account");
        AccountCreateDTO receiverAccountDTO = new AccountCreateDTO("Receiver Account");

        accountService.createAccount(senderAccountDTO, user.getId());
        accountService.createAccount(receiverAccountDTO, user.getId());

        Set<Account> userAccounts = accountRepository.findByUserId(user.getId());

        senderAccount = userAccounts.stream()
                .filter(account -> account.getName().equals("Sender Account"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Compte expéditeur non trouvé."));

        receiverAccount = userAccounts.stream()
                .filter(account -> account.getName().equals("Receiver Account"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Compte destinataire non trouvé."));

            // Adding funds to the sender account
        BalanceUpdateDTO balanceUpdateDTO = new BalanceUpdateDTO();
        balanceUpdateDTO.setAccountId(senderAccount.getId());
        balanceUpdateDTO.setAmount(new BigDecimal("1000"));

        accountService.updateBalanceAccount(balanceUpdateDTO);

        when(securityUtils.getCurrentUserId()).thenReturn(user.getId());
    }

    @Test
    @DisplayName("Affichage de la page des transactions")
    void showTransactions_success_test() throws Exception {
        // When/Then
        mockMvc.perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("receiversAccounts"))
                .andExpect(model().attributeExists("transactionCreate"))
                .andExpect(model().attributeExists("transactions"))
                .andExpect(model().attributeExists("user"));
    }

    @Test
    @DisplayName("Création d'une transaction avec succès")
    void createTransaction_success_test() throws Exception {

        // When
        mockMvc.perform(post("/createTransaction")
                        .param("senderAccountId", senderAccount.getId().toString())
                        .param("receiverAccountId", receiverAccount.getId().toString())
                        .param("amount", "100")
                        .param("description", "Test transaction")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Transaction effectuée avec succès !"));

        Account updatedSenderAccount = accountService.findAccountByIdInternalUse(senderAccount.getId());
        Account updatedReceiverAccount = accountService.findAccountByIdInternalUse(receiverAccount.getId());

        List<Transaction> transactions = transactionRepository.findBySenderAccount_User_IdOrReceiverAccount_User_Id(
                user.getId(), user.getId()
        );
        Transaction transaction = transactions.getFirst();

        // Then
        assertEquals(BigDecimal.valueOf(900), updatedSenderAccount.getBalance());
        assertEquals(BigDecimal.valueOf(100), updatedReceiverAccount.getBalance());

        assertEquals(1, transactions.size());

        assertEquals(BigDecimal.valueOf(100), transaction.getAmount());
        assertEquals("Test transaction", transaction.getDescription());
        assertEquals(senderAccount.getId(), transaction.getSenderAccount().getId());
        assertEquals(receiverAccount.getId(), transaction.getReceiverAccount().getId());
    }

    @Test
    @DisplayName("Création d'une transaction avec montant négatif - Doit lever une exception")
    void createTransaction_withNegativeAmount_shouldThrowException_test() throws Exception {

        // When/Then
        mockMvc.perform(post("/createTransaction")
                        .param("senderAccountId", senderAccount.getId().toString())
                        .param("receiverAccountId", receiverAccount.getId().toString())
                        .param("amount", "-100")
                        .param("description", "Transaction invalide")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeHasFieldErrors("transactionCreate", "amount"));
    }

    @Test
    @DisplayName("Création d'une transaction avec solde insuffisant - Doit lever une exception")
    void createTransaction_withInsufficientBalance_shouldThrowException_test() throws Exception {

        // When/Then
        mockMvc.perform(post("/createTransaction")
                        .param("senderAccountId", senderAccount.getId().toString())
                        .param("receiverAccountId", receiverAccount.getId().toString())
                        .param("amount", "2000")  // Montant supérieur au solde
                        .param("description", "Transaction avec solde insuffisant")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attributeExists("errorMessage1"))
                .andExpect(flash().attribute("errorMessage1", "Solde insuffisant. Veuillez alimenter votre compte."));
    }

    @Test
    @DisplayName("Création d'une transaction vers le même compte - Doit lever une exception")
    void createTransaction_sameAccount_shouldThrowException_test() throws Exception {

        // When/Then
        mockMvc.perform(post("/createTransaction")
                        .param("senderAccountId", senderAccount.getId().toString())
                        .param("receiverAccountId", senderAccount.getId().toString())
                        .param("amount", "100")
                        .param("description", "Transaction même compte")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attributeExists("errorMessage2"))
                .andExpect(flash().attribute("errorMessage2", "Virement interdit sur le même compte."));
    }

    @Test
    @DisplayName("Création de multiples transactions et vérification du solde final")
    void createMultipleTransactions_success_test() throws Exception {

        // When
        mockMvc.perform(post("/createTransaction")
                        .param("senderAccountId", senderAccount.getId().toString())
                        .param("receiverAccountId", receiverAccount.getId().toString())
                        .param("amount", "200")
                        .param("description", "Transaction 1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));

        mockMvc.perform(post("/createTransaction")
                        .param("senderAccountId", senderAccount.getId().toString())
                        .param("receiverAccountId", receiverAccount.getId().toString())
                        .param("amount", "300")
                        .param("description", "Transaction 2")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"));

        Account updatedSenderAccount = accountService.findAccountByIdInternalUse(senderAccount.getId());
        Account updatedReceiverAccount = accountService.findAccountByIdInternalUse(receiverAccount.getId());

        List<Transaction> transactions = transactionRepository.findBySenderAccount_User_IdOrReceiverAccount_User_Id(
                user.getId(), user.getId());

        // Then
        assertEquals(BigDecimal.valueOf(500), updatedSenderAccount.getBalance());
        assertEquals(BigDecimal.valueOf(500), updatedReceiverAccount.getBalance());

        assertEquals(2, transactions.size());
    }
}