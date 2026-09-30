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

@Builder
public record TravelPackageRequestDTO(
   @NotBlank(message = "El nombre es obligatorio")
   String name,

   String description,

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    Double price,

    @NotNull(message = "La fecha de inicio es obligatoria")
    @FutureOrPresent(message = "La fecha de inicio debe ser posterior a la actual")
    LocalDateTime startDate,

    @NotNull(message = "La fecha de finalizacion es obligatoria")
    LocalDateTime endDate,

    @NotNull(message = "El hotel es obligatorio")
    Long hotelId,

    @NotNull(message = "El vuelo de ida es obligatorio")
    Long departureFlightId,

    @NotNull(message = "El vuelo de vuelta es obligatorio")
    Long returnFlightId
) {

}
