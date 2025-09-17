package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageUpdateDTO {
    private Integer messageId;
    private String newContent;
    private String updateType;
    private LocalDateTime timestamp;
}
