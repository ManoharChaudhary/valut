import Link from "next/link";
import type { ReactNode } from "react";
import { cn } from "@/lib/utils";

const nav = [
  { href: "/", label: "Home" },
  { href: "/simulator", label: "Simulator" },
  { href: "/admin/features", label: "Features" },
  { href: "/admin/rules", label: "Rules" },
];

export function AppShell({
  children,
  className,
}: {
  children: ReactNode;
  className?: string;
}) {
  return (
    <div className={cn("min-h-screen bg-slate-950 text-slate-100", className)}>
      <header className="sticky top-0 z-40 border-b border-slate-800 bg-slate-950/95 backdrop-blur">
        <div className="mx-auto flex max-w-6xl items-center justify-between gap-4 px-4 py-3 sm:px-6">
          <Link href="/" className="text-sm font-semibold tracking-tight text-white">
            Vault
          </Link>
          <nav className="flex flex-wrap items-center gap-1 sm:gap-3">
            {nav.map((item) => (
              <Link
                key={item.href}
                href={item.href}
                className="rounded-md px-2 py-1.5 text-sm text-slate-400 transition hover:bg-slate-800 hover:text-slate-100 sm:px-3"
              >
                {item.label}
              </Link>
            ))}
          </nav>
        </div>
      </header>
      <main className="mx-auto max-w-6xl px-4 py-8 sm:px-6">{children}</main>
    </div>
  );
}
