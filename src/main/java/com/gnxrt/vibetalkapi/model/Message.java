package com.gnxrt.vibetalkapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "messages")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Message content is required")
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.TEXT;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<MessageReadStatus> readStatuses = new ArrayList<>();

    public Message(String content, User sender, Chat chat) {
        this.content = content;
        this.sender = sender;
        this.chat = chat;
    }

    public boolean isReadByUser(User user) {
        return readStatuses.stream()
                .anyMatch(status -> status.getUser().getId().equals(user.getId()));
    }

    public LocalDateTime getReadTimeByUser(User user) {
        return readStatuses.stream()
                .filter(status -> status.getUser().getId().equals(user.getId()))
                .map(MessageReadStatus::getReadAt)
                .findFirst()
                .orElse(null);
    }

    public int getReadCount() {
        return readStatuses.size();
    }
}