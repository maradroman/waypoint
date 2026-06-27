package io.github.maradroman.waypointapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Value("${server.servlet.context-path:/api/v1}")
    private String contextPath;

    @Bean
    public OpenAPI waypointOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Waypoint API")
                        .description(
                                "Goal tracking REST API — manage goals, milestones, deposits, transfers, and completions.")
                        .version("0.0.1")
                        .contact(new Contact()
                                .name("Waypoint Team")
                                .url("https://github.com/maradroman/waypoint-future")))
                .servers(List.of(
                        new Server().url("http://localhost:8080" + contextPath).description("Local development"),
                        new Server()
                                .url("https://api.waypoint.example.com" + contextPath)
                                .description("Production")));
    }
}
