package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatUpdateDTO {
    private Integer chatId;
    private String eventType;
    private Integer userId;
    private LocalDateTime timestamp;
}