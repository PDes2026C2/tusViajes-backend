package ar.edu.unq.tusViajes.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.response.BuyerResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.controller.dto.response.PurchaseResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.TravelPackageResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.BuyerService;
import ar.edu.unq.tusViajes.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@Tag(name = "Compradores", description = "Endpoints para administración de compradores y gestión personal de favoritos y compras")
@RestController
@RequestMapping("/api/buyers")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerService buyerService;
    private final PurchaseService purchaseService;

    @Operation(summary = "Listar compradores", description = "Obtiene la lista completa de compradores registrados (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de compradores", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BuyerResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping
    public ResponseEntity<List<BuyerResponseDTO>> getAll() {
        return ResponseEntity.ok(buyerService.getAll().stream().map(BuyerResponseDTO::from).toList());
    }

    @Operation(summary = "Obtener comprador por ID", description = "Obtiene los datos de un comprador según su ID (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Comprador encontrado", content = @Content(schema = @Schema(implementation = BuyerResponseDTO.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Comprador no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BuyerResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(BuyerResponseDTO.from(buyerService.getById(id)));
    }

    @Operation(summary = "Agregar paquete a favoritos", description = "Agrega un paquete turístico a la lista de favoritos del comprador autenticado (requiere rol BUYER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paquete agregado a favoritos con éxito"),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol BUYER)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paquete de viaje no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @PostMapping("/me/favorites/{travelPackageId}")
    public ResponseEntity<Void> addFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                                            @PathVariable Long travelPackageId) {
        buyerService.addFavorite(userDetails.getId(), travelPackageId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Eliminar paquete de favoritos", description = "Elimina un paquete turístico de los favoritos del comprador autenticado (requiere rol BUYER).")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Paquete eliminado de favoritos con éxito"),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol BUYER)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paquete de viaje no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @DeleteMapping("/me/favorites/{travelPackageId}")
    public ResponseEntity<Void> removeFavorite(@AuthenticationPrincipal CustomUserDetails userDetails,
                                               @PathVariable Long travelPackageId) {
        buyerService.removeFavorite(userDetails.getId(), travelPackageId);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Listar paquetes favoritos", description = "Obtiene los paquetes favoritos del comprador autenticado (requiere rol BUYER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de paquetes favoritos", content = @Content(array = @ArraySchema(schema = @Schema(implementation = TravelPackageResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol BUYER)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/me/favorites")
    public ResponseEntity<List<TravelPackageResponseDTO>> getFavorites(@AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(buyerService.getFavorites(userDetails.getId()).stream().map(TravelPackageResponseDTO::from).toList());
    }

    @Operation(summary = "Listar compras propias", description = "Obtiene el historial paginado de compras del comprador autenticado (requiere rol BUYER).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de compras del comprador", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol BUYER)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/me/purchases")
    public ResponseEntity<Page<PurchaseResponseDTO>> getMyPurchases(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(purchaseService.getPurchasesByBuyer(userDetails.getId(), pageable).map(PurchaseResponseDTO::from));
    }
}
