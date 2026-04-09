package com.bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import java.util.Map;

@Service
public class ExternalBookApiService {
    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, Object> fetchBookInfoByIsbn(String isbn) {
        // Sử dụng Open Library API
        String url = UriComponentsBuilder.fromHttpUrl("https://openlibrary.org/api/books")
                .queryParam("bibkeys", "ISBN:" + isbn)
                .queryParam("format", "json")
                .queryParam("jscmd", "data")
                .toUriString();
        return restTemplate.getForObject(url, Map.class);
    }
}
