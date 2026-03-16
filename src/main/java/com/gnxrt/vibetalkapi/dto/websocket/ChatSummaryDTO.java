package com.gnxrt.vibetalkapi.dto.websocket;

import com.gnxrt.vibetalkapi.model.Chat;
import com.gnxrt.vibetalkapi.model.Message;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatSummaryDTO {

    private Chat chat;
    private LastMessageDTO lastMessage;
    private int unreadCount;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LastMessageDTO {
        private Integer id;
        private String content;
        private String messageType;
        private String createdAt;
        private Integer senderId;
        private String senderName;
    }

    public static LastMessageDTO fromMessage(Message message) {
        if (message == null) return null;
        return new LastMessageDTO(
                message.getId(),
                message.getContent(),
                message.getMessageType().name(),
                message.getCreatedAt().toString(),
                message.getSender().getId(),
                message.getSender().getFullName() != null
                        ? message.getSender().getFullName()
                        : message.getSender().getUsername()
        );
    }
}