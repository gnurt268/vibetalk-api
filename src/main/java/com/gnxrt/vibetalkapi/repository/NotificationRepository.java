package com.gnxrt.vibetalkapi.repository;

import com.gnxrt.vibetalkapi.model.Message;
import com.gnxrt.vibetalkapi.model.Notification;
import com.gnxrt.vibetalkapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    int countByUserAndIsReadFalse(User user);

    void deleteByUser(User user);

    @Modifying
    @Query("DELETE FROM Notification n WHERE n.message.id = :messageId")
    void deleteByMessageId(@Param("messageId") Integer messageId);
}