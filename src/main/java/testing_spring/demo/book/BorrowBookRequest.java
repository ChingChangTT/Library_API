package testing_spring.demo.book;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record BorrowBookRequest(@NotBlank @Size(max = 255) String borrowerName) {
}
