-- Catalogs + public IDs + rule/feature many-to-many + scope tables + global default constraint.

create table if not exists sectors (
  id bigserial primary key,
  public_id uuid not null unique default gen_random_uuid(),
  code text not null unique,
  display_name text not null,
  created_at timestamptz not null default now()
);

create table if not exists roles (
  id bigserial primary key,
  public_id uuid not null unique default gen_random_uuid(),
  name text not null unique,
  created_at timestamptz not null default now()
);

alter table tenants add column if not exists public_id uuid;
update tenants set public_id = gen_random_uuid() where public_id is null;
alter table tenants alter column public_id set not null;
create unique index if not exists uq_tenants_public_id on tenants (public_id);

alter table feature_definitions add column if not exists public_id uuid;
update feature_definitions set public_id = gen_random_uuid() where public_id is null;
alter table feature_definitions alter column public_id set not null;
create unique index if not exists uq_feature_definitions_public_id on feature_definitions (public_id);

alter table feature_definitions add column if not exists display_name text;

alter table rules add column if not exists rule_name text not null default 'Unnamed rule';
alter table rules add column if not exists is_default boolean not null default false;

create table if not exists rule_features (
  rule_id bigint not null references rules (id) on delete cascade,
  feature_definition_id bigint not null references feature_definitions (id) on delete cascade,
  primary key (rule_id, feature_definition_id)
);

insert into rule_features (rule_id, feature_definition_id)
select r.id, fd.id
from rules r
inner join feature_definitions fd on fd.feature_key = r.feature_key;

alter table rules drop column if exists feature_key;

create unique index if not exists uq_rules_single_global_default on rules (is_default)
where is_default;

create table if not exists rule_tenant_scopes (
  id bigserial primary key,
  rule_id bigint not null references rules (id) on delete cascade,
  tenant_id bigint not null references tenants (id) on delete cascade,
  unique (rule_id, tenant_id)
);

create table if not exists rule_sector_scopes (
  id bigserial primary key,
  rule_id bigint not null references rules (id) on delete cascade,
  sector_id bigint not null references sectors (id) on delete cascade,
  unique (rule_id, sector_id)
);

create table if not exists rule_role_scopes (
  id bigserial primary key,
  rule_id bigint not null references rules (id) on delete cascade,
  role_id bigint not null references roles (id) on delete cascade,
  unique (rule_id, role_id)
);
