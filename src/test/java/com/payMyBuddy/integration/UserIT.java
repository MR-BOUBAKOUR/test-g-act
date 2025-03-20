package com.payMyBuddy.integration;

import com.payMyBuddy.dto.user.UserCreateDTO;
import com.payMyBuddy.exception.ResourceNotFoundException;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.UserRepository;
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
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(locations = "classpath:application-test.properties")
@Transactional
public class UserIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private UserService userService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private SecurityUtils securityUtils;

    private User currentUser;

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
        currentUser = userService.findByUserEmailInternalUse(createdUser.getEmail());

        when(securityUtils.getCurrentUserId()).thenReturn(currentUser.getId());
    }

    @Test
    @DisplayName("Création d'un utilisateur avec succès")
    @WithAnonymousUser
    void createUser_success_test() throws Exception {
        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(null);

        UserCreateDTO newUser = new UserCreateDTO();
        newUser.setEmail("newUser@example.com");
        newUser.setUsername("newUser");
        newUser.setPassword("456456");
        newUser.setConfirmPassword("456456");

        // When
        mockMvc.perform(post("/processSignup")
                        .param("email", newUser.getEmail())
                        .param("username", newUser.getUsername())
                        .param("password", newUser.getPassword())
                        .param("confirmPassword", newUser.getConfirmPassword())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        // Then
        User createdUser = userService.findByUserEmailInternalUse(newUser.getEmail());

        assert createdUser.getUsername().equals(newUser.getUsername());
        assert createdUser.getAccounts() != null;
    }

    @Test
    @DisplayName("Création d'un utilisateur avec email existant - Doit lever une exception")
    @WithAnonymousUser
    void createUser_whenEmailAlreadyExists_test() throws Exception {

        // Given
        when(securityUtils.getCurrentUserId()).thenReturn(null);

        UserCreateDTO duplicateUser = new UserCreateDTO();
        duplicateUser.setEmail("user@example.com");
        duplicateUser.setUsername("duplicateUser");
        duplicateUser.setPassword("456456");
        duplicateUser.setConfirmPassword("456456");

        // When/Then
        mockMvc.perform(post("/processSignup")
                        .flashAttr("user", duplicateUser)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isOk())
                .andExpect(view().name("signup-form"))
                .andExpect(model().attributeHasFieldErrors("user", "email"))
                .andExpect(model().errorCount(1));
    }

    @Test
    @DisplayName("Création d'un contact avec succès")
    @WithMockUser
    void createContact_success_test() throws Exception {

        // Given
        UserCreateDTO contact = new UserCreateDTO();
        contact.setEmail("contact@example.com");
        contact.setUsername("contact");
        contact.setPassword("789789");
        contact.setConfirmPassword("789789");

        userService.createUser(contact);

        // When/Then
        mockMvc.perform(post("/createContact")
                        .param("email", contact.getEmail())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"));

        // Verify contact was added
        User updatedUser = userService.findByUserIdInternalUse(currentUser.getId());


        assertTrue(updatedUser.getContacts().stream()
                .anyMatch(con -> con.getEmail().equals(contact.getEmail())));
    }

    @Test
    @DisplayName("Suppression d'un contact avec succès")
    @WithMockUser
    void deleteContact_success_test() throws Exception {

        // Given
        UserCreateDTO contact = new UserCreateDTO();
        contact.setEmail("contact@example.com");
        contact.setUsername("contact");
        contact.setPassword("789789");
        contact.setConfirmPassword("789789");

        userService.createUser(contact);

        mockMvc.perform(post("/createContact")
                        .param("email", contact.getEmail())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"));

        // Verify contact was added
        User userWithContact = userService.findByUserIdInternalUse(currentUser.getId());

        assertTrue(userWithContact.getContacts().stream()
                .anyMatch(con -> con.getEmail().equals(contact.getEmail())));

        User targetContact = userWithContact.getContacts().stream()
                .filter(con -> con.getEmail().equals("contact@example.com"))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Contact non trouvé."));

        // When/Then - Delete the contact
        mockMvc.perform(delete("/contacts/{contactId}", targetContact.getId())
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attributeExists("successMessage"))
                .andExpect(flash().attribute("successMessage", "Contact supprimé avec succès !"));

        // Verify contact was removed
        User updatedUser = userService.findByUserIdInternalUse(currentUser.getId());

        assertFalse(updatedUser.getContacts().stream()
                .anyMatch(con -> con.getEmail().equals(contact.getEmail())));
    }

    @Test
    @DisplayName("Suppression d'un contact non existant - Doit lever une exception")
    @WithMockUser
    void deleteContact_whenNonExistingContact_test() throws Exception {
        // When/Then
        mockMvc.perform(delete("/contacts/{contactId}", 9999)
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/contacts"))
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Utilisateur non trouvé."));
    }

    @Test
    @DisplayName("Mise à jour de mot de passe avec succès")
    @WithMockUser
    void updatePassword_success_test() throws Exception {

        // When/Then
        mockMvc.perform(put("/profileUpdate")
                        .param("actualPassword", "123123")
                        .param("newPassword", "newPass")
                        .param("confirmNewPassword", "newPass")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile"));

        // Verify password was updated
        User updatedUser = userService.findByUserIdInternalUse(currentUser.getId());

        assertTrue(passwordEncoder.matches(
                "newPass",
                updatedUser.getPassword()
        ));

    }

    @Test
    @DisplayName("Mise à jour de mot de passe avec mot de passe incorrect - Doit lever une exception")
    @WithMockUser
    void updatePassword_whenNotValid_test() throws Exception {
        // When/Then
        mockMvc.perform(put("/profileUpdate")
                        .param("actualPassword", "wrongPass")
                        .param("newPassword", "newPass")
                        .param("confirmNewPassword", "newPass")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("errorMessage"))
                .andExpect(flash().attribute("errorMessage", "Le mot de passe actuel est incorrect."));
    }
}