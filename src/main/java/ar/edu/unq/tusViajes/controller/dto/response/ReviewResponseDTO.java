package ar.edu.unq.tusViajes.controller.dto.response;

import java.time.LocalDateTime;

import ar.edu.unq.tusViajes.model.Review;

public record ReviewResponseDTO(
        Long id,
        Long buyerId,
        Long travelPackageId,
        Integer score,
        String comment,
        LocalDateTime createdAt
) {

    public static ReviewResponseDTO from(Review review) {
        return new ReviewResponseDTO(
                review.getId(),
                review.getBuyer().getId(),
                review.getTravelPackage().getId(),
                review.getScore(),
                review.getComment(),
                review.getCreatedAt()
        );
    }
}