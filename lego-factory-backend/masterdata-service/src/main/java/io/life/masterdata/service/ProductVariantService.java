package io.life.masterdata.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.life.masterdata.entity.ProductVariant;
import io.life.masterdata.repository.ProductVariantRepository;

@Service
public class ProductVariantService {

    private final ProductVariantRepository repository;

    public ProductVariantService(ProductVariantRepository repository) {
        this.repository = repository;
    }

    public List<ProductVariant> findAll() {
        return repository.findAll();
    }

    @SuppressWarnings("null")
    public Optional<ProductVariant> findById(Long id) {
        return repository.findById(id);
    }

    @SuppressWarnings("null")
    public ProductVariant save(ProductVariant variant) {
        return repository.save(variant);
    }

    @SuppressWarnings("null")
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
