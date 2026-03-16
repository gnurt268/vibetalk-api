package com.gnxrt.vibetalkapi.websocket;

import com.gnxrt.vibetalkapi.config.TokenProvider;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
@Component
public class WebSocketAuthInterceptor implements HandshakeInterceptor, ChannelInterceptor {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;

    public WebSocketAuthInterceptor(TokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                   WebSocketHandler wsHandler, Map<String, Object> attributes) throws Exception {

        String query = request.getURI().getQuery();
        if (query != null && query.contains("token=")) {
            String token = extractTokenFromQuery(query);
            if (token != null && validateToken(token)) {
                String email = tokenProvider.getEmailFromToken(token);
                User user = userRepository.findByEmail(email);
                if (user != null) {
                    attributes.put("user", user);
                    attributes.put("token", token);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                               WebSocketHandler wsHandler, Exception exception) {

    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {

            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);

                if (validateToken(token)) {
                    String email = tokenProvider.getEmailFromToken(token);
                    User user = userRepository.findByEmail(email);

                    if (user != null) {

                        UsernamePasswordAuthenticationToken authentication =
                                new UsernamePasswordAuthenticationToken(user.getId().toString(), null, null);
                        accessor.setUser(authentication);
                        accessor.getSessionAttributes().put("user", user);
                        log.debug("[WS-AUTH] Set principal via Auth header: userId={} ({})", user.getId(), user.getUsername());
                        return message;
                    }
                }
            }

            if (accessor.getUser() == null && accessor.getSessionAttributes() != null) {
                User user = (User) accessor.getSessionAttributes().get("user");
                if (user != null) {
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(user.getId().toString(), null, null);
                    accessor.setUser(authentication);
                    log.debug("[WS-AUTH] Set principal via session fallback: userId={} ({})", user.getId(), user.getUsername());
                } else {
                    log.debug("[WS-AUTH] WARNING: No user in session attributes!");
                }
            } else if (accessor.getUser() == null) {
                log.debug("[WS-AUTH] WARNING: No session attributes available!");
            }
        }

        return message;
    }

    private String extractTokenFromQuery(String query) {
        String[] params = query.split("&");
        for (String param : params) {
            if (param.startsWith("token=")) {
                return param.substring(6);
            }
        }
        return null;
    }

    private boolean validateToken(String token) {
        try {
            String email = tokenProvider.getEmailFromToken(token);
            return email != null && userRepository.findByEmail(email) != null;
        } catch (Exception e) {
            return false;
        }
    }
}