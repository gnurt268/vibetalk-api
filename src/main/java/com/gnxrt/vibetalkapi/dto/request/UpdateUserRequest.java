package com.gnxrt.vibetalkapi.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UpdateUserRequest {

    private String fullName;
    private String urlAvatar;
    private String currentPassword;
    private String newPassword;
}
