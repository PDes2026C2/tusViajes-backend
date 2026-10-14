package ar.edu.unq.tusViajes.controller.dto.request;

import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.specification.TravelPackageSpecifications;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public record TravelPackageFilterDTO(
        @Parameter(description = "Código ISO del país de salida (ej: AR)")
        String originCountryIso,

        @Parameter(description = "ID de la ciudad de salida")
        Long originCityId,

        @Parameter(description = "Código ISO del país de destino/hotel (ej: US)")
        String destinationCountryIso,

        @Parameter(description = "ID de la ciudad de destino/hotel")
        Long destinationCityId,

        @Parameter(description = "Fecha mínima de salida (ISO)")
        LocalDateTime departureFrom,

        @Parameter(description = "Fecha máxima de salida (ISO)")
        LocalDateTime departureTo,

        @Parameter(description = "Fecha mínima de regreso (ISO)")
        LocalDateTime arrivalFrom,

        @Parameter(description = "Fecha máxima de regreso (ISO)")
        LocalDateTime arrivalTo
) {
    public static TravelPackageFilterDTO empty() {
        return new TravelPackageFilterDTO(null, null, null, null, null, null, null, null);
    }

    public Specification<TravelPackage> toSpecification() {
        return TravelPackageSpecifications.fromFilter(this);
    }
}
