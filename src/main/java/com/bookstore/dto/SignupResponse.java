package com.bookstore.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SignupResponse {

	private Long id;
	private String username;
	private String email;
	private String fullName;
	private String message;
	private boolean success;

	public SignupResponse(String message, boolean success) {
		this.message = message;
		this.success = success;
	}
}
