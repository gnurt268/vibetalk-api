package com.gnxrt.vibetalkapi.service.impl;

import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.*;
import com.gnxrt.vibetalkapi.repository.NotificationRepository;
import com.gnxrt.vibetalkapi.service.NotificationService;
import com.gnxrt.vibetalkapi.service.RealtimeIntegrationService;
import com.gnxrt.vibetalkapi.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserService userService;
    private final RealtimeIntegrationService realtimeService;


    public NotificationServiceImpl(NotificationRepository notificationRepository,
                                   UserService userService,
                                   RealtimeIntegrationService realtimeService) {
        this.notificationRepository = notificationRepository;
        this.userService = userService;
        this.realtimeService = realtimeService;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        return notificationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> getUnreadNotifications(String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        return notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadCount(String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        return notificationRepository.countByUserAndIsReadFalse(user);
    }

    @Override
    public void markAsRead(Integer notificationId, String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new UserException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UserException("Access denied");
        }

        notification.markAsRead();
        notificationRepository.save(notification);
    }

    @Override
    public void markAllAsRead(String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        List<Notification> unreadNotifications = notificationRepository.findByUserAndIsReadFalseOrderByCreatedAtDesc(user);

        for (Notification notification : unreadNotifications) {
            notification.markAsRead();
        }

        notificationRepository.saveAll(unreadNotifications);

        realtimeService.broadcastNotificationRead(user.getId());
    }

    @Override
    public void deleteNotification(Integer notificationId, String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new UserException("Notification not found"));

        if (!notification.getUser().getId().equals(user.getId())) {
            throw new UserException("Access denied");
        }

        notificationRepository.delete(notification);
    }

    @Override
    public void notifyNewMessage(Message message) {
        Chat chat = message.getChat();
        User sender = message.getSender();

        for (User member : chat.getMembers()) {
            if (!member.getId().equals(sender.getId())) {
                String title, content;
                NotificationType type;

                if (chat.isGroupChat()) {
                    title = chat.getChatName();
                    content = sender.getUsername() + ": " + getMessagePreview(message);
                    type = NotificationType.NEW_GROUP_MESSAGE;
                } else {
                    title = sender.getUsername();
                    content = getMessagePreview(message);
                    type = NotificationType.NEW_MESSAGE;
                }

                Notification notification = new Notification(member, chat, message, type, title, content);
                Notification savedNotification = notificationRepository.save(notification);

                realtimeService.broadcastNewNotification(savedNotification);
            }
        }
    }

    @Override
    public void notifyAddedToGroup(User addedUser, User addedBy, Chat chat) {
        String title = chat.getChatName();
        String content = addedBy.getUsername() + " added you to the group";

        Notification notification = new Notification(addedUser, chat, NotificationType.ADDED_TO_GROUP, title, content);
        Notification savedNotification = notificationRepository.save(notification);

        realtimeService.broadcastNewNotification(savedNotification);
    }

    @Override
    public void notifyRemovedFromGroup(User removedUser, Chat chat) {
        String title = "Removed from group";
        String content = "You were removed from " + chat.getChatName();

        Notification notification = new Notification(removedUser, chat, NotificationType.REMOVED_FROM_GROUP, title, content);
        Notification savedNotification = notificationRepository.save(notification);

        realtimeService.broadcastNewNotification(savedNotification);
    }

    @Override
    public void notifyUserLeftGroup(User leftUser, Chat chat) {
        for (User member : chat.getMembers()) {
            if (!member.getId().equals(leftUser.getId())) {
                String title = chat.getChatName();
                String content = leftUser.getUsername() + " left the group";

                Notification notification = new Notification(member, chat, NotificationType.USER_LEFT_GROUP, title, content);
                Notification savedNotification = notificationRepository.save(notification);

                realtimeService.broadcastNewNotification(savedNotification);
            }
        }
    }

    @Override
    public void notifyPromotedToAdmin(User promotedUser, Chat chat) {
        String title = chat.getChatName();
        String content = "You are now an admin";

        Notification notification = new Notification(promotedUser, chat, NotificationType.PROMOTED_TO_ADMIN, title, content);
        Notification savedNotification = notificationRepository.save(notification);

        realtimeService.broadcastNewNotification(savedNotification);
    }

    @Override
    public void createSystemNotification(String title, String content, List<User> users) {
        for (User user : users) {
            Notification notification = new Notification(user, null, NotificationType.SYSTEM, title, content);
            notificationRepository.save(notification);
        }
    }

    private String getMessagePreview(Message message) {
        if (message.getMessageType() == MessageType.TEXT) {
            String content = message.getContent();
            return content.length() > 50 ? content.substring(0, 50) + "..." : content;
        } else {
            return switch (message.getMessageType()) {
                case IMAGE -> "📷 Photo";
                case FILE -> "📎 File";
                case SYSTEM -> "System message";
                default -> "Message";
            };
        }
    }
}