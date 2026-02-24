create table if not exists rules (
  id bigserial primary key,
  feature_key text not null,
  hierarchy_level text not null,
  rule_type text not null,
  priority int not null,
  active boolean not null
);

