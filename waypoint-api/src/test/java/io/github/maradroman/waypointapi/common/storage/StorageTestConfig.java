package io.github.maradroman.waypointapi.common.storage;

import java.io.InputStream;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class StorageTestConfig {

    @Bean
    public StorageService storageService() {
        return new StorageService() {
            @Override
            public void store(String key, InputStream inputStream, long size, String contentType) {}

            @Override
            public String getPresignedDownloadUrl(String key) {
                return "http://test-storage/" + key;
            }

            @Override
            public void delete(String key) {}
        };
    }
}
