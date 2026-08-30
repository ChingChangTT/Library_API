package testing_spring.demo.book;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import testing_spring.demo.common.ApiMessage;

@RestController
@RequestMapping("/api/books")
public class LibraryController {
	private final LibraryService libraryService;

	public LibraryController(LibraryService libraryService) {
		this.libraryService = libraryService;
	}

	@GetMapping
	public List<Book> getAllBooks() {
		return libraryService.getAllBooks();
	}

	@GetMapping("/{id}")
	public Book getBookById(@PathVariable Long id) {
		return libraryService.getBookById(id);
	}

	@GetMapping("/search")
	public List<Book> searchBooks(@RequestParam String keyword) {
		return libraryService.searchBooks(keyword);
	}

	@PostMapping
	public ResponseEntity<Book> createBook(@RequestBody BookRequest request) {
		Book createdBook = libraryService.createBook(request);
		return ResponseEntity
				.created(URI.create("/api/books/" + createdBook.getId()))
				.body(createdBook);
	}

	@PutMapping("/{id}")
	public Book updateBook(@PathVariable Long id, @RequestBody BookRequest request) {
		return libraryService.updateBook(id, request);
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<ApiMessage> deleteBook(@PathVariable Long id) {
		libraryService.deleteBook(id);
		return ResponseEntity.ok(new ApiMessage("Book deleted successfully"));
	}

	@PostMapping("/{id}/borrow")
	public Book borrowBook(@PathVariable Long id, @RequestBody BorrowBookRequest request) {
		return libraryService.borrowBook(id, request);
	}

	@PostMapping("/{id}/return")
	public Book returnBook(@PathVariable Long id) {
		return libraryService.returnBook(id);
	}
}
