package ar.edu.unq.tusViajes.controller.dto.response;

public record CompradorResponseDTO(
    Long id,
    String nombre,
    String apellido,
    String email,
    String telefono,
    String dni
) {}
