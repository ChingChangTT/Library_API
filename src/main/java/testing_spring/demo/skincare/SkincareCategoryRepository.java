package testing_spring.demo.skincare;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SkincareCategoryRepository extends JpaRepository<SkincareCategory, Long> {
    Optional<SkincareCategory> findByName(String name);
}
