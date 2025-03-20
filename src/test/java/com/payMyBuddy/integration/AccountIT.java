package com.payMyBuddy.integration;

import com.payMyBuddy.dto.user.UserCreateDTO;
import com.payMyBuddy.exception.ResourceNotFoundException;
import com.payMyBuddy.model.Account;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.AccountRepository;
import com.payMyBuddy.security.CustomUserDetailsService;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.service.AccountService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
@WithMockUser
public class AccountIT {

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
    private AccountRepository accountRepository;

    @MockitoBean
    private SecurityUtils securityUtils;

    private User user;

    @BeforeEach
    void setUp() {

        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();

        // Given
        UserCreateDTO createdUser = new UserCreateDTO();
        createdUser.setEmail("user@example.com");
        createdUser.setUsername("user");
        createdUser.setPassword("123123");
        createdUser.setConfirmPassword("123123");

        userService.createUser(createdUser);
        user = userService.findByUserEmailInternalUse(createdUser.getEmail());

        when(securityUtils.getCurrentUserId()).thenReturn(user.getId());
    }

    @Test
    @DisplayName("Création d'un compte avec succès")
    void createAccount_success_test() throws Exception {

        // When
        mockMvc.perform(post("/createAccount")
                        .param("name", "Test Account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        Set<Account> userAccounts = accountRepository.findByUserId(user.getId());

        Account targetAccount = userAccounts.stream()
                .filter(account -> account.getName().equals("Test Account"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Compte non trouvé."));

        // Then
        assertEquals("Test Account", targetAccount.getName());
        assertEquals(BigDecimal.ZERO, targetAccount.getBalance());
        assertNotNull(targetAccount.getCreatedAt());
    }

    @Test
    @DisplayName("Création d'un compte avec un nom existant - Doit lever une exception")
    void createAccount_withAccountAlreadyExist_shouldThrowException_test() throws Exception {

        // Given
        mockMvc.perform(post("/createAccount")
                        .param("name", "Duplicate Account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        // When
        mockMvc.perform(post("/createAccount")
                        .param("name", "Duplicate Account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Vous avez déjà un compte avec ce nom. Veuillez en choisir un autre."));

        Set<Account> userAccounts = accountRepository.findByUserId(user.getId());

        long duplicateCount = userAccounts.stream()
                .filter(account -> account.getName().equals("Duplicate Account"))
                .count();

        // Then
        assertEquals(1, duplicateCount, "Il devrait y avoir exactement un seul compte nommé 'Duplicate Account'.");
        assertTrue(userAccounts.stream()
                .anyMatch(account -> account.getName().equals("Duplicate Account")));
    }

    @Test
    @DisplayName("Création d'un compte avec une erreur de validation - Doit lever une exception")
    void createAccount_wheNotValidData_shouldThrowException_test() throws Exception {
        // Attempt to create account with empty name
        mockMvc.perform(post("/createAccount")
                        .param("name", "")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("createAccount", "name"));
    }

    @Test
    @DisplayName("Suppression d'un compte avec succès")
    void deleteAccount_success_test() throws Exception {

        // Given
        mockMvc.perform(post("/createAccount")
                        .param("name", "Test Account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        Set<Account> userAccounts = accountRepository.findByUserId(user.getId());

        Account targetAccount = userAccounts.stream()
                .filter(account -> account.getName().equals("Test Account"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Compte non trouvé."));

        // When
        mockMvc.perform(delete("/accounts/{accountId}", targetAccount.getId())
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        // Then
        assertFalse(accountRepository.findByUserId(user.getId()).contains(targetAccount));
    }

    @Test
    @DisplayName("Suppression d'un compte inexistant - Doit lever une exception")
    void deleteAccount_whenNotFound_shouldThrowException_test() throws Exception {

        // When/Then
        mockMvc.perform(delete("/accounts/{accountId}", 9999)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Compte non trouvé."));
    }

    @Test
    @DisplayName("Mise à jour du solde d'un compte avec succès")
    void updateBalanceAccount_success_test() throws Exception {

        // Given
        mockMvc.perform(post("/createAccount")
                        .param("name", "Test Account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        Set<Account> userAccounts = accountRepository.findByUserId(user.getId());

        Account targetAccount = userAccounts.stream()
                .filter(account -> account.getName().equals("Test Account"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Compte non trouvé."));

        // When
        mockMvc.perform(put("/accounts/deposit")
                        .param("accountId", String.valueOf(targetAccount.getId()))
                        .param("amount", "100")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        Account updatedAccount = accountService.findAccountByIdInternalUse(targetAccount.getId());

        // Then
        assertEquals(BigDecimal.valueOf(100), updatedAccount.getBalance());
    }

    @Test
    @DisplayName("Mise à jour du solde d'un compte avec une erreur de validation - Doit lever une exception")
    void updateBalanceAccount_wheNotValidData_shouldThrowException_test() throws Exception {

        // Given
        mockMvc.perform(post("/createAccount")
                        .param("name", "Test Account")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/accounts"));

        Set<Account> userAccounts = accountRepository.findByUserId(user.getId());

        Account targetAccount = userAccounts.stream()
                .filter(account -> account.getName().equals("Test Account"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Compte non trouvé."));

        // When/Then
        mockMvc.perform(put("/accounts/deposit")
                        .param("accountId", String.valueOf(targetAccount.getId()))
                        .param("amount", "-100")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("updateBalance", "amount"));
    }
}