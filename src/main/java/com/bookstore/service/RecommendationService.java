package com.bookstore.service;

import com.bookstore.model.Book;
import com.bookstore.model.WishlistItem;
import com.bookstore.repository.BookRepository;
import com.bookstore.repository.WishlistItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RecommendationService {
    @Autowired
    private WishlistItemRepository wishlistItemRepository;
    @Autowired
    private BookRepository bookRepository;

    public List<Book> getRecommendationsForUser(String username, int limit) {
        List<WishlistItem> wishlist = wishlistItemRepository.findByUsername(username);
        if (wishlist.isEmpty()) return Collections.emptyList();
        Set<String> authors = wishlist.stream().map(w -> w.getBook().getAuthor()).collect(Collectors.toSet());
        Set<Long> categoryIds = wishlist.stream().map(w -> w.getBook().getCategory().getId()).collect(Collectors.toSet());
        Set<Long> wishlistBookIds = wishlist.stream().map(w -> w.getBook().getId()).collect(Collectors.toSet());
        List<Book> byAuthor = bookRepository.findAll().stream()
            .filter(b -> authors.contains(b.getAuthor()) && !wishlistBookIds.contains(b.getId()))
            .collect(Collectors.toList());
        List<Book> byCategory = bookRepository.findAll().stream()
            .filter(b -> categoryIds.contains(b.getCategory().getId()) && !wishlistBookIds.contains(b.getId()))
            .collect(Collectors.toList());
        Set<Book> recommendations = new LinkedHashSet<>();
        recommendations.addAll(byAuthor);
        recommendations.addAll(byCategory);
        return recommendations.stream().limit(limit).collect(Collectors.toList());
    }
}
