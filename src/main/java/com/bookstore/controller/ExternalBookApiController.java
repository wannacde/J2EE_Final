package com.bookstore.controller;

import com.bookstore.service.ExternalBookApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;

@RestController
public class ExternalBookApiController {
    @Autowired
    private ExternalBookApiService externalBookApiService;

    @GetMapping("/api/books/fetch")
    public Map<String, Object> fetchBookByIsbn(@RequestParam String isbn) {
        return externalBookApiService.fetchBookInfoByIsbn(isbn);
    }
}
