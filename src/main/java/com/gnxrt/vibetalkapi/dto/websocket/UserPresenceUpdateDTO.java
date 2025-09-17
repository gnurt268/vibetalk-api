package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserPresenceUpdateDTO {
    private Integer userId;
    private String username;
    private String status;
    private LocalDateTime timestamp;
}
