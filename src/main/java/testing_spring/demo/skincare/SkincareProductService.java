package testing_spring.demo.skincare;

import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SkincareProductService {
    private final SkincareProductRepository repository;

    public SkincareProductService(SkincareProductRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public SkincareProduct getById(Long id) {
        return findProduct(id);
    }

    @Transactional(readOnly = true)
    public Page<SkincareProductResponse> getProducts(String keyword, Pageable pageable) {
        if (keyword == null || keyword.isBlank()) {
            return repository.findAll(pageable).map(SkincareProductResponse::from);
        }
        String value = keyword.trim();
        return repository.findByNameContainingIgnoreCaseOrBrandContainingIgnoreCaseOrCategoryContainingIgnoreCase(
                value, value, value, pageable).map(SkincareProductResponse::from);
    }

    public SkincareProduct create(SkincareProductRequest request) {
        validate(request);
        return repository.save(new SkincareProduct(
                request.name().trim(),
                request.brand().trim(),
                request.category().trim(),
                request.price(),
                request.stock()));
    }

    public SkincareProduct update(Long id, SkincareProductRequest request) {
        validate(request);
        SkincareProduct product = findProduct(id);
        product.setName(request.name().trim());
        product.setBrand(request.brand().trim());
        product.setCategory(request.category().trim());
        product.setPrice(request.price());
        product.setStock(request.stock());
        return repository.save(product);
    }

    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new SkincareProductNotFoundException(id);
        }
        repository.deleteById(id);
    }

    private SkincareProduct findProduct(Long id) {
        return repository.findById(id).orElseThrow(() -> new SkincareProductNotFoundException(id));
    }

    private void validate(SkincareProductRequest request) {
        if (request == null) throw new SkincareProductValidationException("Product details are required");
        if (request.name() == null || request.name().isBlank()) throw new SkincareProductValidationException("Name is required");
        if (request.brand() == null || request.brand().isBlank()) throw new SkincareProductValidationException("Brand is required");
        if (request.category() == null || request.category().isBlank()) throw new SkincareProductValidationException("Category is required");
        if (request.price() == null || request.price().compareTo(BigDecimal.ZERO) < 0) throw new SkincareProductValidationException("Price cannot be negative");
        if (request.stock() == null || request.stock() < 0) throw new SkincareProductValidationException("Stock cannot be negative");
    }
}
