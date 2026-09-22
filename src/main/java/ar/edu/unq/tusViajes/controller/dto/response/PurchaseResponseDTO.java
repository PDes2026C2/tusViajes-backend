package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Purchase;

import java.time.LocalDateTime;

public record PurchaseResponseDTO(
        Long id,
        Double price,
        LocalDateTime purchasedAt,
        Long buyerId,
        TravelPackageResponseDTO travelPackage
) {
    public static PurchaseResponseDTO from(Purchase purchase) {
        if (purchase == null) return null;
        return new PurchaseResponseDTO(
                purchase.getId(),
                purchase.getPrice(),
                purchase.getPurchasedAt(),
                purchase.getBuyer() != null ? purchase.getBuyer().getId() : null,
                TravelPackageResponseDTO.from(purchase.getTravelPackage())
        );
    }
}
