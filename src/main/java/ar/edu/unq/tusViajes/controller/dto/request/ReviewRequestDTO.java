package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequestDTO(
        @NotNull(message = "Score is required")
        @Min(value = 0, message = "Score must be at least 0")
        @Max(value = 5, message = "Score must be at most 5")
        Integer score,
        @Size(max = 1000, message = "Comment must not exceed 1000 characters")
        String comment
) {
}