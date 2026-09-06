package ar.edu.unq.tusViajes.model;

import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
 
@Entity
@Table(name = "perfil_comprador")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PerfilComprador extends Usuario {
 
    @Column(length = 30)
    private String telefono;

    @Column(unique = true, nullable = false, length = 8)
    private String dni;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "favorito", 
            joinColumns = @JoinColumn(name = "comprador_id"),
            inverseJoinColumns = @JoinColumn(name = "paquete_id")
    )
    private Set<Paquete> paquetesFavoritos = new HashSet<>();
 
    public PerfilComprador(String nombre, String apellido, String email,
                            String passwordHash, String telefono, String dni) {
        super(nombre, apellido, email, passwordHash);
        this.telefono = telefono;
        this.dni = dni;
    }

    public void agregarFavorito(Paquete paquete) {
        this.paquetesFavoritos.add(paquete);
    }
 
    public void quitarFavorito(Paquete paquete) {
        this.paquetesFavoritos.remove(paquete);
    }
}