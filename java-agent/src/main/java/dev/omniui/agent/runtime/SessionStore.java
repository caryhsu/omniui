package dev.omniui.agent.runtime;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SessionStore {
    private final Map<String, AgentSession> sessions = new ConcurrentHashMap<>();

    public AgentSession createSession(AutomationTarget target) {
        String sessionId = "session-" + UUID.randomUUID();
        AgentSession session = new AgentSession(sessionId, target);
        sessions.put(sessionId, session);
        return session;
    }

    public AgentSession get(String sessionId) {
        return sessions.get(sessionId);
    }
}
