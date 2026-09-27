package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateTravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
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
        return travelPackageRepository.findByActiveTrue(pageable).map(TravelPackageResponseDTO::from);
    }

    @Transactional(readOnly = true)
    public TravelPackageResponseDTO getById(Long id) {
        return TravelPackageResponseDTO.from(getEntityById(id));
    }

    public TravelPackageResponseDTO create(TravelPackageRequestDTO dto, Long agencyId) {
        Flight departureFlight = flightService.getOrCreateFlight(dto.getDepartureFlightId());
        Flight returnFlight = flightService.getOrCreateFlight(dto.getReturnFlightId());

        return transactionTemplate.execute(status -> {
            Hotel hotel = hotelService.getEntityById(dto.getHotelId());
            Agency agency = agencyService.getEntityById(agencyId);

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

    public TravelPackageResponseDTO update(Long id, Long agencyId, UpdateTravelPackageRequestDTO dto) {
        Flight departureFlight = flightService.getOrCreateFlight(dto.getDepartureFlightId());
        Flight returnFlight = flightService.getOrCreateFlight(dto.getReturnFlightId());

        return transactionTemplate.execute(status -> {
            TravelPackage travelPackage = getEntityById(id);
            Hotel hotel = hotelService.getEntityById(dto.getHotelId());
            Agency agency = agencyService.getEntityById(agencyId);

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

    @Transactional
    public void delete(Long id, Long agencyId) {
        TravelPackage travelPackage = getEntityById(id);
        Agency agency = agencyService.getEntityById(agencyId);
        travelPackage.deactivate(agency);
        travelPackageRepository.save(travelPackage);
    }

    public TravelPackage getEntityById(Long id) {
        return travelPackageRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("TravelPackage with id " + id + " not found"));
    }
}
