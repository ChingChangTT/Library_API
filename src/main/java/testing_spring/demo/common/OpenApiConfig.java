package testing_spring.demo.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    OpenAPI applicationOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Library and Skincare API")
                .version("1.0.0")
                .description("A production-style Spring Boot REST API example."));
    }
}
