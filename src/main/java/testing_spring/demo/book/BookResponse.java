package testing_spring.demo.book;

import java.time.LocalDate;

public record BookResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publishedYear,
        BookStatus status,
        String borrowedBy,
        LocalDate borrowedDate
) {
    public static BookResponse from(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getIsbn(),
                book.getPublishedYear(), book.getStatus(), book.getBorrowedBy(), book.getBorrowedDate());
    }
}
