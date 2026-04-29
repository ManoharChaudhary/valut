"use client";

import { useMutation, useQuery } from "@tanstack/react-query";
import { useMemo, useState } from "react";
import { AppShell } from "@/components/layout/AppShell";
import { DecisionBreadcrumbs } from "@/components/decision/DecisionBreadcrumbs";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { fetchFeatures } from "@/lib/admin-api";
import {
  postEvaluate,
  type EvaluateResponseBody,
} from "@/lib/decisions-api";

const SEED_USER_MANAGEMENT = "d1111111-1111-4111-8111-111111111111";
const SEED_TENANT = "c1111111-1111-4111-8111-111111111111";
const SEED_ROLE = "b1111111-1111-4111-8111-111111111111";

export default function SimulatorPage() {
  const [featureId, setFeatureId] = useState(SEED_USER_MANAGEMENT);
  const [tenantId, setTenantId] = useState(SEED_TENANT);
  const [sector, setSector] = useState("BIGSEG");
  const [roleId, setRoleId] = useState(SEED_ROLE);
  const [extraContextRaw, setExtraContextRaw] = useState("{}");

  const featuresQuery = useQuery({
    queryKey: ["admin", "features"],
    queryFn: fetchFeatures,
  });

  const mutation = useMutation({
    mutationFn: async () => {
      let extra: Record<string, unknown> = {};
      try {
        extra = JSON.parse(extraContextRaw) as Record<string, unknown>;
      } catch {
        throw new Error("Extra context must be valid JSON object.");
      }
      if (!featureId.trim()) {
        throw new Error("Select or enter a feature publicId (UUID).");
      }
      const context: Record<string, unknown> = {
        tenant_id: tenantId.trim(),
        sector: sector.trim(),
        role_id: roleId.trim(),
        ...extra,
      };
      return postEvaluate({ featureId: featureId.trim(), context });
    },
  });

  const data = mutation.data;
  const featureOptions = useMemo(
    () => featuresQuery.data ?? [],
    [featuresQuery.data]
  );

  return (
    <AppShell>
      <div className="space-y-8">
        <header className="space-y-2 border-b border-slate-800 pb-6">
          <p className="text-xs font-medium uppercase tracking-widest text-slate-500">
            Vault
          </p>
          <h1 className="text-2xl font-semibold tracking-tight text-white">
            Decision simulator
          </h1>
          <p className="max-w-2xl text-sm text-slate-400">
            POST <code className="text-slate-300">/api/v1/decisions/evaluate</code> with{" "}
            <code className="text-slate-300">featureId</code> (UUID) and context matching each
            feature&apos;s JSON Schema (<code className="text-slate-300">tenant_id</code>,{" "}
            <code className="text-slate-300">sector</code>, <code className="text-slate-300">role_id</code>).
          </p>
        </header>

        <section className="grid gap-8 lg:grid-cols-2">
          <Card>
            <CardHeader>
              <CardTitle>Request</CardTitle>
            </CardHeader>
            <CardContent className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="featureId">featureId (UUID)</Label>
                <select
                  id="featureId"
                  value={featureId}
                  onChange={(e) => setFeatureId(e.target.value)}
                  className="flex h-10 w-full rounded-lg border border-slate-700 bg-slate-950 px-3 text-sm text-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/70"
                >
                  {featureOptions.length === 0 && (
                    <option value={SEED_USER_MANAGEMENT}>User Management (seed UUID)</option>
                  )}
                  {featureOptions.map((f) => (
                    <option key={f.id} value={f.publicId}>
                      {f.displayName ?? f.featureKey}
                    </option>
                  ))}
                </select>
                <Input
                  value={featureId}
                  onChange={(e) => setFeatureId(e.target.value)}
                  placeholder="Or paste feature public_id"
                  className="font-mono text-xs"
                  spellCheck={false}
                />
              </div>
              <div className="grid gap-3 sm:grid-cols-2">
                <div className="space-y-2">
                  <Label htmlFor="tenant_id">tenant_id</Label>
                  <Input
                    id="tenant_id"
                    value={tenantId}
                    onChange={(e) => setTenantId(e.target.value)}
                    className="font-mono text-xs"
                    spellCheck={false}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="sector">sector (code)</Label>
                  <Input
                    id="sector"
                    value={sector}
                    onChange={(e) => setSector(e.target.value)}
                    className="font-mono text-xs"
                    spellCheck={false}
                  />
                </div>
                <div className="space-y-2 sm:col-span-2">
                  <Label htmlFor="role_id">role_id (UUID)</Label>
                  <Input
                    id="role_id"
                    value={roleId}
                    onChange={(e) => setRoleId(e.target.value)}
                    className="font-mono text-xs"
                    spellCheck={false}
                  />
                </div>
              </div>
              <div className="space-y-2">
                <Label htmlFor="extra">Extra context (JSON object, merged)</Label>
                <textarea
                  id="extra"
                  value={extraContextRaw}
                  onChange={(e) => setExtraContextRaw(e.target.value)}
                  rows={5}
                  spellCheck={false}
                  className="w-full resize-y rounded-lg border border-slate-700 bg-slate-950 px-3 py-2 font-mono text-xs text-slate-100 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/70"
                />
              </div>
              {mutation.isError && (
                <p className="rounded-lg border border-rose-900/60 bg-rose-950/40 px-3 py-2 text-sm text-rose-200">
                  {mutation.error instanceof Error
                    ? mutation.error.message
                    : "Request failed"}
                </p>
              )}
              <Button
                type="button"
                onClick={() => mutation.mutate()}
                disabled={mutation.isPending}
              >
                {mutation.isPending ? "Evaluating…" : "Evaluate"}
              </Button>
            </CardContent>
          </Card>

          <div className="space-y-4">
            {!data && !mutation.isPending && (
              <p className="rounded-xl border border-dashed border-slate-700 p-6 text-center text-sm text-slate-500">
                Run an evaluation to see summary, path, waterfall, and trace.
              </p>
            )}
            {mutation.isPending && (
              <p className="text-sm text-slate-500">Talking to the engine…</p>
            )}
            {data && <ResultPanel result={data} />}
          </div>
        </section>
      </div>
    </AppShell>
  );
}

function ResultPanel({ result }: { result: EvaluateResponseBody }) {
  const isPermit = result.decision === "ALLOW";
  const steps = result.trace ?? [];

  return (
    <div className="space-y-5">
      <div className="flex flex-wrap items-center gap-3">
        <Badge variant={isPermit ? "permit" : "deny"}>
          {isPermit ? "Permit" : "Deny"}
        </Badge>
        <span className="font-mono text-xs text-slate-500">{result.decision}</span>
        {(result.matchedRuleId != null || result.matchedRuleVersionId != null) && (
          <span className="text-xs text-slate-500">
            matched rule{" "}
            <span className="font-mono text-slate-400">
              {result.matchedRuleId ?? "—"}
            </span>{" "}
            / version{" "}
            <span className="font-mono text-slate-400">
              {result.matchedRuleVersionId ?? "—"}
            </span>
          </span>
        )}
      </div>

      <DecisionBreadcrumbs trace={steps} />

      <Card>
        <CardHeader>
          <CardTitle className="text-sm">Summary</CardTitle>
        </CardHeader>
        <CardContent>
          <p className="text-sm leading-relaxed text-slate-200">{result.summary}</p>
        </CardContent>
      </Card>

      {result.reasons?.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">Reasons</CardTitle>
          </CardHeader>
          <CardContent>
            <ul className="list-inside list-disc space-y-1 text-sm text-slate-300">
              {result.reasons.map((r) => (
                <li key={r}>{r}</li>
              ))}
            </ul>
          </CardContent>
        </Card>
      )}

      {result.evaluationPath?.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">Evaluation path</CardTitle>
          </CardHeader>
          <CardContent>
            <ol className="space-y-2 text-sm text-slate-300">
              {result.evaluationPath.map((line, i) => (
                <li key={i} className="flex gap-2">
                  <span className="font-mono text-slate-600">{i + 1}.</span>
                  <span className="min-w-0 break-words font-mono text-xs leading-relaxed text-slate-400">
                    {line}
                  </span>
                </li>
              ))}
            </ol>
          </CardContent>
        </Card>
      )}

      {steps.length > 0 && (
        <Card>
          <CardHeader>
            <CardTitle className="text-sm">Trace timeline</CardTitle>
          </CardHeader>
          <CardContent>
            <ol className="relative space-y-0 border-l border-slate-800 pl-6">
              {steps.map((row, idx) => (
                <li key={idx} className="pb-6 last:pb-0">
                  <span className="absolute -left-[5px] mt-1.5 h-2.5 w-2.5 rounded-full bg-slate-600 ring-4 ring-slate-950" />
                  <div className="rounded-lg border border-slate-800 bg-slate-900/60 p-3">
                    <div className="flex flex-wrap gap-2 text-xs text-slate-500">
                      <span className="font-mono text-slate-400">#{idx + 1}</span>
                      {row.hierarchyLevel && <span>{row.hierarchyLevel}</span>}
                      {row.ruleType && <span>· {row.ruleType}</span>}
                    </div>
                    <p className="mt-1 font-mono text-xs text-slate-400">
                      rule {row.ruleId ?? "—"} · v {row.ruleVersionId ?? "—"} →{" "}
                      <span
                        className={
                          row.decision === "ALLOW" ? "text-emerald-400" : "text-rose-300"
                        }
                      >
                        {row.decision}
                      </span>
                    </p>
                    {row.reason && (
                      <p className="mt-2 text-sm text-slate-200">{row.reason}</p>
                    )}
                  </div>
                </li>
              ))}
            </ol>
          </CardContent>
        </Card>
      )}
    </div>
  );
}
