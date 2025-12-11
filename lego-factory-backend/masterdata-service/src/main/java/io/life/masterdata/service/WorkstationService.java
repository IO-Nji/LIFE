package io.life.masterdata.service;

import io.life.masterdata.entity.Workstation;
import io.life.masterdata.repository.WorkstationRepository;
import io.life.masterdata.dto.WorkstationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WorkstationService {

	private final WorkstationRepository repository;

	public List<WorkstationDto> findAll() {
		return repository.findAll().stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	public List<WorkstationDto> findActive() {
		return repository.findByActive(true).stream()
				.map(this::toDto)
				.collect(Collectors.toList());
	}

	@SuppressWarnings("null")
	public WorkstationDto findById(Long id) {
		return repository.findById(id)
				.map(this::toDto)
				.orElse(null);
	}

	public WorkstationDto findByName(String name) {
		Workstation workstation = repository.findByName(name);
		return workstation != null ? toDto(workstation) : null;
	}

	public WorkstationDto save(WorkstationDto dto) {
		Workstation workstation = new Workstation();
		workstation.setName(dto.getName());
		workstation.setWorkstationType(dto.getWorkstationType());
		workstation.setDescription(dto.getDescription());
		workstation.setActive(dto.getActive() != null ? dto.getActive() : true);

		Workstation saved = repository.save(workstation);
		return toDto(saved);
	}

	@SuppressWarnings("null")
	public void deleteById(Long id) {
		repository.deleteById(id);
	}

	private WorkstationDto toDto(Workstation entity) {
		return new WorkstationDto(
				entity.getId(),
				entity.getName(),
				entity.getWorkstationType(),
				entity.getDescription(),
				entity.getActive()
		);
	}

}
