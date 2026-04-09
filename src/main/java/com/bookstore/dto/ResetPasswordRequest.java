package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest {

	@NotBlank(message = "Token is required")
	private String token;

	@NotBlank(message = "Password is required")
	@Size(min = 6, max = 100, message = "Password must be between 6 and 100 characters")
	private String password;

	@NotBlank(message = "Please confirm password")
	private String confirmPassword;
}
