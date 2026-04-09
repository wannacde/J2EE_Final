package com.bookstore.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CartItem implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private Long bookId;
	private String title;
	private BigDecimal price;
	private int quantity;
	private int stock;

	public CartItem(Long bookId, String title, BigDecimal price, int quantity, int stock) {
		this.bookId = bookId;
		this.title = title;
		this.price = price;
		this.quantity = quantity;
		this.stock = stock;
	}

	public BigDecimal getSubtotal() {
		return price.multiply(BigDecimal.valueOf(quantity));
	}
}