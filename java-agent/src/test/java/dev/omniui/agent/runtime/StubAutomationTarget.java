package dev.omniui.agent.runtime;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

final class StubAutomationTarget implements AutomationTarget {
    private final String appName;
    private final List<String> capabilities;

    StubAutomationTarget(String appName) {
        this(appName, List.of());
    }

    StubAutomationTarget(String appName, List<String> capabilities) {
        this.appName = appName;
        this.capabilities = capabilities;
    }

    @Override
    public String appName() {
        return appName;
    }

    @Override
    public String platform() {
        return "javafx";
    }

    @Override
    public List<String> capabilities() {
        return capabilities;
    }

    @Override
    public List<Map<String, Object>> discover() {
        return List.of();
    }

    public ActionResult perform(String action, com.google.gson.JsonObject selector, com.google.gson.JsonObject payload) {
        return ActionResult.success("javafx", "stub-node", Map.of(), null);
    }

    @Override
    public byte[] screenshot() {
        return new byte[0];
    }
}
