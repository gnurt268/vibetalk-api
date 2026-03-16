package com.gnxrt.vibetalkapi.controller;

import com.gnxrt.vibetalkapi.exception.ChatException;
import com.gnxrt.vibetalkapi.exception.MessageException;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.MessageType;
import com.gnxrt.vibetalkapi.dto.request.SendMessageRequest;
import com.gnxrt.vibetalkapi.dto.response.ApiResponse;
import com.gnxrt.vibetalkapi.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.gnxrt.vibetalkapi.config.JwtConstant.JWT_HEADER;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Message operations")
public class MessageController {

    private final MessageService messageService;

    public MessageController(MessageService messageService) {
        this.messageService = messageService;
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(
            @RequestHeader(JWT_HEADER) String jwt,
            @Valid @RequestBody SendMessageRequest request) throws UserException, ChatException {

        Message message = messageService.sendMessage(
                request.getContent(),
                request.getChatId(),
                request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT,
                jwt
        );

        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @GetMapping("/chat/{chatId}")
    public ResponseEntity<List<Message>> getChatMessages(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) throws UserException, ChatException {

        List<Message> messages = messageService.getChatMessages(chatId, page, size, jwt);

        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping("/{messageId}")
    public ResponseEntity<Message> getMessageById(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer messageId) throws UserException, MessageException, ChatException {

        Message message = messageService.getMessageById(messageId, jwt);
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @PutMapping("/{messageId}")
    public ResponseEntity<Message> editMessage(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer messageId,
            @RequestBody String newContent) throws UserException, MessageException, ChatException {

        Message updatedMessage = messageService.editMessage(messageId, newContent, jwt);
        return new ResponseEntity<>(updatedMessage, HttpStatus.OK);
    }

    @DeleteMapping("/{messageId}")
    public ResponseEntity<ApiResponse> deleteMessage(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer messageId) throws UserException, MessageException, ChatException {

        messageService.deleteMessage(messageId, jwt);

        ApiResponse response = new ApiResponse("Message deleted successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{messageId}/for-me")
    public ResponseEntity<ApiResponse> deleteMessageForMe(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer messageId) throws UserException, MessageException, ChatException {

        messageService.deleteMessageForMe(messageId, jwt);

        ApiResponse response = new ApiResponse("Message deleted for you", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/chat/{chatId}/search")
    public ResponseEntity<List<Message>> searchMessagesInChat(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @RequestParam String query) throws UserException, ChatException {

        List<Message> messages = messageService.searchMessagesInChat(chatId, query, jwt);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @PostMapping("/{messageId}/forward/{targetChatId}")
    public ResponseEntity<Message> forwardMessage(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer messageId,
            @PathVariable Integer targetChatId) throws UserException, MessageException, ChatException {

        Message forwardedMessage = messageService.forwardMessage(messageId, targetChatId, jwt);
        return new ResponseEntity<>(forwardedMessage, HttpStatus.CREATED);
    }

    @GetMapping("/chat/{chatId}/since")
    public ResponseEntity<List<Message>> getMessagesSince(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @RequestParam String lastMessageTime) throws UserException, ChatException {

        List<Message> messages = messageService.getMessagesSince(chatId, lastMessageTime, jwt);
        return new ResponseEntity<>(messages, HttpStatus.OK);
    }

    @GetMapping("/chat/{chatId}/count")
    public ResponseEntity<Integer> getMessageCount(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws UserException, ChatException {

        int count = messageService.getMessageCount(chatId, jwt);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @GetMapping("/{messageId}/can-edit")
    public ResponseEntity<Boolean> canUserEditMessage(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer messageId) throws UserException, MessageException {

        boolean canEdit = messageService.canUserEditMessage(messageId, jwt);
        return new ResponseEntity<>(canEdit, HttpStatus.OK);
    }

    @GetMapping("/{messageId}/can-delete")
    public ResponseEntity<Boolean> canUserDeleteMessage(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer messageId) throws UserException, MessageException {

        boolean canDelete = messageService.canUserDeleteMessage(messageId, jwt);
        return new ResponseEntity<>(canDelete, HttpStatus.OK);
    }
}