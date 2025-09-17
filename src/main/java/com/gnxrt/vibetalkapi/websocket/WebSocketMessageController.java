package com.gnxrt.vibetalkapi.websocket;

import com.gnxrt.vibetalkapi.dto.websocket.*;
import com.gnxrt.vibetalkapi.model.*;
import com.gnxrt.vibetalkapi.service.MessageService;
import com.gnxrt.vibetalkapi.service.ChatService;
import com.gnxrt.vibetalkapi.service.NotificationService;
import com.gnxrt.vibetalkapi.service.UserPresenceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
public class WebSocketMessageController {

    private final SimpMessageSendingOperations messagingTemplate;
    private final MessageService messageService;
    private final ChatService chatService;
    private final NotificationService notificationService;
    private final UserPresenceService userPresenceService;

    public WebSocketMessageController(SimpMessageSendingOperations messagingTemplate,
                                      MessageService messageService,
                                      ChatService chatService,
                                      NotificationService notificationService,
                                      UserPresenceService userPresenceService) {
        this.messagingTemplate = messagingTemplate;
        this.messageService = messageService;
        this.chatService = chatService;
        this.notificationService = notificationService;
        this.userPresenceService = userPresenceService;
    }

    @MessageMapping("/message.send")
    public void sendMessage(@Payload ChatMessageDTO messageDTO, Principal principal) {
        try {
            if (principal == null) {
                throw new SecurityException("Unauthorized");
            }
            User sender = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();
            if (sender == null) {
                throw new SecurityException("Invalid user");
            }

            Message message = messageService.sendMessage(
                    messageDTO.getContent(),
                    messageDTO.getChatId(),
                    messageDTO.getMessageType() != null ? messageDTO.getMessageType() : MessageType.TEXT,
                    "Bearer " + messageDTO.getToken()
            );

            RealtimeMessageDTO realtimeMessage = new RealtimeMessageDTO(
                    message.getId(),
                    message.getContent(),
                    sender.getId(),
                    sender.getUsername(),
                    sender.getUrlAvatar(),
                    message.getChat().getId(),
                    message.getMessageType(),
                    message.getCreatedAt(),
                    "SENT"
            );

            Chat chat = message.getChat();
            for (User member : chat.getMembers()) {
                messagingTemplate.convertAndSendToUser(
                        member.getId().toString(),
                        "/queue/messages",
                        realtimeMessage
                );
            }

            notificationService.notifyNewMessage(message);

            messagingTemplate.convertAndSend("/topic/chats.update", new ChatUpdateDTO(
                    chat.getId(),
                    "MESSAGE_SENT",
                    sender.getId(),
                    LocalDateTime.now()
            ));

        } catch (Exception e) {
            messagingTemplate.convertAndSendToUser(
                    principal.getName(),
                    "/queue/errors",
                    new ErrorMessageDTO("Failed to send message: " + e.getMessage(),LocalDateTime.now())
            );
        }
    }

    @MessageMapping("/typing.start")
    public void handleTypingStart(@Payload TypingDTO typingDTO, Principal principal) throws Exception {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        try {
            Chat chat = chatService.findChatById(typingDTO.getChatId());

            TypingIndicatorDTO indicator = new TypingIndicatorDTO(
                    user.getId(),
                    user.getUsername(),
                    typingDTO.getChatId(),
                    "TYPING",
                    LocalDateTime.now()
            );

            for (User member : chat.getMembers()) {
                if (!member.getId().equals(user.getId())) {
                    messagingTemplate.convertAndSendToUser(
                            member.getId().toString(),
                            "/queue/typing",
                            indicator
                    );
                }
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @MessageMapping("/typing.stop")
    public void handleTypingStop(@Payload TypingDTO typingDTO, Principal principal) throws Exception {
        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        try {
            Chat chat = chatService.findChatById(typingDTO.getChatId());

            TypingIndicatorDTO indicator = new TypingIndicatorDTO(
                    user.getId(),
                    user.getUsername(),
                    typingDTO.getChatId(),
                    "STOPPED_TYPING",
                    LocalDateTime.now()
            );

            for (User member : chat.getMembers()) {
                if (!member.getId().equals(user.getId())) {
                    messagingTemplate.convertAndSendToUser(
                            member.getId().toString(),
                            "/queue/typing",
                            indicator
                    );
                }
            }
        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @MessageMapping("/message.read")
    public void markMessageAsRead(@Payload MessageReadDTO readDTO, Principal principal) throws Exception {
        try {
            User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

            messageService.markMessageAsRead(readDTO.getMessageId(), "Bearer " + readDTO.getToken());

            Message message = messageService.getMessageById(readDTO.getMessageId(), "Bearer " + readDTO.getToken());

            ReadReceiptDTO receipt = new ReadReceiptDTO(
                    readDTO.getMessageId(),
                    user.getId(),
                    user.getUsername(),
                    LocalDateTime.now()
            );

            messagingTemplate.convertAndSendToUser(
                    message.getSender().getId().toString(),
                    "/queue/read-receipts",
                    receipt
            );

        } catch (Exception e) {
            throw new Exception(e);
        }
    }

    @MessageMapping("/presence.update")
    public void updatePresence(@Payload PresenceDTO presenceDTO, Principal principal) {

        User user = (User) ((UsernamePasswordAuthenticationToken) principal).getPrincipal();

        if ("ONLINE".equals(presenceDTO.getStatus())) {
            userPresenceService.markUserOnline(user, presenceDTO.getSessionId());
        }

        UserPresenceUpdateDTO update = new UserPresenceUpdateDTO(
                user.getId(),
                user.getUsername(),
                presenceDTO.getStatus(),
                LocalDateTime.now()
        );

        messagingTemplate.convertAndSend("/topic/presence", update);
    }
}