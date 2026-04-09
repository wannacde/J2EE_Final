package com.bookstore.repository;

import com.bookstore.model.BookOrder;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookOrderRepository extends JpaRepository<BookOrder, Long> {

	List<BookOrder> findByUsernameOrderByCreatedAtDesc(String username);

	Page<BookOrder> findByUsername(String username, Pageable pageable);

	Optional<BookOrder> findByIdAndUsername(Long id, String username);
}