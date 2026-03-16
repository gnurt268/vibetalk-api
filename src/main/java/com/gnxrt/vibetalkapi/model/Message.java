package com.gnxrt.vibetalkapi.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
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
@JsonIgnoreProperties(value = {"hibernateLazyInitializer", "handler", "readCount"},
        allowGetters = true,
        ignoreUnknown = true)
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Message content is required")
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"password", "chats", "sentMessages"})
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    @JsonIgnoreProperties({"messages", "members", "admins"})
    private Chat chat;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type")
    private MessageType messageType = MessageType.TEXT;

    @Column(name = "client_message_id", length = 64)
    private String clientMessageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reply_to_id")
    @JsonIgnoreProperties({"readStatuses", "chat", "replyTo"})
    private Message replyTo;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
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

    @JsonProperty("readCount")
    public int getReadCount() {
        return readStatuses.size();
    }
}