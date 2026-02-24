create table if not exists rule_versions (
  id bigserial primary key,
  rule_id bigint not null references rules(id) on delete cascade,
  conditions jsonb not null,
  variant_value jsonb,
  created_by text not null,
  created_at timestamptz not null default now()
);

