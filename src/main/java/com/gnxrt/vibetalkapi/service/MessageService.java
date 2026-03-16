package com.gnxrt.vibetalkapi.service;

import com.gnxrt.vibetalkapi.exception.ChatException;
import com.gnxrt.vibetalkapi.exception.MessageException;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.MessageType;

import java.util.List;

public interface MessageService {

    public Message sendMessage(String content, Integer chatId, MessageType messageType, String jwt) throws UserException, ChatException;

    public Message sendMessage(String content, Integer chatId, MessageType messageType, String jwt, String clientMessageId) throws UserException, ChatException;

    public Message sendMessage(String content, Integer chatId, MessageType messageType, String jwt, String clientMessageId, Integer replyToId) throws UserException, ChatException;

    public List<Message> getChatMessages(Integer chatId, String jwt) throws UserException, ChatException;

    public List<Message> getChatMessages(Integer chatId, int page, int size, String jwt) throws UserException, ChatException;

    public Message getMessageById(Integer messageId, String jwt) throws UserException, MessageException, ChatException;

    public Message editMessage(Integer messageId, String newContent, String jwt) throws UserException, MessageException, ChatException;

    public void deleteMessage(Integer messageId, String jwt) throws UserException, MessageException, ChatException;

    public void deleteMessageForMe(Integer messageId, String jwt) throws UserException, MessageException, ChatException;

    public List<Message> searchMessagesInChat(Integer chatId, String query, String jwt) throws UserException, ChatException;

    public Message forwardMessage(Integer messageId, Integer targetChatId, String jwt) throws UserException, MessageException, ChatException;

    public List<Message> getMessagesSince(Integer chatId, String lastMessageTime, String jwt) throws UserException, ChatException;

    public int getMessageCount(Integer chatId, String jwt) throws UserException, ChatException;

    public boolean canUserAccessMessage(Integer messageId, String jwt) throws UserException, MessageException;

    public boolean canUserEditMessage(Integer messageId, String jwt) throws UserException, MessageException;

    public boolean canUserDeleteMessage(Integer messageId, String jwt) throws UserException, MessageException;

    public void markMessageAsRead(Integer messageId, String jwt) throws UserException, MessageException, ChatException;

}