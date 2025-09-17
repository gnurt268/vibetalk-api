package com.gnxrt.vibetalkapi.service.impl;

import com.gnxrt.vibetalkapi.exception.ChatException;
import com.gnxrt.vibetalkapi.exception.MessageException;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.Chat;
import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.MessageReadStatus;
import com.gnxrt.vibetalkapi.model.MessageType;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.repository.ChatRepository;
import com.gnxrt.vibetalkapi.repository.MessageRepository;
import com.gnxrt.vibetalkapi.repository.MessageReadStatusRepository;
import com.gnxrt.vibetalkapi.service.ChatService;
import com.gnxrt.vibetalkapi.service.MessageService;
import com.gnxrt.vibetalkapi.service.RealtimeIntegrationService;
import com.gnxrt.vibetalkapi.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageReadStatusRepository messageReadStatusRepository;
    private final UserService userService;
    private final ChatService chatService;
    private final RealtimeIntegrationService realtimeService;
    private final ChatRepository chatRepository;

    public MessageServiceImpl(MessageRepository messageRepository,
                              MessageReadStatusRepository messageReadStatusRepository,
                              UserService userService,
                              ChatService chatService,
                              RealtimeIntegrationService realtimeService,
                              ChatRepository chatRepository) {
        this.messageRepository = messageRepository;
        this.messageReadStatusRepository = messageReadStatusRepository;
        this.userService = userService;
        this.chatService = chatService;
        this.realtimeService = realtimeService;
        this.chatRepository = chatRepository;
    }

    @Override
    public Message sendMessage(String content, Integer chatId, MessageType messageType, String jwt) throws UserException, ChatException {
        User sender = userService.findUserProfile(jwt);
        Chat chat = chatService.findChatById(chatId);

        if (!chatService.hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        if (content == null || content.trim().isEmpty()) {
            throw new ChatException("Message content cannot be empty");
        }

        Message message = new Message();
        message.setContent(content.trim());
        message.setSender(sender);
        message.setChat(chat);
        message.setMessageType(messageType);

        Message savedMessage = messageRepository.save(message);

        chat.setUpdatedAt(LocalDateTime.now());
        chatRepository.save(chat);

        if (realtimeService != null) {
            realtimeService.broadcastNewMessage(savedMessage);
        }

        return savedMessage;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getChatMessages(Integer chatId, String jwt) throws UserException, ChatException {
        if (!chatService.hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        Chat chat = chatService.findChatById(chatId);
        return messageRepository.findByChatOrderByCreatedAtAsc(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getChatMessages(Integer chatId, int page, int size, String jwt) throws UserException, ChatException {
        if (!chatService.hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        Chat chat = chatService.findChatById(chatId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());

        return messageRepository.findByChat(chat, pageable).getContent();
    }

    @Override
    @Transactional(readOnly = true)
    public Message getMessageById(Integer messageId, String jwt) throws UserException, MessageException, ChatException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message not found with id: " + messageId));

        if (!canUserAccessMessage(messageId, jwt)) {
            throw new ChatException("You don't have access to this message");
        }

        return message;
    }

    @Override
    public Message editMessage(Integer messageId, String newContent, String jwt) throws UserException, MessageException, ChatException {
        Message message = getMessageById(messageId, jwt);

        if (!canUserEditMessage(messageId, jwt)) {
            throw new MessageException("You can only edit your own messages");
        }

        if (newContent == null || newContent.trim().isEmpty()) {
            throw new MessageException("Message content cannot be empty");
        }

        if (message.getMessageType() != MessageType.TEXT) {
            throw new MessageException("Only text messages can be edited");
        }

        message.setContent(newContent.trim());
        Message updatedMessage = messageRepository.save(message);

        if (realtimeService != null) {
            realtimeService.broadcastMessageEdit(updatedMessage);
        }

        return updatedMessage;
    }

    @Override
    public void deleteMessage(Integer messageId, String jwt) throws UserException, MessageException, ChatException {
        Message message = getMessageById(messageId, jwt);

        if (!canUserDeleteMessage(messageId, jwt)) {
            throw new MessageException("You don't have permission to delete this message");
        }

        Chat chat = message.getChat();
        User deletedBy = userService.findUserProfile(jwt);

        messageRepository.delete(message);

        if (realtimeService != null) {
            realtimeService.broadcastMessageDelete(messageId, chat, deletedBy);
        }
    }

    @Override
    public void deleteMessageForMe(Integer messageId, String jwt) throws UserException, MessageException, ChatException {
        Message message = getMessageById(messageId, jwt);

        message.setContent("[Message deleted]");
        message.setMessageType(MessageType.SYSTEM);
        messageRepository.save(message);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> searchMessagesInChat(Integer chatId, String query, String jwt) throws UserException, ChatException {
        if (!chatService.hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        Chat chat = chatService.findChatById(chatId);
        return messageRepository.findByChatAndContentContainingIgnoreCase(chat, query);
    }

    @Override
    public Message forwardMessage(Integer messageId, Integer targetChatId, String jwt) throws UserException, MessageException, ChatException {
        Message originalMessage = getMessageById(messageId, jwt);

        if (!chatService.hasAccessToChat(targetChatId, jwt)) {
            throw new ChatException("You don't have access to target chat");
        }

        String forwardedContent = "Forwarded: " + originalMessage.getContent();
        return sendMessage(forwardedContent, targetChatId, originalMessage.getMessageType(), jwt);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Message> getMessagesSince(Integer chatId, String lastMessageTime, String jwt) throws UserException, ChatException {
        if (!chatService.hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        LocalDateTime dateTime = LocalDateTime.parse(lastMessageTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        return messageRepository.findMessagesSince(chatId, dateTime);
    }

    @Override
    @Transactional(readOnly = true)
    public int getMessageCount(Integer chatId, String jwt) throws UserException, ChatException {
        if (!chatService.hasAccessToChat(chatId, jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        Chat chat = chatService.findChatById(chatId);
        return messageRepository.countByChat(chat);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserAccessMessage(Integer messageId, String jwt) throws UserException, MessageException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message not found with id: " + messageId));

        try {
            return chatService.hasAccessToChat(message.getChat().getId(), jwt);
        } catch (ChatException e) {
            return false;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserEditMessage(Integer messageId, String jwt) throws UserException, MessageException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message not found with id: " + messageId));

        User currentUser = userService.findUserProfile(jwt);
        return message.getSender().getId().equals(currentUser.getId());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean canUserDeleteMessage(Integer messageId, String jwt) throws UserException, MessageException {
        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message not found with id: " + messageId));

        User currentUser = userService.findUserProfile(jwt);

        boolean isMessageOwner = message.getSender().getId().equals(currentUser.getId());

        try {
            boolean isChatAdmin = chatService.isGroupAdmin(message.getChat().getId(), jwt);
            return isMessageOwner || isChatAdmin;
        } catch (ChatException e) {
            return isMessageOwner;
        }
    }

    @Override
    public void markMessageAsRead(Integer messageId, String jwt) throws UserException, MessageException, ChatException {
        User currentUser = userService.findUserProfile(jwt);

        Message message = messageRepository.findById(messageId)
                .orElseThrow(() -> new MessageException("Message not found with id: " + messageId));

        if (!chatService.hasAccessToChat(message.getChat().getId(), jwt)) {
            throw new ChatException("You don't have access to this chat");
        }

        if (message.getSender().getId().equals(currentUser.getId())) {
            return;
        }

        if (!messageReadStatusRepository.existsByMessageAndUser(message, currentUser)) {
            MessageReadStatus readStatus = new MessageReadStatus(message, currentUser);
            messageReadStatusRepository.save(readStatus);

            if (realtimeService != null) {
                realtimeService.broadcastMessageRead(message, currentUser);
            }
        }
    }
}