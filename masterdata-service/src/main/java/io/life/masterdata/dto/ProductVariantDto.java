package io.life.masterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantDto {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer estimatedTimeMinutes;
}
