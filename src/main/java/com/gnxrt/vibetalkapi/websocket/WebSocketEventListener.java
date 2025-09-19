package com.gnxrt.vibetalkapi.websocket;

import com.gnxrt.vibetalkapi.dto.websocket.UserPresenceUpdate;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.repository.UserRepository;
import com.gnxrt.vibetalkapi.service.UserPresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.Optional;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserPresenceService userPresenceService;
    private final UserRepository userRepository;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate,
                                  UserPresenceService userPresenceService,
                                  UserRepository userRepository) {
        this.messagingTemplate = messagingTemplate;
        this.userPresenceService = userPresenceService;
        this.userRepository = userRepository;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        if (headerAccessor.getUser() instanceof UsernamePasswordAuthenticationToken) {
            UsernamePasswordAuthenticationToken auth =
                    (UsernamePasswordAuthenticationToken) headerAccessor.getUser();

            User user = null;

            try {
                if (headerAccessor.getSessionAttributes() != null) {
                    user = (User) headerAccessor.getSessionAttributes().get("user");
                }
            } catch (Exception e) {
                logger.debug("Could not get user from session attributes", e);
            }

            if (user == null && auth.getPrincipal() instanceof String) {
                String userId = (String) auth.getPrincipal();
                try {
                    Integer userIdInt = Integer.parseInt(userId);
                    Optional<User> userOpt = userRepository.findById(userIdInt);
                    if (userOpt.isPresent()) {
                        user = userOpt.get();
                    }
                } catch (NumberFormatException e) {
                    logger.error("Invalid user ID format: {}", userId);
                }
            }

            if (user != null) {
                logger.info("User connected: {} (ID: {}) with session: {}",
                        user.getUsername(), user.getId(), headerAccessor.getSessionId());

                userPresenceService.markUserOnline(user, headerAccessor.getSessionId());

                UserPresenceUpdate presenceUpdate = new UserPresenceUpdate(
                        user.getId(),
                        user.getUsername(),
                        "ONLINE",
                        System.currentTimeMillis()
                );
                messagingTemplate.convertAndSend("/topic/presence", presenceUpdate);
            } else {
                logger.warn("Could not identify user for WebSocket connection with session: {}",
                        headerAccessor.getSessionId());
            }
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        User user = null;

        try {
            if (headerAccessor.getSessionAttributes() != null) {
                user = (User) headerAccessor.getSessionAttributes().get("user");
            }
        } catch (Exception e) {
            logger.debug("Could not get user from session attributes on disconnect", e);
        }

        if (user != null) {
            logger.info("User disconnected: {} (ID: {}) with session: {}",
                    user.getUsername(), user.getId(), headerAccessor.getSessionId());

            userPresenceService.markUserOffline(user, headerAccessor.getSessionId());

            if (!userPresenceService.isUserOnline(user.getId())) {
                UserPresenceUpdate presenceUpdate = new UserPresenceUpdate(
                        user.getId(),
                        user.getUsername(),
                        "OFFLINE",
                        System.currentTimeMillis()
                );
                messagingTemplate.convertAndSend("/topic/presence", presenceUpdate);
            }
        } else {
            logger.warn("Could not identify user for WebSocket disconnection with session: {}",
                    headerAccessor.getSessionId());
        }
    }
}