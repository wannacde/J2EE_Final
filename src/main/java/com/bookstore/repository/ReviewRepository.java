package com.bookstore.repository;

import com.bookstore.model.Review;
import com.bookstore.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByBook(Book book);
}
