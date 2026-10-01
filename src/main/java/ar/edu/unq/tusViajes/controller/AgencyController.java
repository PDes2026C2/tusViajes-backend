package ar.edu.unq.tusViajes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.UpdateAgencyRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.AgencyResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.service.AgencyService;

import ar.edu.unq.tusViajes.controller.dto.response.PurchaseResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.PurchaseService;
import ar.edu.unq.tusViajes.service.TravelPackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "Agencias", description = "Endpoints para consulta, administración y gestión propia de agencias")
@RestController
@RequestMapping("/api/agencies")
@RequiredArgsConstructor
public class AgencyController {

    private final AgencyService agencyService;
    private final TravelPackageService travelPackageService;
    private final PurchaseService purchaseService;

    @Operation(summary = "Listar agencias", description = "Obtiene la lista de todas las agencias registradas (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de agencias", content = @Content(array = @ArraySchema(schema = @Schema(implementation = AgencyResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<AgencyResponseDTO>> getAll() {
        return ResponseEntity.ok(agencyService.getAll().stream().map(AgencyResponseDTO::from).toList());
    }

    @Operation(summary = "Obtener agencia por ID", description = "Obtiene los datos de una agencia según su ID (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agencia encontrada", content = @Content(schema = @Schema(implementation = AgencyResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Agencia no encontrada", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AgencyResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(AgencyResponseDTO.from(agencyService.getById(id)));
    }

    @Operation(summary = "Actualizar razón social propia", description = "Permite a la agencia autenticada actualizar su propia razón social a partir del token JWT (requiere rol AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Agencia actualizada con éxito", content = @Content(schema = @Schema(implementation = AgencyResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "Error de validación en la razón social", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol AGENCY)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PutMapping("/me")
    public ResponseEntity<AgencyResponseDTO> updateMine(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody UpdateAgencyRequestDTO dto) {
        return ResponseEntity.ok(AgencyResponseDTO.from(agencyService.update(userDetails.getId(), dto)));
    }

    @Operation(summary = "Eliminar agencia", description = "Elimina una agencia del sistema (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Agencia eliminada con éxito"),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        agencyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar paquetes propios", description = "Obtiene los paquetes creados por la propia agencia autenticada (paginado, requiere rol AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de paquetes de la agencia", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol AGENCY)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/me/packages")
    public ResponseEntity<Page<TravelPackageResponseDTO>> getMyPackages(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(travelPackageService.searchMine(userDetails.getId(), pageable).map(TravelPackageResponseDTO::from));
    }

    @Operation(summary = "Listar ventas propias", description = "Obtiene las ventas de paquetes realizadas por la agencia autenticada (paginado, requiere rol AGENCY).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de ventas de la agencia", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol AGENCY)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/me/sales")
    public ResponseEntity<Page<PurchaseResponseDTO>> getMySales(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(purchaseService.getSalesByAgency(userDetails.getId(), pageable).map(PurchaseResponseDTO::from));
    }
}
