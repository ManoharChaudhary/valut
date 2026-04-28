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

import com.vault.tenancy.Tenant;
import com.vault.tenancy.TenantRepository;
import com.vault.tenancy.TenantStatus;

@RestController
@RequestMapping("/api/v1/admin/tenants")
public class AdminTenantController {
	private final TenantRepository tenantRepository;

	public AdminTenantController(TenantRepository tenantRepository) {
		this.tenantRepository = tenantRepository;
	}

	@GetMapping
	public List<TenantResponse> list() {
		return tenantRepository.findAll().stream().map(TenantResponse::from).toList();
	}

	@GetMapping("/{id}")
	public TenantResponse get(@PathVariable long id) {
		return tenantRepository.findById(id).map(TenantResponse::from)
				.orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public TenantResponse create(@RequestBody TenantWriteRequest body) {
		Tenant t = new Tenant();
		t.setName(body.name());
		t.setStatus(body.status() == null ? TenantStatus.ACTIVE : body.status());
		return TenantResponse.from(tenantRepository.save(t));
	}

	@PutMapping("/{id}")
	public TenantResponse update(@PathVariable long id, @RequestBody TenantWriteRequest body) {
		Tenant t = tenantRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
		if (body.name() != null) {
			t.setName(body.name());
		}
		if (body.status() != null) {
			t.setStatus(body.status());
		}
		return TenantResponse.from(tenantRepository.save(t));
	}

	@DeleteMapping("/{id}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void delete(@PathVariable long id) {
		if (!tenantRepository.existsById(id)) {
			throw new ResponseStatusException(HttpStatus.NOT_FOUND);
		}
		tenantRepository.deleteById(id);
	}

	public record TenantWriteRequest(String name, TenantStatus status) {}

	public record TenantResponse(long id, UUID publicId, String name, TenantStatus status) {
		static TenantResponse from(Tenant t) {
			return new TenantResponse(t.getId(), t.getPublicId(), t.getName(), t.getStatus());
		}
	}
}
