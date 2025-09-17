package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatEventDTO {
    private Integer chatId;
    private String chatName;
    private String eventType;
    private Integer userId;
    private String username;
    private LocalDateTime timestamp;
    private Integer targetUserId;
    private String targetUsername;
    private String oldValue;

    public ChatEventDTO(Integer chatId, String chatName, String eventType, Integer userId, String username, LocalDateTime timestamp) {
        this.chatId = chatId;
        this.chatName = chatName;
        this.eventType = eventType;
        this.userId = userId;
        this.username = username;
        this.timestamp = timestamp;
    }
}