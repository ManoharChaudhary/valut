# Spring & JPA primer (annotation map)

Short reference for reading Vault code. Deep theory is in [backend-foundations-and-request-flow.md](backend-foundations-and-request-flow.md).

## Web layer
| Annotation | Role |
|------------|------|
| `@SpringBootApplication` | Entry point: enables auto-configuration and component scan (default: package of this class and subpackages). |
| `@RestController` | HTTP controller; return values serialized to JSON/XML (via HttpMessageConverters). |
| `@RequestMapping` / `@GetMapping` / `@PostMapping` | URL path + HTTP method binding. |
| `@RequestBody` | Deserialize JSON body into a Java object (DTO). |
| `@Validated` on class | Enables method-level validation on controllers (with `@Valid` on parameters). |
| `@Valid` on parameter | Run Bean Validation on that object (e.g. `@NotBlank`). |

## Stereotypes (components)
| Annotation | Typical use |
|------------|-------------|
| `@Service` | Business logic / use-case orchestration. |
| `@Component` | Generic injectable bean (e.g. strategy implementations). |
| `@Repository` | Persistence layer (often optional when using Spring Data interfaces). |

## Data access
| Piece | Role |
|-------|------|
| `JpaRepository<Entity, Id>` | Spring Data generates CRUD + query methods from names. |
| `@Entity` / `@Table` | Map class ↔ relational table. |
| `@Id` / `@GeneratedValue` | Primary key strategy. |
| `@Column` | Column mapping; `nullable`, `name`, `columnDefinition` for JSONB. |
| `@Enumerated(STRING)` | Store enum as readable text in DB. |
| `@ManyToOne`, `@OneToMany` | Associations; mind fetch type (`LAZY` vs `EAGER`). |
| `@JdbcTypeCode(SqlTypes.JSON)` | Map JSON/JSONB columns (Hibernate 6+ style). |

## Transactions (preview)
| Annotation | Role |
|------------|------|
| `@Transactional` | Declare a transaction boundary (commit/rollback). Often on service methods. |

We will use `@Transactional` more deliberately when we add rule mutation APIs (append-only versions).

## Validation
| Piece | Role |
|-------|------|
| `jakarta.validation.constraints.*` | Declarative constraints (`@NotBlank`, etc.). |
| `spring-boot-starter-validation` | Brings Hibernate Validator onto the classpath. |

## Configuration
| File | Role |
|------|------|
| `application.yml` | Base config. |
| `application-{profile}.yml` | Profile-specific overrides (`local`, `prod`, …). |
| `SPRING_PROFILES_ACTIVE` | Chooses which profile files load. |

## Flyway
| Piece | Role |
|-------|------|
| `db/migration/V{version}__description.sql` | Ordered schema changes applied at startup. |

When you add a migration, restart the app (or test context) so Flyway applies it.
