package com.payMyBuddy.unit.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.payMyBuddy.controller.ProfileController;
import com.payMyBuddy.dto.account.AccountCreateDTO;
import com.payMyBuddy.dto.user.UserPasswordUpdateDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
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

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ProfileController.class)
@Import(SecurityConfig.class)
@WithMockUser
class ProfileControllerTest {
    
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
    void showProfile_shouldDisplayProfilePage() throws Exception {
        // Given
        Integer userId = 1;
        UserResponseDTO user = new UserResponseDTO();
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);

        // When & Then
        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("passwordUpdate", "user"));
    }

    // actualPassword newPassword confirmNewPassword

    @Test
    void updateProfile_whenValidData_shouldUpdatePasswordAndRedirect() throws Exception {
        // Given
        Integer userId = 1;
        UserResponseDTO user = new UserResponseDTO();
        user.setEmail("test@gmail.com");

        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);

        // When & Then
        doNothing().when(userService).updatePasswordByUserId(any(UserPasswordUpdateDTO.class), eq(userId));

        mockMvc.perform(put("/profileUpdate")
                        .param("actualPassword", "123")
                        .param("newPassword", "456789")
                        .param("confirmNewPassword", "456789")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).updatePasswordByUserId(any(UserPasswordUpdateDTO.class), eq(userId));
    }

    @Test
    void updateProfile_whenInvalidData_shouldReturnProfileWithErrors() throws Exception {
        // Given
        Integer userId = 1;
        UserResponseDTO user = new UserResponseDTO();

        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);

        // When & Then
        mockMvc.perform(put("/profileUpdate")
                        .param("actualPassword", "123123")
                        .param("newPassword", "456")
                        .param("confirmNewPassword", "123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("passwordUpdate", "user"))
                .andExpect(model().attributeHasFieldErrors("passwordUpdate", "passwordMatching"));

        verify(userService, never()).updatePasswordByUserId(any(), any());
    }

}