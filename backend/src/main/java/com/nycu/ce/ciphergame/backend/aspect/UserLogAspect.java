package com.nycu.ce.ciphergame.backend.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class UserLogAspect {
    

    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.service.UserService.*(..))")
    public void userServiceMethods() {
    }
    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.controller.UserController.*(..))")
    public void userControllerMethods() {
    }

    @Around("userServiceMethods() || userControllerMethods()")
    public Object logUserServiceDetails(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();
        
        System.out.println(">>> AOP BEFORE: " + methodName + " called with args: " + Arrays.toString(args));
        Object result = joinPoint.proceed();
        System.out.println(">>> AOP AFTER: " + methodName + " returned: " + result);
        return result;
    }
}
