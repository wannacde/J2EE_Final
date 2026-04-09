package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.model.Review;
import com.bookstore.repository.ReviewRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReviewService {
    @Autowired
    private ReviewRepository reviewRepository;

    public List<Review> getReviewsForBook(Book book) {
        return reviewRepository.findByBook(book);
    }

    public Review saveReview(Review review) {
        return reviewRepository.save(review);
    }
}
