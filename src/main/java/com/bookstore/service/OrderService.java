package com.bookstore.service;

import com.bookstore.dto.CheckoutForm;
import com.bookstore.model.AppUser;
import com.bookstore.model.Book;
import com.bookstore.model.BookOrder;
import com.bookstore.model.CartItem;
import com.bookstore.model.OrderStatus;
import com.bookstore.model.OrderStatusHistory;
import com.bookstore.model.OrderDetail;
import com.bookstore.model.ShoppingCart;
import com.bookstore.repository.AppUserRepository;
import com.bookstore.repository.BookOrderRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.OrderStatusHistoryRepository;
import java.time.LocalDateTime;
import java.util.Random;
import org.springframework.web.client.RestTemplate;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

	private final BookOrderRepository bookOrderRepository;
	private final BookRepository bookRepository;
	private final OrderStatusHistoryRepository orderStatusHistoryRepository;
	private final AppUserRepository appUserRepository;
	private final EmailService emailService;
	private final DiscountCodeService discountCodeService;

	public OrderService(
			BookOrderRepository bookOrderRepository,
			BookRepository bookRepository,
			OrderStatusHistoryRepository orderStatusHistoryRepository,
			AppUserRepository appUserRepository,
			EmailService emailService,
			DiscountCodeService discountCodeService) {
		this.bookOrderRepository = bookOrderRepository;
		this.bookRepository = bookRepository;
		this.orderStatusHistoryRepository = orderStatusHistoryRepository;
		this.appUserRepository = appUserRepository;
		this.emailService = emailService;
		this.discountCodeService = discountCodeService;
	}

	@Transactional
	public BookOrder checkout(CheckoutForm form, ShoppingCart shoppingCart, String username) {
		if (shoppingCart.isEmpty()) {
			throw new IllegalStateException("Shopping cart is empty");
		}

		// Lọc các item được chọn, nếu không có selectedItems thì lấy tất cả
		java.util.Map<Long, Integer> selected = form.getSelectedItems();
		java.util.List<CartItem> itemsToCheckout = shoppingCart.getItems().stream()
			.filter(item -> selected == null || selected.containsKey(item.getBookId()))
			.map(item -> {
				if (selected != null && selected.containsKey(item.getBookId())) {
					item.setQuantity(selected.get(item.getBookId()));
				}
				return item;
			})
			.filter(item -> item.getQuantity() > 0)
			.toList();

		if (itemsToCheckout.isEmpty()) {
			throw new IllegalStateException("No items selected for checkout");
		}

		BookOrder order = new BookOrder();
		order.setCustomerName(form.getCustomerName());
		order.setPhoneNumber(form.getPhoneNumber());
		order.setAddress(form.getAddress());
		order.setUsername(username);
		order.setCreatedAt(LocalDateTime.now());

		// Tính total từ các item được chọn
		java.math.BigDecimal total = itemsToCheckout.stream()
			.map(CartItem::getSubtotal)
			.reduce(java.math.BigDecimal.ZERO, java.math.BigDecimal::add);

		String discountCode = form.getDiscountCode();
		if (discountCode != null && !discountCode.isBlank()) {
			java.util.Optional<com.bookstore.model.DiscountCode> codeOpt = discountCodeService.findByCode(discountCode.trim());
			if (codeOpt.isPresent() && discountCodeService.isValid(codeOpt.get())) {
				total = discountCodeService.applyDiscount(codeOpt.get(), total);
				discountCodeService.incrementUsage(codeOpt.get());
			}
		}
		order.setTotalAmount(total);
		order.setStatus(OrderStatus.PENDING);
		order.setLastStatusUpdatedAt(LocalDateTime.now());

		// Kiểm tra tồn kho
		for (CartItem item : itemsToCheckout) {
			Book book = bookRepository.findById(item.getBookId())
				.orElseThrow(() -> new IllegalArgumentException("Book in cart was not found"));
			if (book.getStock() < item.getQuantity()) {
				throw new IllegalStateException("Not enough stock for book: " + book.getTitle());
			}
		}
		// Trừ tồn kho và tạo chi tiết đơn hàng
		for (CartItem item : itemsToCheckout) {
			Book book = bookRepository.findById(item.getBookId())
				.orElseThrow(() -> new IllegalArgumentException("Book in cart was not found"));
			book.setStock(book.getStock() - item.getQuantity());
			bookRepository.save(book);

			OrderDetail detail = new OrderDetail();
			detail.setOrder(order);
			detail.setBook(book);
			detail.setBookTitle(item.getTitle());
			detail.setUnitPrice(item.getPrice());
			detail.setQuantity(item.getQuantity());
			detail.setLineTotal(item.getSubtotal());
			order.getOrderDetails().add(detail);
		}

		BookOrder savedOrder = bookOrderRepository.save(order);
		saveStatusHistory(savedOrder, OrderStatus.PENDING, username, "Order created");
		sendOrderConfirmationEmail(savedOrder);
		return savedOrder;
	}

	@Transactional(readOnly = true)
	public org.springframework.data.domain.Page<BookOrder> findOrdersByUsername(String username, int page) {
		org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(
			Math.max(page, 0), 5, org.springframework.data.domain.Sort.by("createdAt").descending());
		return bookOrderRepository.findByUsername(username, pageable);
	}

	@Transactional(readOnly = true)
	public Optional<BookOrder> findOrderForUser(Long orderId, String username) {
		return bookOrderRepository.findByIdAndUsername(orderId, username);
	}

	@Transactional(readOnly = true)
	public List<OrderStatusHistory> findStatusHistory(Long orderId) {
		return orderStatusHistoryRepository.findByOrderIdOrderByChangedAtDesc(orderId);
	}

	@Transactional
	public Optional<BookOrder> advanceStatus(Long orderId, String username) {
		Optional<BookOrder> optionalOrder = bookOrderRepository.findByIdAndUsername(orderId, username);
		if (optionalOrder.isEmpty()) {
			return Optional.empty();
		}

		BookOrder order = optionalOrder.get();
		OrderStatus nextStatus = nextStatus(order.getStatus());
		if (nextStatus == order.getStatus()) {
			return Optional.of(order);
		}

		order.setStatus(nextStatus);
		order.setLastStatusUpdatedAt(LocalDateTime.now());
		if (nextStatus == OrderStatus.SHIPPED && (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank())) {
			order.setTrackingNumber("TRK-" + order.getId() + "-" + (System.currentTimeMillis() % 100000));
		}

		BookOrder savedOrder = bookOrderRepository.save(order);
		saveStatusHistory(savedOrder, nextStatus, username, "Status moved to " + nextStatus);
		sendStatusEmail(savedOrder);

		return Optional.of(savedOrder);
	}

	/**
	 * Simulate advancing order status using a mock API instead of admin action.
	 */
	@Transactional
	public Optional<BookOrder> advanceStatusWithMockApi(Long orderId) {
		Optional<BookOrder> optionalOrder = bookOrderRepository.findById(orderId);
		if (optionalOrder.isEmpty()) {
			return Optional.empty();
		}

		// Simulate calling an external API to get the next status
		OrderStatus nextStatus = simulateMockApiForOrderStatus(optionalOrder.get().getStatus());
		BookOrder order = optionalOrder.get();
		if (nextStatus == order.getStatus()) {
			return Optional.of(order);
		}

		order.setStatus(nextStatus);
		order.setLastStatusUpdatedAt(LocalDateTime.now());
		if (nextStatus == OrderStatus.SHIPPED && (order.getTrackingNumber() == null || order.getTrackingNumber().isBlank())) {
			order.setTrackingNumber("TRK-" + order.getId() + "-" + (System.currentTimeMillis() % 100000));
		}

		BookOrder savedOrder = bookOrderRepository.save(order);
		saveStatusHistory(savedOrder, nextStatus, "mock-api", "Status updated by mock API to " + nextStatus);
		sendStatusEmail(savedOrder);
		return Optional.of(savedOrder);
	}

	/**
	 * Fake API simulation for order status update.
	 */
	private OrderStatus simulateMockApiForOrderStatus(OrderStatus currentStatus) {
		// Simulate API delay
		try { Thread.sleep(500); } catch (InterruptedException ignored) {}
		// Simulate status progression
		return nextStatus(currentStatus);
	}

	@Transactional(readOnly = true)
	public Optional<BookOrder> findOrderById(Long orderId) {
		return bookOrderRepository.findById(orderId);
	}

	@Transactional
	public Optional<BookOrder> cancelOrder(Long orderId, String username) {
		Optional<BookOrder> optionalOrder = bookOrderRepository.findByIdAndUsername(orderId, username);
		if (optionalOrder.isEmpty()) {
			return Optional.empty();
		}
		BookOrder order = optionalOrder.get();
		if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.DELIVERED || order.getStatus() == OrderStatus.CANCELLED) {
			return Optional.empty();
		}
		// Hoàn lại stock
		for (OrderDetail detail : order.getOrderDetails()) {
			Book book = detail.getBook();
			book.setStock(book.getStock() + detail.getQuantity());
			bookRepository.save(book);
		}
		order.setStatus(OrderStatus.CANCELLED);
		order.setLastStatusUpdatedAt(LocalDateTime.now());
		BookOrder saved = bookOrderRepository.save(order);
		saveStatusHistory(saved, OrderStatus.CANCELLED, username, "Order cancelled by customer");
		return Optional.of(saved);
	}

	private void saveStatusHistory(BookOrder order, OrderStatus status, String changedBy, String note) {
		OrderStatusHistory history = new OrderStatusHistory();
		history.setOrder(order);
		history.setStatus(status);
		history.setChangedAt(LocalDateTime.now());
		history.setChangedBy(changedBy);
		history.setNote(note);
		orderStatusHistoryRepository.save(history);
	}

	private void sendStatusEmail(BookOrder order) {
		Optional<AppUser> optionalUser = appUserRepository.findByUsername(order.getUsername());
		if (optionalUser.isEmpty()) {
			return;
		}

		AppUser user = optionalUser.get();
		String orderId = String.valueOf(order.getId());
		if (order.getStatus() == OrderStatus.SHIPPED) {
			emailService.sendShippingNotificationEmail(user.getEmail(), order.getCustomerName(), orderId, order.getTrackingNumber());
		}
		if (order.getStatus() == OrderStatus.DELIVERED) {
			emailService.sendDeliveryConfirmationEmail(user.getEmail(), order.getCustomerName(), orderId);
		}
	}

	private void sendOrderConfirmationEmail(BookOrder order) {
		Optional<AppUser> optionalUser = appUserRepository.findByUsername(order.getUsername());
		if (optionalUser.isEmpty()) {
			return;
		}

		AppUser user = optionalUser.get();
		emailService.sendOrderConfirmationEmail(user.getEmail(), order.getCustomerName(), String.valueOf(order.getId()));
	}

	private OrderStatus nextStatus(OrderStatus current) {
		if (current == null) {
			return OrderStatus.PENDING;
		}
		return switch (current) {
			case PENDING -> OrderStatus.PROCESSING;
			case PROCESSING -> OrderStatus.SHIPPED;
			case SHIPPED -> OrderStatus.DELIVERED;
			case DELIVERED -> OrderStatus.DELIVERED;
			case CANCELLED -> OrderStatus.CANCELLED;
		};
	}
}