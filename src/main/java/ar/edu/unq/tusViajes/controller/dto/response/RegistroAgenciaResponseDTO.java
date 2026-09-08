package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.EstadoAgencia;

public record RegistroAgenciaResponseDTO(
    Long id,
    String email,
    String razonSocial,
    String cuit,
    EstadoAgencia estado,
    String mensaje
) {
    public static RegistroAgenciaResponseDTO from(Agencia agencia, String mensaje) {
        if (agencia == null) {
            return null;
        }
        return new RegistroAgenciaResponseDTO(
                agencia.getId(),
                agencia.getEmail(),
                agencia.getRazonSocial(),
                agencia.getCuit(),
                agencia.getEstado(),
                mensaje
        );
    }
}
