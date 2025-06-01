// package com.nycu.ce.ciphergame.backend.rls;

// import java.util.UUID;

// import org.springframework.stereotype.Service;

// @Service
// public class RlsContextService {

//     private static final ThreadLocal<UUID> currentUserId = new ThreadLocal<>();

//     public void setCurrentUserId(UUID userId) {
//         currentUserId.set(userId);
//     }

//     public UUID getCurrentUserId() {
//         return currentUserId.get();
//     }

//     public void clear() {
//         currentUserId.remove();
//     }
// }
