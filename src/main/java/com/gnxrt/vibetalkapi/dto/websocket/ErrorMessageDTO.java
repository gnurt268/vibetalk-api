package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ErrorMessageDTO {
    private String error;
    private LocalDateTime timestamp;

}
