package com.gnxrt.vibetalkapi.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class AuthResponse {

    private  String jwt;
    private  boolean authenticated;
}
