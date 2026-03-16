package com.gnxrt.vibetalkapi.controller;

import com.gnxrt.vibetalkapi.exception.ChatException;
import com.gnxrt.vibetalkapi.exception.MessageException;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.MessageType;
import com.gnxrt.vibetalkapi.dto.request.SendMessageRequest;
import com.gnxrt.vibetalkapi.dto.response.ApiResponse;
import com.gnxrt.vibetalkapi.service.CloudinaryService;
import com.gnxrt.vibetalkapi.service.MessageService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.gnxrt.vibetalkapi.config.JwtConstant.JWT_HEADER;

@RestController
@RequestMapping("/api/messages")
@Tag(name = "Messages", description = "Message operations")
public class MessageController {

    private final MessageService messageService;
    private final CloudinaryService cloudinaryService;

    private static final Set<String> IMAGE_CONTENT_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp", "image/svg+xml"
    );

    private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_FILE_SIZE = 25 * 1024 * 1024;  // 25MB

    public MessageController(MessageService messageService, CloudinaryService cloudinaryService) {
        this.messageService = messageService;
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Upload file/ảnh và gửi message trong chat.
     * Tự detect IMAGE vs FILE dựa trên content type.
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadAndSendMessage(
            @RequestHeader(JWT_HEADER) String jwt,
            @RequestParam("file") MultipartFile file,
            @RequestParam("chatId") Integer chatId,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "clientMessageId", required = false) String clientMessageId) throws UserException, ChatException, IOException {

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(new ApiResponse("File is empty", false));
        }

        String contentType = file.getContentType();
        boolean isImage = contentType != null && IMAGE_CONTENT_TYPES.contains(contentType);

        // Validate size
        long maxSize = isImage ? MAX_IMAGE_SIZE : MAX_FILE_SIZE;
        if (file.getSize() > maxSize) {
            String maxSizeMB = String.valueOf(maxSize / (1024 * 1024));
            return ResponseEntity.badRequest().body(
                    new ApiResponse("File size exceeds " + maxSizeMB + "MB limit", false));
        }

        // Upload to Cloudinary
        Map<String, String> uploadResult;
        MessageType messageType;

        if (isImage) {
            uploadResult = cloudinaryService.uploadChatImage(file);
            messageType = MessageType.IMAGE;
        } else {
            uploadResult = cloudinaryService.uploadChatFile(file);
            messageType = MessageType.FILE;
        }

        // Build content: URL + metadata
        String fileUrl = uploadResult.get("url");
        String fileName = uploadResult.get("fileName");
        String fileSize = uploadResult.get("fileSize");

        // Content format: url|fileName|fileSize|caption
        StringBuilder contentBuilder = new StringBuilder(fileUrl);
        contentBuilder.append("|").append(fileName != null ? fileName : "file");
        contentBuilder.append("|").append(fileSize != null ? fileSize : "0");
        if (caption != null && !caption.trim().isEmpty()) {
            contentBuilder.append("|").append(caption.trim());
        }

        Message message = messageService.sendMessage(
                contentBuilder.toString(),
                chatId,
                messageType,
                jwt,
                clientMessageId
        );

        return new ResponseEntity<>(message, HttpStatus.CREATED);
    }

    @PostMapping("/send")
    public ResponseEntity<Message> sendMessage(
            @RequestHeader(JWT_HEADER) String jwt,
            @Valid @RequestBody SendMessageRequest request) throws UserException, ChatException {

        Message message = messageService.sendMessage(
                request.getContent(),
                request.getChatId(),
                request.getMessageType() != null ? request.getMessageType() : MessageType.TEXT,
                jwt,
                request.getClientMessageId(),
                request.getReplyToId()
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