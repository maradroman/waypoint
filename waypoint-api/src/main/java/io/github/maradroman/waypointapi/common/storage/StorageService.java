package io.github.maradroman.waypointapi.common.storage;

import java.io.InputStream;

public interface StorageService {

    void store(String key, InputStream inputStream, long size, String contentType);

    String getPresignedDownloadUrl(String key);

    void delete(String key);
}
