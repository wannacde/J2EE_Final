package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.WishlistService;
import com.bookstore.repository.BookRepository;
import java.security.Principal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/wishlist")
public class WishlistController {
    @Autowired
    private WishlistService wishlistService;
    @Autowired
    private BookRepository bookRepository;

    @GetMapping
    public String viewWishlist(Principal principal, Model model) {
        String username = principal.getName();
        model.addAttribute("wishlist", wishlistService.getWishlistForUser(username));
        return "wishlist/list";
    }

    @PostMapping("/add/{bookId}")
    public String addToWishlist(@PathVariable Long bookId, Principal principal) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        wishlistService.addToWishlist(principal.getName(), book);
        return "redirect:/wishlist";
    }

    @PostMapping("/remove/{bookId}")
    public String removeFromWishlist(@PathVariable Long bookId, Principal principal) {
        Book book = bookRepository.findById(bookId).orElseThrow();
        wishlistService.removeFromWishlist(principal.getName(), book);
        return "redirect:/wishlist";
    }
}
