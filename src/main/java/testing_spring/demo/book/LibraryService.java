package testing_spring.demo.book;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LibraryService {
	private final BookRepository bookRepository;

	public LibraryService(BookRepository bookRepository) {
		this.bookRepository = bookRepository;
	}

	@Transactional(readOnly = true)
	public Page<BookResponse> getBooks(String keyword, Pageable pageable) {
		Page<Book> books;
		if (keyword == null || keyword.isBlank()) {
			books = bookRepository.findAll(pageable);
		} else {
			String value = keyword.trim();
			books = bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
					value, value, value, pageable);
		}
		return books.map(BookResponse::from);
	}

	@Transactional(readOnly = true)
	public Book getBookById(Long id) {
		return findBook(id);
	}

	public Book createBook(BookRequest request) {
		validateBookRequest(request);
		String isbn = request.isbn().trim();
		if (bookRepository.existsByIsbn(isbn)) {
			throw new BookValidationException("ISBN already exists");
		}

		Book book = new Book(
				null,
				request.title().trim(),
				request.author().trim(),
				isbn,
				request.publishedYear(),
				BookStatus.AVAILABLE
		);

		return bookRepository.save(book);
	}

	public Book updateBook(Long id, BookRequest request) {
		validateBookRequest(request);
		String isbn = request.isbn().trim();
		if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
			throw new BookValidationException("ISBN already exists");
		}

		Book book = findBook(id);
		book.setTitle(request.title().trim());
		book.setAuthor(request.author().trim());
		book.setIsbn(isbn);
		book.setPublishedYear(request.publishedYear());
		return bookRepository.save(book);
	}

	public void deleteBook(Long id) {
		if (!bookRepository.existsById(id)) {
			throw new BookNotFoundException(id);
		}

		bookRepository.deleteById(id);
	}

	public Book borrowBook(Long id, BorrowBookRequest request) {
		String borrowerName = request == null ? null : request.borrowerName();
		if (borrowerName == null || borrowerName.isBlank()) {
			throw new BookValidationException("Borrower name is required");
		}

		Book book = findBook(id);
		if (book.getStatus() == BookStatus.BORROWED) {
			throw new BookUnavailableException(id);
		}

		book.setStatus(BookStatus.BORROWED);
		book.setBorrowedBy(borrowerName.trim());
		book.setBorrowedDate(LocalDate.now());
		return bookRepository.save(book);
	}

	public Book returnBook(Long id) {
		Book book = findBook(id);
		book.setStatus(BookStatus.AVAILABLE);
		book.setBorrowedBy(null);
		book.setBorrowedDate(null);
		return bookRepository.save(book);
	}

	@Transactional(readOnly = true)
	private Book findBook(Long id) {
		return bookRepository.findById(id)
				.orElseThrow(() -> new BookNotFoundException(id));
	}

	private void validateBookRequest(BookRequest request) {
		if (request == null) {
			throw new BookValidationException("Book details are required");
		}
		if (request.title() == null || request.title().isBlank()) {
			throw new BookValidationException("Title is required");
		}
		if (request.author() == null || request.author().isBlank()) {
			throw new BookValidationException("Author is required");
		}
		if (request.isbn() == null || request.isbn().isBlank()) {
			throw new BookValidationException("ISBN is required");
		}
		if (request.publishedYear() == null) {
			throw new BookValidationException("Published year is required");
		}
	}
}
