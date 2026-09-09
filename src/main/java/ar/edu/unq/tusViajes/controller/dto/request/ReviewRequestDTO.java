package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Size;

public record ReviewRequestDTO(
        @Min(value = 0, message = "Score must be at least 0")
        @Max(value = 10, message = "Score must be at most 10")
        Integer score,
        @Size(max = 1000, message = "Comment must not exceed 1000 characters")
        String comment
) {

        @AssertTrue(message = "Score or comment is required")
        public boolean hasScoreOrComment() {
                return score != null || (comment != null && !comment.isBlank());
        }
}