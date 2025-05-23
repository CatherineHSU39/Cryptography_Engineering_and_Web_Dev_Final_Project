package com.nycu.ce.ciphergame.backend.aspect;

import java.util.Arrays;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class GroupAuditAspect {

    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.service.GroupService.*(..))")
    public void groupServiceMethods() {
    }

    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.repository.GroupRepository.*(..))")
    public void groupRepositoryMethods() {
    }

    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.repository.GroupMemberRepository.*(..))")
    public void groupMemberRepositoryMethods() {
    }

    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.repository.MessageRepository.*(..))")
    public void messageRepositoryMethods() {
    }

    @Around("groupServiceMethods() || groupRepositoryMethods() || groupMemberRepositoryMethods() || messageRepositoryMethods()")
    public Object logExecutionDetails(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().toShortString();
        Object[] args = joinPoint.getArgs();

        System.out.println(">>> AOP BEFORE: " + methodName + " called with args: " + Arrays.toString(args));

        for (Object arg : args) {
            if (arg instanceof com.nycu.ce.ciphergame.backend.entity.Group group) {
                System.out.println(">>> AOP: Group ID before persist = " + group.getId());
            }
            if (arg instanceof com.nycu.ce.ciphergame.backend.entity.GroupMember gm) {
                System.out.println(">>> AOP: GroupMember ID = " + gm.getId());
            }
        }

        Object result = joinPoint.proceed();

        System.out.println(">>> AOP AFTER: " + methodName + " returned: " + result);

        return result;
    }
}
