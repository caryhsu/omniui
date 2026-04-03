package dev.omniui.agent.runtime;

import com.google.gson.JsonObject;

import java.util.List;
import java.util.Map;

public interface AutomationTarget {
    String appName();

    String platform();

    List<String> capabilities();

    List<Map<String, Object>> discover();

    ActionResult perform(String action, JsonObject selector, JsonObject payload);

    byte[] screenshot();
}
