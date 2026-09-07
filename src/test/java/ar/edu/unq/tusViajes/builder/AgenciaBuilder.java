package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.EstadoAgencia;

public class AgenciaBuilder {

    private String email = "turismo@agencia.com";
    private String passwordHash = "$2a$10$hashedPasswordPlaceholder";
    private String razonSocial = "Turismo Huryn SA";
    private String cuit = "30-12345678-9";
    private EstadoAgencia estado = EstadoAgencia.AUTORIZADA;

    public static AgenciaBuilder anAgencia() {
        return new AgenciaBuilder();
    }

    public AgenciaBuilder withEmail(String email) {
        this.email = email;
        return this;
    }

    public AgenciaBuilder withPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
        return this;
    }

    public AgenciaBuilder withRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
        return this;
    }

    public AgenciaBuilder withCuit(String cuit) {
        this.cuit = cuit;
        return this;
    }

    public AgenciaBuilder withEstado(EstadoAgencia estado) {
        this.estado = estado;
        return this;
    }

    public Agencia build() {
        return new Agencia(email, passwordHash, razonSocial, cuit, estado);
    }
}
