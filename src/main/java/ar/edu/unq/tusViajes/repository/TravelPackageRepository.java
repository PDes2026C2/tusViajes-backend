package ar.edu.unq.tusViajes.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import ar.edu.unq.tusViajes.model.TravelPackage;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long>, JpaSpecificationExecutor<TravelPackage> {

    Page<TravelPackage> findByAgencyId(Long agencyId, Pageable pageable);

    Optional<TravelPackage> findByIdAndAgencyId(Long id, Long agencyId);
}
