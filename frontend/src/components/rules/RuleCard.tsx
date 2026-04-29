import { Badge } from "@/components/ui/badge";
import { Card, CardContent } from "@/components/ui/card";
import type { RuleDto } from "@/lib/admin-api";

export function RuleCard({ rule }: { rule: RuleDto }) {
  return (
    <Card>
      <CardContent className="space-y-3 py-4">
        <div className="flex flex-wrap items-center gap-2">
          <span className="font-medium text-slate-100">{rule.ruleName}</span>
          {rule.defaultRule ? (
            <Badge variant="muted">Default</Badge>
          ) : null}
          <Badge variant="muted">{rule.ruleType}</Badge>
          <Badge variant="muted">{rule.hierarchyLevel}</Badge>
          {!rule.active ? <Badge variant="deny">Inactive</Badge> : null}
        </div>
        <p className="font-mono text-xs text-slate-500">
          id {rule.id} · priority {rule.priority}
        </p>
        <p className="text-xs text-slate-400">
          Features: {rule.featureDefinitionIds.join(", ") || "—"} · Tenants:{" "}
          {rule.tenantIds.length ? rule.tenantIds.join(", ") : "any"} · Sectors:{" "}
          {rule.sectorIds.length ? rule.sectorIds.join(", ") : "any"} · Roles:{" "}
          {rule.roleIds.length ? rule.roleIds.join(", ") : "any"}
        </p>
      </CardContent>
    </Card>
  );
}
