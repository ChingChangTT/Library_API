package testing_spring.demo.common;

public class SearchResultNotFoundException extends RuntimeException {
    public SearchResultNotFoundException(String resource, String keyword) {
        super("No " + resource + " found for keyword: " + keyword);
    }
}
