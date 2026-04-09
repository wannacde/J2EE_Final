package com.bookstore.controller;

import com.bookstore.dto.CheckoutForm;
import com.bookstore.model.BookOrder;
import com.bookstore.model.ShoppingCart;
import com.bookstore.service.OrderService;
import jakarta.validation.Valid;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class OrderController {

	private final ShoppingCart shoppingCart;
	private final OrderService orderService;

	public OrderController(ShoppingCart shoppingCart, OrderService orderService) {
		this.shoppingCart = shoppingCart;
		this.orderService = orderService;
	}

	@GetMapping("/orders/checkout")
	public String checkoutPage(
			@org.springframework.web.bind.annotation.RequestParam(required = false) java.util.Map<String, String> allParams,
			Model model) {
		if (shoppingCart.isEmpty()) {
			return "redirect:/cart?empty";
		}
		if (!model.containsAttribute("checkoutForm")) {
			model.addAttribute("checkoutForm", new CheckoutForm());
		}
		// Parse selected items từ query params: selected[bookId]=qty
		java.util.Map<Long, Integer> selectedItems = new java.util.HashMap<>();
		if (allParams != null) {
			allParams.forEach((key, value) -> {
				if (key.startsWith("selected[")) {
					try {
						Long bookId = Long.parseLong(key.replaceAll("selected\\[|\\]", ""));
						int qty = Integer.parseInt(value);
						if (qty > 0) selectedItems.put(bookId, qty);
					} catch (NumberFormatException ignored) {}
				}
			});
		}
		model.addAttribute("cart", shoppingCart);
		model.addAttribute("selectedItems", selectedItems.isEmpty() ? null : selectedItems);
		return "order/checkout";
	}

	@PostMapping("/orders/checkout")
	public String processCheckout(
			@Valid @ModelAttribute("checkoutForm") CheckoutForm checkoutForm,
			BindingResult bindingResult,
			Authentication authentication,
			@org.springframework.web.bind.annotation.RequestParam(required = false) java.util.Map<String, String> allParams,
			Model model,
			RedirectAttributes redirectAttributes) {
		if (shoppingCart.isEmpty()) {
			return "redirect:/cart?empty";
		}
		// Parse selectedItems[bookId]=quantity từ request params
		java.util.Map<Long, Integer> selectedItems = new java.util.HashMap<>();
		if (allParams != null) {
			allParams.forEach((key, value) -> {
				if (key.startsWith("selectedItems[")) {
					try {
						Long bookId = Long.parseLong(key.replaceAll("selectedItems\\[|\\]", ""));
						int qty = Integer.parseInt(value);
						if (qty > 0) selectedItems.put(bookId, qty);
					} catch (NumberFormatException ignored) {}
				}
			});
		}
		if (!selectedItems.isEmpty()) {
			checkoutForm.setSelectedItems(selectedItems);
		}
		if (bindingResult.hasErrors()) {
			model.addAttribute("cart", shoppingCart);
			return "order/checkout";
		}
		try {
			BookOrder order = orderService.checkout(checkoutForm, shoppingCart, authentication.getName());
			// Chỉ xóa các item đã checkout khỏi cart
			if (!selectedItems.isEmpty()) {
				selectedItems.keySet().forEach(shoppingCart::remove);
			} else {
				shoppingCart.clear();
			}
			redirectAttributes.addFlashAttribute("order", order);
			return "redirect:/orders/success";
		} catch (IllegalStateException ex) {
			if (ex.getMessage().contains("No items selected")) {
				return "redirect:/orders/checkout?noItems=1";
			}
			return "redirect:/orders/checkout?stockError=1";
		}
	}

	@GetMapping("/orders/success")
	public String successPage(Model model) {
		if (!model.containsAttribute("order")) {
			return "redirect:/books";
		}
		return "order/success";
	}

	@GetMapping("/orders")
	public String listMyOrders(
			Authentication authentication,
			@org.springframework.web.bind.annotation.RequestParam(defaultValue = "0") int page,
			Model model) {
		var orderPage = orderService.findOrdersByUsername(authentication.getName(), page);
		model.addAttribute("orderPage", orderPage);
		model.addAttribute("orders", orderPage.getContent());
		return "order/list";
	}

	@GetMapping("/orders/{orderId}")
	public String orderDetail(@PathVariable Long orderId, Authentication authentication, Model model) {
		boolean isAdmin = authentication.getAuthorities().stream()
			.anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));

		Optional<BookOrder> optionalOrder = isAdmin
			? orderService.findOrderById(orderId)
			: orderService.findOrderForUser(orderId, authentication.getName());
		if (optionalOrder.isEmpty()) {
			return "redirect:/orders?not-found";
		}

		model.addAttribute("order", optionalOrder.get());
		model.addAttribute("statusHistory", orderService.findStatusHistory(orderId));
		return "order/detail";
	}

	@PostMapping("/orders/{orderId}/cancel")
	public String cancelOrder(
			@PathVariable Long orderId,
			Authentication authentication,
			RedirectAttributes redirectAttributes) {
		Optional<BookOrder> result = orderService.cancelOrder(orderId, authentication.getName());
		if (result.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Cannot cancel this order.");
		} else {
			redirectAttributes.addFlashAttribute("message", "Order cancelled successfully.");
		}
		return "redirect:/orders/" + orderId;
	}

	@PostMapping("/orders/{orderId}/advance-status")
	public String advanceOrderStatusWithMockApi(
			@PathVariable Long orderId,
			RedirectAttributes redirectAttributes) {
		Optional<BookOrder> updatedOrder = orderService.advanceStatusWithMockApi(orderId);
		if (updatedOrder.isEmpty()) {
			redirectAttributes.addFlashAttribute("error", "Order not found or no permission.");
			return "redirect:/orders";
		}
		redirectAttributes.addFlashAttribute("message", "Order status updated to " + updatedOrder.get().getStatus());
		return "redirect:/orders/" + orderId;
	}
}