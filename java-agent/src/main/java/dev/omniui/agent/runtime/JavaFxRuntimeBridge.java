package dev.omniui.agent.runtime;

import java.util.Objects;
import java.util.function.Supplier;

public final class JavaFxRuntimeBridge {
    private JavaFxRuntimeBridge() {
    }

    public static void registerScene(String appName, Supplier<Object> sceneSupplier) {
        Objects.requireNonNull(appName, "appName");
        Objects.requireNonNull(sceneSupplier, "sceneSupplier");
        TargetRegistry.register(new ReflectiveJavaFxTarget(appName, sceneSupplier));
    }

    public static void registerStage(String appName, Object stage) {
        Objects.requireNonNull(stage, "stage");
        registerScene(appName, () -> ReflectiveJavaFxSupport.invoke(stage, "getScene"));
    }

    public static void unregister(String appName) {
        TargetRegistry.unregister(appName);
    }
}
