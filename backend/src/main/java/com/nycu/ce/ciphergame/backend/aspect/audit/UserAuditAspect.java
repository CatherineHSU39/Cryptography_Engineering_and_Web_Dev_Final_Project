package com.nycu.ce.ciphergame.backend.aspect.audit;

import java.util.UUID;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dao.UserDAO;
import com.nycu.ce.ciphergame.backend.entity.User;
import com.nycu.ce.ciphergame.backend.service.audit.AuditService;
import com.nycu.ce.ciphergame.backend.util.CRUDAction;

@Aspect
@Component
public class UserAuditAspect {

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private AuditService auditService;

    @Pointcut("execution(* com.example.demo.service.UserService.updateUser())")
    public void userUpdateMethods() {
    }

    @Around("userUpdateMethods()")
    public Object aroundUpdateUser(ProceedingJoinPoint joinPoint) throws Throwable {
        UUID userId = (UUID) joinPoint.getArgs()[0];
        User userBefore = userDAO.getDetachedUserById(userId);

        Object result = joinPoint.proceed();
        User userAfter = userDAO.getDetachedUserById(userId);
        System.out.println(">>> AOP: userBefore = " + userBefore.getUsername());
        System.out.println(">>> AOP: userAfter = " + userAfter.getUsername());
        auditService.insertAudit(userId, CRUDAction.UPDATE, userBefore, userAfter);
        System.out.println(">>> AOP: Audit user update");
        return result;
    }

}
