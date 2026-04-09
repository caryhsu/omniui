package dev.omniui.agent.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionStoreTest {

    @Test
    void createSessionStoresSessionAndPrefixesIdentifier() {
        // GIVEN
        SessionStore store = new SessionStore();
        AutomationTarget target = new StubAutomationTarget("StoreApp");

        // WHEN
        AgentSession session = store.createSession(target);

        // THEN
        assertNotNull(session);
        assertTrue(session.sessionId().startsWith("session-"));
        assertSame(target, session.target());
        assertSame(session, store.get(session.sessionId()));
    }

    @Test
    void getReturnsNullForUnknownSessionId() {
        // GIVEN
        SessionStore store = new SessionStore();

        // THEN
        assertNull(store.get("missing-session"));
    }

    @Test
    void createSessionGeneratesDifferentIdsAcrossCalls() {
        // GIVEN
        SessionStore store = new SessionStore();
        AutomationTarget target = new StubAutomationTarget("StoreApp");

        // WHEN
        AgentSession first = store.createSession(target);
        AgentSession second = store.createSession(target);

        // THEN
        assertNotEquals(first.sessionId(), second.sessionId());
        assertSame(first, store.get(first.sessionId()));
        assertSame(second, store.get(second.sessionId()));
    }
}
