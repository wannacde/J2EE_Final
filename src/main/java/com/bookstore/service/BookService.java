package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.model.Category;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.CategoryRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class BookService {

	private final BookRepository bookRepository;
	private final CategoryRepository categoryRepository;

	public BookService(BookRepository bookRepository, CategoryRepository categoryRepository) {
		this.bookRepository = bookRepository;
		this.categoryRepository = categoryRepository;
	}

	// --- ADMIN CRUD ---
	public java.util.List<Book> findAllBooks() {
		return bookRepository.findAll();
	}

	public Book saveBook(Book book) {
		return bookRepository.save(book);
	}

	public void deleteBookById(Long id) {
		bookRepository.deleteById(id);
	}
	// --- END ADMIN CRUD ---


	   public Page<Book> advancedFindBooks(String keyword, String author, List<Long> categoryIds, Integer priceMin, Integer priceMax, String sortDirection, int page, int size) {
		   Sort sort = Sort.by("price");
		   if ("desc".equalsIgnoreCase(sortDirection)) {
			   sort = sort.descending();
		   } else {
			   sort = sort.ascending();
		   }

		   Pageable pageable = PageRequest.of(Math.max(page, 0), size, sort);
		   Specification<Book> specification = (root, query, criteriaBuilder) -> {
			   List<Predicate> predicates = new ArrayList<>();
			   if (keyword != null && !keyword.isBlank()) {
				   predicates.add(criteriaBuilder.like(
					   criteriaBuilder.lower(root.get("title")),
					   "%" + keyword.trim().toLowerCase() + "%"
				   ));
			   }
			   if (author != null && !author.isBlank()) {
				   predicates.add(criteriaBuilder.like(
					   criteriaBuilder.lower(root.get("author")),
					   "%" + author.trim().toLowerCase() + "%"
				   ));
			   }
			   if (categoryIds != null && !categoryIds.isEmpty()) {
				   predicates.add(root.get("category").get("id").in(categoryIds));
			   }
			   if (priceMin != null) {
				   predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), new java.math.BigDecimal(priceMin)));
			   }
			   if (priceMax != null) {
				   predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), new java.math.BigDecimal(priceMax)));
			   }
			   return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
		   };

		   return bookRepository.findAll(specification, pageable);
	   }

	public List<Category> findAllCategories() {
		return categoryRepository.findAll(Sort.by("name").ascending());
	}

	public Book getBookById(Long id) {
		return bookRepository.findById(id)
			.orElseThrow(() -> new IllegalArgumentException("Book not found with id = " + id));
	}
}