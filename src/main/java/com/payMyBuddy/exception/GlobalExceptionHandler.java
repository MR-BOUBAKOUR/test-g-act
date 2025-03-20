package com.payMyBuddy.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * The type Global exception handler.
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handle unauthorized exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(UnauthorizedException.class)
    public String handleUnauthorizedException(UnauthorizedException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/login";
    }

    /**
     * Handle insufficient balance exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(InsufficientBalanceException.class)
    public String handleInsufficientBalanceException(InsufficientBalanceException ex, RedirectAttributes redirectAttributes) {
        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage1", ex.getMessage());
        return "redirect:/transactions";
    }

    /**
     * Handle self sending amount exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(SelfSendingAmountException.class)
    public String handleSelfSendingAmountException(SelfSendingAmountException ex, RedirectAttributes redirectAttributes) {
        logger.warn(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage2", ex.getMessage());
        return "redirect:/transactions";
    }

    /**
     * Handle add contact exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(AddContactException.class)
    public String handleAddContactException(AddContactException ex, RedirectAttributes redirectAttributes) {
        logger.error(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/contacts";
    }

    /**
     * Handle conflict exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(ConflictException.class)
    public String handleConflictException(ConflictException ex, RedirectAttributes redirectAttributes) {
        logger.error(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/accounts";
    }

    /**
     * Handle email already exist exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(EmailAlreadyExistException.class)
    public String handleEmailAlreadyExistException(EmailAlreadyExistException ex, RedirectAttributes redirectAttributes) {
        logger.error(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/signup";
    }

    /**
     * Handle incorrect password exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(IncorrectPasswordException.class)
    public String handleIncorrectPasswordException(IncorrectPasswordException ex, RedirectAttributes redirectAttributes) {
        logger.error(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/profile";
    }

    /**
     * Handle resource not found exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public String handleResourceNotFoundException(ResourceNotFoundException ex, RedirectAttributes redirectAttributes) {
        logger.error(ex.getMessage());
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());

        if (ex.getMessage() != null && ex.getMessage().contains("Compte non trouvé.")) {
            return "redirect:/accounts";
        }

        return "redirect:/contacts";
    }

    /**
     * Handle general exception string.
     *
     * @param ex                 the ex
     * @param redirectAttributes the redirect attributes
     * @return the string
     */
    @ExceptionHandler(Exception.class)
    public String handleGeneralException(Exception ex, RedirectAttributes redirectAttributes) {
        logger.error(String.valueOf(ex));
        redirectAttributes.addFlashAttribute("errorMessage", "Une erreur inattendue est survenue. Veuillez réessayer plus tard.");
        return "redirect:/error";
    }
}
