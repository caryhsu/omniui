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

    default ActionResult startRecording() {
        throw new UnsupportedOperationException("startRecording not supported by this target");
    }

    default List<Map<String, Object>> stopRecordingFlush() {
        throw new UnsupportedOperationException("stopRecordingFlush not supported by this target");
    }

    default List<Map<String, Object>> pollEvents() {
        throw new UnsupportedOperationException("pollEvents not supported by this target");
    }

    default Map<String, Object> assertContext(double sceneX, double sceneY) {
        throw new UnsupportedOperationException("assertContext not supported by this target");
    }
}
