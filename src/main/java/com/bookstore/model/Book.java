package com.bookstore.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import jakarta.persistence.OneToMany;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "books")
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String author;

	@Column(length = 2000)
	private String description;

	@Column(nullable = false, precision = 12, scale = 2)
	private BigDecimal price;

	@Column(length = 500)
	private String imageUrl;

    @Column(nullable = false)
    private int stock = 0; // Số lượng tồn kho

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id", nullable = false)
	private Category category;

	@OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
	private List<Review> reviews;

	public Book(String title, String author, String description, BigDecimal price, String imageUrl, Category category, int stock) {
		this.title = title;
		this.author = author;
		this.description = description;
		this.price = price;
		this.imageUrl = imageUrl;
		this.category = category;
		this.stock = stock;
	}
}