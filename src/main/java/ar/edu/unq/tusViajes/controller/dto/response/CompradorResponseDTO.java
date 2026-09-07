package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Comprador;

public record CompradorResponseDTO(
    Long id,
    String nombre,
    String apellido,
    String email,
    String telefono,
    String dni
) {
    public static CompradorResponseDTO from(Comprador comprador) {
        if (comprador == null) {
            return null;
        }
        return new CompradorResponseDTO(
                comprador.getId(),
                comprador.getNombre(),
                comprador.getApellido(),
                comprador.getEmail(),
                comprador.getTelefono(),
                comprador.getDni()
        );
    }
}
