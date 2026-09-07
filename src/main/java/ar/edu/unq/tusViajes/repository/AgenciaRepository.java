package ar.edu.unq.tusViajes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.Agencia;
import ar.edu.unq.tusViajes.model.EstadoAgencia;

public interface AgenciaRepository extends JpaRepository<Agencia, Long> {
    boolean existsByCuit(String cuit);
    List<Agencia> findByEstado(EstadoAgencia estado);
    Optional<Agencia> findByEmail(String email);
}
