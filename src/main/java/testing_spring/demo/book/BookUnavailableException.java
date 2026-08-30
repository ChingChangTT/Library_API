package testing_spring.demo.book;

public class BookUnavailableException extends RuntimeException {
	public BookUnavailableException(Long id) {
		super("Book with id " + id + " is not available");
	}
}
