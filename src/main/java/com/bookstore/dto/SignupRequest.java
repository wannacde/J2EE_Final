package com.bookstore.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupRequest {

	@NotBlank(message = "Username không được trống")
	@Size(min = 3, max = 20, message = "Username phải từ 3-20 ký tự")
	private String username;

	@NotBlank(message = "Email không được trống")
	@Email(message = "Email không hợp lệ")
	private String email;

	@NotBlank(message = "Full Name không được trống")
	@Size(min = 3, max = 50, message = "Full Name phải từ 3-50 ký tự")
	private String fullName;

	@NotBlank(message = "Password không được trống")
	@Size(min = 6, max = 100, message = "Password phải từ 6-100 ký tự")
	private String password;

	@NotBlank(message = "Xác nhân password không được trống")
	private String confirmPassword;
}
