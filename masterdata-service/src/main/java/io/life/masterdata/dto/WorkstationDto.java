package io.life.masterdata.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WorkstationDto {

	private Long id;
	private String name;
	private String workstationType;
	private String description;
	private Boolean active;

}
