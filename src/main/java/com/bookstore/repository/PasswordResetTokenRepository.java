package com.bookstore.repository;

import com.bookstore.model.AppUser;
import com.bookstore.model.PasswordResetToken;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

	Optional<PasswordResetToken> findByTokenAndUsedFalse(String token);

	boolean existsByUserUsernameAndUsedFalse(String username);

	Optional<PasswordResetToken> findTopByUserUsernameAndUsedFalseOrderByIdDesc(String username);

	void deleteByUser(AppUser user);
}
