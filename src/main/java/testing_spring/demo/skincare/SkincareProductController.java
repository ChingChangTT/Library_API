package testing_spring.demo.skincare;

import java.net.URI;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import testing_spring.demo.common.PageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/skincare-products")
public class SkincareProductController {
    private final SkincareProductService service;

    public SkincareProductController(SkincareProductService service) {
        this.service = service;
    }

    @GetMapping
    public PageResponse<SkincareProductResponse> getProducts(
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "name") Pageable pageable) {
        return PageResponse.from(service.getProducts(keyword, pageable));
    }

    @GetMapping("/{id}")
    public SkincareProductResponse getById(@PathVariable Long id) {
        return SkincareProductResponse.from(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<SkincareProductResponse> create(@Valid @RequestBody SkincareProductRequest request) {
        SkincareProduct product = service.create(request);
        return ResponseEntity.created(URI.create("/api/skincare-products/" + product.getId()))
                .body(SkincareProductResponse.from(product));
    }

    @PutMapping("/{id}")
    public SkincareProductResponse update(@PathVariable Long id, @Valid @RequestBody SkincareProductRequest request) {
        return SkincareProductResponse.from(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
