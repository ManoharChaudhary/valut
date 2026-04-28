-- One global default rule (boolean allow) linked to all core features when not already present.

insert into rules (rule_name, hierarchy_level, rule_type, priority, active, is_default)
select 'Global default permit', 'GLOBAL', 'BOOLEAN', 0, true, true
where not exists (select 1 from rules where is_default = true);

insert into rule_features (rule_id, feature_definition_id)
select r.id, fd.id
from rules r
cross join feature_definitions fd
where r.is_default = true
  and r.rule_name = 'Global default permit'
  and not exists (
    select 1 from rule_features rf where rf.rule_id = r.id and rf.feature_definition_id = fd.id
  );

insert into rule_versions (rule_id, conditions, variant_value, created_by, created_at)
select r.id, '{"allow": true}'::jsonb, null, 'flyway-seed', now()
from rules r
where r.is_default = true
  and r.rule_name = 'Global default permit'
  and not exists (select 1 from rule_versions rv where rv.rule_id = r.id);
