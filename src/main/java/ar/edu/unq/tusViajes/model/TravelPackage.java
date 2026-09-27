package ar.edu.unq.tusViajes.model;

import ar.edu.unq.tusViajes.exception.InvalidTravelPackageException;
import ar.edu.unq.tusViajes.exception.UnauthorizedAgencyException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "travel_packages")
@Getter
@EqualsAndHashCode
@NoArgsConstructor
public class TravelPackage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    @Column(nullable = false)
    private Double price;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "agency_id", nullable = false)
    private Agency agency;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_flight_id", nullable = false)
    private Flight departureFlight;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "return_flight_id", nullable = false)
    private Flight returnFlight;

    @Column(nullable = false)
    private boolean active = true;

    public TravelPackage(String name, String description, Double price, LocalDateTime startDate,
                         LocalDateTime endDate, Hotel hotel, Agency agency, Flight departureFlight, Flight returnFlight) {
        this.validateHotelAndFlights(hotel, departureFlight, returnFlight);
        this.name = name;
        this.description = description;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hotel = hotel;
        this.agency = agency;
        this.departureFlight = departureFlight;
        this.returnFlight = returnFlight;
    }

    public void updateData(String name, String description, Double price, LocalDateTime startDate,
                           LocalDateTime endDate, Hotel hotel, Agency agency, Flight departureFlight, Flight returnFlight) {
        this.validAgencyIsOwner(agency);
        this.validateHotelAndFlights(hotel, departureFlight, returnFlight);
        this.name = name;
        this.description = description;
        this.price = price;
        this.startDate = startDate;
        this.endDate = endDate;
        this.hotel = hotel;
        this.departureFlight = departureFlight;
        this.returnFlight = returnFlight;
    }

    public boolean hasEnded(){
        return endDate.isBefore(LocalDateTime.now());
    }

    public void deactivate(Agency agency) {
        this.validAgencyIsOwner(agency);
        this.active = false;
    }

    public void activate(Agency agency) {
        this.validAgencyIsOwner(agency);
        this.active = true;
    }

    private void validAgencyIsOwner(Agency ag) {
        if (! ag.equals(this.agency)) {
            throw new UnauthorizedAgencyException("La agencia no tiene permiso para realizar esta operacion");
        }
    }

    private void validateHotelAndFlights(Hotel hotel, Flight departureFlight, Flight returnFlight) {
        Long hotelCityId = hotel.getCity().getId();
        Long departureDestinationCityId = departureFlight.getDestinationCity().getId();
        Long returnOriginCityId = returnFlight.getOriginCity().getId();

        if (!hotelCityId.equals(departureDestinationCityId) || !hotelCityId.equals(returnOriginCityId)) {
            throw new InvalidTravelPackageException(
                    "La ciudad del hotel debe coincidir con el destino del vuelo de ida y el origen del vuelo de vuelta. Id de ciudad del hotel: "
                            + hotelCityId + ", id de destino del vuelo de ida: " + departureDestinationCityId
                            + ", id de origen del vuelo de vuelta: " + returnOriginCityId);
        }
    }
}
