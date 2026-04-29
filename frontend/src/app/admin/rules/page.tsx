"use client";

import { useQuery } from "@tanstack/react-query";
import { AppShell } from "@/components/layout/AppShell";
import { RuleCard } from "@/components/rules/RuleCard";
import { fetchRules } from "@/lib/admin-api";

export default function AdminRulesPage() {
  const q = useQuery({ queryKey: ["admin", "rules"], queryFn: fetchRules });

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-white">Rules</h1>
          <p className="mt-1 text-sm text-slate-400">
            CRUD API is available at <code className="text-slate-300">/api/v1/admin/rules</code>.
            This page lists active configuration for review.
          </p>
        </div>
        {q.isLoading && <p className="text-sm text-slate-500">Loading…</p>}
        {q.isError && (
          <p className="text-sm text-rose-300">
            {q.error instanceof Error ? q.error.message : "Failed to load"}
          </p>
        )}
        {q.data && (
          <div className="grid gap-4">
            {q.data.map((r) => (
              <RuleCard key={r.id} rule={r} />
            ))}
          </div>
        )}
      </div>
    </AppShell>
  );
}
