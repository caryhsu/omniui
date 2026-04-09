package dev.omniui.agent.runtime;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class JavaFxRuntimeDiscovery {
    @FunctionalInterface
    interface Discoverer {
        Optional<AutomationTarget> discover(String appName);
    }

    private static Discoverer discoverer = JavaFxRuntimeDiscovery::discoverViaJavaFxRuntime;

    private JavaFxRuntimeDiscovery() {
    }

    public static Optional<AutomationTarget> discover(String appName) {
        return discoverer.discover(appName);
    }

    static void setDiscovererForTest(Discoverer customDiscoverer) {
        discoverer = customDiscoverer;
    }

    static void resetDiscovererForTest() {
        discoverer = JavaFxRuntimeDiscovery::discoverViaJavaFxRuntime;
    }

    private static Optional<AutomationTarget> discoverViaJavaFxRuntime(String appName) {
        return discoverSceneSupplier()
            .map(sceneSupplier -> new ReflectiveJavaFxTarget(appName, sceneSupplier));
    }

    private static Optional<java.util.function.Supplier<Object>> discoverSceneSupplier() {
        try {
            Class<?> windowClass = Class.forName("javafx.stage.Window");
            Object windows = windowClass.getMethod("getWindows").invoke(null);
            if (!(windows instanceof Iterable<?> iterable)) {
                return Optional.empty();
            }

            List<Object> showingWindows = new ArrayList<>();
            for (Object window : iterable) {
                if (window == null) {
                    continue;
                }
                Object showing = window.getClass().getMethod("isShowing").invoke(window);
                if (Boolean.TRUE.equals(showing)) {
                    showingWindows.add(window);
                }
            }
            if (showingWindows.isEmpty()) {
                return Optional.empty();
            }

            Object selectedWindow = showingWindows.getFirst();
            return Optional.of(() -> ReflectiveJavaFxSupport.invoke(selectedWindow, "getScene"));
        } catch (ClassNotFoundException ex) {
            return Optional.empty();
        } catch (ReflectiveOperationException ex) {
            return Optional.empty();
        }
    }
}
