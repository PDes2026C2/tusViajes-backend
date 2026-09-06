package ar.edu.unq.tusViajes.controller.dto.response;

  public record PerfilCompradorResponseDTO (
    Long id,
    String nombre,
    String apellido,
    String email,
    String telefono,
    String dni
) {}
