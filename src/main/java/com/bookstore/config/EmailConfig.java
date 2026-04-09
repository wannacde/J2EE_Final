package com.bookstore.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Email configuration to enable async email sending
 */
@Configuration
@EnableAsync
public class EmailConfig {

	/**
	 * Async email configuration is automatically enabled when spring.mail properties are configured
	 * The EnableAsync annotation enables the @Async functionality across the application
	 */
}
