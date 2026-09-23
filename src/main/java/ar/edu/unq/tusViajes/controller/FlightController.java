package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightFilterDTO;
import ar.edu.unq.tusViajes.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAvailableFlights(
            @RequestParam(required = false) String airline,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDateTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateTo,
            @RequestParam(required = false) Long originCityId,
            @RequestParam(required = false) String originCountryIsoCode,
            @RequestParam(required = false) Long destinationCityId,
            @RequestParam(required = false) String destinationCountryIsoCode,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer size
    ) {
        FlightFilterDTO filter = new FlightFilterDTO(
                airline,
                departureDateFrom,
                departureDateTo,
                arrivalDateFrom,
                arrivalDateTo,
                originCityId,
                originCountryIsoCode,
                destinationCityId,
                destinationCountryIsoCode
        );
        return ResponseEntity.ok(flightService.getAvailableFlights(filter, page, size));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getAvailableFlight(id));
    }
}
