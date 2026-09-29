package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Buyer;

public record BuyerResponseDTO(
    Long id,
    String firstName,
    String lastName,
    String email,
    String phoneNumber,
    String nationalId
) {
    public static BuyerResponseDTO from(Buyer buyer) {
        return new BuyerResponseDTO(
                buyer.getId(),
                buyer.getFirstName(),
                buyer.getLastName(),
                buyer.getEmail(),
                buyer.getPhoneNumber(),
                buyer.getNationalId()
        );
    }
}
