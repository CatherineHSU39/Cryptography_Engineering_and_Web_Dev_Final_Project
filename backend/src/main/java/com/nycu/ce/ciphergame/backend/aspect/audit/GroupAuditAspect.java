package com.nycu.ce.ciphergame.backend.aspect.audit;

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
import com.nycu.ce.ciphergame.backend.dto.group.GroupResponse;
import com.nycu.ce.ciphergame.backend.entity.Group;
import com.nycu.ce.ciphergame.backend.entity.id.GroupId;
import com.nycu.ce.ciphergame.backend.entity.id.UserId;
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
        UserId userId = UserId.fromString(jwt.getSubject());
        ResponseEntity<GroupResponse> response = (ResponseEntity<GroupResponse>) result;

        if (!response.getStatusCode().is2xxSuccessful()) {
            return;
        }

        GroupResponse responseBody = response.getBody();
        GroupId groupId = GroupId.fromUUID(responseBody.getId());
        String groupName = responseBody.getName();

        Group newGroup = new Group(groupName);
        Group dummyGroup = new Group();
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
        UserId userId = UserId.fromString(jwt.getSubject());
        GroupId groupId = (GroupId) joinPoint.getArgs()[1];
        Group groupBefore = groupDAO.getDetachedGroupById(groupId);

        Object result = joinPoint.proceed();
        ResponseEntity<GroupResponse> response = (ResponseEntity<GroupResponse>) result;

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
        UserId userId = UserId.fromString(jwt.getSubject());
        GroupId groupId = (GroupId) joinPoint.getArgs()[1];
        Group groupBefore = groupDAO.getDetachedGroupById(groupId);

        Object result = joinPoint.proceed();
        ResponseEntity<GroupResponse> response = (ResponseEntity<GroupResponse>) result;

        if (!response.getStatusCode().is2xxSuccessful()) {
            return result;
        }

        Group dummyGroup = new Group();
        System.out.println(">>> AOP: groupBefore = " + groupBefore.getName());
        System.out.println(">>> AOP: groupAfter = " + dummyGroup.getName());
        auditService.insertAudit(userId, CRUDAction.DELETE, groupId, groupBefore, dummyGroup);
        System.out.println(">>> AOP: Audit group update");
        return result;
    }
}
