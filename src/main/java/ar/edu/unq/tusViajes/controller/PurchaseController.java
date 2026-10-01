package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PurchaseResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@Tag(name = "Compras", description = "Endpoints para la adquisición y compra de paquetes turísticos")
@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;

    @Operation(summary = "Comprar paquete de viaje", description = "Registra la compra de un paquete turístico para el comprador autenticado (requiere rol BUYER).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Compra realizada con éxito", content = @Content(schema = @Schema(implementation = PurchaseResponseDTO.class))),
            @ApiResponse(responseCode = "400", description = "DNI no numérico, precio nulo o paquete ya finalizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol BUYER)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paquete de viaje o comprador no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "409", description = "El comprador ya adquirió este paquete", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/{travelPackageId}")
    public ResponseEntity<PurchaseResponseDTO> purchase(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long travelPackageId) {
        PurchaseResponseDTO created = PurchaseResponseDTO.from(purchaseService.purchase(userDetails.getId(), travelPackageId));
        return ResponseEntity.created(URI.create("/api/purchases/" + created.id())).body(created);
    }
}
