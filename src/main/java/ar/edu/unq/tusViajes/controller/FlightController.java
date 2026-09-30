package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.adapters.dto.FlightDTO;
import ar.edu.unq.tusViajes.adapters.dto.FlightFilterDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.service.FlightService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Vuelos", description = "Endpoints para consulta de vuelos")
@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @GetMapping
    public ResponseEntity<List<FlightDTO>> getAvailableFlights(
            @Parameter(description = "Nombre de la aerolínea") @RequestParam(required = false) String airline,
            @Parameter(description = "Fecha/hora mínima de salida (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDateFrom,
            @Parameter(description = "Fecha/hora máxima de salida (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime departureDateTo,
            @Parameter(description = "Fecha/hora mínima de llegada (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateFrom,
            @Parameter(description = "Fecha/hora máxima de llegada (ISO)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime arrivalDateTo,
            @Parameter(description = "ID de la ciudad de origen") @RequestParam(required = false) Long originCityId,
            @Parameter(description = "Código ISO del país de origen (ej: AR)") @RequestParam(required = false) String originCountryIsoCode,
            @Parameter(description = "ID de la ciudad de destino") @RequestParam(required = false) Long destinationCityId,
            @Parameter(description = "Código ISO del país de destino (ej: BR)") @RequestParam(required = false) String destinationCountryIsoCode,
            @Parameter(description = "Número de página") @RequestParam(required = false) Integer page,
            @Parameter(description = "Tamaño de página") @RequestParam(required = false) Integer size
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

    @Operation(summary = "Obtener vuelo por ID", description = "Obtiene los detalles de un vuelo específico por su ID (requiere rol ADMIN o AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Vuelo encontrado", content = @Content(schema = @Schema(implementation = FlightDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN o AGENCY)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Vuelo no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<FlightDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(flightService.getAvailableFlight(id));
    }
}
