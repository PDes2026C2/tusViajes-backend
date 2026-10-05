package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.controller.dto.response.BuyersTopResponseDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.service.AdminMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Reportes de Admin", description = "Sistema de reportes de uso administrativo")
@RestController
@RequestMapping("/api/admin/metrics")
@RequiredArgsConstructor
public class AdminMetricsController {

    private final AdminMetricsService adminMetricsService;

    @Operation(summary = "Obtener top 5 compradores", description = "Devuelve hasta cinco compradores con compras, ordenados por cantidad de compras descendente y ID de comprador ascendente en caso de empate (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Top compradores con su cantidad de compras", content = @Content(array = @ArraySchema(schema = @Schema(implementation = BuyersTopResponseDTO.class)))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping(value = "/top-buyers", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<BuyersTopResponseDTO>> getTopBuyers() {
        return ResponseEntity.ok(adminMetricsService.getTopBuyers().stream()
                .map(BuyersTopResponseDTO::from).toList());
    }
}
