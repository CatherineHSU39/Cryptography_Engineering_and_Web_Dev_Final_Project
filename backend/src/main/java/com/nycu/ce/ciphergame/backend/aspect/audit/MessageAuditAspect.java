// package com.nycu.ce.ciphergame.backend.aspect.audit;

// import java.util.UUID;

// import org.aspectj.lang.JoinPoint;
// import org.aspectj.lang.ProceedingJoinPoint;
// import org.aspectj.lang.annotation.AfterReturning;
// import org.aspectj.lang.annotation.Around;
// import org.aspectj.lang.annotation.Aspect;
// import org.aspectj.lang.annotation.Pointcut;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.http.ResponseEntity;
// import org.springframework.security.oauth2.jwt.Jwt;
// import org.springframework.stereotype.Component;

// import com.nycu.ce.ciphergame.backend.dao.MessageDAO;
// import com.nycu.ce.ciphergame.backend.dto.group.GroupResponse;
// import com.nycu.ce.ciphergame.backend.dto.message.MessageResponse;
// import com.nycu.ce.ciphergame.backend.entity.Message;
// import com.nycu.ce.ciphergame.backend.service.audit.AuditService;
// import com.nycu.ce.ciphergame.backend.util.CRUDAction;

// @Aspect
// @Component
// public class MessageAuditAspect {

//     @Autowired
//     AuditService auditService;

//     @Autowired
//     MessageDAO messageDAO;

//     @Pointcut("execution(* com.nycu.ce.ciphergame.backend.controller.MessageController.createMessage(..))")
//     public void messageCreateMethods() {
//     }

//     @AfterReturning(pointcut = "messageCreateMethods()", returning = "result")
//     public void aroundCreateMessage(JoinPoint joinPoint, Object result) {
//         Jwt jwt = (Jwt) joinPoint.getArgs()[0];
//         UUID userId = UUID.fromString(jwt.getSubject());
//         ResponseEntity<MessageResponse> response = (ResponseEntity<MessageResponse>) result;

//         if (!response.getStatusCode().is2xxSuccessful()) {
//             return;
//         }

//         MessageResponse responseBody = response.getBody();
//         UUID messageId = responseBody.getMessageId();
//         String encryptedMessage = responseBody.getEncryptedMessage();

//         Message newMessage = Message.builder()
//                 .encryptedMessage(encryptedMessage)
//                 .build();
//         Message dummyMessage = Message.builder().build();
//         System.out.println(">>> AOP: messageBefore = null");
//         System.out.println(">>> AOP: messageAfter = " + encryptedMessage);
//         auditService.insertAudit(userId, CRUDAction.CREATE, messageId, dummyMessage, newMessage);
//         System.out.println(">>> AOP: Audit message create");
//     }

//     @Pointcut("execution(* com.nycu.ce.ciphergame.backend.controller.MessageController.updateMessage(..))")
//     public void messageUpdateMethods() {
//     }

//     @Around("messageUpdateMethods()")
//     public Object aroundUpdateMessage(ProceedingJoinPoint joinPoint) throws Throwable {
//         Jwt jwt = (Jwt) joinPoint.getArgs()[0];
//         UUID userId = UUID.fromString(jwt.getSubject());
//         UUID messageId = (UUID) joinPoint.getArgs()[2];
//         Message messageBefore = messageDAO.getDetachedMessageById(messageId);

//         Object result = joinPoint.proceed();
//         ResponseEntity<GroupResponse> response = (ResponseEntity<GroupResponse>) result;

//         if (!response.getStatusCode().is2xxSuccessful()) {
//             return result;
//         }

//         Message messageAfter = messageDAO.getDetachedMessageById(messageId);
//         System.out.println(">>> AOP: messageBefore = " + messageBefore.getEncryptedMessage());
//         System.out.println(">>> AOP: messageAfter = " + messageAfter.getEncryptedMessage());
//         auditService.insertAudit(userId, CRUDAction.UPDATE, messageId, messageBefore, messageAfter);
//         System.out.println(">>> AOP: Audit message update");
//         return result;
//     }

//     @Pointcut("execution(* com.nycu.ce.ciphergame.backend.controller.MessageController.deleteMessage(..))")
//     public void messageDeleteMethods() {
//     }

//     @Around("messageDeleteMethods()")
//     public Object aroundDeleteMessage(ProceedingJoinPoint joinPoint) throws Throwable {
//         Jwt jwt = (Jwt) joinPoint.getArgs()[0];
//         UUID userId = UUID.fromString(jwt.getSubject());
//         UUID messageId = (UUID) joinPoint.getArgs()[2];
//         Message messageBefore = messageDAO.getDetachedMessageById(messageId);

//         Object result = joinPoint.proceed();
//         ResponseEntity<GroupResponse> response = (ResponseEntity<GroupResponse>) result;

//         if (!response.getStatusCode().is2xxSuccessful()) {
//             return result;
//         }

//         Message dummyMessage = Message.builder().build();
//         System.out.println(">>> AOP: messageBefore = " + messageBefore.getEncryptedMessage());
//         System.out.println(">>> AOP: messageAfter = " + dummyMessage.getEncryptedMessage());
//         auditService.insertAudit(userId, CRUDAction.DELETE, messageId, messageBefore, dummyMessage);
//         System.out.println(">>> AOP: Audit message update");
//         return result;
//     }
// }
