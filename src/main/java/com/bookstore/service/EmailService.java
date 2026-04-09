package com.bookstore.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

	private final JavaMailSender mailSender;

	@Value("${app.email.enabled:false}")
	private boolean emailEnabled;

	@Value("${spring.mail.from:${spring.mail.username:noreply@bookstore.local}}")
	private String fromEmail;

	@Value("${spring.mail.username:}")
	private String mailUsername;

	@Value("${spring.mail.password:}")
	private String mailPassword;

	/**
	 * Send simple text email
	 */
	public void sendSimpleEmail(String to, String subject, String text) {
		if (shouldSkipSending()) {
			return;
		}
		try {
			SimpleMailMessage message = new SimpleMailMessage();
			message.setFrom(fromEmail);
			message.setTo(to);
			message.setSubject(subject);
			message.setText(text);

			mailSender.send(message);
			log.info("Simple email sent to: {}", to);
		} catch (Exception e) {
			log.error("Failed to send simple email to: {}", to, e);
		}
	}

	/**
	 * Send HTML email
	 */
	public void sendHtmlEmail(String to, String subject, String htmlContent) {
		if (shouldSkipSending()) {
			return;
		}
		try {
			MimeMessage message = mailSender.createMimeMessage();
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

			helper.setFrom(fromEmail);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlContent, true);

			mailSender.send(message);
			log.info("HTML email sent to: {}", to);
		} catch (MessagingException e) {
			log.error("Failed to send HTML email to: {}", to, e);
		}
	}

	private boolean shouldSkipSending() {
		if (!emailEnabled) {
			log.debug("Email sending is disabled by app.email.enabled=false");
			return true;
		}

		boolean hasPlaceholderCreds =
			mailUsername == null || mailPassword == null
				|| mailUsername.isBlank() || mailPassword.isBlank()
				|| mailUsername.contains("your-email") || mailPassword.contains("your-app-password");

		if (hasPlaceholderCreds) {
			log.warn("Email sending skipped because SMTP credentials are placeholders or empty.");
			return true;
		}

		return false;
	}

	/**
	 * Send email asynchronously (non-blocking)
	 */
	@Async
	public void sendSimpleEmailAsync(String to, String subject, String text) {
		sendSimpleEmail(to, subject, text);
	}

	/**
	 * Send HTML email asynchronously (non-blocking)
	 */
	@Async
	public void sendHtmlEmailAsync(String to, String subject, String htmlContent) {
		sendHtmlEmail(to, subject, htmlContent);
	}

	/**
	 * Send order confirmation email
	 */
	@Async
	public void sendOrderConfirmationEmail(String to, String customerName, String orderId) {
		String subject = "Order Confirmation - " + orderId;
		String htmlContent = buildOrderConfirmationEmail(customerName, orderId);
		sendHtmlEmail(to, subject, htmlContent);
	}

	/**
	 * Send shipping notification email
	 */
	@Async
	public void sendShippingNotificationEmail(String to, String customerName, String orderId, String trackingNumber) {
		String subject = "Your order has been shipped - " + orderId;
		String htmlContent = buildShippingNotificationEmail(customerName, orderId, trackingNumber);
		sendHtmlEmail(to, subject, htmlContent);
	}

	/**
	 * Send delivery confirmation email
	 */
	@Async
	public void sendDeliveryConfirmationEmail(String to, String customerName, String orderId) {
		String subject = "Your order has been delivered - " + orderId;
		String htmlContent = buildDeliveryConfirmationEmail(customerName, orderId);
		sendHtmlEmail(to, subject, htmlContent);
	}

	/**
	 * Send password reset email
	 */
	@Async
	public void sendPasswordResetEmail(String to, String resetLink) {
		String subject = "Password Reset Request";
		String htmlContent = buildPasswordResetEmail(resetLink);
		sendHtmlEmail(to, subject, htmlContent);
	}

	/**
	 * Send email verification email
	 */
	@Async
	public void sendEmailVerificationEmail(String to, String verificationLink) {
		String subject = "Verify Your Email Address";
		String htmlContent = buildEmailVerificationEmail(verificationLink);
		sendHtmlEmail(to, subject, htmlContent);
	}

	// ==================== EMAIL TEMPLATE BUILDERS ====================

	private String buildOrderConfirmationEmail(String customerName, String orderId) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<style>
					body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
					.container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
					.header { color: #333; border-bottom: 2px solid #f59e0b; padding-bottom: 10px; margin-bottom: 20px; }
					.content { color: #666; line-height: 1.6; margin: 15px 0; }
					.button { display: inline-block; background: #f59e0b; color: white; padding: 10px 20px; border-radius: 5px; text-decoration: none; margin: 15px 0; }
					.footer { background: #f5f5f5; padding: 15px; text-align: center; color: #999; font-size: 12px; border-radius: 5px; margin-top: 20px; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<h1>Order Confirmation</h1>
					</div>
					<div class="content">
						<p>Dear <strong>%s</strong>,</p>
						<p>Thank you for your order!</p>
						<p><strong>Order ID:</strong> %s</p>
						<p>Your order has been received and is being processed. You will receive a shipping notification soon with tracking information.</p>
						<p><a href="#" class="button">Track Your Order</a></p>
						<p>If you have any questions, please contact our support team.</p>
						<p>Best regards,<br><strong>Bookstore Team</strong></p>
					</div>
					<div class="footer">
						<p>This is an automated email. Please do not reply directly.</p>
					</div>
				</div>
			</body>
			</html>
			""", customerName, orderId);
	}

	private String buildShippingNotificationEmail(String customerName, String orderId, String trackingNumber) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<style>
					body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
					.container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
					.header { color: #333; border-bottom: 2px solid #f59e0b; padding-bottom: 10px; margin-bottom: 20px; }
					.content { color: #666; line-height: 1.6; margin: 15px 0; }
					.tracking-box { background: #f9f9f9; padding: 15px; border-left: 4px solid #f59e0b; margin: 15px 0; }
					.footer { background: #f5f5f5; padding: 15px; text-align: center; color: #999; font-size: 12px; border-radius: 5px; margin-top: 20px; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<h1>Your Order Has Been Shipped!</h1>
					</div>
					<div class="content">
						<p>Dear <strong>%s</strong>,</p>
						<p>Great news! Your order has been shipped and is on its way to you.</p>
						<p><strong>Order ID:</strong> %s</p>
						<div class="tracking-box">
							<strong>Tracking Number:</strong> %s
						</div>
						<p>You can track your package using the tracking number above. This usually takes 2-3 business days to arrive.</p>
						<p>If you have any questions, contact our support team.</p>
						<p>Best regards,<br><strong>Bookstore Team</strong></p>
					</div>
					<div class="footer">
						<p>This is an automated email. Please do not reply directly.</p>
					</div>
				</div>
			</body>
			</html>
			""", customerName, orderId, trackingNumber);
	}

	private String buildDeliveryConfirmationEmail(String customerName, String orderId) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<style>
					body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
					.container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
					.header { color: #333; border-bottom: 2px solid #10b981; padding-bottom: 10px; margin-bottom: 20px; }
					.content { color: #666; line-height: 1.6; margin: 15px 0; }
					.footer { background: #f5f5f5; padding: 15px; text-align: center; color: #999; font-size: 12px; border-radius: 5px; margin-top: 20px; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<h1>Order Delivered</h1>
					</div>
					<div class="content">
						<p>Dear <strong>%s</strong>,</p>
						<p>Your order has been successfully delivered!</p>
						<p><strong>Order ID:</strong> %s</p>
						<p>We hope you enjoy your books. Please rate your experience and leave a review.</p>
						<p>Thank you for shopping with us!</p>
						<p>Best regards,<br><strong>Bookstore Team</strong></p>
					</div>
					<div class="footer">
						<p>This is an automated email. Please do not reply directly.</p>
					</div>
				</div>
			</body>
			</html>
			""", customerName, orderId);
	}

	private String buildPasswordResetEmail(String resetLink) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<style>
					body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
					.container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
					.header { color: #333; border-bottom: 2px solid #f59e0b; padding-bottom: 10px; margin-bottom: 20px; }
					.content { color: #666; line-height: 1.6; margin: 15px 0; }
					.button { display: inline-block; background: #f59e0b; color: white; padding: 10px 20px; border-radius: 5px; text-decoration: none; margin: 15px 0; }
					.warning { color: #d97706; font-size: 12px; margin: 10px 0; }
					.footer { background: #f5f5f5; padding: 15px; text-align: center; color: #999; font-size: 12px; border-radius: 5px; margin-top: 20px; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<h1>Password Reset Request</h1>
					</div>
					<div class="content">
						<p>We received a request to reset your password. Click the button below to create a new password.</p>
						<p><a href=\"%s\" class="button">Reset Password</a></p>
						<p class="warning">This link will expire in 24 hours.</p>
						<p>If you didn't request a password reset, please ignore this email and your password will remain unchanged.</p>
						<p>Best regards,<br><strong>Bookstore Team</strong></p>
					</div>
					<div class="footer">
						<p>This is an automated email. Please do not reply directly.</p>
					</div>
				</div>
			</body>
			</html>
			""", resetLink);
	}

	private String buildEmailVerificationEmail(String verificationLink) {
		return String.format("""
			<!DOCTYPE html>
			<html>
			<head>
				<meta charset="UTF-8">
				<style>
					body { font-family: Arial, sans-serif; background-color: #f5f5f5; }
					.container { max-width: 600px; margin: 0 auto; background: white; padding: 20px; border-radius: 8px; }
					.header { color: #333; border-bottom: 2px solid #10b981; padding-bottom: 10px; margin-bottom: 20px; }
					.content { color: #666; line-height: 1.6; margin: 15px 0; }
					.button { display: inline-block; background: #10b981; color: white; padding: 10px 20px; border-radius: 5px; text-decoration: none; margin: 15px 0; }
					.footer { background: #f5f5f5; padding: 15px; text-align: center; color: #999; font-size: 12px; border-radius: 5px; margin-top: 20px; }
				</style>
			</head>
			<body>
				<div class="container">
					<div class="header">
						<h1>Verify Your Email Address</h1>
					</div>
					<div class="content">
						<p>Welcome to Bookstore! Please verify your email address to activate your account.</p>
						<p><a href=\"%s\" class="button">Verify Email</a></p>
						<p>This link will expire in 24 hours.</p>
						<p>If you didn't create this account, please ignore this email.</p>
						<p>Best regards,<br><strong>Bookstore Team</strong></p>
					</div>
					<div class="footer">
						<p>This is an automated email. Please do not reply directly.</p>
					</div>
				</div>
			</body>
			</html>
			""", verificationLink);
	}
}
