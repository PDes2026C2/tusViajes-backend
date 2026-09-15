package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.exception.InvalidTravelPackageException;
import ar.edu.unq.tusViajes.exception.ResourceNotFoundException;
import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.model.Hotel;
import ar.edu.unq.tusViajes.model.TravelPackage;
import ar.edu.unq.tusViajes.repository.TravelPackageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
public class TravelPackageService {

    private final TravelPackageRepository travelPackageRepository;
    private final HotelService hotelService;
    private final AgencyService agencyService;
    private final FlightService flightService;
    private final TransactionTemplate transactionTemplate;

    @Transactional(readOnly = true)
    public Page<TravelPackageResponseDTO> search(Pageable pageable) {
        return travelPackageRepository.findAll(pageable).map(TravelPackageResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public TravelPackageResponseDTO getById(Long id) {
        return TravelPackageResponseDTO.from(getEntityById(id));
    }

    public TravelPackageResponseDTO create(TravelPackageRequestDTO dto) {
        Flight departureFlight = flightService.getOrCreateFlight(dto.getDepartureFlightId());
        Flight returnFlight = flightService.getOrCreateFlight(dto.getReturnFlightId());

        return transactionTemplate.execute(status -> {
            Hotel hotel = hotelService.getEntityById(dto.getHotelId());
            Agency agency = agencyService.getEntityById(dto.getAgencyId());

            validateHotelAndFlights(hotel, departureFlight, returnFlight);

            TravelPackage travelPackage = new TravelPackage(
                    dto.getName(),
                    dto.getDescription(),
                    dto.getPrice(),
                    dto.getStartDate(),
                    dto.getEndDate(),
                    hotel,
                    agency,
                    departureFlight,
                    returnFlight
            );
            return TravelPackageResponseDTO.from(travelPackageRepository.save(travelPackage));
        });
    }

    public TravelPackageResponseDTO update(Long id, TravelPackageRequestDTO dto) {
        Flight departureFlight = flightService.getOrCreateFlight(dto.getDepartureFlightId());
        Flight returnFlight = flightService.getOrCreateFlight(dto.getReturnFlightId());

        return transactionTemplate.execute(status -> {
            TravelPackage travelPackage = getEntityById(id);
            Hotel hotel = hotelService.getEntityById(dto.getHotelId());
            Agency agency = agencyService.getEntityById(dto.getAgencyId());

            validateHotelAndFlights(hotel, departureFlight, returnFlight);

            travelPackage.updateData(
                    dto.getName(),
                    dto.getDescription(),
                    dto.getPrice(),
                    dto.getStartDate(),
                    dto.getEndDate(),
                    hotel,
                    agency,
                    departureFlight,
                    returnFlight
            );

            return TravelPackageResponseDTO.from(travelPackageRepository.save(travelPackage));
        });
    }

    private void validateHotelAndFlights(Hotel hotel, Flight departureFlight, Flight returnFlight) {
        Long hotelCityId = hotel.getCity().getId();
        Long departureDestinationCityId = departureFlight.getDestinationCity().getId();
        Long returnOriginCityId = returnFlight.getOriginCity().getId();

        if (!hotelCityId.equals(departureDestinationCityId) || !hotelCityId.equals(returnOriginCityId)) {
            throw new InvalidTravelPackageException(
                    "Hotel city must match destination of departure flight and origin of return flight. Hotel city id: "
                            + hotelCityId + ", departure destination id: " + departureDestinationCityId
                            + ", return origin id: " + returnOriginCityId);
        }
    }

    @Transactional
    public void delete(Long id) {
        if (!travelPackageRepository.existsById(id)) {
            throw new ResourceNotFoundException("TravelPackage with id " + id + " not found");
        }
        travelPackageRepository.deleteById(id);
    }

    public TravelPackage getEntityById(Long id) {
        return travelPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPackage with id " + id + " not found"));
    }
}
