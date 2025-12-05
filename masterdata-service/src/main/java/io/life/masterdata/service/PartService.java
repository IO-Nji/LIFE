package io.life.masterdata.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.life.masterdata.entity.Part;
import io.life.masterdata.repository.PartRepository;

@Service
public class PartService {

    private final PartRepository repository;

    public PartService(PartRepository repository) {
        this.repository = repository;
    }

    public List<Part> findAll() {
        return repository.findAll();
    }

    public Optional<Part> findById(Long id) {
        return repository.findById(id);
    }

    public Part save(Part part) {
        return repository.save(part);
    }

    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
