package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReadReceiptDTO {
    private Integer messageId;
    private Integer readBy;
    private String readByUsername;
    private LocalDateTime readAt;
}
