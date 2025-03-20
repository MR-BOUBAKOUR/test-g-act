package com.payMyBuddy.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class LoggingAspect {

    @Pointcut("within(com.payMyBuddy.controller..*)")
    public void controllerMethods() {}

    @Pointcut("within(com.payMyBuddy.service..*)")
    public void serviceMethods() {}

    @Pointcut("controllerMethods() || serviceMethods()")
    public void applicationPackagePointcut() {}

    // Advice pour logger les entrées/sorties des méthodes et leur temps d'exécution

    @Around("applicationPackagePointcut()")
    public Object logAround(ProceedingJoinPoint joinPoint) throws Throwable {

        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());

        if (logger.isDebugEnabled()) {

            logger.debug("==============================================================");
            logger.debug("➡️  [ENTRÉE] {}() | Arguments: {}",
                joinPoint.getSignature().getName(),
                Arrays.toString(joinPoint.getArgs())
            );

        }

        try {
            long start = System.currentTimeMillis();
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - start;

            if (logger.isDebugEnabled()) {

                logger.debug("==============================================================");
                logger.debug("✅ [SORTIE] {}() | Résultat: {} | Temps d'exécution: {}ms",
                    joinPoint.getSignature().getName(),
                    result,
                    executionTime
                );

            }
            return result;
        } catch (IllegalArgumentException ex) {
            logger.error("❌ [ERREUR] Argument illégal dans {}()",
                joinPoint.getSignature().getName()
            );
            throw ex;
        }
    }

     // Advice pour logger les exceptions

    @AfterThrowing(pointcut = "applicationPackagePointcut()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {

        Logger logger = LoggerFactory.getLogger(joinPoint.getSignature().getDeclaringTypeName());

        logger.error("==============================================================");
            logger.error("🚨 [EXCEPTION] {}() | Cause: {}",
                joinPoint.getSignature().getName(),
                ex.getCause() != null
                    ? ex.getCause()
                    : "NULL"
            );

    }
}
