package com.gnxrt.vibetalkapi.dto.websocket;

import com.gnxrt.vibetalkapi.model.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class NotificationDTO {
    private Integer id;
    private NotificationType type;
    private String title;
    private String content;
    private Integer chatId;
    private Integer messageId;
    private LocalDateTime timestamp;
}
