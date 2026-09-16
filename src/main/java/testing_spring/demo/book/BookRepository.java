package testing_spring.demo.book;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class BookRepository {
	private static final Map<String, String> SORT_COLUMNS = Map.of(
			"id", "id", "title", "title", "author", "author", "isbn", "isbn",
			"publishedYear", "published_year", "status", "status",
			"borrowedBy", "borrowed_by", "borrowedDate", "borrowed_date");

	public Page<Book> findAll(Pageable pageable) {
		return findPage("", List.of(), pageable);
	}

	public Page<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
			String title, String author, String isbn, Pageable pageable) {
		return findPage(" WHERE LOWER(title) LIKE LOWER(?) OR LOWER(author) LIKE LOWER(?) OR LOWER(isbn) LIKE LOWER(?)",
				List.of("%" + title + "%", "%" + author + "%", "%" + isbn + "%"), pageable);
	}

	private Page<Book> findPage(String where, List<Object> parameters, Pageable pageable) {
		String order = pageable.getSort().stream().map(item -> {
			String column = SORT_COLUMNS.get(item.getProperty());
			if (column == null) {
				throw new BookValidationException("Unsupported sort property: " + item.getProperty());
			}
			return column + (item.isAscending() ? " ASC" : " DESC");
		}).collect(Collectors.joining(", "));
		String sql = "SELECT * FROM book" + where + " ORDER BY " + (order.isEmpty() ? "id" : order + ", id");
		long total = count("SELECT COUNT(*) FROM book" + where, parameters.toArray());
		List<Object> arguments = new ArrayList<>(parameters);
		if (pageable.isPaged()) {
			sql += " LIMIT ? OFFSET ?";
			arguments.add(pageable.getPageSize());
			arguments.add(pageable.getOffset());
		}
		return new PageImpl<>(jdbcTemplate.query(sql, BOOK_ROW_MAPPER, arguments.toArray()), pageable, total);
	}
	private static final RowMapper<Book> BOOK_ROW_MAPPER = (resultSet, rowNumber) -> {
		Book book = new Book(
				resultSet.getLong("id"),
				resultSet.getString("title"),
				resultSet.getString("author"),
				resultSet.getString("isbn"),
				resultSet.getInt("published_year"),
				BookStatus.valueOf(resultSet.getString("status"))
		);
		book.setBorrowedBy(resultSet.getString("borrowed_by"));
		Date borrowedDate = resultSet.getDate("borrowed_date");
		book.setBorrowedDate(borrowedDate == null ? null : borrowedDate.toLocalDate());
		return book;
	};

	private final JdbcTemplate jdbcTemplate;

	public BookRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Book> findAll() {
		return jdbcTemplate.query("SELECT * FROM book ORDER BY id", BOOK_ROW_MAPPER);
	}

	public Optional<Book> findById(Long id) {
		List<Book> books = jdbcTemplate.query(
				"SELECT * FROM book WHERE id = ?",
				BOOK_ROW_MAPPER,
				id
		);
		return books.stream().findFirst();
	}

	public Book save(Book book) {
		if (book.getId() == null) {
			return insert(book);
		}

		jdbcTemplate.update(
				"""
				UPDATE book
				SET title = ?, author = ?, isbn = ?, published_year = ?,
				    status = ?, borrowed_by = ?, borrowed_date = ?
				WHERE id = ?
				""",
				book.getTitle(),
				book.getAuthor(),
				book.getIsbn(),
				book.getPublishedYear(),
				book.getStatus().name(),
				book.getBorrowedBy(),
				book.getBorrowedDate(),
				book.getId()
		);
		return book;
	}

	private Book insert(Book book) {
		KeyHolder keyHolder = new GeneratedKeyHolder();
		jdbcTemplate.update(connection -> {
			PreparedStatement statement = connection.prepareStatement(
					"""
					INSERT INTO book (title, author, isbn, published_year, status, borrowed_by, borrowed_date)
					VALUES (?, ?, ?, ?, ?, ?, ?)
					""",
					new String[] { "id" }
			);
			statement.setString(1, book.getTitle());
			statement.setString(2, book.getAuthor());
			statement.setString(3, book.getIsbn());
			statement.setInt(4, book.getPublishedYear());
			statement.setString(5, book.getStatus().name());
			statement.setString(6, book.getBorrowedBy());
			statement.setObject(7, book.getBorrowedDate());
			return statement;
		}, keyHolder);

		Number generatedId = keyHolder.getKey();
		if (generatedId == null) {
			throw new IllegalStateException("Database did not return an ID for the new book");
		}
		book.setId(generatedId.longValue());
		return book;
	}

	public void deleteById(Long id) {
		jdbcTemplate.update("DELETE FROM book WHERE id = ?", id);
	}

	public boolean existsById(Long id) {
		return count("SELECT COUNT(*) FROM book WHERE id = ?", id) > 0;
	}

	public boolean existsByIsbn(String isbn) {
		return count("SELECT COUNT(*) FROM book WHERE isbn = ?", isbn) > 0;
	}

	public boolean existsByIsbnAndIdNot(String isbn, Long id) {
		return count("SELECT COUNT(*) FROM book WHERE isbn = ? AND id <> ?", isbn, id) > 0;
	}

	public List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCaseOrIsbnContainingIgnoreCase(
			String title,
			String author,
			String isbn
	) {
		return jdbcTemplate.query(
				"""
				SELECT * FROM book
				WHERE LOWER(title) LIKE LOWER(?)
				   OR LOWER(author) LIKE LOWER(?)
				   OR LOWER(isbn) LIKE LOWER(?)
				ORDER BY id
				""",
				BOOK_ROW_MAPPER,
				"%" + title + "%",
				"%" + author + "%",
				"%" + isbn + "%"
		);
	}

	private long count(String sql, Object... arguments) {
		Long result = jdbcTemplate.queryForObject(sql, Long.class, arguments);
		return result == null ? 0 : result;
	}
}
