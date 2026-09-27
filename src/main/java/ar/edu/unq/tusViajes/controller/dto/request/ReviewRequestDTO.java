package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequestDTO(
        @NotNull(message = "Se debe ingresar un puntaje")
        @Min(value = 0, message = "El puntaje debe ser mayor a 0")
        @Max(value = 10, message = "El puntaje no puede ser mayor a 10")
        Integer score,
        @Size(max = 1000, message = "El comentario no puede exceder los 1000 caracteres.")
        String comment
) {
}