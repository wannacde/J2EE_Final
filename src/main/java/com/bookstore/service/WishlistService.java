package com.bookstore.service;

import org.springframework.transaction.annotation.Transactional;
import com.bookstore.model.Book;
import com.bookstore.model.WishlistItem;
import com.bookstore.repository.WishlistItemRepository;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class WishlistService {
    @Autowired
    private WishlistItemRepository wishlistItemRepository;

    public List<WishlistItem> getWishlistForUser(String username) {
        return wishlistItemRepository.findByUsername(username);
    }

    public void addToWishlist(String username, Book book) {
        if (!wishlistItemRepository.existsByUsernameAndBook(username, book)) {
            WishlistItem item = new WishlistItem();
            item.setUsername(username);
            item.setBook(book);
            wishlistItemRepository.save(item);
        }
    }

    public void removeFromWishlist(String username, Book book) {
        wishlistItemRepository.deleteByUsernameAndBook(username, book);
    }
}
