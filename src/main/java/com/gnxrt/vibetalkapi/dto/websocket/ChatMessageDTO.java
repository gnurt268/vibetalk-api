package com.gnxrt.vibetalkapi.dto.websocket;
import com.gnxrt.vibetalkapi.model.MessageType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ChatMessageDTO {
    private String content;
    private Integer chatId;
    private MessageType messageType;
    private String token;

}
