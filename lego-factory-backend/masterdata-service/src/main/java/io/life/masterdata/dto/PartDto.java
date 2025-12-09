package io.life.masterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartDto {

    private Long id;
    private String name;
    private String description;
    private String category;
    private Double unitCost;
}
