package com.bookstore.controller;

import com.bookstore.model.Book;
import com.bookstore.service.RecommendationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.security.Principal;
import java.util.List;

@Controller
public class RecommendationController {
    @Autowired
    private RecommendationService recommendationService;

    @GetMapping("/recommendations")
    public String recommendationsPage(Principal principal, Model model) {
        if (principal != null) {
            List<Book> recs = recommendationService.getRecommendationsForUser(principal.getName(), 8);
            model.addAttribute("recommendations", recs);
        }
        return "recommendations/list";
    }
}
