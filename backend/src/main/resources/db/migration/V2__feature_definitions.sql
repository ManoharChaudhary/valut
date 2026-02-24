create table if not exists feature_definitions (
  id bigserial primary key,
  feature_key text not null,
  context_schema jsonb not null,
  created_at timestamptz not null default now(),
  constraint uq_feature_definitions_feature_key unique (feature_key)
);

