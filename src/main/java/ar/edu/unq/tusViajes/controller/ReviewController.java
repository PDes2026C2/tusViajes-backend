package ar.edu.unq.tusViajes.controller;

import java.net.URI;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import ar.edu.unq.tusViajes.controller.dto.request.ReviewRequestDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ErrorDTO;
import ar.edu.unq.tusViajes.controller.dto.response.ReviewResponseDTO;
import ar.edu.unq.tusViajes.security.CustomUserDetails;
import ar.edu.unq.tusViajes.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Reseñas", description = "Endpoints para consulta administrativa y publicación de opiniones por compradores")
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @Operation(summary = "Listar reseñas de un paquete", description = "Obtiene las opiniones y puntuaciones paginadas de un paquete de viaje (requiere rol ADMIN).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de reseñas del paquete", content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "401", description = "Acceso no autorizado", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "403", description = "Acceso denegado (requiere rol ADMIN)", content = @Content(schema = @Schema(implementation = ErrorDTO.class))),
            @ApiResponse(responseCode = "404", description = "Paquete de viaje no encontrado", content = @Content(schema = @Schema(implementation = ErrorDTO.class)))
    })
    @GetMapping("/{travelPackageId}")
    public ResponseEntity<Page<ReviewResponseDTO>> getByTravelPackageId(
            @PathVariable Long travelPackageId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(reviewService.getByTravelPackageId(travelPackageId, pageable).map(ReviewResponseDTO::from));
    }

    @PostMapping("/{travelPackageId}")
    public ResponseEntity<ReviewResponseDTO> create(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long travelPackageId,
            @Valid @RequestBody ReviewRequestDTO dto) {
        ReviewResponseDTO created = ReviewResponseDTO.from(reviewService.create(userDetails, travelPackageId, dto));
        return ResponseEntity.created(URI.create("/api/reviews/" + travelPackageId + created.id()))
                .body(created);
    }

}