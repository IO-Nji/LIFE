package io.life.masterdata.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.life.masterdata.dto.ModuleDto;
import io.life.masterdata.dto.PartDto;
import io.life.masterdata.dto.WorkstationDto;
import io.life.masterdata.entity.Module;
import io.life.masterdata.entity.Part;
import io.life.masterdata.service.ModuleService;
import io.life.masterdata.service.PartService;
import io.life.masterdata.service.WorkstationService;

@RestController
@RequestMapping("/api/masterdata")
public class MasterdataController {

    private final ModuleService moduleService;
    private final PartService partService;
    private final WorkstationService workstationService;

    public MasterdataController(ModuleService moduleService,
            PartService partService, WorkstationService workstationService) {
        this.moduleService = moduleService;
        this.partService = partService;
        this.workstationService = workstationService;
    }

    @GetMapping("/modules")
    public List<ModuleDto> getModules() {
        return moduleService.findAll().stream()
            .map(this::toModuleDto)
            .collect(Collectors.toList());
    }

    @GetMapping("/workstations")
    public List<WorkstationDto> getWorkstations() {
        return workstationService.findAll();
    }

    @GetMapping("/parts")
    public List<PartDto> getParts() {
        return partService.findAll().stream()
            .map(this::toPartDto)
            .collect(Collectors.toList());
    }

    private ModuleDto toModuleDto(Module module) {
        return new ModuleDto(
            module.getId(),
            module.getName(),
            module.getDescription(),
            module.getType()
        );
    }

    private PartDto toPartDto(Part part) {
        return new PartDto(
            part.getId(),
            part.getName(),
            part.getDescription(),
            part.getCategory(),
            part.getUnitCost()
        );
    }
}
