package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.model.RatedDestinationsTop;


public record RatedDestinationsTopResponseDTO(CityDTO city,Double stars) {

    public static RatedDestinationsTopResponseDTO from(RatedDestinationsTop ratedDestinationsTop) {
        return new RatedDestinationsTopResponseDTO(CityDTO.from(ratedDestinationsTop.city()), ratedDestinationsTop.stars());
    }
}
