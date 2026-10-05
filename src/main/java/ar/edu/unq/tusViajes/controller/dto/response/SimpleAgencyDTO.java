package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Agency;

public record SimpleAgencyDTO(Long id,String businessName,String email) {

    public static SimpleAgencyDTO from(Agency agency) {
        return new SimpleAgencyDTO(agency.getId(), agency.getBusinessName(), agency.getEmail());
    }
}
