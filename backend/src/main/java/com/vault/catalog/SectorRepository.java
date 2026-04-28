package com.vault.catalog;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SectorRepository extends JpaRepository<Sector, Long> {
	Optional<Sector> findByPublicId(UUID publicId);

	Optional<Sector> findByCodeIgnoreCase(String code);
}
