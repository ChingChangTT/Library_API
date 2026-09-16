package testing_spring.demo.book;

import java.time.LocalDate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class LibraryService {
	// Spring injects the JDBC repository through the constructor below.
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
		// Use the shared helper so every missing-book lookup behaves consistently.
		return findBook(id);
	}

	public Book createBook(BookRequest request) {
		// Validate before reading request fields to avoid invalid data and null errors.
		validateBookRequest(request);
		String isbn = request.isbn().trim();

		// ISBN identifies a book uniquely, so an existing value cannot be reused.
		if (bookRepository.existsByIsbn(isbn)) {
			throw new BookValidationException("ISBN already exists");
		}

		// New books have no ID yet (the database creates it) and start as available.
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

		// Ignore this book's own ID while checking whether another book owns the ISBN.
		if (bookRepository.existsByIsbnAndIdNot(isbn, id)) {
			throw new BookValidationException("ISBN already exists");
		}

		// Update only the catalog details; borrowing information stays unchanged.
		Book book = findBook(id);
		book.setTitle(request.title().trim());
		book.setAuthor(request.author().trim());
		book.setIsbn(isbn);
		book.setPublishedYear(request.publishedYear());
		return bookRepository.save(book);
	}

	public void deleteBook(Long id) {
		// Report a clear domain error instead of silently deleting nothing.
		if (!bookRepository.existsById(id)) {
			throw new BookNotFoundException(id);
		}

		bookRepository.deleteById(id);
	}

	/**
	 * Borrows one available book for the person named in the request.
	 *
	 * <p>The method follows four steps: validate the request, load the book, make
	 * sure it is available, and save its new borrowing details.</p>
	 *
	 * @param id ID of the book that the user wants to borrow
	 * @param request request body containing the borrower's name
	 * @return the saved book with its status and borrowing details updated
	 * @throws BookValidationException if the request or borrower name is missing
	 * @throws BookNotFoundException if no book has the supplied ID
	 * @throws BookUnavailableException if the book is already borrowed
	 */
	public Book borrowBook(Long id, BorrowBookRequest request) {
		// Step 1: A request body must exist before we can read the borrower name.
		if (request == null) {
			throw new BookValidationException("Borrower name is required");
		}

		// Step 2: Reject null, empty, and spaces-only names.
		String borrowerName = request.borrowerName();
		if (borrowerName == null || borrowerName.isBlank()) {
			throw new BookValidationException("Borrower name is required");
		}

		// Step 3: Load the book. findBook throws BookNotFoundException if it is absent.
		Book book = findBook(id);

		// A book can have only one borrower at a time.
		if (book.getStatus() == BookStatus.BORROWED) {
			throw new BookUnavailableException(id);
		}

		// Step 4: Mark it borrowed, remember who borrowed it and record today's date.
		book.setStatus(BookStatus.BORROWED);
		book.setBorrowedBy(borrowerName.trim());
		book.setBorrowedDate(LocalDate.now());

		// save() updates the database and returns the updated Book object.
		return bookRepository.save(book);
	}

	/**
	 * Returns a book to the library and clears its borrowing information.
	 *
	 * <p>If the book is already available, this operation is still safe: it keeps
	 * the book available and ensures that no old borrower details remain.</p>
	 *
	 * @param id ID of the book being returned
	 * @return the saved book in its available state
	 * @throws BookNotFoundException if no book has the supplied ID
	 */
	public Book returnBook(Long id) {
		// Step 1: Load the book. findBook reports a clear error if the ID is unknown.
		Book book = findBook(id);

		// Step 2: Make this book ready for the next person to borrow.
		book.setStatus(BookStatus.AVAILABLE);

		// Step 3: null removes information that belongs to the completed loan.
		book.setBorrowedBy(null);
		book.setBorrowedDate(null);

		// Step 4: Persist the three changes and return the updated Book object.
		return bookRepository.save(book);
	}

	private Book findBook(Long id) {
		// Convert an empty Optional into the exception understood by the API layer.
		return bookRepository.findById(id)
				.orElseThrow(() -> new BookNotFoundException(id));
	}

	private void validateBookRequest(BookRequest request) {
		// Validate the object first, followed by each required catalog field.
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
