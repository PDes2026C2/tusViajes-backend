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
    public Page<TravelPackageResponseDTO> searchMine(Long agencyId, Pageable pageable) {
        return travelPackageRepository.findByAgencyId(agencyId, pageable).map(TravelPackageResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public TravelPackageResponseDTO getById(Long id) {
        return TravelPackageResponseDTO.from(getEntityById(id));
    }

    public TravelPackageResponseDTO create(Long agencyId, TravelPackageRequestDTO dto) {
        Flight departureFlight = flightService.getOrCreateFlight(dto.getDepartureFlightId());
        Flight returnFlight = flightService.getOrCreateFlight(dto.getReturnFlightId());

        return transactionTemplate.execute(status -> {
            Hotel hotel = hotelService.getEntityById(dto.getHotelId());
            Agency agency = agencyService.getEntityById(agencyId);

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

    public TravelPackageResponseDTO update(Long agencyId, Long id, TravelPackageRequestDTO dto) {
        Flight departureFlight = flightService.getOrCreateFlight(dto.getDepartureFlightId());
        Flight returnFlight = flightService.getOrCreateFlight(dto.getReturnFlightId());

        return transactionTemplate.execute(status -> {
            TravelPackage travelPackage = getOwnedEntityById(agencyId, id);
            Hotel hotel = hotelService.getEntityById(dto.getHotelId());
            Agency agency = travelPackage.getAgency();

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
                    "La ciudad del hotel debe coincidir con el destino del vuelo de ida y el origen del vuelo de vuelta. Id de ciudad del hotel: "
                            + hotelCityId + ", id de destino del vuelo de ida: " + departureDestinationCityId
                            + ", id de origen del vuelo de vuelta: " + returnOriginCityId);
        }
    }

    @Transactional
    public void delete(Long agencyId, Long id) {
        TravelPackage travelPackage = getOwnedEntityById(agencyId, id);
        travelPackageRepository.deleteById(travelPackage.getId());
    }

    private TravelPackage getOwnedEntityById(Long agencyId, Long id) {
        return travelPackageRepository.findByIdAndAgencyId(id, agencyId)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete de viaje con id " + id + " no encontrado"));
    }

    public TravelPackage getEntityById(Long id) {
        return travelPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paquete de viaje con id " + id + " no encontrado"));
    }
}
