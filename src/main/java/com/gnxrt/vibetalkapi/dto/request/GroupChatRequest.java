package com.gnxrt.vibetalkapi.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class GroupChatRequest {

    @NotBlank(message = "Group name is required")
    @Size(max = 100, message = "Group name must not exceed 100 characters")
    private String chatName;

    private String chatImage;

    @NotEmpty(message = "At least 2 users must be added to the group")
    @Size(min = 2, message = "Group chat requires at least 2 members")
    private List<Integer> userIds;
}