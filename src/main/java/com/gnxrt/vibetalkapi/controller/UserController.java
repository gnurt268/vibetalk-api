package com.gnxrt.vibetalkapi.controller;

import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.dto.request.UpdateUserRequest;
import com.gnxrt.vibetalkapi.dto.request.ChangePasswordRequest;
import com.gnxrt.vibetalkapi.dto.response.ApiResponse;
import com.gnxrt.vibetalkapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.gnxrt.vibetalkapi.config.JwtConstant.JWT_HEADER;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/profile")
    public ResponseEntity<User> getUserProfileHandler(@RequestHeader(JWT_HEADER) String token) throws UserException {
        User user = userService.findUserProfile(token);
        return new ResponseEntity<>(user, HttpStatus.OK);
    }

    @GetMapping("/search/{query}")
    public ResponseEntity<List<User>> searchUserHandler(@PathVariable("query") String query) {
        List<User> users = userService.searchUser(query);
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PutMapping("/update")
    public ResponseEntity<ApiResponse> updateUserHandler(
            @RequestHeader(JWT_HEADER) String token,
            @RequestBody UpdateUserRequest request) throws UserException {

        User user = userService.findUserProfile(token);
        userService.updateUser(user.getId(), request);

        ApiResponse response = new ApiResponse("Update user success", true);
        return new ResponseEntity<>(response, HttpStatus.ACCEPTED);
    }

    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse> changePasswordHandler(
            @RequestHeader(JWT_HEADER) String token,
            @Valid @RequestBody ChangePasswordRequest request) throws UserException {

        User user = userService.findUserProfile(token);
        userService.changePassword(user.getId(), request);

        ApiResponse response = new ApiResponse("Password changed successfully", true);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping(value = "/upload-avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadAvatarHandler(
            @RequestParam("file") MultipartFile file) {

        try {
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Only image files are allowed");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            if (file.getSize() > 5 * 1024 * 1024) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "File size must be less than 5MB");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            String imageUrl = userService.uploadAvatar(file);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Avatar uploaded successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/upload-avatar-base64")
    public ResponseEntity<Map<String, String>> uploadAvatarBase64Handler(
            @RequestBody Map<String, String> request) {

        try {
            String base64Image = request.get("image");
            if (base64Image == null || base64Image.trim().isEmpty()) {
                Map<String, String> error = new HashMap<>();
                error.put("error", "Base64 image is required");
                return new ResponseEntity<>(error, HttpStatus.BAD_REQUEST);
            }

            String imageUrl = userService.uploadAvatarBase64(base64Image);

            Map<String, String> response = new HashMap<>();
            response.put("imageUrl", imageUrl);
            response.put("message", "Avatar uploaded successfully");

            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IOException e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Failed to upload image: " + e.getMessage());
            return new ResponseEntity<>(error, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserByIdHandler(@PathVariable Integer userId) {
        try {
            User user = userService.findUserById(userId);
            return new ResponseEntity<>(user, HttpStatus.OK);
        } catch (UserException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}