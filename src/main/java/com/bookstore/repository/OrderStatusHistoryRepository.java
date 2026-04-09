package com.bookstore.repository;

import com.bookstore.model.OrderStatusHistory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Long> {

	List<OrderStatusHistory> findByOrderIdOrderByChangedAtDesc(Long orderId);
}
