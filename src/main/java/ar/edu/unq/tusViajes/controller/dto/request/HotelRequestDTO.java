package ar.edu.unq.tusViajes.controller.dto.request;

import jakarta.validation.constraints.NotBlank;

public record HotelRequestDTO(
    @NotBlank(message = "El nombre es obligatorio") String nombre,
    @NotBlank(message = "El destino es obligatorio") String destino,
    String fotoUrl,
    String servicio
) {}
