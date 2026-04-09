package dev.omniui.agent.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AgentSessionTest {

    @Test
    void recordExposesConstructorArguments() {
        // GIVEN
        AutomationTarget target = new StubAutomationTarget("SessionApp");

        // WHEN
        AgentSession session = new AgentSession("session-123", target);

        // THEN
        assertEquals("session-123", session.sessionId());
        assertSame(target, session.target());
    }
}
