package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.model.DestinationsTop;

public record DestinationsTopResponseDTO(CityDTO city,Long salesCount) {

    public static DestinationsTopResponseDTO from(DestinationsTop destinationsTop) {
        return new DestinationsTopResponseDTO(CityDTO.from(destinationsTop.city()), destinationsTop.salesCount());
    }
}
