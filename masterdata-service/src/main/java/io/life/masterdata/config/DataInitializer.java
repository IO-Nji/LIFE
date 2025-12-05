package io.life.masterdata.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import io.life.masterdata.entity.Module;
import io.life.masterdata.entity.Part;
import io.life.masterdata.entity.ProductVariant;
import io.life.masterdata.entity.Workstation;
import io.life.masterdata.service.ModuleService;
import io.life.masterdata.service.PartService;
import io.life.masterdata.service.ProductVariantService;
import io.life.masterdata.service.WorkstationService;

@Component
public class DataInitializer implements CommandLineRunner {

    private final ProductVariantService productVariantService;
    private final ModuleService moduleService;
    private final PartService partService;
    private final WorkstationService workstationService;

    public DataInitializer(ProductVariantService productVariantService, ModuleService moduleService,
            PartService partService, WorkstationService workstationService) {
        this.productVariantService = productVariantService;
        this.moduleService = moduleService;
        this.partService = partService;
        this.workstationService = workstationService;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed workstations
        if (workstationService.findAll().isEmpty()) {
            workstationService.save(createWorkstationDto(null, "Injection Molding Station 1", "MANUFACTURING", "Plastic injection molding equipment", true));
            workstationService.save(createWorkstationDto(null, "Parts Pre-Production 1", "MANUFACTURING", "Parts preparation and assembly", true));
            workstationService.save(createWorkstationDto(null, "Part Finishing 1", "MANUFACTURING", "Part finishing and quality control", true));
            workstationService.save(createWorkstationDto(null, "Gear Assembly 1", "ASSEMBLY", "Gear assembly station", true));
            workstationService.save(createWorkstationDto(null, "Motor Assembly 1", "ASSEMBLY", "Motor assembly station", true));
            workstationService.save(createWorkstationDto(null, "Final Assembly 1", "ASSEMBLY", "Final assembly station", true));
            workstationService.save(createWorkstationDto(null, "Plant Warehouse", "WAREHOUSE", "Main warehouse for Plant", true));
            workstationService.save(createWorkstationDto(null, "Modules Supermarket", "WAREHOUSE", "Modules inventory point", true));
            workstationService.save(createWorkstationDto(null, "Parts Supply Warehouse", "WAREHOUSE", "Parts supply warehouse", true));
        }

        // Seed product variants
        if (productVariantService.findAll().isEmpty()) {
            productVariantService.save(new ProductVariant(null, "Technic Truck Yellow", "Yellow LEGO Technic truck", 149.99, 240));
            productVariantService.save(new ProductVariant(null, "Technic Truck Red", "Red LEGO Technic truck", 149.99, 240));
            productVariantService.save(new ProductVariant(null, "Creator House", "Modular LEGO Creator house", 99.99, 180));
            productVariantService.save(new ProductVariant(null, "Friends Cafe", "LEGO Friends coffee shop", 79.99, 150));
        }

        // Seed modules
        if (moduleService.findAll().isEmpty()) {
            moduleService.save(new Module(null, "Chassis Module", "Main structural frame", "MECHANICAL"));
            moduleService.save(new Module(null, "Electrical Module", "Power and electronics", "ELECTRICAL"));
            moduleService.save(new Module(null, "Wheel Assembly", "Tire and axle system", "MECHANICAL"));
            moduleService.save(new Module(null, "Control Panel", "User interface", "ELECTRICAL"));
        }

        // Seed parts
        if (partService.findAll().isEmpty()) {
            partService.save(new Part(null, "Brick 2x4", "Standard building brick", "STRUCTURAL", 0.25));
            partService.save(new Part(null, "Wheel 68.82x34.4", "Large wheel component", "MECHANICAL", 1.50));
            partService.save(new Part(null, "Motor XM", "Power motor", "ELECTRICAL", 8.99));
            partService.save(new Part(null, "Connector Pin", "Standard connector", "MECHANICAL", 0.10));
            partService.save(new Part(null, "LED Light", "Colored LED element", "ELECTRICAL", 2.50));
        }
    }

    private io.life.masterdata.dto.WorkstationDto createWorkstationDto(Long id, String name, String type, String description, Boolean active) {
        io.life.masterdata.dto.WorkstationDto dto = new io.life.masterdata.dto.WorkstationDto();
        dto.setId(id);
        dto.setName(name);
        dto.setWorkstationType(type);
        dto.setDescription(description);
        dto.setActive(active);
        return dto;
    }
}

