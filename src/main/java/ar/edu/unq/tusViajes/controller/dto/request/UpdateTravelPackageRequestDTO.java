package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateTravelPackageRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private Double price;

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser posterior a la actual")
    private LocalDateTime startDate;

    @NotNull(message = "La fecha de finalizacion es obligatoria")
    private LocalDateTime endDate;

    @NotNull(message = "El hotel es obligatorio")
    private Long hotelId;

    @NotNull(message = "El vuelo de ida es obligatorio")
    private Long departureFlightId;

    @NotNull(message = "El vuelo de vuelta es obligatorio")
    private Long returnFlightId;
}

