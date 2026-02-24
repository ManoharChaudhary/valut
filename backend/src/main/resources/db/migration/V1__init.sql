-- Initial schema for Vault (Phase 1).
-- TODO: add indexes once access patterns are proven.

create table if not exists tenants (
  id bigserial primary key,
  name text not null,
  status text not null,
  created_at timestamptz not null default now()
);
