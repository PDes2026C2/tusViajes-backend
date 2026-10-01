package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.adapters.dto.CityDTO;
import ar.edu.unq.tusViajes.service.CityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Ciudades", description = "Endpoints públicos para consulta de ciudades")
@RestController
@RequestMapping("/api/cities")
@RequiredArgsConstructor
public class CityController {

    private final CityService cityService;

    @Operation(summary = "Listar ciudades", description = "Obtiene la lista pública de ciudades registradas con soporte de filtro opcional por código ISO de país.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de ciudades obtenida exitosamente", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CityDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<CityDTO>> getCities(
            @Parameter(description = "Código ISO del país para filtrar (ej: AR)") @RequestParam(name = "isoCode", required = false) String isoCode) {
        return ResponseEntity.ok(cityService.getCities(isoCode));
    }
}
