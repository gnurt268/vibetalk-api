package com.gnxrt.vibetalkapi.controller;

import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.Notification;
import com.gnxrt.vibetalkapi.dto.response.ApiResponse;
import com.gnxrt.vibetalkapi.service.NotificationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.gnxrt.vibetalkapi.config.JwtConstant.JWT_HEADER;

@RestController
@RequestMapping("/api/notifications")
@Tag(name = "Notifications", description = "Notification system")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public ResponseEntity<List<Notification>> getUserNotifications(
            @RequestHeader(JWT_HEADER) String jwt) throws UserException {

        List<Notification> notifications = notificationService.getUserNotifications(jwt);
        return new ResponseEntity<>(notifications, HttpStatus.OK);
    }

    @GetMapping("/unread")
    public ResponseEntity<List<Notification>> getUnreadNotifications(
            @RequestHeader(JWT_HEADER) String jwt) throws UserException {

        List<Notification> unreadNotifications = notificationService.getUnreadNotifications(jwt);
        return new ResponseEntity<>(unreadNotifications, HttpStatus.OK);
    }

    @GetMapping("/unread/count")
    public ResponseEntity<Integer> getUnreadCount(
            @RequestHeader(JWT_HEADER) String jwt) throws UserException {

        int count = notificationService.getUnreadCount(jwt);
        return new ResponseEntity<>(count, HttpStatus.OK);
    }

    @PostMapping("/{notificationId}/read")
    public ResponseEntity<ApiResponse> markAsRead(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer notificationId) throws UserException {

        notificationService.markAsRead(notificationId, jwt);

        ApiResponse response = new ApiResponse("Notification marked as read", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/read-all")
    public ResponseEntity<ApiResponse> markAllAsRead(
            @RequestHeader(JWT_HEADER) String jwt) throws UserException {

        notificationService.markAllAsRead(jwt);

        ApiResponse response = new ApiResponse("All notifications marked as read", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @DeleteMapping("/{notificationId}")
    public ResponseEntity<ApiResponse> deleteNotification(
            @RequestHeader(JWT_HEADER) String jwt,
            @PathVariable Integer notificationId) throws UserException {

        notificationService.deleteNotification(notificationId, jwt);

        ApiResponse response = new ApiResponse("Notification deleted", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}