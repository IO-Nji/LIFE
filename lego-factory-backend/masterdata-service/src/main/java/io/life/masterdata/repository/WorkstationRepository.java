package io.life.masterdata.repository;

import io.life.masterdata.entity.Workstation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WorkstationRepository extends JpaRepository<Workstation, Long> {

	List<Workstation> findByActive(Boolean active);

	Workstation findByName(String name);

}
