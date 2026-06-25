package io.github.maradroman.waypointapi.common.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException {
    private final String code;
    private final Object details;

    public BadRequestException(String code, String message) {
        this(code, message, null);
    }

    public BadRequestException(String code, String message, Object details) {
        super(message);
        this.code = code;
        this.details = details;
    }

}
