package com.gnxrt.vibetalkapi.repository;

import com.gnxrt.vibetalkapi.model.DeletedMessage;
import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface DeletedMessageRepository extends JpaRepository<DeletedMessage, Integer> {

    boolean existsByUserAndMessage(User user, Message message);

    @Query("SELECT dm.message.id FROM DeletedMessage dm WHERE dm.user = :user AND dm.message.chat.id = :chatId")
    Set<Integer> findDeletedMessageIdsByUserAndChat(@Param("user") User user, @Param("chatId") Integer chatId);

    void deleteByMessageId(Integer messageId);

    @Modifying
    @Query("DELETE FROM DeletedMessage dm WHERE dm.message.chat.id = :chatId")
    void deleteByChatId(@Param("chatId") Integer chatId);
}