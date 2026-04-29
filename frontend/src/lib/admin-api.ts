const BASE = "/vault-api/api/v1/admin";

async function parseJson<T>(res: Response): Promise<T> {
  const text = await res.text();
  if (!res.ok) {
    throw new Error(text || `${res.status} ${res.statusText}`);
  }
  return text ? (JSON.parse(text) as T) : ({} as T);
}

export type FeatureDefinitionDto = {
  id: number;
  publicId: string;
  featureKey: string;
  displayName: string | null;
  contextSchema: unknown;
};

export type RuleDto = {
  id: number;
  ruleName: string;
  defaultRule: boolean;
  hierarchyLevel: string;
  ruleType: string;
  priority: number;
  active: boolean;
  featureDefinitionIds: number[];
  tenantIds: number[];
  sectorIds: number[];
  roleIds: number[];
};

export async function fetchFeatures(): Promise<FeatureDefinitionDto[]> {
  const res = await fetch(`${BASE}/features`);
  return parseJson<FeatureDefinitionDto[]>(res);
}

export async function fetchRules(): Promise<RuleDto[]> {
  const res = await fetch(`${BASE}/rules`);
  return parseJson<RuleDto[]>(res);
}
