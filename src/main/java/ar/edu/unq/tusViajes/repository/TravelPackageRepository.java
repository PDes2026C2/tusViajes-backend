package ar.edu.unq.tusViajes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ar.edu.unq.tusViajes.model.TravelPackage;

public interface TravelPackageRepository extends JpaRepository<TravelPackage, Long> {
}
