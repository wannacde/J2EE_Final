package com.bookstore.repository;

import com.bookstore.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

	long countByOrderId(Long orderId);
}