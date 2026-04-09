package com.bookstore.controller;

import com.bookstore.service.AnalyticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AnalyticsController {
    @Autowired
    private AnalyticsService analyticsService;

    @GetMapping("/admin/analytics")
    public String analyticsDashboard(Model model) {
        model.addAttribute("totalSales", analyticsService.getTotalSales());
        model.addAttribute("totalOrders", analyticsService.getTotalOrders());
        model.addAttribute("totalRevenue", analyticsService.getTotalRevenue());
        model.addAttribute("salesByDay", analyticsService.getSalesByDay());
        return "admin/analytics";
    }
}
