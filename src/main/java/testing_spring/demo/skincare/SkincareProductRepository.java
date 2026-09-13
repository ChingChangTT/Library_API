package testing_spring.demo.skincare;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkincareProductRepository extends JpaRepository<SkincareProduct, Long> {
    Page<SkincareProduct> findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
            String name, String brand, String category, Pageable pageable);
}
