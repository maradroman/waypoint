package io.github.maradroman.waypointapi.common.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {
    private final String code;
    private final Object details;

    public ResourceNotFoundException(String code, String message) {
        this(code, message, null);
    }

    public ResourceNotFoundException(String code, String message, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }

}
