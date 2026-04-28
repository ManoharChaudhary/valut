insert into sectors (public_id, code, display_name)
values
  ('a1111111-1111-4111-8111-111111111111'::uuid, 'BIGSEG', 'Big Tech Segment'),
  ('a2222222-2222-4222-8222-222222222222'::uuid, 'GOV-SEC', 'Government Sector'),
  ('a3333333-3333-4333-8333-333333333333'::uuid, 'BS-SEC', 'Business Services')
on conflict (code) do nothing;

insert into roles (public_id, name)
values
  ('b1111111-1111-4111-8111-111111111111'::uuid, 'Super Admin'),
  ('b2222222-2222-4222-8222-222222222222'::uuid, 'Admin'),
  ('b3333333-3333-4333-8333-333333333333'::uuid, 'Manager'),
  ('b4444444-4444-4444-8444-444444444444'::uuid, 'User')
on conflict (name) do nothing;

insert into tenants (name, status, public_id)
select 'JPMorgan Demo', 'ACTIVE', 'c1111111-1111-4111-8111-111111111111'::uuid
where not exists (select 1 from tenants where public_id = 'c1111111-1111-4111-8111-111111111111'::uuid);

insert into tenants (name, status, public_id)
select 'DBS Demo', 'ACTIVE', 'c2222222-2222-4222-8222-222222222222'::uuid
where not exists (select 1 from tenants where public_id = 'c2222222-2222-4222-8222-222222222222'::uuid);

insert into tenants (name, status, public_id)
select 'ST Engineering Demo', 'ACTIVE', 'c3333333-3333-4333-8333-333333333333'::uuid
where not exists (select 1 from tenants where public_id = 'c3333333-3333-4333-8333-333333333333'::uuid);

insert into feature_definitions (feature_key, context_schema, public_id, display_name)
values (
  'core.user_management',
  '{
    "type": "object",
    "required": ["tenant_id", "sector", "role_id"],
    "properties": {
      "tenant_id": { "type": "string", "description": "Tenant public UUID" },
      "sector": { "type": "string" },
      "role_id": { "type": "string" },
      "role_name": { "type": "string" },
      "user_id": { "type": "string" }
    }
  }'::jsonb,
  'd1111111-1111-4111-8111-111111111111'::uuid,
  'User Management'
)
on conflict (feature_key) do update set
  display_name = excluded.display_name,
  context_schema = excluded.context_schema,
  public_id = excluded.public_id;

insert into feature_definitions (feature_key, context_schema, public_id, display_name)
values (
  'core.report_customer_care',
  '{
    "type": "object",
    "required": ["tenant_id", "sector", "role_id"],
    "properties": {
      "tenant_id": { "type": "string" },
      "sector": { "type": "string" },
      "role_id": { "type": "string" },
      "role_name": { "type": "string" },
      "user_id": { "type": "string" }
    }
  }'::jsonb,
  'd2222222-2222-4222-8222-222222222222'::uuid,
  'Report Customer Care'
)
on conflict (feature_key) do update set
  display_name = excluded.display_name,
  context_schema = excluded.context_schema,
  public_id = excluded.public_id;

insert into feature_definitions (feature_key, context_schema, public_id, display_name)
values (
  'core.incident_maintenance',
  '{
    "type": "object",
    "required": ["tenant_id", "sector", "role_id"],
    "properties": {
      "tenant_id": { "type": "string" },
      "sector": { "type": "string" },
      "role_id": { "type": "string" },
      "role_name": { "type": "string" },
      "user_id": { "type": "string" }
    }
  }'::jsonb,
  'd3333333-3333-4333-8333-333333333333'::uuid,
  'Incident & Maintenance'
)
on conflict (feature_key) do update set
  display_name = excluded.display_name,
  context_schema = excluded.context_schema,
  public_id = excluded.public_id;
