package com.nycu.ce.ciphergame.backend.aspect.audit;

import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import com.nycu.ce.ciphergame.backend.dao.GroupDAO;
import com.nycu.ce.ciphergame.backend.dto.group.CUGroupResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.service.audit.AuditService;
import com.nycu.ce.ciphergame.backend.util.CRUDAction;

@Aspect
@Component
public class GroupAuditAspect {

    @Autowired
    private GroupDAO groupDAO;

    @Autowired
    private AuditService auditService;

    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.controller.GroupController.createGroup(..))")
    public void groupCreateMethods() {
    }

    @AfterReturning(pointcut = "groupCreateMethods()", returning = "result")
    public void aroundCreateGroup(JoinPoint joinPoint, Object result) {
        Jwt jwt = (Jwt) joinPoint.getArgs()[0];
        UUID userId = UUID.fromString(jwt.getSubject());
        ResponseEntity<CUGroupResponse> response = (ResponseEntity<CUGroupResponse>) result;

        if (!response.getStatusCode().is2xxSuccessful()) {
            return;
        }

        CUGroupResponse responseBody = response.getBody();
        UUID groupId = responseBody.getId();
        String groupName = responseBody.getName();

        Group newGroup = Group.builder()
                .name(groupName)
                .build();
        Group dummyGroup = Group.builder().build();
        System.out.println(">>> AOP: groupBefore = null");
        System.out.println(">>> AOP: groupAfter = " + groupName);
        auditService.insertAudit(userId, CRUDAction.CREATE, groupId, dummyGroup, newGroup);
        System.out.println(">>> AOP: Audit group create");
    }

    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.controller.GroupController.updateGroup(..))")
    public void groupUpdateMethods() {
    }

    @Around("groupUpdateMethods()")
    public Object aroundUpdateGroup(ProceedingJoinPoint joinPoint) throws Throwable {
        Jwt jwt = (Jwt) joinPoint.getArgs()[0];
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID groupId = (UUID) joinPoint.getArgs()[1];
        Group groupBefore = groupDAO.getDetachedGroupById(groupId);

        Object result = joinPoint.proceed();
        ResponseEntity<CUGroupResponse> response = (ResponseEntity<CUGroupResponse>) result;

        if (!response.getStatusCode().is2xxSuccessful()) {
            return result;
        }

        Group groupAfter = groupDAO.getDetachedGroupById(groupId);
        System.out.println(">>> AOP: groupBefore = " + groupBefore.getName());
        System.out.println(">>> AOP: groupAfter = " + groupAfter.getName());
        auditService.insertAudit(userId, CRUDAction.UPDATE, groupId, groupBefore, groupAfter);
        System.out.println(">>> AOP: Audit group update");
        return result;
    }

    @Pointcut("execution(* com.nycu.ce.ciphergame.backend.controller.GroupController.deleteGroup(..))")
    public void groupDeleteMethods() {
    }

    @Around("groupDeleteMethods()")
    public Object aroundDeleteGroup(ProceedingJoinPoint joinPoint) throws Throwable {
        Jwt jwt = (Jwt) joinPoint.getArgs()[0];
        UUID userId = UUID.fromString(jwt.getSubject());
        UUID groupId = (UUID) joinPoint.getArgs()[1];
        Group groupBefore = groupDAO.getDetachedGroupById(groupId);

        Object result = joinPoint.proceed();
        ResponseEntity<CUGroupResponse> response = (ResponseEntity<CUGroupResponse>) result;

        if (!response.getStatusCode().is2xxSuccessful()) {
            return result;
        }

        Group dummyGroup = Group.builder().build();
        System.out.println(">>> AOP: groupBefore = " + groupBefore.getName());
        System.out.println(">>> AOP: groupAfter = " + dummyGroup.getName());
        auditService.insertAudit(userId, CRUDAction.DELETE, groupId, groupBefore, dummyGroup);
        System.out.println(">>> AOP: Audit group update");
        return result;
    }
}
