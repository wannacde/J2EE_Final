package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.model.ShoppingCart;
import com.bookstore.model.Review;
import com.bookstore.service.BookService;
import com.bookstore.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import java.util.List;

@Controller
public class BookController {

	private final ReviewService reviewService;

	private final BookService bookService;
	private final ShoppingCart shoppingCart;

	// --- ADMIN CRUD ---

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/books")
	public String adminListBooks(Model model) {
		List<Book> books = bookService.findAllBooks();
		long lowStockCount = books.stream().filter(b -> b.getStock() <= 5).count();
        long outOfStockCount = books.stream().filter(b -> b.getStock() == 0).count();
		model.addAttribute("books", books);
		model.addAttribute("lowStockCount", lowStockCount);
        model.addAttribute("outOfStockCount", outOfStockCount);
		return "books/admin-list";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/books/create")
	public String showCreateBookForm(Model model) {
		model.addAttribute("book", new Book());
		model.addAttribute("categories", bookService.findAllCategories());
		return "books/book-form";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/admin/books/create")
	public String createBook(Book book) {
		bookService.saveBook(book);
		return "redirect:/admin/books";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@GetMapping("/admin/books/edit")
	public String showEditBookForm(@RequestParam Long id, Model model) {
		Book book = bookService.getBookById(id);
		model.addAttribute("book", book);
		model.addAttribute("categories", bookService.findAllCategories());
		return "books/book-form";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/admin/books/edit")
	public String editBook(Book book) {
		bookService.saveBook(book);
		return "redirect:/admin/books";
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/admin/books/delete")
	public String deleteBook(@RequestParam Long id) {
		try {
			bookService.deleteBookById(id);
			return "redirect:/admin/books";
		} catch (Exception ex) {
			return "redirect:/admin/books?deleteError=1";
		}
	}

	public BookController(BookService bookService, ShoppingCart shoppingCart, ReviewService reviewService) {
		this.bookService = bookService;
		this.shoppingCart = shoppingCart;
		this.reviewService = reviewService;
	}

	@GetMapping("/books/{id}")
	public String bookDetail(@org.springframework.web.bind.annotation.PathVariable Long id, Model model) {
		Book book = bookService.getBookById(id);
		model.addAttribute("book", book);
		model.addAttribute("reviews", reviewService.getReviewsForBook(book));
		model.addAttribute("newReview", new Review());
		return "books/book-detail";
		}

	@Autowired
	private com.bookstore.service.RecommendationService recommendationService;

		   @GetMapping("/books")
		   public String listBooks(
				   @RequestParam(defaultValue = "") String keyword,
				   @RequestParam(defaultValue = "") String author,
				   @RequestParam(required = false) List<Long> categoryIds,
				   @RequestParam(required = false) Long categoryId, // legacy param for single category
				   @RequestParam(required = false) Integer priceMin,
				   @RequestParam(required = false) Integer priceMax,
				   @RequestParam(defaultValue = "asc") String sort,
				   @RequestParam(defaultValue = "0") int page,
				   Model model,
				   HttpServletRequest request,
				   java.security.Principal principal) {
			   // If categoryIds is null or empty but categoryId is present, use it
			   if ((categoryIds == null || categoryIds.isEmpty()) && categoryId != null) {
				   categoryIds = java.util.Collections.singletonList(categoryId);
			   }
			   Page<Book> bookPage = bookService.advancedFindBooks(keyword, author, categoryIds, priceMin, priceMax, sort, page, 5);

			   model.addAttribute("bookPage", bookPage);
			   model.addAttribute("books", bookPage.getContent());
			   model.addAttribute("categories", bookService.findAllCategories());
			   model.addAttribute("keyword", keyword);
			   model.addAttribute("author", author);
			   model.addAttribute("selectedCategoryIds", categoryIds);
			   model.addAttribute("priceMin", priceMin);
			   model.addAttribute("priceMax", priceMax);
			   model.addAttribute("sort", sort);
			   model.addAttribute("cartCount", shoppingCart.getTotalQuantity());
			   model.addAttribute("currentUrl", buildCurrentUrl(request));
			   if (principal != null) {
				   var recs = recommendationService.getRecommendationsForUser(principal.getName(), 4);
				   model.addAttribute("recommendations", recs);
			   }
			   return "books/list";
		   }

	private String buildCurrentUrl(HttpServletRequest request) {
		String query = request.getQueryString();
		return query == null ? request.getRequestURI() : request.getRequestURI() + "?" + query;
	}
    // --- END ADMIN CRUD ---
}