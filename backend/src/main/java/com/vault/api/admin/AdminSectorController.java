package com.vault.api.admin;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.vault.catalog.Sector;
import com.vault.catalog.SectorRepository;

@RestController
@RequestMapping("/api/v1/admin/sectors")
public class AdminSectorController {
	private final SectorRepository sectorRepository;

	public AdminSectorController(SectorRepository sectorRepository) {
		this.sectorRepository = sectorRepository;
	}

	@GetMapping
	public List<SectorResponse> list() {
		return sectorRepository.findAll().stream().map(SectorResponse::from).toList();
	}

	@GetMapping("/{id}")
	public SectorResponse get(@PathVariable long id) {
		return sectorRepository.findById(id).map(SectorResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public SectorResponse create(@RequestBody SectorWriteRequest body) {
		if (body.code() == null || body.code().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "code required");
		}
		Sector s = new Sector();
		s.setCode(body.code().trim().toUpperCase(java.util.Locale.ROOT));
		s.setDisplayName(body.displayName() == null ? s.getCode() : body.displayName());
		return SectorResponse.from(sectorRepository.save(s));
	}

	@PutMapping("/{id}")
	public SectorResponse update(@PathVariable long id, @RequestBody SectorWriteRequest body) {
		Sector s = sectorRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (body.displayName() != null) {
			s.setDisplayName(body.displayName());
		}
		if (body.code() != null && !body.code().isBlank()) {
			s.setCode(body.code().trim().toUpperCase(java.util.Locale.ROOT));
		}
		return SectorResponse.from(sectorRepository.save(s));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		if (!sectorRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		sectorRepository.deleteById(id);
	}

	public record SectorWriteRequest(String code, String displayName) {}

	public record SectorResponse(long id, UUID publicId, String code, String displayName) {
		static SectorResponse from(Sector s) {
			return new SectorResponse(s.getId(), s.getPublicId(), s.getCode(), s.getDisplayName());
		}
	}
}
