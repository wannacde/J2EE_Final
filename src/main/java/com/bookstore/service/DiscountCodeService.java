package com.bookstore.service;

import com.bookstore.model.DiscountCode;
import com.bookstore.repository.DiscountCodeRepository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DiscountCodeService {
    @Autowired
    private DiscountCodeRepository discountCodeRepository;

    public java.util.List<DiscountCode> getAllCodes() {
        return discountCodeRepository.findAll();
    }

    public DiscountCode save(DiscountCode code) {
        return discountCodeRepository.save(code);
    }

    public void deleteById(Long id) {
        discountCodeRepository.deleteById(id);
    }

    public Optional<DiscountCode> findByCode(String code) {
        return discountCodeRepository.findByCode(code);
    }

    public boolean isValid(DiscountCode code) {
        LocalDateTime now = LocalDateTime.now();
        return (code.getValidFrom() == null || !now.isBefore(code.getValidFrom()))
            && (code.getValidTo() == null || !now.isAfter(code.getValidTo()))
            && (code.getUsageLimit() == 0 || code.getUsedCount() < code.getUsageLimit());
    }

    public BigDecimal applyDiscount(DiscountCode code, BigDecimal total) {
        if (code.isPercentage()) {
            return total.subtract(total.multiply(code.getDiscountAmount()).divide(new BigDecimal("100")));
        } else {
            return total.subtract(code.getDiscountAmount());
        }
    }

    public void incrementUsage(DiscountCode code) {
        code.setUsedCount(code.getUsedCount() + 1);
        discountCodeRepository.save(code);
    }
}
