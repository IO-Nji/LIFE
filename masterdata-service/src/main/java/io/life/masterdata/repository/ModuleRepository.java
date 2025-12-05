package io.life.masterdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.life.masterdata.entity.Module;

public interface ModuleRepository extends JpaRepository<Module, Long> {

}
