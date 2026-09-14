package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.controller.dto.response.FlightResponseDTO;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.repository.FlightRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final EntityValidator entityValidator;
    private final FlightRepository flightRepository;
    private final FlightsApiService flightsApiService;
    private final EntityManager entityManager;

    @Transactional(readOnly = true)
    public List<FlightResponseDTO> getAll() {
        return flightRepository.findAll().stream()
                .map(FlightResponseDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public FlightResponseDTO getById(Long id) {
        return FlightResponseDTO.from(getEntityById(id));
    }

    @Transactional
    public FlightResponseDTO create(FlightDTO dto) {
        Flight flight = toEntity(dto);
        return FlightResponseDTO.from(flightRepository.save(flight));
    }

    public Flight getEntityById(Long id) {
        return entityValidator.findByIdOrThrow(flightRepository, id, "Flight");
    }

    public Flight toEntity(FlightDTO dto) {
        return new Flight(
                dto.id(),
                dto.airline(),
                CityDTO.to(dto.originCity()),
                CityDTO.to(dto.destinationCity()),
                dto.departureDate(),
                dto.arrivalDate()
        );
    }

    public Flight getOrCreateFlight(Long flightId) {
        if (!flightRepository.existsById(flightId)) {
            FlightDTO flightDTO = flightsApiService.getFlight(flightId);

            City originCity = entityManager.getReference(City.class, flightDTO.originCity().id());
            City destinationCity = entityManager.getReference(City.class, flightDTO.destinationCity().id());

            return flightRepository.save(new Flight(
                    flightDTO.id(),
                    flightDTO.airline(),
                    originCity,
                    destinationCity,
                    flightDTO.departureDate(),
                    flightDTO.arrivalDate()));
        }
        return this.getEntityById(flightId);
    }
}
