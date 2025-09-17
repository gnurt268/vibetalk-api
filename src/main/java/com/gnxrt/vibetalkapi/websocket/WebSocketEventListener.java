package com.gnxrt.vibetalkapi.websocket;

import com.gnxrt.vibetalkapi.dto.websocket.UserPresenceUpdate;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.service.UserPresenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SimpMessageSendingOperations messagingTemplate;
    private final UserPresenceService userPresenceService;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate,
                                  UserPresenceService userPresenceService) {
        this.messagingTemplate = messagingTemplate;
        this.userPresenceService = userPresenceService;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        User user = (User) headerAccessor.getSessionAttributes().get("user");

        if (user != null) {
            logger.info("User connected: {} with session: {}", user.getUsername(), headerAccessor.getSessionId());

            userPresenceService.markUserOnline(user, headerAccessor.getSessionId());

            UserPresenceUpdate presenceUpdate = new UserPresenceUpdate(
                    user.getId(), user.getUsername(), "ONLINE", System.currentTimeMillis()
            );
            messagingTemplate.convertAndSend("/topic/presence", presenceUpdate);
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        User user = (User) headerAccessor.getSessionAttributes().get("user");

        if (user != null) {
            logger.info("User disconnected: {} with session: {}", user.getUsername(), headerAccessor.getSessionId());

            userPresenceService.markUserOffline(user, headerAccessor.getSessionId());

            if (!userPresenceService.isUserOnline(user.getId())) {
                UserPresenceUpdate presenceUpdate = new UserPresenceUpdate(
                        user.getId(), user.getUsername(), "OFFLINE", System.currentTimeMillis()
                );
                messagingTemplate.convertAndSend("/topic/presence", presenceUpdate);
            }
        }
    }

}