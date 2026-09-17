package ar.edu.unq.tusViajes.controller.dto.response;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorDTO(
    int code,
    String message,
    Instant timestamp,
    Map<String, String> errors
) {
    public ErrorDTO(int code, String message) {
        this(code, message, Instant.now(), null);
    }

    public ErrorDTO(int code, String message, Map<String, String> errors) {
        this(code, message, Instant.now(), errors);
    }
}
