package com.bookstore.service;

import com.bookstore.repository.BookOrderRepository;
import com.bookstore.model.BookOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {
    @Autowired
    private BookOrderRepository bookOrderRepository;

    public long getTotalSales() {
        return bookOrderRepository.count();
    }

    public long getTotalOrders() {
        return bookOrderRepository.count();
    }

    public BigDecimal getTotalRevenue() {
        return bookOrderRepository.findAll().stream()
                .map(BookOrder::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Map<LocalDate, BigDecimal> getSalesByDay() {
        return bookOrderRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        order -> order.getCreatedAt().toLocalDate(),
                        Collectors.reducing(BigDecimal.ZERO, BookOrder::getTotalAmount, BigDecimal::add)
                ));
    }
}
