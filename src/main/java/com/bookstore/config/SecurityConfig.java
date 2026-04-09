package com.bookstore.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import com.bookstore.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		return http
			.authorizeHttpRequests(auth -> auth
				   .requestMatchers("/", "/books", "/login", "/signup", "/forgot-password", "/reset-password", "/cart/**").permitAll()
				   .requestMatchers("/admin/books/**").hasRole("ADMIN")
				   .requestMatchers("/orders/**").authenticated()
				   .anyRequest().permitAll())
			.formLogin(form -> form
				.loginPage("/login")
				.defaultSuccessUrl("/books", true)
				.permitAll())
			.logout(logout -> logout
				.logoutSuccessUrl("/books?logout")
				.permitAll())
			.build();
	}

	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	public DaoAuthenticationProvider authenticationProvider(
			CustomUserDetailsService customUserDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
		provider.setUserDetailsService(customUserDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}
}