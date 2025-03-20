package com.payMyBuddy.controller;

import com.payMyBuddy.dto.user.ContactCreateDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The type Contact controller.
 */
@Controller
public class ContactController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    /**
     * Instantiates a new Contact controller.
     *
     * @param userService   the user service
     * @param securityUtils the security utils
     */
    @Autowired
    public ContactController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    /**
     * Show contacts.
     *
     * @param model the model
     * @return the string
     */
    @GetMapping("/contacts")
    public String showContacts(Model model) {
        Integer userId = securityUtils.getCurrentUserId();
        UserResponseDTO userResponseDTO = userService.findByUserId(userId);

        model.addAttribute("createContact", new ContactCreateDTO());
        model.addAttribute("user", userResponseDTO);
        return "contacts";
    }

    /**
     * Create contact.
     *
     * @param contactCreateDTO   the contact create dto
     * @param bindingResult      the binding result
     * @param model              the model
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @PostMapping("/createContact")
    public String createContact(
            @Valid @ModelAttribute("createContact") ContactCreateDTO contactCreateDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Integer userId = securityUtils.getCurrentUserId();
        UserResponseDTO userResponseDTO = userService.findByUserId(userId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("createContact", contactCreateDTO);
            model.addAttribute("user", userResponseDTO);
            return "contacts";
        }

        userService.createContact(userId, contactCreateDTO);
        redirectAttributes.addFlashAttribute("successMessage", "Contact ajouté avec succès !");
        return "redirect:/contacts";
    }

    /**
     * Delete contact.
     *
     * @param contactId          the contact id
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @DeleteMapping("/contacts/{contactId}")
    public String deleteContact(
            @PathVariable Integer contactId,
            RedirectAttributes redirectAttributes
    ) {
        Integer userId = securityUtils.getCurrentUserId();

        userService.deleteContact(userId, contactId);
        redirectAttributes.addFlashAttribute("successMessage", "Contact supprimé avec succès !");
        return "redirect:/contacts";
    }
}