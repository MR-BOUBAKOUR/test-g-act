package com.payMyBuddy.unit.controller;

import com.payMyBuddy.controller.ContactController;
import com.payMyBuddy.dto.user.ContactCreateDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.exception.AddContactException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = ContactController.class)
@Import(SecurityConfig.class)
@WithMockUser
class ContactControllerTest {

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
    void showContact_shouldDisplayContactsPage() throws Exception {

        // Given
        Integer UserId = 1;
        UserResponseDTO user = new UserResponseDTO();
        user.setContacts(Set.of());

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(UserId);
        when(userService.findByUserId(UserId)).thenReturn(user);

        // Then
        mockMvc
                .perform(get("/contacts"))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("createContact", "user"));
    }

    @Test
    void createContact_whenValidData_shouldCreateContactsAndRedirect() throws Exception {

        // Given
        Integer userId = 1;
        UserResponseDTO user = new UserResponseDTO();
        user.setContacts(Set.of());

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);
        doNothing().when(userService).createContact(eq(userId), any(ContactCreateDTO.class));

        // Then
        mockMvc
                .perform(post("/createContact")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "contact@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).createContact(eq(userId), any(ContactCreateDTO.class));
    }

    @Test
    void createContact_whenValidationErrors_shouldReturnToContactsPage() throws Exception {
        // Given
        Integer userId = 1;
        UserResponseDTO user = new UserResponseDTO();
        user.setContacts(Set.of());

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);

        // Then
        mockMvc
                .perform(post("/createContact")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "A"))
                .andExpect(status().isOk())
                .andExpect(view().name("contacts"))
                .andExpect(model().attributeExists("createContact", "user"))
                .andExpect(model().attributeHasFieldErrors("createContact", "email"));

        verify(userService, never()).createContact(any(), any());
    }

    @Test
    void createContact_AddContactException_shouldShowErrorMessage() throws Exception {

        // Given
        Integer userId = 1;
        UserResponseDTO user = new UserResponseDTO();
        user.setContacts(Set.of());

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        when(userService.findByUserId(userId)).thenReturn(user);
        doThrow(new AddContactException("Ce contact est déjà dans votre liste."))
                .when(userService).createContact(eq(userId), any(ContactCreateDTO.class));

        // Then
        mockMvc
                .perform(post("/createContact")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("email", "contact@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attribute("errorMessage", "Ce contact est déjà dans votre liste."));

        verify(userService, times(1)).createContact(eq(userId), any(ContactCreateDTO.class));
    }

    @Test
    void deleteContact_shouldDeleteContactsAndRedirect() throws Exception {

        // Given
        Integer userId = 1;
        Integer contactId = 2;

        // When
        when(securityUtils.getCurrentUserId()).thenReturn(userId);
        doNothing().when(userService).deleteContact(userId, contactId);

        // Then
        mockMvc
                .perform(delete("/contacts/{contactId}", contactId)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(userService, times(1)).deleteContact(userId, contactId);
    }
}