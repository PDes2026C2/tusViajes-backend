package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;

public record AgencyResponseDTO(
    Long id,
    String businessName,
    String taxId,
    String email,
    AgencyStatus status
) {
    public AgencyResponseDTO(Long id, String businessName, String taxId) {
        this(id, businessName, taxId, null, AgencyStatus.AUTHORIZED);
    }

    public static AgencyResponseDTO from(Agency agency) {
        if (agency == null) {
            return null;
        }
        return new AgencyResponseDTO(
                agency.getId(),
                agency.getBusinessName(),
                agency.getTaxId(),
                agency.getEmail(),
                agency.getStatus()
        );
    }
}
