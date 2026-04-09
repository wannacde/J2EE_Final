package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.model.Review;
import com.bookstore.repository.BookRepository;
import com.bookstore.service.ReviewService;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired
    private ReviewService reviewService;
    @Autowired
    private BookRepository bookRepository;

    @PostMapping("/add/{bookId}")
    public String addReview(@PathVariable Long bookId, @Valid @ModelAttribute Review review, BindingResult result, Principal principal, Model model) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        if (result.hasErrors()) {
            model.addAttribute("book", book);
            model.addAttribute("reviews", reviewService.getReviewsForBook(book));
            return "book-detail";
        }
        review.setBook(book);
        review.setUsername(principal.getName());
        reviewService.saveReview(review);
        return "redirect:/books/" + bookId;
    }
}
