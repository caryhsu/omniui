package dev.omniui.agent.runtime;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ActionResultTest {

    @Test
    void successBuildsResolvedAndTracePayload() {
        // GIVEN
        Map<String, Object> matchedAttributes = Map.of("fxId", "loginButton");

        // WHEN
        ActionResult result = ActionResult.success(
            "javafx",
            "node-1",
            matchedAttributes,
            "clicked"
        );

        // THEN
        assertTrue(result.ok());
        assertEquals("javafx", result.resolved().get("tier"));
        assertEquals("node-1", result.resolved().get("targetRef"));
        assertEquals(matchedAttributes, result.resolved().get("matchedAttributes"));
        assertNull(result.resolved().get("confidence"));
        assertEquals(List.of("javafx"), result.trace().get("attemptedTiers"));
        assertEquals("javafx", result.trace().get("resolvedTier"));
        assertEquals("clicked", result.value());
    }

    @Test
    void failureBuildsTraceWithDetailsOnly() {
        // GIVEN
        Map<String, Object> details = Map.of("reason", "selector_not_found");

        // WHEN
        ActionResult result = ActionResult.failure(List.of("javafx", "refresh"), details);

        // THEN
        assertFalse(result.ok());
        assertNull(result.resolved());
        assertEquals(List.of("javafx", "refresh"), result.trace().get("attemptedTiers"));
        assertNull(result.trace().get("resolvedTier"));
        assertEquals(details, result.trace().get("details"));
        assertNull(result.value());
    }
}
