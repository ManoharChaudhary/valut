import type { DecisionTraceEntry } from "@/lib/decisions-api";
import { Badge } from "@/components/ui/badge";

const HIERARCHY_ORDER = ["GLOBAL", "SECTOR", "TENANT", "PLAN", "ROLE"] as const;

function rank(level: string | null): number {
  if (!level) return 99;
  const i = HIERARCHY_ORDER.indexOf(level as (typeof HIERARCHY_ORDER)[number]);
  return i === -1 ? 50 : i;
}

export function DecisionBreadcrumbs({ trace }: { trace: DecisionTraceEntry[] }) {
  if (!trace.length) {
    return null;
  }
  const ordered = [...trace].sort(
    (a, b) => rank(a.hierarchyLevel) - rank(b.hierarchyLevel)
  );
  const finalDeny = trace.some((t) => t.decision === "DENY");
  const finalAllow = trace.some((t) => t.decision === "ALLOW") && !finalDeny;

  return (
    <div className="rounded-xl border border-slate-800 bg-slate-900/40 p-4">
      <h3 className="text-xs font-semibold uppercase tracking-wide text-slate-500">
        Waterfall (policy order)
      </h3>
      <ol className="mt-3 flex flex-wrap items-center gap-2 text-sm">
        {ordered.map((row, idx) => (
          <li key={idx} className="flex items-center gap-2">
            {idx > 0 ? (
              <span className="text-slate-600" aria-hidden>
                →
              </span>
            ) : null}
            <span className="font-mono text-xs text-slate-400">
              {row.hierarchyLevel ?? "—"}
            </span>
            <Badge variant={row.decision === "ALLOW" ? "permit" : "deny"}>
              {row.decision ?? "?"}
            </Badge>
          </li>
        ))}
      </ol>
      <p className="mt-3 text-xs text-slate-500">
        Resolver: deny wins across evaluated rules. Scoped rules run first; if none match,
        the global default rule applies when linked to this feature.
      </p>
      {(finalAllow || finalDeny) && (
        <p className="mt-2 text-xs text-slate-400">
          Net outcome from trace:{" "}
          <span className={finalAllow ? "text-emerald-400" : "text-rose-300"}>
            {finalAllow ? "ALLOW" : "DENY"}
          </span>
        </p>
      )}
    </div>
  );
}
