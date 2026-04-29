"use client";

import { useQuery } from "@tanstack/react-query";
import { AppShell } from "@/components/layout/AppShell";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { fetchFeatures } from "@/lib/admin-api";

export default function AdminFeaturesPage() {
  const q = useQuery({ queryKey: ["admin", "features"], queryFn: fetchFeatures });

  return (
    <AppShell>
      <div className="space-y-6">
        <div>
          <h1 className="text-2xl font-semibold tracking-tight text-white">Features</h1>
          <p className="mt-1 text-sm text-slate-400">
            Loaded from <code className="text-slate-300">GET /api/v1/admin/features</code>.
          </p>
        </div>
        {q.isLoading && <p className="text-sm text-slate-500">Loading…</p>}
        {q.isError && (
          <p className="text-sm text-rose-300">
            {q.error instanceof Error ? q.error.message : "Failed to load"}
          </p>
        )}
        {q.data && (
          <div className="grid gap-4 sm:grid-cols-2">
            {q.data.map((f) => (
              <Card key={f.id}>
                <CardHeader>
                  <CardTitle className="text-base">{f.displayName ?? f.featureKey}</CardTitle>
                </CardHeader>
                <CardContent className="space-y-2 text-sm">
                  <p className="font-mono text-xs text-slate-400">{f.featureKey}</p>
                  <p className="font-mono text-xs text-slate-500 break-all">
                    publicId: {f.publicId}
                  </p>
                  <Badge variant="muted">id {f.id}</Badge>
                </CardContent>
              </Card>
            ))}
          </div>
        )}
      </div>
    </AppShell>
  );
}
