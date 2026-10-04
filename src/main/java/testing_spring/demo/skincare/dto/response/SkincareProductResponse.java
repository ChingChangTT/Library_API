package testing_spring.demo.skincare.dto.response;

import testing_spring.demo.skincare.SkincareProduct;
import java.math.BigDecimal;

public record SkincareProductResponse(
        Long id,
        String name,
        String brand,
        String category,
        BigDecimal price,
        Integer stock
) {
    public static SkincareProductResponse from(SkincareProduct product) {
        return new SkincareProductResponse(product.getId(), product.getName(), product.getBrand(),
                product.getCategory().getName(), product.getPrice(), product.getStock());
    }
}
