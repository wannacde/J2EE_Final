package com.bookstore.service;

import com.bookstore.dto.SignupRequest;
import com.bookstore.dto.SignupResponse;
import com.bookstore.model.AppUser;
import com.bookstore.model.PasswordResetToken;
import com.bookstore.repository.AppUserRepository;
import com.bookstore.repository.PasswordResetTokenRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

	private final AppUserRepository userRepository;
	private final PasswordResetTokenRepository passwordResetTokenRepository;
	private final PasswordEncoder passwordEncoder;
	private final EmailService emailService;

	public SignupResponse signup(SignupRequest request) {
		// Validate passwords match
		if (!request.getPassword().equals(request.getConfirmPassword())) {
			return new SignupResponse("Password và Confirm Password không khớp", false);
		}

		// Check if username already exists
		if (userRepository.existsByUsername(request.getUsername())) {
			return new SignupResponse("Username đã tồn tại", false);
		}

		// Check if email already exists
		if (userRepository.existsByEmail(request.getEmail())) {
			return new SignupResponse("Email đã tồn tại", false);
		}

		try {
			// Create new user
			AppUser newUser = new AppUser(
				request.getUsername(),
				request.getEmail(),
				passwordEncoder.encode(request.getPassword()),
				request.getFullName(),
				"ROLE_USER"
			);

			AppUser savedUser = userRepository.save(newUser);
			log.info("New user registered: {}", request.getUsername());

			SignupResponse response = new SignupResponse();
			response.setId(savedUser.getId());
			response.setUsername(savedUser.getUsername());
			response.setEmail(savedUser.getEmail());
			response.setFullName(savedUser.getFullName());
			response.setMessage("Đăng ký thành công! Vui lòng đăng nhập.");
			response.setSuccess(true);

			return response;
		} catch (Exception e) {
			log.error("Error during signup", e);
			return new SignupResponse("Lỗi đăng ký: " + e.getMessage(), false);
		}
	}

	public AppUser findByUsername(String username) {
		return userRepository.findByUsername(username).orElse(null);
	}

	public AppUser findByEmail(String email) {
		return userRepository.findByEmail(email).orElse(null);
	}

	@Transactional
	public void requestPasswordReset(String email, String appBaseUrl) {
		Optional<AppUser> optionalUser = userRepository.findByEmail(email);
		if (optionalUser.isEmpty()) {
			log.info("Password reset requested for non-existing email: {}", email);
			return;
		}

		AppUser user = optionalUser.get();
		passwordResetTokenRepository.deleteByUser(user);

		String token = UUID.randomUUID().toString();
		PasswordResetToken resetToken = new PasswordResetToken();
		resetToken.setToken(token);
		resetToken.setUser(user);
		resetToken.setUsed(false);
		resetToken.setExpiresAt(LocalDateTime.now().plusHours(24));
		passwordResetTokenRepository.save(resetToken);

		String resetLink = appBaseUrl + "/reset-password?token=" + token;
		emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
		log.info("Password reset link sent to user: {}", user.getUsername());
	}

	public boolean isValidResetToken(String token) {
		return passwordResetTokenRepository.findByTokenAndUsedFalse(token)
			.filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
			.isPresent();
	}

	public SignupResponse resetPassword(String token, String newPassword, String confirmPassword) {
		if (!newPassword.equals(confirmPassword)) {
			return new SignupResponse("Password and confirm password do not match", false);
		}

		Optional<PasswordResetToken> optionalToken = passwordResetTokenRepository.findByTokenAndUsedFalse(token);
		if (optionalToken.isEmpty()) {
			return new SignupResponse("Invalid reset token", false);
		}

		PasswordResetToken resetToken = optionalToken.get();
		if (resetToken.getExpiresAt().isBefore(LocalDateTime.now())) {
			return new SignupResponse("Reset token has expired", false);
		}

		AppUser user = resetToken.getUser();
		user.setPassword(passwordEncoder.encode(newPassword));
		user.setUpdatedAt(LocalDateTime.now());
		userRepository.save(user);

		resetToken.setUsed(true);
		passwordResetTokenRepository.save(resetToken);

		log.info("Password reset completed for user: {}", user.getUsername());
		return new SignupResponse("Password reset successful. Please login with your new password.", true);
	}
}
