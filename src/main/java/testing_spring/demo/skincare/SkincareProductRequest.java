package testing_spring.demo.skincare;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record SkincareProductRequest(
		@NotBlank @Size(max = 255)
        String name,
		@NotBlank @Size(max = 255)
        String brand,
		@NotBlank @Size(max = 255)
        String category,
		@NotNull @DecimalMin(value = "0.0", inclusive = true)
        BigDecimal price,
		@NotNull @PositiveOrZero
        Integer stock
) {
}
