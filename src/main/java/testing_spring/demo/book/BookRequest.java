package testing_spring.demo.book;

public record BookRequest(
		String title,
		String author,
		String isbn,
		Integer publishedYear
) {
}
