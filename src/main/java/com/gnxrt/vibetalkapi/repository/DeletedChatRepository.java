package com.gnxrt.vibetalkapi.repository;

import com.gnxrt.vibetalkapi.model.DeletedChat;
import com.gnxrt.vibetalkapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface DeletedChatRepository extends JpaRepository<DeletedChat, Integer> {

    boolean existsByUserIdAndChatId(Integer userId, Integer chatId);

    @Query("SELECT dc.chat.id FROM DeletedChat dc WHERE dc.user = :user")
    Set<Integer> findDeletedChatIdsByUser(@Param("user") User user);

    void deleteByUserIdAndChatId(Integer userId, Integer chatId);

    void deleteByChatId(Integer chatId);
}