package ar.edu.unq.tusViajes.controller;

import java.net.URI;
import java.util.List;
import java.util.Objects;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.HotelRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.controller.dto.response.HotelResponseDTO;
import ar.edu.unq.tusViajes.service.HotelService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Hoteles", description = "Endpoints para consulta de hoteles y registro administrativo")
@RestController
@RequestMapping("/api/hotels")
@RequiredArgsConstructor
public class HotelController {

    private final HotelService hotelService;

    @Operation(summary = "Listar hoteles", description = "Obtiene la lista de hoteles disponibles (requiere rol ADMIN o AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de hoteles", content = @Content(array = @ArraySchema(schema = @Schema(implementation = HotelResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN o AGENCY)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<HotelResponseDTO>> getAll() {
        return ResponseEntity.ok(hotelService.getAll().stream().map(HotelResponseDTO::from).toList());
    }

    @Operation(summary = "Obtener hotel por ID", description = "Obtiene los datos de un hotel según su identificador (requiere rol ADMIN o AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Hotel encontrado", content = @Content(schema = @Schema(implementation = HotelResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN o AGENCY)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Hotel no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<HotelResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(HotelResponseDTO.from(hotelService.getById(id)));
    }

    @Operation(summary = "Registrar hotel", description = "Registra un nuevo hotel asociado a una ciudad existente (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Hotel registrado con éxito", content = @Content(schema = @Schema(implementation = HotelResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en campos del hotel", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Ciudad no encontrada", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping
    public ResponseEntity<HotelResponseDTO> create(@Valid @RequestBody HotelRequestDTO dto) {
        HotelResponseDTO created = Objects.requireNonNull(HotelResponseDTO.from(hotelService.create(dto)), "El mapeo de hotel no debe ser nulo");
        return ResponseEntity.created(URI.create("/api/hotels/" + created.id())).body(created);
    }
}
