package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.AgenciesTop;

public record AgenciesTopResponseDTO(SimpleAgencyDTO agency,Long sellsCount) {

    public static AgenciesTopResponseDTO from(AgenciesTop agenciesTop) {
        return new AgenciesTopResponseDTO(SimpleAgencyDTO.from(agenciesTop.agency()), agenciesTop.sellsCount());
    }
}
