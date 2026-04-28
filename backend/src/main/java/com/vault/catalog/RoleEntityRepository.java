package com.vault.catalog;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleEntityRepository extends JpaRepository<RoleEntity, Long> {
	Optional<RoleEntity> findByPublicId(UUID publicId);

	Optional<RoleEntity> findByNameIgnoreCase(String name);
}
