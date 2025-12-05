package io.life.masterdata.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import io.life.masterdata.entity.ProductVariant;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

}
