package testing_spring.demo.skincare;

public class SkincareProductNotFoundException extends RuntimeException {
    public SkincareProductNotFoundException(Long id) {
        super("Skincare product with id " + id + " was not found");
    }
}
