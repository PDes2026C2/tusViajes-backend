package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageFilterDTO;

import java.time.LocalDateTime;

public class TravelPackageFilterDTOBuilder {
    private String originCountryIso;
    private Long originCityId;
    private String destinationCountryIso;
    private Long destinationCityId;
    private LocalDateTime departureFrom;
    private LocalDateTime departureTo;
    private LocalDateTime arrivalFrom;
    private LocalDateTime arrivalTo;

    public static TravelPackageFilterDTOBuilder aFilter() {
        return new TravelPackageFilterDTOBuilder();
    }

    public TravelPackageFilterDTOBuilder withOriginCountryIso(String originCountryIso) {
        this.originCountryIso = originCountryIso;
        return this;
    }

    public TravelPackageFilterDTOBuilder withOriginCityId(Long originCityId) {
        this.originCityId = originCityId;
        return this;
    }

    public TravelPackageFilterDTOBuilder withDestinationCountryIso(String destinationCountryIso) {
        this.destinationCountryIso = destinationCountryIso;
        return this;
    }

    public TravelPackageFilterDTOBuilder withDestinationCityId(Long destinationCityId) {
        this.destinationCityId = destinationCityId;
        return this;
    }

    public TravelPackageFilterDTOBuilder withDepartureFrom(LocalDateTime departureFrom) {
        this.departureFrom = departureFrom;
        return this;
    }

    public TravelPackageFilterDTOBuilder withDepartureTo(LocalDateTime departureTo) {
        this.departureTo = departureTo;
        return this;
    }

    public TravelPackageFilterDTOBuilder withArrivalFrom(LocalDateTime arrivalFrom) {
        this.arrivalFrom = arrivalFrom;
        return this;
    }

    public TravelPackageFilterDTOBuilder withArrivalTo(LocalDateTime arrivalTo) {
        this.arrivalTo = arrivalTo;
        return this;
    }

    public TravelPackageFilterDTO build() {
        return new TravelPackageFilterDTO(
                originCountryIso,
                originCityId,
                destinationCountryIso,
                destinationCityId,
                departureFrom,
                departureTo,
                arrivalFrom,
                arrivalTo
        );
    }
}
