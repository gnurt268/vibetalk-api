package com.gnxrt.vibetalkapi.service.impl;

import com.gnxrt.vibetalkapi.exception.ChatException;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.Chat;
import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.MessageReadStatus;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.repository.ChatRepository;
import com.gnxrt.vibetalkapi.repository.MessageReadStatusRepository;
import com.gnxrt.vibetalkapi.repository.MessageRepository;
import com.gnxrt.vibetalkapi.dto.request.GroupChatRequest;
import com.gnxrt.vibetalkapi.service.ChatService;
import com.gnxrt.vibetalkapi.service.NotificationService;
import com.gnxrt.vibetalkapi.service.RealtimeIntegrationService;
import com.gnxrt.vibetalkapi.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final UserService userService;
    private final MessageRepository messageRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final RealtimeIntegrationService realtimeService;
    private final NotificationService notificationService;


    public ChatServiceImpl(ChatRepository chatRepository,
                           UserService userService,
                           MessageRepository messageRepository,
                           MessageReadStatusRepository messageReadStatusRepository,
                           RealtimeIntegrationService realtimeService,
                           NotificationService notificationService) {
        this.chatRepository = chatRepository;
        this.userService = userService;
        this.messageRepository = messageRepository;
        this.messageReadStatusRepository = messageReadStatusRepository;
        this.realtimeService = realtimeService;
        this.notificationService = notificationService;
    }

    @Override
    public Chat createPrivateChat(String jwt, Integer otherUserId) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        User otherUser = userService.findUserById(otherUserId);

        if (currentUser.getId().equals(otherUserId)) {
            throw new ChatException("Cannot create chat with yourself");
        }

        Chat existingChat = chatRepository.findPrivateChatByUsers(currentUser, otherUser);
        if (existingChat != null) {
            return existingChat;
        }

        Chat newChat = new Chat();
        newChat.setChatName(otherUser.getFullName());
        newChat.setGroupChat(false);
        newChat.setCreatedBy(currentUser);

        Set<User> members = new HashSet<>();
        members.add(currentUser);
        members.add(otherUser);
        newChat.setMembers(members);

        return chatRepository.save(newChat);
    }

    @Override
    public Chat createGroupChat(GroupChatRequest request, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);

        if (request.getChatName() == null || request.getChatName().trim().isEmpty()) {
            throw new ChatException("Group name is required");
        }

        if (request.getUserIds() == null || request.getUserIds().size() < 2) {
            throw new ChatException("Group chat must have at least 2 members besides creator");
        }

        Chat groupChat = new Chat();
        groupChat.setChatName(request.getChatName().trim());
        groupChat.setChatImage(request.getChatImage());
        groupChat.setGroupChat(true);
        groupChat.setCreatedBy(currentUser);

        Set<User> members = new HashSet<>();
        members.add(currentUser);

        for (Integer userId : request.getUserIds()) {
            if (!userId.equals(currentUser.getId())) {
                User member = userService.findUserById(userId);
                members.add(member);
            }
        }

        groupChat.setMembers(members);

        Set<User> admins = new HashSet<>();
        admins.add(currentUser);
        groupChat.setAdmins(admins);

        Chat savedChat = chatRepository.save(groupChat);

        realtimeService.broadcastChatCreated(savedChat, currentUser);

        return savedChat;
    }

    @Override
    @Transactional(readOnly = true)
    public Chat findChatById(Integer chatId) throws ChatException {
        Optional<Chat> chat = chatRepository.findById(chatId);
        if (chat.isPresent()) {
            return chat.get();
        }
        throw new ChatException("Chat not found with id: " + chatId);
    }

    @Override
    @Transactional(readOnly = true)
    public Chat findPrivateChatBetweenUsers(String jwt, Integer otherUserId) throws UserException {
        User currentUser = userService.findUserProfile(jwt);
        User otherUser = userService.findUserById(otherUserId);

        return chatRepository.findPrivateChatByUsers(currentUser, otherUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getUserChats(String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        return chatRepository.findChatsByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getUserGroupChats(String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        return chatRepository.findGroupChatsByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getUserPrivateChats(String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        return chatRepository.findPrivateChatsByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> getGroupChatsWhereUserIsAdmin(String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        return chatRepository.findGroupChatsWhereUserIsAdmin(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasAccessToChat(Integer chatId, String jwt) throws UserException, ChatException {
        User user = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        return chat.getMembers().contains(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGroupAdmin(Integer chatId, String jwt) throws UserException, ChatException {
        User user = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        if (!chat.isGroupChat()) {
            return false;
        }

        return chat.isUserAdmin(user);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isGroupOwner(Integer chatId, String jwt) throws UserException, ChatException {
        User user = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        if (!chat.isGroupChat()) {
            return false;
        }

        return chat.isOwner(user);
    }

    @Override
    public Chat addUserToGroup(Integer chatId, Integer userId, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);
        User userToAdd = userService.findUserById(userId);

        if (!chat.isGroupChat()) {
            throw new ChatException("Cannot add user to private chat");
        }

        if (!isGroupAdmin(chatId, jwt)) {
            throw new ChatException("Only group admin can add members");
        }

        if (chat.getMembers().contains(userToAdd)) {
            throw new ChatException("User is already a member of this group");
        }

        chat.getMembers().add(userToAdd);
        Chat updatedChat = chatRepository.save(chat);

        realtimeService.broadcastUserAddedToChat(updatedChat, userToAdd, currentUser);
        notificationService.notifyAddedToGroup(userToAdd, currentUser, updatedChat);

        return updatedChat;
    }

    @Override
    public Chat removeUserFromGroup(Integer chatId, Integer userId, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);
        User userToRemove = userService.findUserById(userId);

        if (!chat.isGroupChat()) {
            throw new ChatException("Cannot remove user from private chat");
        }

        if (!chat.getMembers().contains(userToRemove)) {
            throw new ChatException("User is not a member of this group");
        }

        boolean isAdmin = isGroupAdmin(chatId, jwt);
        boolean isSelfRemoval = currentUser.getId().equals(userId);
        boolean removingOwner = chat.isOwner(userToRemove);

        if (!isAdmin && !isSelfRemoval) {
            throw new ChatException("You can only remove yourself from the group");
        }

        if (removingOwner && !isSelfRemoval) {
            throw new ChatException("Cannot remove group owner. Transfer ownership first");
        }

        chat.getMembers().remove(userToRemove);
        chat.getAdmins().remove(userToRemove);

        if (removingOwner && isSelfRemoval && !chat.getMembers().isEmpty()) {
            User newOwner = chat.getAdmins().stream().findFirst()
                    .orElse(chat.getMembers().iterator().next());
            chat.setCreatedBy(newOwner);
            if (!chat.getAdmins().contains(newOwner)) {
                chat.getAdmins().add(newOwner);
            }
        }

        Chat updatedChat = chatRepository.save(chat);

        if (isSelfRemoval) {
            realtimeService.broadcastUserLeftGroup(updatedChat, userToRemove);
            notificationService.notifyUserLeftGroup(userToRemove, updatedChat);
        } else {
            realtimeService.broadcastUserRemovedFromChat(updatedChat, userToRemove, currentUser);
            notificationService.notifyRemovedFromGroup(userToRemove, updatedChat);
        }

        return updatedChat;
    }

    @Override
    public Chat addAdminToGroup(Integer chatId, Integer userId, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);
        User userToPromote = userService.findUserById(userId);

        if (!chat.isGroupChat()) {
            throw new ChatException("Cannot add admin to private chat");
        }

        if (!isGroupOwner(chatId, jwt)) {
            throw new ChatException("Only group owner can add admins");
        }

        if (!chat.getMembers().contains(userToPromote)) {
            throw new ChatException("User must be a member to become admin");
        }

        if (chat.getAdmins().contains(userToPromote)) {
            throw new ChatException("User is already an admin");
        }

        chat.addAdmin(userToPromote);
        Chat updatedChat = chatRepository.save(chat);

        realtimeService.broadcastAdminPromoted(updatedChat, userToPromote, currentUser);
        notificationService.notifyPromotedToAdmin(userToPromote, updatedChat);

        return updatedChat;
    }

    @Override
    public Chat removeAdminFromGroup(Integer chatId, Integer userId, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);
        User userToDemote = userService.findUserById(userId);

        if (!chat.isGroupChat()) {
            throw new ChatException("Cannot remove admin from private chat");
        }

        if (!isGroupOwner(chatId, jwt)) {
            throw new ChatException("Only group owner can remove admins");
        }

        if (chat.isOwner(userToDemote)) {
            throw new ChatException("Cannot remove owner admin status. Transfer ownership first");
        }

        if (!chat.getAdmins().contains(userToDemote)) {
            throw new ChatException("User is not an admin");
        }

        chat.removeAdmin(userToDemote);
        Chat updatedChat = chatRepository.save(chat);

        realtimeService.broadcastAdminDemoted(updatedChat, userToDemote, currentUser);

        return updatedChat;
    }

    @Override
    public Chat renameGroup(Integer chatId, String groupName, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        if (!chat.isGroupChat()) {
            throw new ChatException("Cannot rename private chat");
        }

        if (!isGroupAdmin(chatId, jwt)) {
            throw new ChatException("Only group admin can rename group");
        }

        if (groupName == null || groupName.trim().isEmpty()) {
            throw new ChatException("Group name cannot be empty");
        }

        String oldName = chat.getChatName();
        chat.setChatName(groupName.trim());
        Chat updatedChat = chatRepository.save(chat);

        realtimeService.broadcastChatRenamed(updatedChat, currentUser, oldName);

        return updatedChat;
    }

    @Override
    public Chat updateGroupImage(Integer chatId, String imageUrl, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        if (!chat.isGroupChat()) {
            throw new ChatException("Cannot update image for private chat");
        }

        if (!isGroupAdmin(chatId, jwt)) {
            throw new ChatException("Only group admin can update group image");
        }

        chat.setChatImage(imageUrl);
        return chatRepository.save(chat);
    }

    @Override
    public Chat transferGroupOwnership(Integer chatId, Integer newOwnerId, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);
        User newOwner = userService.findUserById(newOwnerId);

        if (!chat.isGroupChat()) {
            throw new ChatException("Cannot transfer ownership of private chat");
        }

        if (!isGroupOwner(chatId, jwt)) {
            throw new ChatException("Only current owner can transfer ownership");
        }

        if (!chat.getMembers().contains(newOwner)) {
            throw new ChatException("New owner must be a member of the group");
        }

        if (chat.isOwner(newOwner)) {
            throw new ChatException("User is already the owner");
        }

        chat.setCreatedBy(newOwner);
        if (!chat.getAdmins().contains(newOwner)) {
            chat.getAdmins().add(newOwner);
        }

        Chat updatedChat = chatRepository.save(chat);

        realtimeService.broadcastOwnershipTransferred(updatedChat, newOwner, currentUser);

        return updatedChat;
    }

    @Override
    public Chat leaveChat(Integer chatId, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        if (!chat.getMembers().contains(currentUser)) {
            throw new ChatException("You are not a member of this chat");
        }

        if (chat.isGroupChat()) {
            return removeUserFromGroup(chatId, currentUser.getId(), jwt);
        } else {
            throw new ChatException("Cannot leave private chat. Use delete instead");
        }
    }

    @Override
    public void deleteChat(Integer chatId, String jwt) throws UserException, ChatException {
        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        if (!hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        if (chat.isGroupChat()) {
            if (!isGroupOwner(chatId, jwt)) {
                throw new ChatException("Only group owner can delete group chat");
            }
            chatRepository.delete(chat);
        } else {
            chat.getMembers().remove(currentUser);

            if (chat.getMembers().isEmpty()) {
                chatRepository.delete(chat);
            } else {
                chatRepository.save(chat);
            }
        }
    }

    @Override
    @Transactional
    public void markChatAsRead(Integer chatId, String jwt) throws UserException, ChatException {
        if (!hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        List<Message> unreadMessages = messageRepository.findUnreadMessagesByUserInChat(chat, currentUser);

        for (Message message : unreadMessages) {
            if (!messageReadStatusRepository.existsByMessageAndUser(message, currentUser)) {
                MessageReadStatus readStatus = new MessageReadStatus(message, currentUser);
                messageReadStatusRepository.save(readStatus);
            }
        }

        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public int getUnreadMessageCount(Integer chatId, String jwt) throws UserException, ChatException {
        if (!hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        User currentUser = userService.findUserProfile(jwt);
        Chat chat = findChatById(chatId);

        return messageRepository.countUnreadMessagesByUserInChat(chat, currentUser);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Chat> searchChats(String query, String jwt) throws UserException {
        User user = userService.findUserProfile(jwt);
        List<Chat> userChats = chatRepository.findChatsByUser(user);

        return userChats.stream()
                .filter(chat -> chat.getChatName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
    }

}