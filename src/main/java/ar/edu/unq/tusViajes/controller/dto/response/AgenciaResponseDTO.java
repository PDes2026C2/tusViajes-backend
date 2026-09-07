package ar.edu.unq.tusViajes.controller.dto.response;

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
}
