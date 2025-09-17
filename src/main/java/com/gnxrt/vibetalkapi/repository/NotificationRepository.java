package com.gnxrt.vibetalkapi.repository;

import com.gnxrt.vibetalkapi.model.Notification;
import com.gnxrt.vibetalkapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);

    List<Notification> findByUserAndIsReadFalseOrderByCreatedAtDesc(User user);

    int countByUserAndIsReadFalse(User user);

    void deleteByUser(User user);
}