package ar.edu.unq.tusViajes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.Agency;
import ar.edu.unq.tusViajes.model.AgencyStatus;

public interface AgencyRepository extends JpaRepository<Agency, Long> {
    boolean existsByTaxId(String taxId);
    List<Agency> findByStatus(AgencyStatus status);
    Optional<Agency> findByEmail(String email);
}
