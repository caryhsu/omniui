package dev.omniui.agent.runtime;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class TargetRegistry {
    private static final Map<String, AutomationTarget> TARGETS = new ConcurrentHashMap<>();

    private TargetRegistry() {
    }

    public static void register(AutomationTarget target) {
        TARGETS.put(target.appName(), target);
    }

    public static void unregister(String appName) {
        TARGETS.remove(appName);
    }

    public static AutomationTarget resolve(String appName) {
        AutomationTarget existing = TARGETS.get(appName);
        if (existing != null) {
            return existing;
        }

        Optional<AutomationTarget> discovered = JavaFxRuntimeDiscovery.discover(appName);
        discovered.ifPresent(TargetRegistry::register);
        return discovered.orElse(null);
    }
}
