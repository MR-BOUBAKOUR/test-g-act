package com.payMyBuddy.controller;

import com.payMyBuddy.dto.transaction.TransactionResponseDTO;
import com.payMyBuddy.dto.user.UserResponseDTO;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.service.TransactionService;
import com.payMyBuddy.service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * The type Dashboard controller.
 */
@Controller
public class DashboardController {

    private final UserService userService;
    private final TransactionService transactionService;
    private final SecurityUtils securityUtils;

    /**
     * Instantiates a new Dashboard controller.
     *
     * @param userService        the user service
     * @param transactionService the transaction service
     * @param securityUtils      the security utils
     */
    @Autowired
    public DashboardController(UserService userService, TransactionService transactionService, SecurityUtils securityUtils) {
        this.userService = userService;
        this.transactionService = transactionService;
        this.securityUtils = securityUtils;
    }

    /**
     * Show dashboard.
     *
     * @param model the model
     * @return the string
     */
    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        Integer userId = securityUtils.getCurrentUserId();

        UserResponseDTO user = userService.findByUserId(userId);
        List<TransactionResponseDTO> recentTransactions = transactionService.findTransactionsForCurrentUser(userId, 5);

        model.addAttribute("user", user);
        model.addAttribute("recentTransactions", recentTransactions);
        return "dashboard";
    }
}