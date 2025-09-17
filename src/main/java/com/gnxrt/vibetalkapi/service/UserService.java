package com.gnxrt.vibetalkapi.service;

import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.dto.request.UpdateUserRequest;
import com.gnxrt.vibetalkapi.dto.request.ChangePasswordRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface UserService {

    public User findUserById(Integer id) throws UserException;

    public User findUserProfile(String jwt) throws UserException;

    public User updateUser(Integer userId, UpdateUserRequest req) throws UserException;

    public User changePassword(Integer userId, ChangePasswordRequest req) throws UserException;

    public String uploadAvatar(MultipartFile file) throws IOException;

    public String uploadAvatarBase64(String base64Image) throws IOException;

    public List<User> searchUser(String query);
}