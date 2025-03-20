package com.payMyBuddy.unit.service;

import com.payMyBuddy.dto.account.AccountCreateDTO;
import com.payMyBuddy.dto.user.ContactCreateDTO;
import com.payMyBuddy.dto.user.UserCreateDTO;
import com.payMyBuddy.dto.user.UserPasswordUpdateDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.exception.EmailAlreadyExistException;
import com.payMyBuddy.exception.IncorrectPasswordException;
import com.payMyBuddy.mapper.UserMapper;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.UserRepository;
import com.payMyBuddy.service.AccountService;
import com.payMyBuddy.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserMapper userMapper;
    @Mock
    private AccountService accountService;
    @InjectMocks
    private UserService userService;

    @Test
    void createUser_whenEmailDoesNotExist_shouldCreateUser() {

        // Given
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setId(1);
        userCreateDTO.setEmail("utilisateur@gmail.com");
        userCreateDTO.setPassword("123123");

        User user = new User();
        user.setId(1);
        user.setEmail("utilisateur@gmail.com");
        user.setPassword("123123");

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(userMapper.toEntityFromCreateDTO(any(UserCreateDTO.class))).thenReturn(user);
        when(userRepository.save(any(User.class))).thenReturn(user);
        doNothing().when(accountService).createAccount(any(AccountCreateDTO.class), anyInt());

        // When
        userService.createUser(userCreateDTO);

        // Then
        verify(userRepository, times(1)).existsByEmail("utilisateur@gmail.com");
        verify(userRepository, times(1)).save(any(User.class));

        verify(accountService, times(1)).createAccount(any(AccountCreateDTO.class), anyInt());
    }

    @Test
    void createUser_whenEmailExists_shouldThrowException() {

        // Given
        UserCreateDTO userCreateDTO = new UserCreateDTO();
        userCreateDTO.setEmail("existingUser@gmail.com");

        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        // When
        EmailAlreadyExistException exception = assertThrows(
                EmailAlreadyExistException.class,
                () -> userService.createUser(userCreateDTO)
        );

        // Then
        assertEquals("Adresse email déjà utilisée. Veuillez en choisir une autre.", exception.getMessage());
        verify(userRepository, times(1)).existsByEmail("existingUser@gmail.com");
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void updatePasswordByUserId_whenValidData_shouldUpdatePassword() {

        // Given
        UserPasswordUpdateDTO passwordUpdateDTO = new UserPasswordUpdateDTO();
        passwordUpdateDTO.setActualPassword("oldPassword");
        passwordUpdateDTO.setNewPassword("newPassword");

        User user = new User();
        user.setPassword(new BCryptPasswordEncoder().encode("oldPassword"));

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);

        // When
        userService.updatePasswordByUserId(passwordUpdateDTO, 1);

        // Then
        assertTrue(new BCryptPasswordEncoder().matches("newPassword", user.getPassword()));
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void updatePasswordByUserId_whenNotValidData_shouldThrowException() {

        // Given
        UserPasswordUpdateDTO passwordUpdateDTO = new UserPasswordUpdateDTO();
        passwordUpdateDTO.setActualPassword("wrongPassword");
        passwordUpdateDTO.setNewPassword("newPassword");

        User user = new User();
        user.setPassword(new BCryptPasswordEncoder().encode("oldPassword"));

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));

        // When
        IncorrectPasswordException exception = assertThrows(
                IncorrectPasswordException.class,
                () -> userService.updatePasswordByUserId(passwordUpdateDTO, 1)
        );

        // Then
        assertEquals("Le mot de passe actuel est incorrect.", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void findByUserId_whenUserExists_shouldReturnUser() {
        // Given
        User user = new User();
        user.setId(1);

        UserResponseDTO userResponseDTO = new UserResponseDTO();
        userResponseDTO.setId(1);

        when(userRepository.findById(anyInt())).thenReturn(Optional.of(user));
        when(userMapper.toUserResponseDTO(user)).thenReturn(userResponseDTO);

        // When
        UserResponseDTO result = userService.findByUserId(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getId());
        verify(userRepository, times(1)).findById(1);
    }

    @Test
    void findByUserEmailInternalUse_whenUserExists_shouldReturnUser() {

        // Given
        User user = new User();
        user.setEmail("user@example.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        // When
        User result = userService.findByUserEmailInternalUse("user@example.com");

        // Then
        assertNotNull(result);
        assertEquals("user@example.com", result.getEmail());
        verify(userRepository, times(1)).findByEmail("user@example.com");
    }

    @Test
    void createContact_whenUserExistsAndContactExists_shouldAddContact() {

        // Given
        User user = new User();
        user.setId(1);
        user.setContacts(new HashSet<>());

        User contact = new User();
        contact.setId(2);
        contact.setEmail("contact@example.com");
        contact.setContacts(new HashSet<>());

        ContactCreateDTO contactCreateDTO = new ContactCreateDTO();
        contactCreateDTO.setEmail("contact@example.com");

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("contact@example.com")).thenReturn(Optional.of(contact));

        // When
        userService.createContact(1, contactCreateDTO);

        // Then
        assertTrue(user.getContacts().contains(contact));
        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).save(contact);
    }

    @Test
    void deleteContact_whenUserExistsAndContactExists_shouldRemoveContacts() {

        // Given
        User user = new User();
        user.setId(1);
        user.setContacts(new HashSet<>());

        User contact = new User();
        contact.setId(2);
        contact.setContacts(new HashSet<>());

        user.getContacts().add(contact);
        contact.getContacts().add(user);

        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        when(userRepository.findById(2)).thenReturn(Optional.of(contact));

        // When
        userService.deleteContact(1, 2);

        // Then
        assertFalse(user.getContacts().contains(contact));
        assertFalse(contact.getContacts().contains(user));
        verify(userRepository, times(1)).save(user);
        verify(userRepository, times(1)).save(contact);
    }


}