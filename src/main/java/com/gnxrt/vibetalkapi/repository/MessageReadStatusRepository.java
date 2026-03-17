package com.gnxrt.vibetalkapi.repository;

import com.gnxrt.vibetalkapi.model.Chat;
import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.MessageReadStatus;
import com.gnxrt.vibetalkapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MessageReadStatusRepository extends JpaRepository<MessageReadStatus, Integer> {

    Optional<MessageReadStatus> findByMessageAndUser(Message message, User user);

    @Query("SELECT mrs FROM MessageReadStatus mrs WHERE mrs.message.chat = :chat AND mrs.user = :user ORDER BY mrs.readAt DESC")
    List<MessageReadStatus> findByUserAndChat(@Param("user") User user, @Param("chat") Chat chat);

    @Query("SELECT mrs FROM MessageReadStatus mrs WHERE mrs.message = :message")
    List<MessageReadStatus> findByMessage(@Param("message") Message message);

    @Modifying
    @Query("DELETE FROM MessageReadStatus mrs WHERE mrs.message.chat = :chat AND mrs.user = :user")
    void deleteByUserAndChat(@Param("user") User user, @Param("chat") Chat chat);

    boolean existsByMessageAndUser(Message message, User user);

    @Query("SELECT COUNT(mrs) FROM MessageReadStatus mrs WHERE mrs.message = :message")
    int countReadStatusByMessage(@Param("message") Message message);

    @Query("SELECT mrs.user FROM MessageReadStatus mrs WHERE mrs.message = :message")
    List<User> findUsersWhoReadMessage(@Param("message") Message message);

    @Modifying
    @Query("DELETE FROM MessageReadStatus mrs WHERE mrs.message.id = :messageId")
    void deleteByMessageId(@Param("messageId") Integer messageId);

    @Modifying
    @Query("DELETE FROM MessageReadStatus mrs WHERE mrs.message.chat.id = :chatId")
    void deleteByChatId(@Param("chatId") Integer chatId);
}