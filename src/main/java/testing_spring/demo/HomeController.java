package testing_spring.demo;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, String> home() {
        return Map.of(
                "name", "Library and Skincare API",
                "status", "running",
                "documentation", "/docs");
    }
}
