package com.payMyBuddy.service;

import com.payMyBuddy.dto.account.AccountCreateDTO;
import com.payMyBuddy.dto.user.ContactCreateDTO;
import com.payMyBuddy.dto.user.UserCreateDTO;
import com.payMyBuddy.dto.user.UserPasswordUpdateDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.exception.EmailAlreadyExistException;
import com.payMyBuddy.exception.IncorrectPasswordException;
import com.payMyBuddy.exception.ResourceNotFoundException;
import com.payMyBuddy.mapper.UserMapper;
import com.payMyBuddy.model.User;
import com.payMyBuddy.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * The type User service.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    private final AccountService accountService;

    /**
     * Instantiates a new User service.
     *
     * @param userRepository the user repository
     * @param userMapper     the user mapper
     * @param accountService the account service
     */
    @Autowired
    public UserService(UserRepository userRepository, UserMapper userMapper, @Lazy AccountService accountService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = new BCryptPasswordEncoder();

        this.accountService = accountService;
    }

    /**
     * Find by user id - internal use.
     *
     * @param userId the user id
     * @return the user
     */
    public User findByUserIdInternalUse(Integer userId) {
        return userRepository.findById(userId)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));
    }

    /**
     * Find by user id.
     *
     * @param userId the user id
     * @return the user response dto
     */
    public UserResponseDTO findByUserId(Integer userId) {
        return userRepository.findById(userId)
            .map(userMapper::toUserResponseDTO)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));
    }

    /**
     * Find by user email - internal use.
     *
     * @param email the email
     * @return the user
     */
    public User findByUserEmailInternalUse(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé."));
    }

    /**
     * Check if Exists by email.
     *
     * @param email the email
     * @return the boolean
     */
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Create user.
     *
     * @param newUser the new user
     */
    public void createUser(UserCreateDTO newUser) {

        if (existsByEmail(newUser.getEmail())) {
            throw new EmailAlreadyExistException("Adresse email déjà utilisée. Veuillez en choisir une autre.");
        }

        User user = userMapper.toEntityFromCreateDTO(newUser);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        userRepository.save(user);

        accountService.createAccount(
            new AccountCreateDTO("Pay My Buddy"),
            user.getId()
        );
    }

    /**
     * Create contact.
     *
     * @param userId           the user id
     * @param contactCreateDTO the contact create dto
     */
    public void createContact(Integer userId, ContactCreateDTO contactCreateDTO) {

        User user = findByUserIdInternalUse(userId);
        User contact = findByUserEmailInternalUse(contactCreateDTO.getEmail());

        // bidirectional relation
        user.addContact(contact);

        userRepository.save(user);
        userRepository.save(contact);
    }

    /**
     * Delete contact.
     *
     * @param userId    the user id
     * @param contactId the contact id
     */
    public void deleteContact(Integer userId, Integer contactId) {

        User user = findByUserIdInternalUse(userId);
        User contact = findByUserIdInternalUse(contactId);

        // bidirectional relation
        user.removeContact(contact);

        userRepository.save(user);
        userRepository.save(contact);
    }

    /**
     * Update password by user id.
     *
     * @param userPasswordUpdateDTO the user password update dto
     * @param userId                the user id
     */
    public void updatePasswordByUserId(UserPasswordUpdateDTO userPasswordUpdateDTO, Integer userId) {

        User user = findByUserIdInternalUse(userId);
        if (!passwordEncoder.matches(
            userPasswordUpdateDTO.getActualPassword(),
            user.getPassword()
        )) {
            throw new IncorrectPasswordException("Le mot de passe actuel est incorrect.");
        }

        user.setPassword(passwordEncoder.encode(userPasswordUpdateDTO.getNewPassword()));

        userRepository.save(user);
    }
}
