package com.gnxrt.vibetalkapi.repository;

import com.gnxrt.vibetalkapi.model.Chat;
import com.gnxrt.vibetalkapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatRepository extends JpaRepository<Chat, Integer> {

    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.members LEFT JOIN FETCH c.admins WHERE c.id = :chatId")
    Optional<Chat> findByIdWithMembers(@Param("chatId") Integer chatId);

    @Query("SELECT c FROM Chat c LEFT JOIN FETCH c.members WHERE c.isGroupChat = false AND :user MEMBER OF c.members AND :reqUser MEMBER OF c.members")
    public Chat findPrivateChatByUsers(@Param("user") User user, @Param("reqUser") User requestUser);

    @Query("SELECT DISTINCT c FROM Chat c LEFT JOIN FETCH c.members WHERE :user MEMBER OF c.members ORDER BY c.updatedAt DESC")
    public List<Chat> findChatsByUser(@Param("user") User user);

    @Query("SELECT DISTINCT c FROM Chat c LEFT JOIN FETCH c.members WHERE c.isGroupChat = true AND :user MEMBER OF c.members ORDER BY c.updatedAt DESC")
    public List<Chat> findGroupChatsByUser(@Param("user") User user);

    @Query("SELECT DISTINCT c FROM Chat c LEFT JOIN FETCH c.members WHERE c.isGroupChat = false AND :user MEMBER OF c.members ORDER BY c.updatedAt DESC")
    public List<Chat> findPrivateChatsByUser(@Param("user") User user);

    @Query("SELECT DISTINCT c FROM Chat c LEFT JOIN FETCH c.members WHERE c.isGroupChat = true AND (:user MEMBER OF c.admins OR c.createdBy = :user)")
    public List<Chat> findGroupChatsWhereUserIsAdmin(@Param("user") User user);

    @Query("SELECT c FROM Chat c WHERE c.createdBy = :user")
    public List<Chat> findChatsCreatedByUser(@Param("user") User user);
}