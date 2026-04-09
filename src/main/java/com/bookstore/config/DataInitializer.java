package com.bookstore.config;

import com.bookstore.model.AppUser;
import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.AppUserRepository;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

	private final CategoryRepository categoryRepository;
	private final BookRepository bookRepository;
	private final AppUserRepository appUserRepository;
	private final PasswordEncoder passwordEncoder;

	public DataInitializer(
			CategoryRepository categoryRepository,
			BookRepository bookRepository,
			AppUserRepository appUserRepository,
			PasswordEncoder passwordEncoder) {
		this.categoryRepository = categoryRepository;
		this.bookRepository = bookRepository;
		this.appUserRepository = appUserRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(String... args) {
		seedCategoriesAndBooks();
		seedDefaultUser();
		seedDefaultAdmin();
	}

	private void seedCategoriesAndBooks() {
		if (bookRepository.count() > 0) {
			normalizeExistingBooksToEnglish();
			return;
		}

		Category programming = categoryRepository.save(new Category("Programming"));
		Category business = categoryRepository.save(new Category("Business"));
		Category novel = categoryRepository.save(new Category("Novel"));

		bookRepository.saveAll(List.of(
			new Book("Clean Code", "Robert C. Martin", "A practical guide to writing clean and maintainable code.", new BigDecimal("120000"), "https://images.unsplash.com/photo-1512820790803-83ca734da794", programming, 20),
			new Book("Spring In Action", "Craig Walls", "Hands-on reference for Spring Framework and Spring Boot.", new BigDecimal("185000"), "https://images.unsplash.com/photo-1495446815901-a7297e633e8d", programming, 15),
			new Book("Effective Java", "Joshua Bloch", "Best practices for professional Java development.", new BigDecimal("210000"), "https://images.unsplash.com/photo-1516979187457-637abb4f9353", programming, 18),
			new Book("Head First Design Patterns", "Eric Freeman", "Learn design patterns through a visual and intuitive approach.", new BigDecimal("199000"), "https://images.unsplash.com/photo-1519682577862-22b62b24e493", programming, 12),
			new Book("Atomic Habits", "James Clear", "Build good habits and break bad ones with small daily changes.", new BigDecimal("99000"), "https://images.unsplash.com/photo-1511108690759-009324a90311", business, 25),
			new Book("The Lean Startup", "Eric Ries", "A modern framework for launching products with rapid feedback.", new BigDecimal("135000"), "https://images.unsplash.com/photo-1512820790803-83ca734da794", business, 10),
			new Book("Deep Work", "Cal Newport", "Train deep focus to produce high-value work in less time.", new BigDecimal("110000"), "https://images.unsplash.com/photo-1495446815901-a7297e633e8d", business, 14),
			new Book("The Alchemist", "Paulo Coelho", "A symbolic journey of a shepherd searching for his treasure.", new BigDecimal("85000"), "https://images.unsplash.com/photo-1516979187457-637abb4f9353", novel, 30),
			new Book("Give Me a Ticket to Childhood", "Nguyen Nhat Anh", "A nostalgic novel reflecting on childhood memories.", new BigDecimal("78000"), "https://images.unsplash.com/photo-1519682577862-22b62b24e493", novel, 22),
			new Book("How to Win Friends and Influence People", "Dale Carnegie", "Timeless communication and relationship-building principles.", new BigDecimal("95000"), "https://images.unsplash.com/photo-1511108690759-009324a90311", business, 17),
			new Book("Java Concurrency In Practice", "Brian Goetz", "An advanced guide to concurrent programming in Java.", new BigDecimal("225000"), "https://images.unsplash.com/photo-1512820790803-83ca734da794", programming, 8),
			new Book("Sherlock Holmes", "Arthur Conan Doyle", "A classic collection of detective stories featuring Sherlock Holmes.", new BigDecimal("89000"), "https://images.unsplash.com/photo-1495446815901-a7297e633e8d", novel, 16)
		));
	}

	private void normalizeExistingBooksToEnglish() {
		Map<String, String> titleMap = Map.of(
			"Nha Gia Kim", "The Alchemist",
			"Cho Toi Xin Mot Ve Di Tuoi Tho", "Give Me a Ticket to Childhood",
			"Dac Nhan Tam", "How to Win Friends and Influence People"
		);

		Map<String, String> descriptionByTitle = Map.ofEntries(
			Map.entry("Clean Code", "A practical guide to writing clean and maintainable code."),
			Map.entry("Spring In Action", "Hands-on reference for Spring Framework and Spring Boot."),
			Map.entry("Effective Java", "Best practices for professional Java development."),
			Map.entry("Head First Design Patterns", "Learn design patterns through a visual and intuitive approach."),
			Map.entry("Atomic Habits", "Build good habits and break bad ones with small daily changes."),
			Map.entry("The Lean Startup", "A modern framework for launching products with rapid feedback."),
			Map.entry("Deep Work", "Train deep focus to produce high-value work in less time."),
			Map.entry("The Alchemist", "A symbolic journey of a shepherd searching for his treasure."),
			Map.entry("Give Me a Ticket to Childhood", "A nostalgic novel reflecting on childhood memories."),
			Map.entry("How to Win Friends and Influence People", "Timeless communication and relationship-building principles."),
			Map.entry("Java Concurrency In Practice", "An advanced guide to concurrent programming in Java."),
			Map.entry("Sherlock Holmes", "A classic collection of detective stories featuring Sherlock Holmes.")
		);

		List<Book> books = bookRepository.findAll();
		boolean changed = false;

		for (Book book : books) {
			String normalizedTitle = titleMap.getOrDefault(book.getTitle(), book.getTitle());
			if (!normalizedTitle.equals(book.getTitle())) {
				book.setTitle(normalizedTitle);
				changed = true;
			}

			String normalizedDescription = descriptionByTitle.get(normalizedTitle);
			if (normalizedDescription != null && !normalizedDescription.equals(book.getDescription())) {
				book.setDescription(normalizedDescription);
				changed = true;
			}
		}

		if (changed) {
			bookRepository.saveAll(books);
		}
	}

	private void seedDefaultUser() {
		if (appUserRepository.findByUsername("user").isPresent()) {
			return;
		}

		appUserRepository.save(new AppUser(
			"user",
			"user@bookstore.com",
			passwordEncoder.encode("123456"),
			"Demo User",
			"ROLE_USER"
		));
	}

	private void seedDefaultAdmin() {
		if (appUserRepository.findByUsername("admin").isPresent()) {
			return;
		}

		appUserRepository.save(new AppUser(
			"admin",
			"admin@bookstore.com",
			passwordEncoder.encode("admin123"),
			"System Admin",
			"ROLE_ADMIN"
		));
	}
}