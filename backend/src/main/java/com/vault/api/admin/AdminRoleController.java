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

import com.vault.catalog.RoleEntity;
import com.vault.catalog.RoleEntityRepository;

@RestController
@RequestMapping("/api/v1/admin/roles")
public class AdminRoleController {
	private final RoleEntityRepository roleEntityRepository;

	public AdminRoleController(RoleEntityRepository roleEntityRepository) {
		this.roleEntityRepository = roleEntityRepository;
	}

	@GetMapping
	public List<RoleResponse> list() {
		return roleEntityRepository.findAll().stream().map(RoleResponse::from).toList();
	}

	@GetMapping("/{id}")
	public RoleResponse get(@PathVariable long id) {
		return roleEntityRepository.findById(id).map(RoleResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public RoleResponse create(@RequestBody RoleWriteRequest body) {
		if (body.name() == null || body.name().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "name required");
		}
		RoleEntity r = new RoleEntity();
		r.setName(body.name().trim());
		return RoleResponse.from(roleEntityRepository.save(r));
	}

	@PutMapping("/{id}")
	public RoleResponse update(@PathVariable long id, @RequestBody RoleWriteRequest body) {
		RoleEntity r = roleEntityRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (body.name() != null && !body.name().isBlank()) {
			r.setName(body.name().trim());
		}
		return RoleResponse.from(roleEntityRepository.save(r));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		if (!roleEntityRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		roleEntityRepository.deleteById(id);
	}

	public record RoleWriteRequest(String name) {}

	public record RoleResponse(long id, UUID publicId, String name) {
		static RoleResponse from(RoleEntity r) {
			return new RoleResponse(r.getId(), r.getPublicId(), r.getName());
		}
	}
}
