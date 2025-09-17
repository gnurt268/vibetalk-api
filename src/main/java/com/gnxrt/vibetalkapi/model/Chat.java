package com.gnxrt.vibetalkapi.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "chats")
@AllArgsConstructor
@NoArgsConstructor
@Data
public class Chat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @NotBlank(message = "Chat name is required")
    @Size(max = 100, message = "Chat name must not exceed 100 characters")
    @Column(name = "chat_name")
    private String chatName;

    @Column(name = "chat_image")
    private String chatImage;

    @Column(name = "is_group_chat", nullable = false)
    private boolean isGroupChat = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chat_members",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "chat_admins",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> admins = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Message> messages = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public boolean isUserAdmin(User user) {
        return admins.contains(user) || createdBy.getId().equals(user.getId());
    }

    public boolean isOwner(User user) {
        return createdBy.getId().equals(user.getId());
    }

    public void addAdmin(User user) {
        if (members.contains(user)) {
            admins.add(user);
        }
    }

    public void removeAdmin(User user) {
        if (!isOwner(user)) {
            admins.remove(user);
        }
    }
}