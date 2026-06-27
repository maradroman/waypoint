package io.github.maradroman.waypointapi.common.exception;

import lombok.Getter;

@Getter
public class DuplicateResourceException extends RuntimeException {
    private final String code;

    public DuplicateResourceException(String code, String message) {
        super(message);
        this.code = code;
    }
}
