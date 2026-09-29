package testing_spring.demo;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import testing_spring.demo.skincare.*;

@SpringBootTest
@Transactional
class SkincareRelationshipTests {
    @Autowired SkincareProductService service;
    @Autowired SkincareProductRepository products;
    @Autowired SkincareCategoryRepository categories;
    @Autowired JdbcTemplate jdbc;

    private SkincareProductRequest request(String name, String category) {
        return new SkincareProductRequest(name, "Example", category, new BigDecimal("10.00"), 5);
    }

    @Test
    void productsShareCategoryAndDeletingOnePreservesTheOther() {
        var first = service.create(request("First", "Learning category"));
        var second = service.create(request("Second", "Learning category"));
        Long categoryId = first.getCategory().getId();
        assertThat(second.getCategory().getId()).isEqualTo(categoryId);
        assertThat(jdbc.queryForObject(
                "SELECT COUNT(*) FROM skincare_product WHERE category_id = ?", Long.class, categoryId))
                .isEqualTo(2L);
        service.delete(first.getId());
        products.flush();
        assertThat(categories.existsById(categoryId)).isTrue();
        assertThat(service.getById(second.getId()).getCategory().getId()).isEqualTo(categoryId);
    }

    @Test
    void updatingCategoryChangesOnlyThatProductsLinkAndSearchTraversesRelationship() {
        var first = service.create(request("First", "Original category"));
        var second = service.create(request("Second", "Original category"));
        service.update(first.getId(), request("First", "Changed category"));
        products.flush();
        assertThat(second.getCategory().getName()).isEqualTo("Original category");
        assertThat(service.getProducts("CHANGED CATEGORY", PageRequest.of(0, 20)).getContent())
                .extracting(SkincareProductResponse::id).containsExactly(first.getId());
    }

    @Test
    void migrationPreservesSeedCategoryNames() {
        assertThat(service.getProducts("Cleanser", PageRequest.of(0, 20)).getContent())
                .extracting(SkincareProductResponse::category).contains("Cleanser");
    }

    @Test
    void databaseRejectsCategoryThatDoesNotExist() {
        assertThatThrownBy(() -> jdbc.update("""
                INSERT INTO skincare_product (name, brand, category_id, price, stock)
                VALUES ('Invalid', 'Example', -1, 10, 1)
                """))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
