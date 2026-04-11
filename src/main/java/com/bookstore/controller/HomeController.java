package com.bookstore.controller;

import com.bookstore.dto.ForgotPasswordRequest;
import com.bookstore.dto.ResetPasswordRequest;
import com.bookstore.dto.SignupRequest;
import com.bookstore.dto.SignupResponse;
import com.bookstore.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class HomeController {

	private final AuthService authService;

	@GetMapping("/")
	public String home() {
		return "redirect:/books";
	}

	@GetMapping("/login")
	public String login() {
		return "auth/login";
	}

	@GetMapping("/signup")
	public String showSignupForm(Model model) {
		model.addAttribute("signupRequest", new SignupRequest());
		return "auth/signup";
	}

	@PostMapping("/signup")
	public String handleSignup(
			@Valid @ModelAttribute("signupRequest") SignupRequest signupRequest,
			BindingResult bindingResult,
			Model model) {
		
		if (bindingResult.hasErrors()) {
			return "auth/signup";
		}

		SignupResponse response = authService.signup(signupRequest);

		if (response.isSuccess()) {
			model.addAttribute("message", response.getMessage());
			return "auth/signup-success";
		} else {
			model.addAttribute("error", response.getMessage());
			return "auth/signup";
		}
	}

	@GetMapping("/forgot-password")
	public String forgotPasswordForm(Model model) {
		if (!model.containsAttribute("forgotPasswordRequest")) {
			model.addAttribute("forgotPasswordRequest", new ForgotPasswordRequest());
		}
		return "auth/forgot-password";
	}

	@PostMapping("/forgot-password")
	public String handleForgotPassword(
			@Valid @ModelAttribute("forgotPasswordRequest") ForgotPasswordRequest request,
			BindingResult bindingResult,
			Model model,
			HttpServletRequest httpRequest) {
		if (bindingResult.hasErrors()) {
			return "auth/forgot-password";
		}

		String baseUrl = httpRequest.getScheme() + "://" + httpRequest.getServerName() + ":" + httpRequest.getServerPort();
		authService.requestPasswordReset(request.getEmail(), baseUrl);
		model.addAttribute("message", "If this email exists, a reset link has been sent.");
		return "auth/forgot-password";
	}

	@GetMapping("/reset-password")
	public String resetPasswordForm(@RequestParam("token") String token, Model model) {
		ResetPasswordRequest request = new ResetPasswordRequest();
		request.setToken(token);
		model.addAttribute("resetPasswordRequest", request);
		model.addAttribute("tokenValid", authService.isValidResetToken(token));
		return "auth/reset-password";
	}

	@PostMapping("/reset-password")
	public String handleResetPassword(
			@Valid @ModelAttribute("resetPasswordRequest") ResetPasswordRequest request,
			BindingResult bindingResult,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (bindingResult.hasErrors()) {
			model.addAttribute("tokenValid", true);
			return "auth/reset-password";
		}

		SignupResponse response = authService.resetPassword(
			request.getToken(),
			request.getPassword(),
			request.getConfirmPassword()
		);

		if (response.isSuccess()) {
			redirectAttributes.addFlashAttribute("message", "Password reset successful. Please login with your new password.");
			return "redirect:/login";
		} else {
			model.addAttribute("tokenValid", authService.isValidResetToken(request.getToken()));
			model.addAttribute("error", response.getMessage());
			return "auth/reset-password";
		}
	}
}