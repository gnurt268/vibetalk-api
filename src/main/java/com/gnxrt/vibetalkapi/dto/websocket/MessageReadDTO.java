package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class MessageReadDTO {
    private Integer messageId;
    private String token;
}
