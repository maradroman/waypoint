package io.github.maradroman.waypointapi.common.storage;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.RemoveObjectArgs;
import io.minio.http.Method;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;
    private final StorageProperties properties;

    @PostConstruct
    void init() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(properties.getBucket()).build());
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(properties.getBucket()).build());
                log.info("Created MinIO bucket: {}", properties.getBucket());
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket: {}", e.getMessage(), e);
            throw new IllegalStateException("Could not initialize MinIO storage", e);
        }
    }

    @Override
    public void store(String key, InputStream inputStream, long size, String contentType) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(key)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to store file: " + key, e);
        }
    }

    @Override
    public String getPresignedDownloadUrl(String key) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(properties.getBucket())
                            .object(key)
                            .expiry(properties.getPresignedUrlExpirySeconds(), TimeUnit.SECONDS)
                            .build());
        } catch (Exception e) {
            throw new IllegalStateException("Failed to generate presigned URL for: " + key, e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(properties.getBucket())
                            .object(key)
                            .build());
        } catch (Exception e) {
            log.error("Failed to delete file: {}", key, e);
        }
    }
}
