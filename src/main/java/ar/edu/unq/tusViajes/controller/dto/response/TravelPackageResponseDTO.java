package ar.edu.unq.tusViajes.controller.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import ar.edu.unq.tusViajes.model.TravelPackage;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TravelPackageResponseDTO {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private HotelResponseDTO hotel;
    private AgencyResponseDTO agency;

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
                AgencyResponseDTO.from(travelPackage.getAgency())
        );
    }
}
