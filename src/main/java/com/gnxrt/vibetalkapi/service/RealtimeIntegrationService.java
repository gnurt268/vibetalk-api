package com.gnxrt.vibetalkapi.service;

import com.gnxrt.vibetalkapi.dto.websocket.*;
import com.gnxrt.vibetalkapi.model.*;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class RealtimeIntegrationService {

    private final SimpMessageSendingOperations messagingTemplate;

    public RealtimeIntegrationService(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void broadcastNewMessage(Message message) {
        Chat chat = message.getChat();
        User sender = message.getSender();

        RealtimeMessageDTO realtimeMessage = new RealtimeMessageDTO();
        realtimeMessage.setId(message.getId());
        realtimeMessage.setContent(message.getContent());
        realtimeMessage.setSenderId(sender.getId());
        realtimeMessage.setSenderUsername(sender.getUsername());
        realtimeMessage.setSenderFullName(sender.getFullName());
        realtimeMessage.setSenderAvatar(sender.getUrlAvatar());
        realtimeMessage.setChatId(chat.getId());
        realtimeMessage.setMessageType(message.getMessageType());
        realtimeMessage.setTimestamp(message.getCreatedAt());
        realtimeMessage.setStatus("DELIVERED");
        realtimeMessage.setClientMessageId(message.getClientMessageId());

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSend(
                    "/topic/user/" + member.getId() + "/messages",
                    realtimeMessage
            );
        }
    }

    public void broadcastMessageRead(Message message, User readBy) {
        ReadReceiptDTO receipt = new ReadReceiptDTO(
                message.getId(),
                readBy.getId(),
                readBy.getUsername(),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSendToUser(
                message.getSender().getId().toString(),
                "/queue/read-receipts",
                receipt
        );
    }

    public void broadcastMessageEdit(Message message) {
        MessageUpdateDTO update = new MessageUpdateDTO(
                message.getId(),
                message.getContent(),
                "EDITED",
                LocalDateTime.now()
        );

        for (User member : message.getChat().getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/message-updates",
                    update
            );
        }
    }

    public void broadcastMessageDelete(Integer messageId, Chat chat, User deletedBy) {
        MessageUpdateDTO update = new MessageUpdateDTO(
                messageId,
                null,
                "DELETED",
                LocalDateTime.now()
        );

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/message-updates",
                    update
            );
        }
    }

    public void broadcastChatCreated(Chat chat, User createdBy) {
        ChatEventDTO event = new ChatEventDTO(
                chat.getId(),
                chat.getChatName(),
                "CHAT_CREATED",
                createdBy.getId(),
                createdBy.getUsername(),
                LocalDateTime.now()
        );

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/chat-events",
                    event
            );
        }
    }

    public void broadcastUserAddedToChat(Chat chat, User addedUser, User addedBy) {
        ChatEventDTO event = new ChatEventDTO(
                chat.getId(),
                chat.getChatName(),
                "USER_ADDED",
                addedBy.getId(),
                addedBy.getUsername(),
                LocalDateTime.now()
        );
        event.setTargetUserId(addedUser.getId());
        event.setTargetUsername(addedUser.getUsername());

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/chat-events",
                    event
            );
        }
    }

    public void broadcastUserLeftGroup(Chat chat, User leftUser) {
        ChatEventDTO event = new ChatEventDTO(
                chat.getId(),
                chat.getChatName(),
                "USER_LEFT_GROUP",
                leftUser.getId(),
                leftUser.getUsername(),
                LocalDateTime.now()
        );

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/chat-events",
                    event
            );
        }

        messagingTemplate.convertAndSendToUser(
                leftUser.getId().toString(),
                "/queue/chat-events",
                event
        );
    }

    public void broadcastUserRemovedFromChat(Chat chat, User removedUser, User removedBy) {
        ChatEventDTO event = new ChatEventDTO(
                chat.getId(),
                chat.getChatName(),
                "USER_REMOVED",
                removedBy.getId(),
                removedBy.getUsername(),
                LocalDateTime.now()
        );
        event.setTargetUserId(removedUser.getId());
        event.setTargetUsername(removedUser.getUsername());

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/chat-events",
                    event
            );
        }

        messagingTemplate.convertAndSendToUser(
                removedUser.getId().toString(),
                "/queue/chat-events",
                event
        );
    }

    public void broadcastChatRenamed(Chat chat, User renamedBy, String oldName) {
        ChatEventDTO event = new ChatEventDTO(
                chat.getId(),
                chat.getChatName(),
                "CHAT_RENAMED",
                renamedBy.getId(),
                renamedBy.getUsername(),
                LocalDateTime.now()
        );
        event.setOldValue(oldName);

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/chat-events",
                    event
            );
        }
    }

    public void broadcastAdminPromoted(Chat chat, User promotedUser, User promotedBy) {
        ChatEventDTO event = new ChatEventDTO(
                chat.getId(),
                chat.getChatName(),
                "ADMIN_PROMOTED",
                promotedBy.getId(),
                promotedBy.getUsername(),
                LocalDateTime.now()
        );
        event.setTargetUserId(promotedUser.getId());
        event.setTargetUsername(promotedUser.getUsername());

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/chat-events",
                    event
            );
        }
    }

    public void broadcastAdminDemoted(Chat chat, User demotedUser, User demotedBy) {
        ChatEventDTO event = new ChatEventDTO(
                chat.getId(),
                chat.getChatName(),
                "ADMIN_DEMOTED",
                demotedBy.getId(),
                demotedBy.getUsername(),
                LocalDateTime.now()
        );
        event.setTargetUserId(demotedUser.getId());
        event.setTargetUsername(demotedUser.getUsername());

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/chat-events",
                    event
            );
        }
    }

    public void broadcastOwnershipTransferred(Chat chat, User newOwner, User oldOwner) {
        ChatEventDTO event = new ChatEventDTO(
                chat.getId(),
                chat.getChatName(),
                "OWNERSHIP_TRANSFERRED",
                oldOwner.getId(),
                oldOwner.getUsername(),
                LocalDateTime.now()
        );
        event.setTargetUserId(newOwner.getId());
        event.setTargetUsername(newOwner.getUsername());

        for (User member : chat.getMembers()) {
            messagingTemplate.convertAndSendToUser(
                    member.getId().toString(),
                    "/queue/chat-events",
                    event
            );
        }
    }

    public void broadcastNewNotification(Notification notification) {
        NotificationDTO notificationDTO = new NotificationDTO(
                notification.getId(),
                notification.getType(),
                notification.getTitle(),
                notification.getContent(),
                notification.getChat() != null ? notification.getChat().getId() : null,
                notification.getMessage() != null ? notification.getMessage().getId() : null,
                notification.getCreatedAt()
        );

        messagingTemplate.convertAndSendToUser(
                notification.getUser().getId().toString(),
                "/queue/notifications",
                notificationDTO
        );

        messagingTemplate.convertAndSendToUser(
                notification.getUser().getId().toString(),
                "/queue/notification-count",
                new NotificationCountDTO(1)
        );
    }

    public void broadcastNotificationRead(Integer userId) {
        messagingTemplate.convertAndSendToUser(
                userId.toString(),
                "/queue/notification-count",
                new NotificationCountDTO(0)
        );
    }

    public void broadcastUserOnline(User user) {
        UserPresenceDTO presence = new UserPresenceDTO(
                user.getId(),
                user.getUsername(),
                "ONLINE",
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/presence", presence);
    }

    public void broadcastUserOffline(User user) {
        UserPresenceDTO presence = new UserPresenceDTO(
                user.getId(),
                user.getUsername(),
                "OFFLINE",
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/presence", presence);
    }

}