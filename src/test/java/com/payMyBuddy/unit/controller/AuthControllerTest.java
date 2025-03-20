package com.payMyBuddy.unit.controller;

import com.payMyBuddy.controller.AuthController;
import com.payMyBuddy.dto.user.UserCreateDTO;
import com.payMyBuddy.exception.EmailAlreadyExistException;
import com.payMyBuddy.security.CustomUserDetailsService;
import com.payMyBuddy.security.SecurityConfig;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import(SecurityConfig.class)
class AuthControllerTest {

    @MockitoBean
    private UserService userService;

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
    @WithAnonymousUser
    void login_whenNotLoggedIn_shouldDisplayLoginForm() throws Exception {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("login-form"))
            .andExpect(model().attributeExists("user"));

        verify(securityUtils).getCurrentUserId();
    }

    @Test
    @WithAnonymousUser
    void signup_whenNotLoggedIn_shouldDisplaySignupForm() throws Exception {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/signup"))
            .andExpect(status().isOk())
            .andExpect(view().name("signup-form"))
            .andExpect(model().attributeExists("user"));

        verify(securityUtils).getCurrentUserId();
    }

    @Test
    @WithMockUser
    void login_whenLoggedIn_shouldRedirectToDashboard() throws Exception {
        // When & Then
        mockMvc.perform(get("/login"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));

        verify(securityUtils).getCurrentUserId();
    }

    @Test
    @WithMockUser
    void signup_whenLoggedIn_shouldRedirectToDashboard() throws Exception {

        // When & Then
        mockMvc.perform(get("/signup"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));

        verify(securityUtils).getCurrentUserId();
    }

    @Test
    @WithMockUser
    void processSignup_whenLoggedIn_shouldRedirectToDashboard() throws Exception {
        // When & Then
        mockMvc.perform(post("/processSignup")
                .param("username", "John")
                .param("email", "john.doe@example.com")
                .param("password", "123123")
                .param("confirmPassword", "123123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/dashboard"));

        // Verify
        verify(userService, never()).createUser(any());
        verify(securityUtils).getCurrentUserId();
    }

    @Test
    @WithAnonymousUser
    void processSignup_whenNotValidData_shouldReturnToSignupForm() throws Exception {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(null);

        // When & Then
        mockMvc.perform(post("/processSignup")
                .param("username", "")
                .param("email", "john")
                .param("password", "123")
                .param("confirmPassword", "123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("signup-form"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().hasErrors());

        // Verify
        verify(userService, never()).createUser(any());
        verify(securityUtils).getCurrentUserId();
    }

    @Test
    @WithAnonymousUser
    void processSignup_whenEmailAlreadyExists_shouldReturnToSignupForm() throws Exception {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(null);
        doThrow(new EmailAlreadyExistException("Adresse email déjà utilisée. Veuillez en choisir une autre."))
                .when(userService).createUser(any(UserCreateDTO.class));

        // When & Then
        mockMvc.perform(post("/processSignup")
                .param("username", "John")
                .param("email", "john.doe@example.com")
                .param("password", "123123")
                .param("confirmPassword", "123123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk())
            .andExpect(view().name("signup-form"))
            .andExpect(model().attributeExists("user"))
            .andExpect(model().hasErrors())
            .andExpect(model().attributeHasFieldErrors("user", "email"));

        // Verify
        verify(userService, times(1)).createUser(any(UserCreateDTO.class));
        verify(securityUtils).getCurrentUserId();
    }

    @Test
    @WithAnonymousUser
    void processSignup_whenValidData_shouldCreateUserAndRedirect() throws Exception {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(null);
        doNothing().when(userService).createUser(any(UserCreateDTO.class));

        // When & Then
        mockMvc.perform(post("/processSignup")
                .param("username", "John")
                .param("email", "john.doe@example.com")
                .param("password", "123123")
                .param("confirmPassword", "123123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/login"));

        // Verify
        ArgumentCaptor<UserCreateDTO> userCaptor = ArgumentCaptor.forClass(UserCreateDTO.class);

        verify(userService, times(1)).createUser(userCaptor.capture());
        verify(securityUtils).getCurrentUserId();

        UserCreateDTO capturedUser = userCaptor.getValue();
        assertEquals("John", capturedUser.getUsername());
        assertEquals("john.doe@example.com", capturedUser.getEmail());
        assertEquals("123123", capturedUser.getPassword());
        assertEquals("123123", capturedUser.getConfirmPassword());
    }
}