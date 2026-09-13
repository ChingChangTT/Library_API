package testing_spring.demo.book;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record BookRequest(
		@NotBlank @Size(max = 255)
		String title,
		@NotBlank @Size(max = 255)
		String author,
		@NotBlank @Pattern(regexp = "(?:\\d{10}|\\d{13})", message = "must contain 10 or 13 digits")
		String isbn,
		@Min(0) @Max(2100)
		Integer publishedYear
) {
}
