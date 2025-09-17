package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class TypingIndicatorDTO {
    private Integer userId;
    private String username;
    private Integer chatId;
    private String status;
    private LocalDateTime timestamp;
}
