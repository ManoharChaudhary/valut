import type { HTMLAttributes } from "react";
import { cn } from "@/lib/utils";

export type BadgeProps = HTMLAttributes<HTMLSpanElement> & {
  variant?: "default" | "permit" | "deny" | "muted";
};

export function Badge({ className, variant = "default", ...props }: BadgeProps) {
  return (
    <span
      className={cn(
        "inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-semibold uppercase tracking-wide",
        variant === "default" && "bg-slate-800 text-slate-200 ring-1 ring-slate-700",
        variant === "permit" && "bg-emerald-950 text-emerald-300 ring-1 ring-emerald-800",
        variant === "deny" && "bg-rose-950 text-rose-200 ring-1 ring-rose-900",
        variant === "muted" && "bg-slate-900 text-slate-500",
        className
      )}
      {...props}
    />
  );
}
