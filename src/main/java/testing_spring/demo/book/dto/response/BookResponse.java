package testing_spring.demo.book.dto.response;

import testing_spring.demo.book.BookStatus;
import testing_spring.demo.book.Book;
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
