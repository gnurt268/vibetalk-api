package com.gnxrt.vibetalkapi.service;

import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.name:VibeTalk}")
    private String appName;

    @Value("${app.url:http://localhost:3030}")
    private String appUrl;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPasswordResetEmail(String toEmail, String username, String resetToken) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject("Password Reset Request - " + appName);

            String resetUrl = appUrl + "/reset-password?token=" + resetToken;
            String emailContent = buildPasswordResetEmailTemplate(username, resetUrl);

            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    public void sendPasswordResetConfirmationEmail(String toEmail, String username) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail, appName);
            helper.setTo(toEmail);
            helper.setSubject("Password Changed Successfully - " + appName);

            String emailContent = buildPasswordResetConfirmationTemplate(username);

            helper.setText(emailContent, true);

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset confirmation email", e);
        }
    }

    private String buildPasswordResetEmailTemplate(String username, String resetUrl) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Reset</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .button { display: inline-block; background-color: #007bff; color: white; padding: 12px 24px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }
                    .warning { background-color: #fff3cd; border: 1px solid #ffeaa7; padding: 15px; border-radius: 5px; margin: 15px 0; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <h2>Password Reset Request</h2>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        
                        <p>We received a request to reset your password for your %s account. If you made this request, click the button below to reset your password:</p>
                        
                        <div style="text-align: center;">
                            <a href="%s" class="button">Reset Password</a>
                        </div>
                        
                        <p>If the button above doesn't work, copy and paste the following link into your browser:</p>
                        <p style="word-break: break-all; background-color: #f1f1f1; padding: 10px; border-radius: 5px;">%s</p>
                        
                        <div class="warning">
                            <strong>⚠️ Important:</strong>
                            <ul>
                                <li>This link will expire in <strong>15 minutes</strong></li>
                                <li>If you didn't request this password reset, please ignore this email</li>
                                <li>Your password will remain unchanged until you create a new one</li>
                            </ul>
                        </div>
                        
                        <p>If you have any questions or concerns, please contact our support team.</p>
                        
                        <p>Best regards,<br>Trung Nguyen</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply to this message.</p>
                        <p>&copy; 2025 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, username, appName, resetUrl, resetUrl, appName);
    }

    private String buildPasswordResetConfirmationTemplate(String username) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
                <title>Password Changed</title>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #28a745; color: white; padding: 20px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 8px 8px; }
                    .success { background-color: #d1edff; border: 1px solid #007bff; padding: 15px; border-radius: 5px; margin: 15px 0; }
                    .footer { margin-top: 20px; font-size: 12px; color: #666; text-align: center; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <h2>Password Changed Successfully</h2>
                    </div>
                    <div class="content">
                        <p>Hi <strong>%s</strong>,</p>
                        
                        <div class="success">
                            <strong>✅ Success!</strong> Your password has been changed successfully.
                        </div>
                        
                        <p>Your %s account password has been updated. You can now log in with your new password.</p>
                        
                        <p>If you didn't make this change, please contact our support team immediately.</p>
                        
                        <p>For security reasons, we recommend:</p>
                        <ul>
                            <li>Using a strong, unique password</li>
                            <li>Not sharing your password with anyone</li>
                            <li>Logging out of all devices if you suspect unauthorized access</li>
                        </ul>
                        
                        <p>Best regards,<br>Trung Nguyen</p>
                    </div>
                    <div class="footer">
                        <p>This is an automated email. Please do not reply to this message.</p>
                        <p>&copy; 2025 %s. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(appName, username, appName, appName);
    }
}