package com.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutForm {

	@NotBlank(message = "Please enter the recipient name")
	private String customerName;

	@NotBlank(message = "Please enter the phone number")
	@jakarta.validation.constraints.Pattern(regexp = "^[0-9]{9,11}$", message = "Phone number must be 9-11 digits")
	private String phoneNumber;

	@NotBlank(message = "Please enter the shipping address")
	private String address;

	private String discountCode;

	// bookId -> quantity (chỉ các item được chọn)
	private Map<Long, Integer> selectedItems;
}