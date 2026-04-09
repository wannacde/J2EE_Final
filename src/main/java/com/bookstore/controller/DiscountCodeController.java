package com.bookstore.controller;

import com.bookstore.model.DiscountCode;
import com.bookstore.service.DiscountCodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/discount-codes")
public class DiscountCodeController {
    @Autowired
    private DiscountCodeService discountCodeService;

    @GetMapping("/admin")
    public String listCodes(Model model) {
        model.addAttribute("codes", discountCodeService.getAllCodes());
        return "discount/list";
    }

    @GetMapping("/admin/create")
    public String showCreateForm(Model model) {
        model.addAttribute("discountCode", new DiscountCode());
        return "discount/form";
    }

    @PostMapping("/admin/create")
    public String createCode(@ModelAttribute DiscountCode discountCode) {
        discountCodeService.save(discountCode);
        return "redirect:/discount-codes/admin";
    }

    @PostMapping("/admin/delete/{id}")
    public String deleteCode(@PathVariable Long id) {
        discountCodeService.deleteById(id);
        return "redirect:/discount-codes/admin";
    }
}
