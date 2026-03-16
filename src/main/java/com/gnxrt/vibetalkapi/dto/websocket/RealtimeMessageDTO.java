package com.gnxrt.vibetalkapi.dto.websocket;

import com.gnxrt.vibetalkapi.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class RealtimeMessageDTO {
    private Integer id;
    private String content;
    private Integer senderId;
    private String senderUsername;
    private String senderFullName;
    private String senderAvatar;
    private Integer chatId;
    private MessageType messageType;
    private LocalDateTime timestamp;
    private String status;
    private String clientMessageId;

    // Reply info
    private Integer replyToId;
    private String replyToContent;
    private String replyToSenderName;
    private String replyToMessageType;
}