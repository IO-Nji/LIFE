package io.life.masterdata.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "workstations")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Workstation {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, unique = true, length = 100)
	private String name;

	@Column(length = 50)
	private String workstationType;

	@Column(length = 500)
	private String description;

	@Column(nullable = false)
	private Boolean active;

}
