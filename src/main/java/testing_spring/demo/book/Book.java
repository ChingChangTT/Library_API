package testing_spring.demo.book;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Book {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String author;

	@Column(nullable = false, unique = true)
	private String isbn;

	@Column(nullable = false)
	private Integer publishedYear;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private BookStatus status;

	private String borrowedBy;
	private LocalDate borrowedDate;

	public Book() {
	}

	public Book(Long id, String title, String author, String isbn, Integer publishedYear, BookStatus status) {
		this.id = id;
		this.title = title;
		this.author = author;
		this.isbn = isbn;
		this.publishedYear = publishedYear;
		this.status = status;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public String getIsbn() {
		return isbn;
	}

	public void setIsbn(String isbn) {
		this.isbn = isbn;
	}

	public Integer getPublishedYear() {
		return publishedYear;
	}

	public void setPublishedYear(Integer publishedYear) {
		this.publishedYear = publishedYear;
	}

	public BookStatus getStatus() {
		return status;
	}

	public void setStatus(BookStatus status) {
		this.status = status;
	}

	public String getBorrowedBy() {
		return borrowedBy;
	}

	public void setBorrowedBy(String borrowedBy) {
		this.borrowedBy = borrowedBy;
	}

	public LocalDate getBorrowedDate() {
		return borrowedDate;
	}

	public void setBorrowedDate(LocalDate borrowedDate) {
		this.borrowedDate = borrowedDate;
	}
}
