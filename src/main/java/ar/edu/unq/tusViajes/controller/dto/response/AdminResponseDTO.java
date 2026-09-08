package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Admin;

public record AdminResponseDTO(
    Long id,
    String nombre,
    String apellido,
    String email
) {
    public static AdminResponseDTO from(Admin admin) {
        if (admin == null) {
            return null;
        }
        return new AdminResponseDTO(
                admin.getId(),
                admin.getNombre(),
                admin.getApellido(),
                admin.getEmail()
        );
    }
}
