import Link from "next/link";
import { AppShell } from "@/components/layout/AppShell";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { cn } from "@/lib/utils";

const linkBtn =
  "inline-flex w-full items-center justify-center rounded-lg px-4 py-2.5 text-sm font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/80 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950";

export default function Home() {
  return (
    <AppShell>
      <div className="mx-auto max-w-lg space-y-8 py-8">
        <div>
          <p className="text-xs font-medium uppercase tracking-widest text-slate-500">
            Vault
          </p>
          <h1 className="mt-2 text-3xl font-semibold tracking-tight text-white">
            Control plane
          </h1>
          <p className="mt-3 text-sm leading-relaxed text-slate-400">
            Enterprise rule management and decision simulator. Browser calls use the
            same-origin <code className="text-slate-300">/vault-api</code> rewrite to Spring Boot.
          </p>
        </div>
        <Card>
          <CardHeader>
            <CardTitle className="text-base">Get started</CardTitle>
          </CardHeader>
          <CardContent className="flex flex-col gap-3">
            <Link
              href="/simulator"
              className={cn(linkBtn, "bg-blue-500 text-white hover:bg-blue-400")}
            >
              Open decision simulator
            </Link>
            <Link
              href="/admin/features"
              className={cn(
                linkBtn,
                "border border-slate-600 bg-slate-800 text-slate-100 hover:bg-slate-700"
              )}
            >
              View features
            </Link>
            <Link
              href="/admin/rules"
              className={cn(
                linkBtn,
                "border border-slate-600 bg-slate-800 text-slate-100 hover:bg-slate-700"
              )}
            >
              View rules
            </Link>
          </CardContent>
        </Card>
        <p className="text-xs text-slate-600">
          Copy{" "}
          <code className="rounded bg-slate-900 px-1.5 py-0.5 text-slate-400">
            frontend/.env.example
          </code>{" "}
          to{" "}
          <code className="rounded bg-slate-900 px-1.5 py-0.5 text-slate-400">
            .env.local
          </code>{" "}
          if the API is not on{" "}
          <code className="rounded bg-slate-900 px-1.5 py-0.5 text-slate-400">
            localhost:8080
          </code>
          .
        </p>
      </div>
    </AppShell>
  );
}
