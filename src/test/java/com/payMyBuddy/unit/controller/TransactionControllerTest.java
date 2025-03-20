package com.payMyBuddy.unit.controller;

import com.payMyBuddy.controller.TransactionController;
import com.payMyBuddy.dto.transaction.TransactionCreateDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.exception.InsufficientBalanceException;
import com.payMyBuddy.exception.SelfSendingAmountException;
import com.payMyBuddy.security.CustomUserDetailsService;
import com.payMyBuddy.security.SecurityConfig;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.service.AccountService;
import com.payMyBuddy.service.TransactionService;
import com.payMyBuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = TransactionController.class)
@Import(SecurityConfig.class)
@WithMockUser
class TransactionControllerTest {

    @MockitoBean
    private AccountService accountService;
    @MockitoBean
    private UserService userService;
    @MockitoBean
    private TransactionService transactionService;

    @Autowired
    private WebApplicationContext context;
    @Autowired
    private MockMvc mockMvc;
    @MockitoBean
    private SecurityUtils securityUtils;
    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    void showTransactions_shouldDisplayTransactionsPage() throws Exception {

        // Given
        Integer userId = 1;
        UserResponseDTO user = new UserResponseDTO();

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);

        // Then
        mockMvc
                .perform(get("/transactions"))
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions", "user", "transactionCreate", "receiversAccounts"));
    }

    @Test
    void createTransaction_withValidData_shouldRedirectToTransactions() throws Exception {

        // Given
        Integer userId = 1;
        Integer receiverId = 2;
        UserResponseDTO user = new UserResponseDTO();
        TransactionCreateDTO transactionCreateDTO = new TransactionCreateDTO();

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);
        doNothing().when(transactionService).createTransaction(transactionCreateDTO);

        // Then
        mockMvc
                .perform(post("/createTransaction")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("amount", "100.0")
                        .param("description", "Test transaction")
                        .param("senderAccountId", userId.toString())
                        .param("receiverAccountId", receiverId.toString())
                )
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    void createTransaction_withInvalidData_shouldReturnTransactionsView() throws Exception {

        // Given
        Integer userId = 1;
        Integer receiverId = 2;
        UserResponseDTO user = new UserResponseDTO();

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);

        // Then
        mockMvc
                .perform(post("/createTransaction")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("amount", "-50.0")
                        .param("description", "A".repeat(256))
                        .param("senderAccountId", userId.toString())
                        .param("receiverAccountId", receiverId.toString())
                )
                .andExpect(status().isOk())
                .andExpect(view().name("transactions"))
                .andExpect(model().attributeExists("transactions", "user", "transactionCreate", "receiversAccounts"))
                .andExpect(model().attributeHasFieldErrors("transactionCreate", "amount", "description"));
    }

    @Test
    void createTransaction_insufficientFunds_shouldShowErrorMessage() throws Exception {

        doThrow(new InsufficientBalanceException("Solde insuffisant. Veuillez alimenter votre compte."))
                .when(transactionService).createTransaction(any(TransactionCreateDTO.class));

        // Then
        mockMvc
                .perform(post("/createTransaction")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("amount", "1000.0")
                        .param("description", "Test transaction")
                        .param("senderAccountId", "1")
                        .param("receiverAccountId", "2")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attributeExists("errorMessage1"))
                .andExpect(flash().attribute("errorMessage1", "Solde insuffisant. Veuillez alimenter votre compte."));
    }


    @Test
    void createTransaction_withSameSourceAndDestination_shouldShowError() throws Exception {

        doThrow(new SelfSendingAmountException("Virement interdit sur le même compte."))
                .when(transactionService).createTransaction(any(TransactionCreateDTO.class));

        // Then
        mockMvc
                .perform(post("/createTransaction")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .with(csrf())
                        .param("amount", "100.0")
                        .param("description", "Self transfer")
                        .param("senderAccountId", "1")
                        .param("receiverAccountId", "1")
                )
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/transactions"))
                .andExpect(flash().attributeExists("errorMessage2"))
                .andExpect(flash().attribute("errorMessage2", "Virement interdit sur le même compte."));
    }
}