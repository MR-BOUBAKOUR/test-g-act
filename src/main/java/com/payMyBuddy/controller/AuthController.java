package com.payMyBuddy.controller;

import com.payMyBuddy.dto.user.UserCreateDTO;
import com.payMyBuddy.dto.user.UserLoginDTO;
import com.payMyBuddy.exception.EmailAlreadyExistException;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.service.UserService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The type Auth controller.
 */
@Controller
public class AuthController {

    private final UserService userService;
    private final SecurityUtils securityUtils;

    /**
     * Instantiates a new Auth controller.
     *
     * @param userService   the user service
     * @param securityUtils the security utils
     */
    @Autowired
    public AuthController(UserService userService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.securityUtils = securityUtils;
    }

    /**
     * Show login form.
     *
     * @param model the model
     * @return the string
     */
    @GetMapping("/login")
    public String showLoginForm(Model model) {
        if (securityUtils.getCurrentUserId() != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", new UserLoginDTO());
        return "login-form";
    }

    /**
     * Show signup form.
     *
     * @param model the model
     * @return the string
     */
    @GetMapping("/signup")
    public String showSignupForm(Model model) {
        if (securityUtils.getCurrentUserId() != null) {
            return "redirect:/dashboard";
        }

        model.addAttribute("user", new UserCreateDTO());
        return "signup-form";
    }

    /**
     * Process signup.
     *
     * @param model              the model
     * @param userCreateDTO      the user create dto
     * @param bindingResult      the binding result
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @PostMapping("/processSignup")
    public String processSignup(
            Model model,
            @Valid @ModelAttribute("user") UserCreateDTO userCreateDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ) {
        if (securityUtils.getCurrentUserId() != null) {
            return "redirect:/dashboard";
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("user", userCreateDTO);
            return "signup-form";
        }

        try {
            userService.createUser(userCreateDTO);
        } catch (EmailAlreadyExistException e) {
            bindingResult.rejectValue(
                    "email", "error.user", e.getMessage()
            );
            model.addAttribute("user", userCreateDTO);
            return "signup-form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Inscription réussie ! Vous pouvez vous connecter.");
        return "redirect:/login";
    }
}