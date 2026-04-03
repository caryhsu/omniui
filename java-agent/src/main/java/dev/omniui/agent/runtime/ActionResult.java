package dev.omniui.agent.runtime;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public record ActionResult(
    boolean ok,
    Map<String, Object> resolved,
    Map<String, Object> trace,
    Object value
) {
    public static ActionResult success(String tier, String targetRef, Map<String, Object> matchedAttributes, Object value) {
        Map<String, Object> resolved = new LinkedHashMap<>();
        resolved.put("tier", tier);
        resolved.put("targetRef", targetRef);
        resolved.put("matchedAttributes", matchedAttributes);
        resolved.put("confidence", null);

        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("attemptedTiers", List.of("javafx"));
        trace.put("resolvedTier", tier);

        return new ActionResult(
            true,
            resolved,
            trace,
            value
        );
    }

    public static ActionResult failure(List<String> attemptedTiers, Map<String, Object> details) {
        Map<String, Object> trace = new LinkedHashMap<>();
        trace.put("attemptedTiers", attemptedTiers);
        trace.put("resolvedTier", null);
        trace.put("details", details);
        return new ActionResult(
            false,
            null,
            trace,
            null
        );
    }
}
