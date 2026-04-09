package com.bookstore.repository;

import com.bookstore.model.DiscountCode;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {
    Optional<DiscountCode> findByCode(String code);
}
