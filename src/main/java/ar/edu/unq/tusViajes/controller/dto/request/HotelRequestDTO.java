package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record HotelRequestDTO(
    @NotBlank(message = "El nombre es obligatorio") String name,
    @NotNull(message = "La ciudad es obligatoria") Long cityId,
    String photoUrl,
    String services
) {}
