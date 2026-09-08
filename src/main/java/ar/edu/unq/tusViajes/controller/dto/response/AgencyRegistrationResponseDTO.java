package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;

public record AgencyRegistrationResponseDTO(
    Long id,
    String email,
    String businessName,
    String taxId,
    AgencyStatus status,
    String message
) {
    public static AgencyRegistrationResponseDTO from(Agency agency, String message) {
        if (agency == null) {
            return null;
        }
        return new AgencyRegistrationResponseDTO(
                agency.getId(),
                agency.getEmail(),
                agency.getBusinessName(),
                agency.getTaxId(),
                agency.getStatus(),
                message
        );
    }
}
