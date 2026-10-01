package ar.edu.unq.tusViajes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.response.AgencyResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.service.AgencyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Administración de Agencias", description = "Endpoints administrativos para revisión, aprobación y rechazo de agencias postulantes")
@RestController
@RequestMapping("/api/admin/agencies")
@RequiredArgsConstructor
public class AdminAgencyController {

    private final AgencyService agencyService;

    @Operation(summary = "Listar agencias pendientes", description = "Obtiene todas las agencias con estado PENDING que esperan aprobación (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de agencias pendientes", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AgencyResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/pending")
    public ResponseEntity<List<AgencyResponseDTO>> getPending() {
        return ResponseEntity.ok(agencyService.getPending().stream().map(AgencyResponseDTO::from).toList());
    }

    @Operation(summary = "Autorizar agencia", description = "Aprueba una agencia postulante cambiando su estado a AUTHORIZED (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agencia autorizada con éxito", content = @Content(schema = @Schema(implementation = AgencyResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agencia no encontrada", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{id}/authorize")
    public ResponseEntity<AgencyResponseDTO> authorize(@PathVariable Long id) {
        return ResponseEntity.ok(AgencyResponseDTO.from(agencyService.authorize(id)));
    }

    @Operation(summary = "Rechazar agencia", description = "Rechaza una agencia postulante cambiando su estado a REJECTED (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agencia rechazada con éxito", content = @Content(schema = @Schema(implementation = AgencyResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agencia no encontrada", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{id}/reject")
    public ResponseEntity<AgencyResponseDTO> reject(@PathVariable Long id) {
        return ResponseEntity.ok(AgencyResponseDTO.from(agencyService.reject(id)));
    }
}
