import { forwardRef, type ButtonHTMLAttributes } from "react";
import { cn } from "@/lib/utils";

export type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
  variant?: "primary" | "secondary" | "ghost" | "destructive";
  size?: "sm" | "md" | "lg";
};

export const Button = forwardRef<HTMLButtonElement, ButtonProps>(
  ({ className, variant = "primary", size = "md", ...props }, ref) => (
    <button
      ref={ref}
      className={cn(
        "inline-flex items-center justify-center rounded-lg font-medium transition focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/80 focus-visible:ring-offset-2 focus-visible:ring-offset-slate-950 disabled:pointer-events-none disabled:opacity-50",
        variant === "primary" &&
          "bg-blue-500 text-white hover:bg-blue-400 active:bg-blue-600",
        variant === "secondary" &&
          "border border-slate-600 bg-slate-800 text-slate-100 hover:bg-slate-700",
        variant === "ghost" &&
          "text-slate-300 hover:bg-slate-800/80 hover:text-white",
        variant === "destructive" &&
          "bg-rose-600 text-white hover:bg-rose-500",
        size === "sm" && "px-3 py-1.5 text-xs",
        size === "md" && "px-4 py-2.5 text-sm",
        size === "lg" && "px-6 py-3 text-base",
        className
      )}
      {...props}
    />
  )
);
Button.displayName = "Button";
