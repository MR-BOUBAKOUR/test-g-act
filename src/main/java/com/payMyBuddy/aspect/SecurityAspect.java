package com.payMyBuddy.aspect;

import com.payMyBuddy.controller.AccountController;
import com.payMyBuddy.security.SecurityUtils;
import com.payMyBuddy.exception.UnauthorizedException;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class SecurityAspect {

    private static final Logger logger = LoggerFactory.getLogger(SecurityAspect.class);

    private final SecurityUtils securityUtils;

    @Autowired
    public SecurityAspect(SecurityUtils securityUtils) {
        this.securityUtils = securityUtils;
    }

    // Pointcuts qui interceptent toutes les méthodes des contrôleurs
    @Pointcut("execution(* com.payMyBuddy.controller.AccountController.*(..))")
    public void accountControllerMethods() {}

    @Pointcut("execution(* com.payMyBuddy.controller.ContactController.*(..))")
    public void contactControllerMethods() {}

    @Pointcut("execution(* com.payMyBuddy.controller.DashboardController.*(..))")
    public void dashboardControllerMethods() {}

    @Pointcut("execution(* com.payMyBuddy.controller.TransactionController.*(..))")
    public void transactionControllerMethods() {}

    @Pointcut("execution(* com.payMyBuddy.controller.ProfileController.*(..))")
    public void ProfileControllerMethods() {}

    @Pointcut("accountControllerMethods() || contactControllerMethods() || dashboardControllerMethods() || transactionControllerMethods() || ProfileControllerMethods()")
    public void securedControllers() {}

    // Avant l'exécution des méthodes, vérifier si l'utilisateur est authentifié
    @Before("securedControllers()")
    public void checkAuthentication() {
        Integer userId = securityUtils.getCurrentUserId();
        if (userId == null) {
            throw new UnauthorizedException("Utilisateur non authentifié");
        }
    }
}
