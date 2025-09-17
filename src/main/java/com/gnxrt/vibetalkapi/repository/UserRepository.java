package com.gnxrt.vibetalkapi.repository;

import com.gnxrt.vibetalkapi.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserRepository extends JpaRepository<User,Integer> {
    public User findByEmail(String email);

    public User findByUsername(String username);

    @Query("select u from User u where LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) or LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%'))")
    public List<User> searchUser(@Param("query") String query);
}
