package ar.edu.unq.tusViajes.builder;

import ar.edu.unq.tusViajes.model.Agencia;

public class AgenciaBuilder {

    private String razonSocial = "Turismo Huryn SA";
    private String cuit = "30-12345678-9";

    public static AgenciaBuilder anAgencia() {
        return new AgenciaBuilder();
    }

    public AgenciaBuilder withRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
        return this;
    }

    public AgenciaBuilder withCuit(String cuit) {
        this.cuit = cuit;
        return this;
    }

    public Agencia build() {
        return new Agencia(razonSocial, cuit);
    }
}

