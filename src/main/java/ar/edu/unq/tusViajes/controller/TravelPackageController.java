package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageFilterDTO;
import ar.edu.unq.tusViajes.controller.dto.request.TravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.request.UpdateTravelPackageRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.TravelPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@Tag(name = "Paquetes de Viaje", description = "Endpoints para búsqueda pública y administración de paquetes turísticos por agencias")
@RestController
@RequestMapping("/api/travel-packages")
@RequiredArgsConstructor
public class TravelPackageController {

    private final TravelPackageService travelPackageService;

    @Operation(summary = "Buscar paquetes de viaje", description = "Consulta pública y paginada de todos los paquetes de viaje disponibles con soporte de filtros opcionales (origen, destino, fechas).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de paquetes de viaje", content = @Content(schema = @Schema(implementation = Page.class)))
    })
    @GetMapping
    public ResponseEntity<Page<TravelPackageResponseDTO>> search(
            @ModelAttribute TravelPackageFilterDTO filter,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(travelPackageService.search(filter, pageable).map(TravelPackageResponseDTO::from));
    }

    @Operation(summary = "Obtener paquete por ID", description = "Consulta pública de los detalles de un paquete de viaje por su ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paquete de viaje encontrado", content = @Content(schema = @Schema(implementation = TravelPackageResponseDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paquete de viaje no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<TravelPackageResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(TravelPackageResponseDTO.from(travelPackageService.getById(id)));
    }

    @Operation(summary = "Crear paquete de viaje", description = "Crea un nuevo paquete de viaje para la agencia autenticada (requiere rol AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paquete de viaje creado con éxito", content = @Content(schema = @Schema(implementation = TravelPackageResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en campos o inconsistencia en vuelos/fechas", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol AGENCY)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Hotel, vuelo o agencia no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping
    public ResponseEntity<TravelPackageResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody TravelPackageRequestDTO dto) {
        TravelPackageResponseDTO created = TravelPackageResponseDTO.from(travelPackageService.create(dto, userDetails.getId()));
        return ResponseEntity.created(URI.create("/api/travel-packages/" + created.id())).body(created);
    }

    @Operation(summary = "Actualizar paquete de viaje", description = "Actualiza los datos de un paquete propio de la agencia autenticada (requiere rol AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paquete de viaje actualizado con éxito", content = @Content(schema = @Schema(implementation = TravelPackageResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en campos o fechas", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (no es propietario del paquete o rol no autorizado)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paquete de viaje o hotel no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<TravelPackageResponseDTO> update(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateTravelPackageRequestDTO dto) {
        return ResponseEntity.ok(TravelPackageResponseDTO.from(travelPackageService.update(userDetails.getId(), dto)));
    }

    @Operation(summary = "Eliminar paquete de viaje", description = "Desactiva un paquete de viaje propio de la agencia autenticada (requiere rol AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Paquete de viaje eliminado con éxito"),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (no es propietario del paquete o rol no autorizado)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paquete de viaje no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        travelPackageService.delete(id, userDetails.getId());
        return ResponseEntity.noContent().build();
    }
}
