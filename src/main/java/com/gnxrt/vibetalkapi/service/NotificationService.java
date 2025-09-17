package com.gnxrt.vibetalkapi.service;

import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.*;

import java.util.List;

public interface NotificationService {

    public List<Notification> getUserNotifications(String jwt) throws UserException;

    public List<Notification> getUnreadNotifications(String jwt) throws UserException;

    public int getUnreadCount(String jwt) throws UserException;

    public void markAsRead(Integer notificationId, String jwt) throws UserException;

    public void markAllAsRead(String jwt) throws UserException;

    public void deleteNotification(Integer notificationId, String jwt) throws UserException;

    public void notifyNewMessage(Message message);

    public void notifyAddedToGroup(User addedUser, User addedBy, Chat chat);

    public void notifyRemovedFromGroup(User removedUser, Chat chat);

    public void notifyUserLeftGroup(User leftUser, Chat chat);

    public void notifyPromotedToAdmin(User promotedUser, Chat chat);

    public void createSystemNotification(String title, String content, List<User> users);
}