package com.gnxrt.vibetalkapi.dto.websocket;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserPresenceUpdate {
    private Integer userId;
    private String username;
    private String status;
    private Long timestamp;
}
