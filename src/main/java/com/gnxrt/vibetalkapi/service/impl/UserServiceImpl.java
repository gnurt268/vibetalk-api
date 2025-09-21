package com.gnxrt.vibetalkapi.service.impl;

import com.gnxrt.vibetalkapi.config.TokenProvider;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.repository.UserRepository;
import com.gnxrt.vibetalkapi.dto.request.UpdateUserRequest;
import com.gnxrt.vibetalkapi.dto.request.ChangePasswordRequest;
import com.gnxrt.vibetalkapi.service.UserService;
import com.gnxrt.vibetalkapi.service.CloudinaryService;
import com.gnxrt.vibetalkapi.service.EmailService;
import com.gnxrt.vibetalkapi.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final CloudinaryService cloudinaryService;
    private final EmailService emailService;
    private final CacheService cacheService;

    public UserServiceImpl(UserRepository userRepository,
                           TokenProvider tokenProvider,
                           PasswordEncoder passwordEncoder,
                           CloudinaryService cloudinaryService,
                           EmailService emailService,
                           CacheService cacheService) {
        this.userRepository = userRepository;
        this.tokenProvider = tokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.cloudinaryService = cloudinaryService;
        this.emailService = emailService;
        this.cacheService = cacheService;
    }

    @Override
    public User findUserById(Integer id) throws UserException {

        User cachedUser = (User) cacheService.getCachedUserProfile(id);
        if (cachedUser != null) {
            log.debug("Retrieved user from cache for userId: {}", id);
            return cachedUser;
        }

        Optional<User> user = userRepository.findById(id);
        if (user.isPresent()) {
            User foundUser = user.get();
            cacheService.cacheUserProfile(id, foundUser);
            return foundUser;
        }
        throw new UserException("User not found with id: " + id);
    }

    @Override
    public User findUserProfile(String jwt) throws UserException {
        String email = tokenProvider.getEmailFromToken(jwt);
        if (email == null) {
            throw new BadCredentialsException("Invalid token");
        }
        User user = userRepository.findByEmail(email);

        if (user == null) {
            throw new UserException("User not found with email: " + email);
        }

        cacheService.cacheUserProfile(user.getId(), user);

        return user;
    }

    @Override
    public User updateUser(Integer userId, UpdateUserRequest req) throws UserException {
        User user = findUserById(userId);

        if (req.getFullName() != null && !req.getFullName().trim().isEmpty()) {
            user.setFullName(req.getFullName().trim());
        }

        if (req.getUrlAvatar() != null && !req.getUrlAvatar().trim().isEmpty()) {
            if (user.getUrlAvatar() != null && user.getUrlAvatar().contains("cloudinary.com")) {
                try {
                    String publicId = cloudinaryService.extractPublicIdFromUrl(user.getUrlAvatar());
                    if (publicId != null) {
                        cloudinaryService.deleteImage(publicId);
                    }
                } catch (IOException e) {
                    System.err.println("Failed to delete old avatar: " + e.getMessage());
                }
            }
            user.setUrlAvatar(req.getUrlAvatar());
        }

        User updatedUser = userRepository.save(user);

        cacheService.cacheUserProfile(userId, updatedUser);
        cacheService.clearUserChatsCache(userId);

        return updatedUser;
    }

    @Override
    public User changePassword(Integer userId, ChangePasswordRequest req) throws UserException {
        User user = findUserById(userId);

        if (!passwordEncoder.matches(req.getCurrentPassword(), user.getPassword())) {
            throw new UserException("Current password is incorrect");
        }

        if (!req.getNewPassword().equals(req.getConfirmPassword())) {
            throw new UserException("New password and confirmation do not match");
        }

        user.setPassword(passwordEncoder.encode(req.getNewPassword()));
        User updatedUser = userRepository.save(user);

        try {
            emailService.sendPasswordResetConfirmationEmail(user.getEmail(), user.getUsername());
        } catch (Exception e) {
            System.err.println("Failed to send password change confirmation email: " + e.getMessage());
        }

        cacheService.cacheUserProfile(userId, updatedUser);

        return updatedUser;
    }

    @Override
    public String uploadAvatar(MultipartFile file) throws IOException {
        return cloudinaryService.uploadImage(file);
    }

    @Override
    public String uploadAvatarBase64(String base64Image) throws IOException {
        return cloudinaryService.uploadImageFromBase64(base64Image);
    }

    @Override
    public List<User> searchUser(String query) {
        List<User> users = userRepository.searchUser(query);

        users.forEach(user -> cacheService.cacheUserProfile(user.getId(), user));

        return users;
    }
}