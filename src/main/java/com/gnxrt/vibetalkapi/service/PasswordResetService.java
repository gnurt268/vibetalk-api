package com.gnxrt.vibetalkapi.service;

import com.gnxrt.vibetalkapi.exception.UserException;
import com.gnxrt.vibetalkapi.model.PasswordResetToken;
import com.gnxrt.vibetalkapi.model.User;
import com.gnxrt.vibetalkapi.repository.PasswordResetTokenRepository;
import com.gnxrt.vibetalkapi.repository.UserRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Optional;

@Service
@Transactional
public class PasswordResetService {

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    private static final int TOKEN_VALIDITY_MINUTES = 15;
    private static final int RATE_LIMIT_MINUTES = 5;
    private static final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(PasswordResetTokenRepository tokenRepository,
                                UserRepository userRepository,
                                EmailService emailService,
                                PasswordEncoder passwordEncoder) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public void initiatePasswordReset(String email) throws UserException {
        User user = userRepository.findByEmail(email);
        if (user == null) {
            return;
        }

        LocalDateTime rateLimitTime = LocalDateTime.now().minusMinutes(RATE_LIMIT_MINUTES);
        if (tokenRepository.existsByUserAndCreatedAtAfter(user, rateLimitTime)) {
            throw new UserException("Please wait before requesting another password reset email");
        }

        tokenRepository.deleteByUser(user);

        String resetToken = generateSecureToken();
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(TOKEN_VALIDITY_MINUTES);

        PasswordResetToken passwordResetToken = new PasswordResetToken(resetToken, user, expiresAt);
        tokenRepository.save(passwordResetToken);

        sendPasswordResetEmailAsync(user.getEmail(), user.getUsername(), resetToken);
    }

    public void resetPassword(String token, String newPassword) throws UserException {
        PasswordResetToken resetToken = tokenRepository.findByTokenAndUsedFalse(token)
                .orElseThrow(() -> new UserException("Invalid or expired reset token"));

        if (!resetToken.isValid()) {
            throw new UserException("Reset token has expired or been used");
        }

        User user = resetToken.getUser();

        if (newPassword == null || newPassword.trim().length() < 6) {
            throw new UserException("Password must be at least 6 characters long");
        }

        user.setPassword(passwordEncoder.encode(newPassword.trim()));
        userRepository.save(user);

        resetToken.markAsUsed();
        tokenRepository.save(resetToken);

        sendPasswordResetConfirmationAsync(user.getEmail(), user.getUsername());
    }

    public boolean validateResetToken(String token) {
        Optional<PasswordResetToken> resetToken = tokenRepository.findByTokenAndUsedFalse(token);
        return resetToken.isPresent() && resetToken.get().isValid();
    }

    private String generateSecureToken() {
        byte[] tokenBytes = new byte[32];
        secureRandom.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }

    @Async
    protected void sendPasswordResetEmailAsync(String email, String username, String token) {
        try {
            emailService.sendPasswordResetEmail(email, username, token);
        } catch (Exception e) {
            System.err.println("Failed to send password reset email to: " + email + ". Error: " + e.getMessage());
        }
    }

    @Async
    protected void sendPasswordResetConfirmationAsync(String email, String username) {
        try {
            emailService.sendPasswordResetConfirmationEmail(email, username);
        } catch (Exception e) {
            System.err.println("Failed to send password reset confirmation email to: " + email + ". Error: " + e.getMessage());
        }
    }

    @Scheduled(fixedRate = 3600000)
    @Transactional
    public void cleanupExpiredTokens() {
        tokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }
}