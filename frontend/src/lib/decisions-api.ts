/** Browser calls same-origin path; Next rewrites to Spring (see next.config.ts). */
export const EVALUATE_PATH = "/vault-api/api/v1/decisions/evaluate";

export type EvaluateRequestBody = {
  /** Prefer UUID from `feature_definitions.public_id` (string form). */
  featureId?: string;
  /** Fallback when `featureId` is not set. */
  featureKey?: string;
  context: Record<string, unknown>;
};

export type DecisionTraceEntry = {
  hierarchyLevel: string | null;
  ruleType: string | null;
  ruleId: number | null;
  ruleVersionId: number | null;
  decision: string | null;
  reason: string | null;
};

export type EvaluateResponseBody = {
  decision: string;
  summary: string;
  reasons: string[];
  evaluationPath: string[];
  matchedRuleId: number | null;
  matchedRuleVersionId: number | null;
  trace: DecisionTraceEntry[];
};

function formatEvaluateError(status: number, statusText: string, body: string): string {
  const raw = body.trim();
  if (!raw) {
    return `${status} ${statusText}`;
  }
  try {
    const j = JSON.parse(raw) as Record<string, unknown>;
    if (typeof j.detail === "string") {
      return j.detail;
    }
    if (typeof j.message === "string") {
      return j.message;
    }
    if (typeof j.title === "string") {
      const d = typeof j.detail === "string" ? j.detail : "";
      return d ? `${j.title}: ${d}` : j.title;
    }
    if (typeof j.error === "string") {
      return j.error;
    }
    const errs = j.errors;
    if (Array.isArray(errs) && errs.length > 0) {
      const first = errs[0] as Record<string, unknown>;
      const msg =
        typeof first.defaultMessage === "string"
          ? first.defaultMessage
          : typeof first.message === "string"
            ? first.message
            : null;
      if (msg) {
        return msg;
      }
    }
  } catch {
    /* not JSON */
  }
  return raw.length > 800 ? `${raw.slice(0, 800)}…` : raw;
}

export async function postEvaluate(
  body: EvaluateRequestBody
): Promise<EvaluateResponseBody> {
  const payload: Record<string, unknown> = {
    context: body.context ?? {},
  };
  if (body.featureId) {
    payload.featureId = body.featureId;
  }
  if (body.featureKey) {
    payload.featureKey = body.featureKey;
  }
  const res = await fetch(EVALUATE_PATH, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(payload),
  });
  const text = await res.text();
  if (!res.ok) {
    throw new Error(formatEvaluateError(res.status, res.statusText, text));
  }
  return JSON.parse(text) as EvaluateResponseBody;
}
