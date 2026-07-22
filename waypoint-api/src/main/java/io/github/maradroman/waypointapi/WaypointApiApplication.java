package io.github.maradroman.waypointapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class WaypointApiApplication {

    public static void main(String[] args) {

        SpringApplication.run(WaypointApiApplication.class, args);
    }
}
