package com.bookstore.controller;

import com.bookstore.model.ShoppingCart;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.BookService;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {

	private final ShoppingCart shoppingCart;
	private final BookService bookService;
	private final BookRepository bookRepository;

	public CartController(ShoppingCart shoppingCart, BookService bookService, BookRepository bookRepository) {
		this.shoppingCart = shoppingCart;
		this.bookService = bookService;
		this.bookRepository = bookRepository;
	}

	@PostMapping("/cart/add")
	public String addToCart(
			@RequestParam Long bookId,
			@RequestParam(defaultValue = "1") int quantity,
			@RequestParam(defaultValue = "/books") String redirectUrl) {
		shoppingCart.addBook(bookService.getBookById(bookId), quantity);
		return "redirect:" + redirectUrl;
	}

	@GetMapping("/cart")
	public String viewCart(Model model) {
		model.addAttribute("cart", shoppingCart);
		// Lấy stock hiện tại từ DB cho từng item trong cart
		Map<Long, Integer> stockMap = shoppingCart.getItems().stream()
			.collect(Collectors.toMap(
				item -> item.getBookId(),
				item -> bookRepository.findById(item.getBookId())
					.map(b -> b.getStock()).orElse(0)
			));
		model.addAttribute("stockMap", stockMap);
		return "cart/view";
	}

	@PostMapping("/cart/update")
	public String updateQuantity(@RequestParam Long bookId, @RequestParam int quantity) {
		shoppingCart.updateQuantity(bookId, quantity);
		return "redirect:/cart";
	}

	@PostMapping("/cart/remove")
	public String removeItem(@RequestParam Long bookId) {
		shoppingCart.remove(bookId);
		return "redirect:/cart";
	}
}