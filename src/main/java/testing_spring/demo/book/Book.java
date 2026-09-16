package testing_spring.demo.book;
import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

// Keep unavailable loan details out of JSON instead of returning them as null.
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Book {
	private Long id;

	private String title;

	private String author;

	private String isbn;

	private Integer publishedYear;

	private BookStatus status;

	private String borrowedBy;

	// Make the API's date format explicit and consistent for clients.
	@JsonFormat(pattern = "yyyy-MM-dd")
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
