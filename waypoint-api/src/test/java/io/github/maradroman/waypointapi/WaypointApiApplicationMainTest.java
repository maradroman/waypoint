package io.github.maradroman.waypointapi;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WaypointApiApplicationMainTest {

    @Test
    @DisplayName("main starts application without exception")
    void main_startsApplication() {
        WaypointApiApplication.main(new String[] {
            "--spring.main.web-application-type=none",
            "--spring.profiles.active=test",
            "--spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration"
        });
    }
}
