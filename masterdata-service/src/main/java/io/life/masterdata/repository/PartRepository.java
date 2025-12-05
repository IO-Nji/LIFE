package io.life.masterdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.life.masterdata.entity.Part;

public interface PartRepository extends JpaRepository<Part, Long> {

}
