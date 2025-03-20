package com.payMyBuddy.controller;

import com.payMyBuddy.dto.transaction.TransactionResponseDTO;
import com.payMyBuddy.dto.user.UserPasswordUpdateDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.service.TransactionService;
import com.payMyBuddy.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * The type Profile controller.
 */
@Controller
public class ProfileController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    /**
     * Instantiates a new Profile controller.
     *
     * @param userService        the user service
     * @param transactionService the transaction service
     * @param securityUtils      the security utils
     */
    @Autowired
    public ProfileController(UserService userService, TransactionService transactionService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    /**
     * Show profile.
     *
     * @param model the model
     * @return the string
     */
    @GetMapping("/profile")
    public String showProfile(Model model) {
        Integer userId = securityUtils.getCurrentUserId();
        UserResponseDTO user = userService.findByUserId(userId);

        model.addAttribute("passwordUpdate", new UserPasswordUpdateDTO());
        model.addAttribute("user", user);
        return "profile";
    }

    /**
     * Profile update.
     *
     * @param userPasswordUpdateDTO the user password update dto
     * @param bindingResult         the binding result
     * @param model                 the model
     * @param redirectAttributes    the redirect attributes
     * @return the string
     */
    @PutMapping("/profileUpdate")
    public String profileUpdate(
            @Valid @ModelAttribute("passwordUpdate") UserPasswordUpdateDTO userPasswordUpdateDTO,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        Integer userId = securityUtils.getCurrentUserId();
        UserResponseDTO user = userService.findByUserId(userId);

        if (bindingResult.hasErrors()) {
            model.addAttribute("passwordUpdate", userPasswordUpdateDTO);
            model.addAttribute("user", user);
            return "profile";
        }

        userService.updatePasswordByUserId(userPasswordUpdateDTO, userId);
        redirectAttributes.addFlashAttribute("successMessage", "Votre mot de passe a été modifié avec succès !");
        return "redirect:/profile";
    }
}