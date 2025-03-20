package com.payMyBuddy.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payMyBuddy.controller.DashboardController;
import com.payMyBuddy.dto.transaction.TransactionResponseDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.exception.ResourceNotFoundException;
import com.payMyBuddy.exception.UnauthorizedException;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.payMyBuddy.model.TransactionType.SELF_TRANSFER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = DashboardController.class)
@Import(SecurityConfig.class)
@WithMockUser
class DashboardControllerTest {

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
    void showDashboard_shouldDisplayDashboardPage() throws Exception {

        // Given
        Integer userId = 1;
        UserResponseDTO user = new UserResponseDTO();

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);

        // Then
        mockMvc
                .perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"))
                .andExpect(model().attributeExists("user", "recentTransactions"));
    }

    @Test
    void showDashboard_shouldHandleUserNotFoundException() throws Exception {

        // Given
        Integer userId = 1;

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenThrow(new ResourceNotFoundException("Utilisateur non trouvé."));

        // Then
        mockMvc
                .perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"));
    }

}