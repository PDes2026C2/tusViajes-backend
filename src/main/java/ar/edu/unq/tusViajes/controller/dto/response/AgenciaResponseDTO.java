package ar.edu.unq.tusViajes.controller.dto.response;

import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.EstadoAgencia;

public record AgenciaResponseDTO(
    Long id,
    String razonSocial,
    String cuit,
    String email,
    EstadoAgencia estado
) {
    public AgenciaResponseDTO(Long id, String razonSocial, String cuit) {
        this(id, razonSocial, cuit, null, EstadoAgencia.AUTORIZADA);
    }

    public static AgenciaResponseDTO from(Agencia agencia) {
        if (agencia == null) {
            return null;
        }
        return new AgenciaResponseDTO(
                agencia.getId(),
                agencia.getRazonSocial(),
                agencia.getCuit(),
                agencia.getEmail(),
                agencia.getEstado()
        );
    }
}
