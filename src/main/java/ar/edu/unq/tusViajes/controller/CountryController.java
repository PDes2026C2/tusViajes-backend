package ar.edu.unq.tusViajes.controller;

import ar.edu.unq.tusViajes.adapters.dto.CountryDTO;
import ar.edu.unq.tusViajes.service.CountryService;
import io.swagger.v3.oas.annotations.Operation;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Países", description = "Endpoints públicos para consulta de países")
@RestController
@RequestMapping("/api/countries")
@RequiredArgsConstructor
public class CountryController {

    private final CountryService countryService;

    @Operation(summary = "Listar países", description = "Obtiene la lista pública de todos los países disponibles en la plataforma.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de países obtenida exitosamente", content = @Content(array = @ArraySchema(schema = @Schema(implementation = CountryDTO.class))))
    })
    @GetMapping
    public ResponseEntity<List<CountryDTO>> getCountries() {
        return ResponseEntity.ok(countryService.getCountries());
    }
}
