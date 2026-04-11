
package com.bookstore;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.bookstore.model.Book;
import com.bookstore.model.BookOrder;
import com.bookstore.model.OrderStatus;
import com.bookstore.model.PasswordResetToken;
import com.bookstore.repository.BookOrderRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import com.bookstore.repository.OrderStatusHistoryRepository;
import com.bookstore.repository.OrderDetailRepository;
import com.bookstore.repository.PasswordResetTokenRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class BookstoreApplicationTests {

	@Test
	void shouldSubmitAndDisplayBookReview() throws Exception {
		Book book = findBookByTitle("Atomic Habits");

		// Submit a review as user
		mockMvc.perform(post("/reviews/add/" + book.getId())
				.with(user("user").roles("USER"))
				.with(csrf())
				.param("rating", "5")
				.param("comment", "Great book!"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/books/" + book.getId()));

		// Check that the review appears on the book detail page
		MvcResult result = mockMvc.perform(get("/books/" + book.getId())
				.with(user("user").roles("USER")))
			.andExpect(status().isOk())
			.andReturn();
		String content = result.getResponse().getContentAsString();
		assertThat(content, containsString("Great book!"));
		assertThat(content, containsString("5"));
		assertThat(content, containsString("user"));
	}

	@Test
	void shouldShowLowStockWarningInAdminList() throws Exception {
		// Đảm bảo có ít nhất 1 sách tồn kho thấp
		Book lowStockBook = bookRepository.findAll().stream()
			.filter(b -> b.getStock() > 0)
			.findFirst().orElseThrow();
		lowStockBook.setStock(3);
		bookRepository.save(lowStockBook);

		MvcResult result = mockMvc.perform(get("/admin/books").with(user("admin").roles("ADMIN")))
			.andExpect(status().isOk())
			.andReturn();
		String content = result.getResponse().getContentAsString();
		// Kiểm tra hiển thị số tồn kho và cảnh báo "Low!"
		assertThat(content, containsString(">3<"));
		assertThat(content, containsString("Low!"));
	}

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private BookRepository bookRepository;

	@Autowired
	private BookOrderRepository bookOrderRepository;

	@Autowired
	private CategoryRepository categoryRepository;

	@Autowired
	private OrderDetailRepository orderDetailRepository;

	@Autowired
	private PasswordResetTokenRepository passwordResetTokenRepository;

	@Autowired
	private OrderStatusHistoryRepository orderStatusHistoryRepository;

	@MockBean
	private JavaMailSender javaMailSender;

	@Test
	void contextLoads() {
	}

	@Test
	void shouldSearchBooksByKeyword() throws Exception {
		MvcResult result = mockMvc.perform(get("/books").param("keyword", "Clean"))
			.andExpect(status().isOk())
			.andReturn();

		String content = result.getResponse().getContentAsString();
		assertThat(content, containsString("Clean Code"));
		assertThat(content, not(containsString("Atomic Habits")));
	}

	@Test
	void shouldPaginateFiveBooksPerPage() throws Exception {
		MvcResult result = mockMvc.perform(get("/books")
			.param("page", "1")
			.param("sort", "asc"))
			.andExpect(status().isOk())
			.andReturn();

		String content = result.getResponse().getContentAsString();
		assertThat(content, containsString("Deep Work"));
		assertThat(content, containsString("Head First Design Patterns"));
		assertThat(content, not(containsString("Atomic Habits")));
		assertThat(content, containsString("Previous"));
		assertThat(content, containsString("Next"));
	}

	@Test
	void shouldSortBooksByPriceAscendingAndDescending() throws Exception {
		String ascendingContent = mockMvc.perform(get("/books")
				.param("sort", "asc")
				.param("page", "0"))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertComesBefore(ascendingContent, "Give Me a Ticket to Childhood", "Atomic Habits");

		String descendingContent = mockMvc.perform(get("/books")
				.param("sort", "desc")
				.param("page", "0"))
			.andExpect(status().isOk())
			.andReturn()
			.getResponse()
			.getContentAsString();
		assertComesBefore(descendingContent, "Java Concurrency In Practice", "The Lean Startup");
	}

	@Test
	void shouldFilterBooksByCategory() throws Exception {
		Long programmingCategoryId = categoryRepository.findByName("Programming")
			.orElseThrow()
			.getId();

		MvcResult result = mockMvc.perform(get("/books").param("categoryId", programmingCategoryId.toString()))
			.andExpect(status().isOk())
			.andReturn();

		String content = result.getResponse().getContentAsString();
		assertThat(content, containsString("Clean Code"));
		assertThat(content, containsString("Effective Java"));
		assertThat(content, not(containsString("Atomic Habits")));
	}

	@Test
	void shouldAddBookToSessionCartAndShowTotals() throws Exception {
		Book book = findBookByTitle("Atomic Habits");
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/cart/add")
				.session(session)
				.with(csrf())
				.param("bookId", book.getId().toString())
				.param("quantity", "2")
				.param("redirectUrl", "/books"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/books"));

		MvcResult cartResult = mockMvc.perform(get("/cart").session(session))
			.andExpect(status().isOk())
			.andReturn();

		String content = cartResult.getResponse().getContentAsString();
		assertThat(content, containsString("Atomic Habits"));
		assertThat(content, containsString("198,000 ₫"));
		assertThat(content, containsString("value=\"2\""));
	}

	@Test
	void shouldCreateOrderDetailsAndTotalWhenCheckout() throws Exception {
		Book atomicHabits = findBookByTitle("Atomic Habits");
		Book cleanCode = findBookByTitle("Clean Code");
		MockHttpSession session = new MockHttpSession();
		long initialOrderCount = bookOrderRepository.count();

		mockMvc.perform(post("/cart/add")
				.session(session)
				.with(csrf())
				.param("bookId", atomicHabits.getId().toString())
				.param("quantity", "2")
				.param("redirectUrl", "/books"))
			.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/cart/add")
				.session(session)
				.with(csrf())
				.param("bookId", cleanCode.getId().toString())
				.param("quantity", "1")
				.param("redirectUrl", "/books"))
			.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/orders/checkout")
				.session(session)
				.with(user("user").roles("USER"))
				.with(csrf())
				.param("customerName", "Nguyen Van A")
				.param("phoneNumber", "0901234567")
				.param("address", "123 Duong ABC"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/orders/success"));

		assertEquals(initialOrderCount + 1, bookOrderRepository.count());

		BookOrder savedOrder = bookOrderRepository.findAll().stream()
			.max(Comparator.comparing(BookOrder::getId))
			.orElseThrow();

		assertNotNull(savedOrder.getCreatedAt());
		assertEquals("Nguyen Van A", savedOrder.getCustomerName());
		assertEquals("123 Duong ABC", savedOrder.getAddress());
		assertEquals("user", savedOrder.getUsername());
		assertEquals(0, savedOrder.getTotalAmount().compareTo(new BigDecimal("318000")));
		assertEquals(2, orderDetailRepository.countByOrderId(savedOrder.getId()));
		assertEquals(OrderStatus.PENDING, savedOrder.getStatus());
	}

	@Test
	void shouldGeneratePasswordResetTokenWhenForgotPassword() throws Exception {
		mockMvc.perform(post("/forgot-password")
				.with(csrf())
				.param("email", "user@bookstore.com"))
			.andExpect(status().isOk());

		boolean tokenExists = passwordResetTokenRepository.existsByUserUsernameAndUsedFalse("user");
		assertTrue(tokenExists, "Expected unused password reset token for user");
	}

	@Test
	void shouldResetPasswordWithValidToken() throws Exception {
		mockMvc.perform(post("/forgot-password")
				.with(csrf())
				.param("email", "user@bookstore.com"))
			.andExpect(status().isOk());

		PasswordResetToken token = passwordResetTokenRepository
			.findTopByUserUsernameAndUsedFalseOrderByIdDesc("user")
			.orElseThrow();

		mockMvc.perform(post("/reset-password")
				.with(csrf())
				.param("token", token.getToken())
				.param("password", "newpass123")
				.param("confirmPassword", "newpass123"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/login"));

		PasswordResetToken updatedToken = passwordResetTokenRepository.findById(token.getId()).orElseThrow();
		assertTrue(updatedToken.isUsed(), "Expected reset token marked as used");

		mockMvc.perform(post("/login")
				.with(csrf())
				.param("username", "user")
				.param("password", "newpass123"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/books"));
	}

	@Test
	void shouldAdvanceOrderStatus() throws Exception {
		Book atomicHabits = findBookByTitle("Atomic Habits");
		MockHttpSession session = new MockHttpSession();

		mockMvc.perform(post("/cart/add")
				.session(session)
				.with(csrf())
				.param("bookId", atomicHabits.getId().toString())
				.param("quantity", "1")
				.param("redirectUrl", "/books"))
			.andExpect(status().is3xxRedirection());

		mockMvc.perform(post("/orders/checkout")
				.session(session)
				.with(user("user").roles("USER"))
				.with(csrf())
				.param("customerName", "Order Test")
				.param("phoneNumber", "0901234567")
				.param("address", "1 Test Street"))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/orders/success"));

		BookOrder order = bookOrderRepository.findAll().stream()
			.max(Comparator.comparing(BookOrder::getId))
			.orElseThrow();

		// Regular user can advance their own order (demo button available to all)
		mockMvc.perform(post("/orders/" + order.getId() + "/advance-status")
				.with(user("user").roles("USER"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/orders/" + order.getId()));

		// Admin can also advance order status
		mockMvc.perform(post("/orders/" + order.getId() + "/advance-status")
				.with(user("admin").roles("ADMIN"))
				.with(csrf()))
			.andExpect(status().is3xxRedirection())
			.andExpect(redirectedUrl("/orders/" + order.getId()));

		// After 2 advances: PENDING -> PROCESSING -> SHIPPED
		BookOrder updatedOrder = bookOrderRepository.findById(order.getId()).orElseThrow();
		assertEquals(OrderStatus.SHIPPED, updatedOrder.getStatus());
		assertTrue(orderStatusHistoryRepository.findByOrderIdOrderByChangedAtDesc(order.getId()).size() >= 2);
	}

	private void assertComesBefore(String content, String first, String second) {
		int firstIndex = content.indexOf(first);
		int secondIndex = content.indexOf(second);
		assertTrue(firstIndex >= 0, "Expected to find: " + first);
		assertTrue(secondIndex >= 0, "Expected to find: " + second);
		assertFalse(firstIndex > secondIndex, () -> first + " should appear before " + second);
	}

	private Book findBookByTitle(String title) {
		return bookRepository.findAll().stream()
			.filter(book -> title.equals(book.getTitle()))
			.findFirst()
			.orElseThrow();
	}

}
