package com.payMyBuddy.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * The type Custom error controller.
 */
@Controller
public class CustomErrorController implements ErrorController {

    /**
     * Handle error.
     *
     * @return the string
     */
    @RequestMapping("/error")
    public String handleError() {
        return "error";
    }
}
