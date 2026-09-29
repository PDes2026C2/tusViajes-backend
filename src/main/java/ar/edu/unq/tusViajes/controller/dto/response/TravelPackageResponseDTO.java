package ar.edu.unq.tusViajes.controller.dto.response;

import lombok.Builder;

import java.time.LocalDateTime;

import ar.edu.unq.tusViajes.model.TravelPackage;

@Builder
public record TravelPackageResponseDTO(
        Long id,
        String name,
        String description,
        Double price,
        LocalDateTime startDate,
        LocalDateTime endDate,
        HotelResponseDTO hotel,
        AgencyResponseDTO agency,
        FlightResponseDTO departureFlight,
        FlightResponseDTO arrivalFlight,
        boolean active
) {
    public static TravelPackageResponseDTO from(TravelPackage travelPackage) {
        if (travelPackage == null) {
            return null;
        }
        return new TravelPackageResponseDTO(
                travelPackage.getId(),
                travelPackage.getName(),
                travelPackage.getDescription(),
                travelPackage.getPrice(),
                travelPackage.getStartDate(),
                travelPackage.getEndDate(),
                HotelResponseDTO.from(travelPackage.getHotel()),
                AgencyResponseDTO.from(travelPackage.getAgency()),
                FlightResponseDTO.from(travelPackage.getDepartureFlight()),
                FlightResponseDTO.from(travelPackage.getReturnFlight()),
                travelPackage.isActive()
        );
    }
}
