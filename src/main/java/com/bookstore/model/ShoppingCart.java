package com.bookstore.model;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@Scope(value = WebApplicationContext.SCOPE_SESSION, proxyMode = ScopedProxyMode.TARGET_CLASS)
public class ShoppingCart implements Serializable {

	@Serial
	private static final long serialVersionUID = 1L;

	private final Map<Long, CartItem> items = new LinkedHashMap<>();

	public void addBook(Book book, int quantity) {
		int safeQuantity = Math.max(quantity, 1);
		CartItem existing = items.get(book.getId());
		if (existing == null) {
			items.put(book.getId(), new CartItem(book.getId(), book.getTitle(), book.getPrice(), safeQuantity, book.getStock()));
			return;
		}
		existing.setQuantity(existing.getQuantity() + safeQuantity);
	}

	public void updateQuantity(Long bookId, int quantity) {
		if (!items.containsKey(bookId)) {
			return;
		}
		if (quantity <= 0) {
			items.remove(bookId);
			return;
		}
		items.get(bookId).setQuantity(quantity);
	}

	public void remove(Long bookId) {
		items.remove(bookId);
	}

	public Collection<CartItem> getItems() {
		return items.values();
	}

	public int getTotalQuantity() {
		return items.values().stream().mapToInt(CartItem::getQuantity).sum();
	}

	public BigDecimal getTotalAmount() {
		return items.values().stream()
			.map(CartItem::getSubtotal)
			.reduce(BigDecimal.ZERO, BigDecimal::add);
	}

	public boolean isEmpty() {
		return items.isEmpty();
	}

	public void clear() {
		items.clear();
	}
}