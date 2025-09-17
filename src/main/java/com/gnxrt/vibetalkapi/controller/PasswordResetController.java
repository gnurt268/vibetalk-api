package com.gnxrt.vibetalkapi.controller;

import com.gnxrt.vibetalkapi.dto.request.ForgotPasswordRequest;
import com.gnxrt.vibetalkapi.dto.request.ResetPasswordRequest;
import com.gnxrt.vibetalkapi.dto.request.ValidateTokenRequest;
import com.gnxrt.vibetalkapi.dto.response.ApiResponse;
import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.service.PasswordResetService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@Tag(name = "Password Reset", description = "Password reset functionality")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot")
    @Operation(
            summary = "Request password reset",
            description = "Send password reset email to user if account exists"
    )
    public ResponseEntity<ApiResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        try {
            passwordResetService.initiatePasswordReset(request.getEmail());

            ApiResponse response = new ApiResponse(
                    "If an account with that email exists, we've sent you a password reset link",
                    true
            );
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (UserException e) {
            ApiResponse response = new ApiResponse(e.getMessage(), false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(
                    "An error occurred while processing your request",
                    false
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/reset")
    @Operation(
            summary = "Reset password",
            description = "Reset user password using valid reset token"
    )
    public ResponseEntity<ApiResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {

        try {
            if (!request.passwordsMatch()) {
                ApiResponse response = new ApiResponse("Passwords do not match", false);
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            passwordResetService.resetPassword(request.getToken(), request.getNewPassword());

            ApiResponse response = new ApiResponse(
                    "Password has been reset successfully. You can now log in with your new password",
                    true
            );
            return new ResponseEntity<>(response, HttpStatus.OK);

        } catch (UserException e) {
            ApiResponse response = new ApiResponse(e.getMessage(), false);
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            ApiResponse response = new ApiResponse(
                    "An error occurred while resetting your password",
                    false
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/validate-token")
    @Operation(
            summary = "Validate reset token",
            description = "Check if password reset token is valid and not expired"
    )
    public ResponseEntity<ApiResponse> validateResetToken(
            @Valid @RequestBody ValidateTokenRequest request) {

        try {
            boolean isValid = passwordResetService.validateResetToken(request.getToken());

            if (isValid) {
                ApiResponse response = new ApiResponse("Token is valid", true);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse response = new ApiResponse(
                        "Invalid or expired reset token",
                        false
                );
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            ApiResponse response = new ApiResponse(
                    "An error occurred while validating the token",
                    false
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/validate-token/{token}")
    @Operation(
            summary = "Validate reset token via URL",
            description = "Alternative endpoint to validate token via URL parameter"
    )
    public ResponseEntity<ApiResponse> validateResetTokenViaUrl(
            @PathVariable String token) {

        try {
            boolean isValid = passwordResetService.validateResetToken(token);

            if (isValid) {
                ApiResponse response = new ApiResponse("Token is valid", true);
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                ApiResponse response = new ApiResponse(
                        "Invalid or expired reset token",
                        false
                );
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

        } catch (Exception e) {
            ApiResponse response = new ApiResponse(
                    "An error occurred while validating the token",
                    false
            );
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}