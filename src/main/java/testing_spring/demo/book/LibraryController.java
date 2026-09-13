package testing_spring.demo.book;

import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import testing_spring.demo.common.PageResponse;
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

@RestController
@RequestMapping("/api/books")
public class LibraryController {
	private final LibraryService libraryService;

	public LibraryController(LibraryService libraryService) {
		this.libraryService = libraryService;
	}

	@GetMapping
	public PageResponse<BookResponse> getBooks(
			@RequestParam(required = false) String keyword,
			@PageableDefault(size = 20, sort = "title") Pageable pageable) {
		return PageResponse.from(libraryService.getBooks(keyword, pageable));
	}

	@GetMapping("/{id}")
	public BookResponse getBookById(@PathVariable Long id) {
		return BookResponse.from(libraryService.getBookById(id));
	}

	@PostMapping
	public ResponseEntity<BookResponse> createBook(@Valid @RequestBody BookRequest request) {
		Book createdBook = libraryService.createBook(request);
		return ResponseEntity
				.created(URI.create("/api/books/" + createdBook.getId()))
				.body(BookResponse.from(createdBook));
	}

	@PutMapping("/{id}")
	public BookResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookRequest request) {
		return BookResponse.from(libraryService.updateBook(id, request));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
		libraryService.deleteBook(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/borrow")
	public BookResponse borrowBook(@PathVariable Long id, @Valid @RequestBody BorrowBookRequest request) {
		return BookResponse.from(libraryService.borrowBook(id, request));
	}

	@PostMapping("/{id}/return")
	public BookResponse returnBook(@PathVariable Long id) {
		return BookResponse.from(libraryService.returnBook(id));
	}
}
