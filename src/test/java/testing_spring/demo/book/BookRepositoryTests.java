package testing_spring.demo.book;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class BookRepositoryTests {
	@Autowired
	private BookRepository bookRepository;

	@Test
	void readsSeedBooksWithJdbc() {
		assertThat(bookRepository.findAll())
				.extracting(Book::getTitle)
				.contains("Clean Code", "Effective Java", "Spring in Action");
	}

	@Test
	void insertsBookAndReturnsGeneratedId() {
		Book book = new Book(null, "Refactoring", "Martin Fowler", "9780134757599", 2018, BookStatus.AVAILABLE);

		Book savedBook = bookRepository.save(book);

		assertThat(savedBook.getId()).isNotNull();
		assertThat(bookRepository.findById(savedBook.getId()))
				.isPresent()
				.get()
				.extracting(Book::getIsbn)
				.isEqualTo("9780134757599");
	}

	@Test
	void searchesAcrossTitleAuthorAndIsbn() {
		assertThat(bookRepository
				.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
						"spring",
						"spring",
						"spring"
				))
				.extracting(Book::getTitle)
				.containsExactly("Spring in Action");
	}
}
