package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Purchase;

import java.time.LocalDateTime;

public record PurchaseResponseDTO(
        Long id,
        Double price,
        LocalDateTime purchasedAt,
        BuyerResponseDTO buyer,
        TravelPackageResponseDTO travelPackage
) {
    public static PurchaseResponseDTO from(Purchase purchase) {
        return new PurchaseResponseDTO(
                purchase.getId(),
                purchase.getPrice(),
                purchase.getPurchasedAt(),
                purchase.getBuyer() != null ? BuyerResponseDTO.from(purchase.getBuyer()) : null,
                TravelPackageResponseDTO.from(purchase.getTravelPackage())
        );
    }
}
