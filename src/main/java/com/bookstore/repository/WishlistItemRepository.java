package com.bookstore.repository;

import com.bookstore.model.WishlistItem;
import com.bookstore.model.Book;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {
    List<WishlistItem> findByUsername(String username);
    boolean existsByUsernameAndBook(String username, Book book);
    void deleteByUsernameAndBook(String username, Book book);
}
