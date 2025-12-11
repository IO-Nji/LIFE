package io.life.masterdata.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import io.life.masterdata.entity.Module;
import io.life.masterdata.repository.ModuleRepository;

@Service
public class ModuleService {

    private final ModuleRepository repository;

    public ModuleService(ModuleRepository repository) {
        this.repository = repository;
    }

    public List<Module> findAll() {
        return repository.findAll();
    }

    @SuppressWarnings("null")
    public Optional<Module> findById(Long id) {
        return repository.findById(id);
    }

    @SuppressWarnings("null")
    public Module save(Module module) {
        return repository.save(module);
    }

    @SuppressWarnings("null")
    public void deleteById(Long id) {
        repository.deleteById(id);
    }
}
