package com.gnxrt.vibetalkapi.controller;

import com.gnxrt.vibetalkapi.exception.ChatException;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.Chat;
import com.gnxrt.vibetalkapi.dto.request.GroupChatRequest;
import com.gnxrt.vibetalkapi.dto.response.ApiResponse;
import com.gnxrt.vibetalkapi.service.ChatService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.gnxrt.vibetalkapi.config.JwtConstant.JWT_HEADER;

@RestController
@RequestMapping("/api/chats")
@Tag(name = "Chats", description = "Chat management endpoints")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping("/private/{userId}")
    public ResponseEntity<Chat> createPrivateChat(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer userId) throws UserException, ChatException {

        Chat chat = chatService.createPrivateChat(jwt, userId);
        return new ResponseEntity<>(chat, HttpStatus.CREATED);
    }

    @PostMapping("/group")
    public ResponseEntity<Chat> createGroupChat(
            @RequestHeader(JWT_HEADER) String jwt,
            @Valid @RequestBody GroupChatRequest request) throws UserException, ChatException {

        Chat groupChat = chatService.createGroupChat(request, jwt);
        return new ResponseEntity<>(groupChat, HttpStatus.CREATED);
    }

    @GetMapping("/{chatId}")
    public ResponseEntity<Chat> getChatById(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws ChatException, UserException {

        if (!chatService.hasAccessToChat(chatId, jwt)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        Chat chat = chatService.findChatById(chatId);
        return new ResponseEntity<>(chat, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<Chat>> getUserChats(
            @RequestHeader(JWT_HEADER) String jwt) throws UserException {

        List<Chat> chats = chatService.getUserChats(jwt);
        return new ResponseEntity<>(chats, HttpStatus.OK);
    }

    @GetMapping("/groups")
    public ResponseEntity<List<Chat>> getUserGroupChats(
            @RequestHeader(JWT_HEADER) String jwt) throws UserException {

        List<Chat> groupChats = chatService.getUserGroupChats(jwt);
        return new ResponseEntity<>(groupChats, HttpStatus.OK);
    }

    @GetMapping("/private")
    public ResponseEntity<List<Chat>> getUserPrivateChats(
            @RequestHeader(JWT_HEADER) String jwt) throws UserException {

        List<Chat> privateChats = chatService.getUserPrivateChats(jwt);
        return new ResponseEntity<>(privateChats, HttpStatus.OK);
    }

    @GetMapping("/admin-groups")
    public ResponseEntity<List<Chat>> getGroupChatsWhereUserIsAdmin(
            @RequestHeader(JWT_HEADER) String jwt) throws UserException {

        List<Chat> adminChats = chatService.getGroupChatsWhereUserIsAdmin(jwt);
        return new ResponseEntity<>(adminChats, HttpStatus.OK);
    }

    @GetMapping("/private/find/{otherUserId}")
    public ResponseEntity<Chat> findPrivateChat(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer otherUserId) throws UserException {

        Chat privateChat = chatService.findPrivateChatBetweenUsers(jwt, otherUserId);

        if (privateChat != null) {
            return new ResponseEntity<>(privateChat, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<Chat>> searchChats(
            @RequestHeader(JWT_HEADER) String jwt,
            @RequestParam String query) throws UserException {

        List<Chat> searchResults = chatService.searchChats(query, jwt);
        return new ResponseEntity<>(searchResults, HttpStatus.OK);
    }

    @PostMapping("/{chatId}/members/{userId}")
    public ResponseEntity<Chat> addUserToGroup(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @PathVariable Integer userId) throws UserException, ChatException {

        Chat updatedChat = chatService.addUserToGroup(chatId, userId, jwt);
        return new ResponseEntity<>(updatedChat, HttpStatus.OK);
    }

    @DeleteMapping("/{chatId}/members/{userId}")
    public ResponseEntity<Chat> removeUserFromGroup(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @PathVariable Integer userId) throws UserException, ChatException {

        Chat updatedChat = chatService.removeUserFromGroup(chatId, userId, jwt);
        return new ResponseEntity<>(updatedChat, HttpStatus.OK);
    }

    @PostMapping("/{chatId}/admins/{userId}")
    public ResponseEntity<Chat> addAdminToGroup(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @PathVariable Integer userId) throws UserException, ChatException {

        Chat updatedChat = chatService.addAdminToGroup(chatId, userId, jwt);
        return new ResponseEntity<>(updatedChat, HttpStatus.OK);
    }

    @DeleteMapping("/{chatId}/admins/{userId}")
    public ResponseEntity<Chat> removeAdminFromGroup(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @PathVariable Integer userId) throws UserException, ChatException {

        Chat updatedChat = chatService.removeAdminFromGroup(chatId, userId, jwt);
        return new ResponseEntity<>(updatedChat, HttpStatus.OK);
    }

    @PutMapping("/{chatId}/name")
    public ResponseEntity<Chat> renameGroup(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @RequestBody String groupName) throws UserException, ChatException {

        Chat updatedChat = chatService.renameGroup(chatId, groupName, jwt);
        return new ResponseEntity<>(updatedChat, HttpStatus.OK);
    }

    @PutMapping("/{chatId}/image")
    public ResponseEntity<Chat> updateGroupImage(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @RequestBody String imageUrl) throws UserException, ChatException {

        Chat updatedChat = chatService.updateGroupImage(chatId, imageUrl, jwt);
        return new ResponseEntity<>(updatedChat, HttpStatus.OK);
    }

    @PutMapping("/{chatId}/transfer-ownership/{newOwnerId}")
    public ResponseEntity<Chat> transferGroupOwnership(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId,
            @PathVariable Integer newOwnerId) throws UserException, ChatException {

        Chat updatedChat = chatService.transferGroupOwnership(chatId, newOwnerId, jwt);
        return new ResponseEntity<>(updatedChat, HttpStatus.OK);
    }

    @PostMapping("/{chatId}/leave")
    public ResponseEntity<ApiResponse> leaveChat(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws UserException, ChatException {

        chatService.leaveChat(chatId, jwt);

        ApiResponse response = new ApiResponse("Left chat successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{chatId}")
    public ResponseEntity<ApiResponse> deleteChat(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws UserException, ChatException {

        chatService.deleteChat(chatId, jwt);

        ApiResponse response = new ApiResponse("Chat deleted successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/{chatId}/mark-read")
    public ResponseEntity<ApiResponse> markChatAsRead(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws UserException, ChatException {

        chatService.markChatAsRead(chatId, jwt);

        ApiResponse response = new ApiResponse("Chat marked as read", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/{chatId}/unread-count")
    public ResponseEntity<Integer> getUnreadMessageCount(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws UserException, ChatException {

        int unreadCount = chatService.getUnreadMessageCount(chatId, jwt);
        return new ResponseEntity<>(unreadCount, HttpStatus.OK);
    }

    @GetMapping("/{chatId}/has-access")
    public ResponseEntity<Boolean> hasAccessToChat(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws UserException, ChatException {

        boolean hasAccess = chatService.hasAccessToChat(chatId, jwt);
        return new ResponseEntity<>(hasAccess, HttpStatus.OK);
    }

    @GetMapping("/{chatId}/is-admin")
    public ResponseEntity<Boolean> isGroupAdmin(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws UserException, ChatException {

        boolean isAdmin = chatService.isGroupAdmin(chatId, jwt);
        return new ResponseEntity<>(isAdmin, HttpStatus.OK);
    }

    @GetMapping("/{chatId}/is-owner")
    public ResponseEntity<Boolean> isGroupOwner(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer chatId) throws UserException, ChatException {

        boolean isOwner = chatService.isGroupOwner(chatId, jwt);
        return new ResponseEntity<>(isOwner, HttpStatus.OK);
    }
}