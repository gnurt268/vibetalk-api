package com.gnxrt.vibetalkapi.dto.request;

import com.gnxrt.vibetalkapi.model.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class SendMessageRequest {

    @NotBlank(message = "Message content is required")
    private String content;

    @NotNull(message = "Chat ID is required")
    private Integer chatId;

    private MessageType messageType = MessageType.TEXT;

    private String clientMessageId;
}