package io.life.inventory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockRecordDto {

	private Long id;
	private Long workstationId;
	private String itemType;
	private Long itemId;
	private Integer quantity;
	private LocalDateTime lastUpdated;

}
