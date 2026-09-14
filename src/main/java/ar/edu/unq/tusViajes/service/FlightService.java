package ar.edu.unq.tusViajes.service;

import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.controller.dto.response.FlightResponseDTO;
import ar.edu.unq.tusViajes.model.City;
import ar.edu.unq.tusViajes.model.Country;
import ar.edu.unq.tusViajes.model.Flight;
import ar.edu.unq.tusViajes.repository.FlightRepository;
import ar.edu.unq.tusViajes.validator.EntityValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FlightService {

    private final EntityValidator entityValidator;
    private final FlightRepository flightRepository;

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

        Country originCountry = new Country(dto.originCity().country().isoCode(), dto.originCity().country().name());
        City originCity = new City(dto.id(), dto.originCity().name(), originCountry);

        Country destinationCountry = new Country(dto.destinationCity().country().isoCode(), dto.destinationCity().country().name());
        City destinationCity = new City(dto.id(), dto.destinationCity().name(), destinationCountry);

        return new Flight(dto.id(), dto.airline(), originCity, destinationCity, dto.departureDate(), dto.arrivalDate());
    }
}
