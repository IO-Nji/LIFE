package io.life.masterdata.controller;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.life.masterdata.dto.ModuleDto;
import io.life.masterdata.dto.PartDto;
import io.life.masterdata.dto.ProductVariantDto;
import io.life.masterdata.entity.Module;
import io.life.masterdata.entity.Part;
import io.life.masterdata.entity.ProductVariant;
import io.life.masterdata.service.ModuleService;
import io.life.masterdata.service.PartService;
import io.life.masterdata.service.ProductVariantService;

@RestController
@RequestMapping("/api/masterdata")
public class MasterdataController {

    private final ProductVariantService productVariantService;
    private final ModuleService moduleService;
    private final PartService partService;

    public MasterdataController(ProductVariantService productVariantService, ModuleService moduleService,
            PartService partService) {
        this.productVariantService = productVariantService;
        this.moduleService = moduleService;
        this.partService = partService;
    }

    @GetMapping("/product-variants")
    public List<ProductVariantDto> getProductVariants() {
        return productVariantService.findAll().stream()
            .map(this::toProductVariantDto)
            .collect(Collectors.toList());
    }

    @GetMapping("/modules")
    public List<ModuleDto> getModules() {
        return moduleService.findAll().stream()
            .map(this::toModuleDto)
            .collect(Collectors.toList());
    }

    @GetMapping("/parts")
    public List<PartDto> getParts() {
        return partService.findAll().stream()
            .map(this::toPartDto)
            .collect(Collectors.toList());
    }

    private ProductVariantDto toProductVariantDto(ProductVariant variant) {
        return new ProductVariantDto(
            variant.getId(),
            variant.getName(),
            variant.getDescription(),
            variant.getPrice(),
            variant.getEstimatedTimeMinutes()
        );
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
