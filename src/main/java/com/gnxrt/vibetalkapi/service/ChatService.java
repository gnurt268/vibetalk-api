package com.gnxrt.vibetalkapi.service;

import com.gnxrt.vibetalkapi.exception.ChatException;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.Chat;
import com.gnxrt.vibetalkapi.dto.request.GroupChatRequest;

import java.util.List;

public interface ChatService {

    public Chat createPrivateChat(String jwt, Integer otherUserId) throws UserException, ChatException;

    public Chat createGroupChat(GroupChatRequest request, String jwt) throws UserException, ChatException;

    public Chat findChatById(Integer chatId) throws ChatException;

    public Chat findPrivateChatBetweenUsers(String jwt, Integer otherUserId) throws UserException;

    public List<Chat> getUserChats(String jwt) throws UserException;

    public List<Chat> getUserGroupChats(String jwt) throws UserException;

    public List<Chat> getUserPrivateChats(String jwt) throws UserException;

    public boolean hasAccessToChat(Integer chatId, String jwt) throws UserException, ChatException;

    public boolean isGroupAdmin(Integer chatId, String jwt) throws UserException, ChatException;

    public boolean isGroupOwner(Integer chatId, String jwt) throws UserException, ChatException;

    public Chat addUserToGroup(Integer chatId, Integer userId, String jwt) throws UserException, ChatException;

    public Chat removeUserFromGroup(Integer chatId, Integer userId, String jwt) throws UserException, ChatException;

    public Chat addAdminToGroup(Integer chatId, Integer userId, String jwt) throws UserException, ChatException;

    public Chat removeAdminFromGroup(Integer chatId, Integer userId, String jwt) throws UserException, ChatException;

    public List<Chat> getGroupChatsWhereUserIsAdmin(String jwt) throws UserException;

    public Chat renameGroup(Integer chatId, String groupName, String jwt) throws UserException, ChatException;

    public Chat updateGroupImage(Integer chatId, String imageUrl, String jwt) throws UserException, ChatException;

    public Chat transferGroupOwnership(Integer chatId, Integer newOwnerId, String jwt) throws UserException, ChatException;

    public Chat leaveChat(Integer chatId, String jwt) throws UserException, ChatException;

    public void deleteChat(Integer chatId, String jwt) throws UserException, ChatException;

    public void markChatAsRead(Integer chatId, String jwt) throws UserException, ChatException;

    public int getUnreadMessageCount(Integer chatId, String jwt) throws UserException, ChatException;

    public List<Chat> searchChats(String query, String jwt) throws UserException;

}