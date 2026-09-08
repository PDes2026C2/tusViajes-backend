package ar.edu.unq.tusViajes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ar.edu.unq.tusViajes.model.Admin;

public interface AdminRepository extends JpaRepository<Admin, Long> {
}
