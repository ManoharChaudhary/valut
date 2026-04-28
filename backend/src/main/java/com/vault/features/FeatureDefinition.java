package com.vault.features;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.PrePersist;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.databind.JsonNode;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "feature_definitions",
		uniqueConstraints = @UniqueConstraint(name = "uq_feature_definitions_feature_key", columnNames = "feature_key")
)
public class FeatureDefinition {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@JdbcTypeCode(SqlTypes.UUID)
	@Column(name = "public_id", nullable = false, unique = true)
	private UUID publicId;

	@Column(name = "feature_key", nullable = false)
	private String featureKey;

	@Column(name = "display_name")
	private String displayName;

	/**
	 * JSON Schema describing the required shape of the evaluation context.
	 * Fail closed if this is missing or invalid.
	 */
	@JdbcTypeCode(SqlTypes.JSON)
	@Column(name = "context_schema", nullable = false, columnDefinition = "jsonb")
	private JsonNode contextSchema;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public UUID getPublicId() {
		return publicId;
	}

	public void setPublicId(UUID publicId) {
		this.publicId = publicId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getFeatureKey() {
		return featureKey;
	}

	public void setFeatureKey(String featureKey) {
		this.featureKey = featureKey;
	}

	public JsonNode getContextSchema() {
		return contextSchema;
	}

	public void setContextSchema(JsonNode contextSchema) {
		this.contextSchema = contextSchema;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	@PrePersist
	void prePersist() {
		if (publicId == null) {
			publicId = UUID.randomUUID();
		}
		if (createdAt == null) {
			createdAt = Instant.now();
		}
	}
}

