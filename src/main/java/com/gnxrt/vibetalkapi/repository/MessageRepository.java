package com.gnxrt.vibetalkapi.repository;

import com.gnxrt.vibetalkapi.model.Chat;
import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Integer> {

    @Query("SELECT m FROM Message m WHERE m.chat = :chat ORDER BY m.createdAt ASC")
    List<Message> findByChatOrderByCreatedAtAsc(@Param("chat") Chat chat);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat ORDER BY m.createdAt DESC")
    List<Message> findByChatOrderByCreatedAtDesc(@Param("chat") Chat chat);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat ORDER BY m.createdAt DESC")
    Page<Message> findByChat(@Param("chat") Chat chat, Pageable pageable);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND LOWER(m.content) LIKE LOWER(CONCAT('%', :content, '%')) ORDER BY m.createdAt DESC")
    List<Message> findByChatAndContentContainingIgnoreCase(@Param("chat") Chat chat, @Param("content") String content);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat = :chat")
    int countByChat(@Param("chat") Chat chat);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat = :chat AND m.sender != :user " +
            "AND m.id NOT IN (SELECT mrs.message.id FROM MessageReadStatus mrs WHERE mrs.user = :user)")
    int countUnreadMessagesByUserInChat(@Param("chat") Chat chat, @Param("user") User user);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.sender != :user " +
            "AND m.id NOT IN (SELECT mrs.message.id FROM MessageReadStatus mrs WHERE mrs.user = :user)")
    List<Message> findUnreadMessagesByUserInChat(@Param("chat") Chat chat, @Param("user") User user);

    @Query("SELECT m FROM Message m WHERE m.chat.id = :chatId AND m.createdAt > :lastReadTime ORDER BY m.createdAt ASC")
    List<Message> findMessagesSince(@Param("chatId") Integer chatId, @Param("lastReadTime") LocalDateTime lastReadTime);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat ORDER BY m.createdAt DESC LIMIT 1")
    Message findTopByChatOrderByCreatedAtDesc(@Param("chat") Chat chat);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat IN :chats AND m.sender != :user " +
            "AND m.id NOT IN (SELECT mrs.message.id FROM MessageReadStatus mrs WHERE mrs.user = :user)")
    int countTotalUnreadMessagesByUser(@Param("chats") List<Chat> chats, @Param("user") User user);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.createdAt BETWEEN :startDate AND :endDate ORDER BY m.createdAt ASC")
    List<Message> findByChatAndDateRange(@Param("chat") Chat chat, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT m FROM Message m WHERE m.sender = :user ORDER BY m.createdAt DESC")
    List<Message> findBySenderOrderByCreatedAtDesc(@Param("user") User user);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.messageType = :messageType ORDER BY m.createdAt DESC")
    List<Message> findByChatAndMessageType(@Param("chat") Chat chat, @Param("messageType") com.gnxrt.vibetalkapi.model.MessageType messageType);

    @Query("SELECT COUNT(m) FROM Message m WHERE m.chat = :chat AND m.sender = :user")
    int countByChatAndSender(@Param("chat") Chat chat, @Param("user") User user);

    @Query("SELECT m FROM Message m WHERE m.chat = :chat AND m.sender = :user ORDER BY m.createdAt DESC")
    List<Message> findByChatAndSender(@Param("chat") Chat chat, @Param("user") User user);

    @Modifying
    @Query("UPDATE Message m SET m.replyTo = null WHERE m.replyTo.id = :messageId")
    void clearReplyToByMessageId(@Param("messageId") Integer messageId);

    @Modifying
    @Query("UPDATE Message m SET m.replyTo = null WHERE m.chat.id = :chatId")
    void clearReplyToByChatId(@Param("chatId") Integer chatId);
}