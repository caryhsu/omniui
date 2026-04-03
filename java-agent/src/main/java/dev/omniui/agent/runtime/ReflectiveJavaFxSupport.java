package dev.omniui.agent.runtime;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

final class ReflectiveJavaFxSupport {
    private ReflectiveJavaFxSupport() {
    }

    static boolean isJavaFxPresent() {
        try {
            Class.forName("javafx.application.Platform");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    static <T> T onFxThread(Supplier<T> supplier) {
        Objects.requireNonNull(supplier, "supplier");
        if (!isJavaFxPresent()) {
            return supplier.get();
        }

        Object platformClass = loadClass("javafx.application.Platform");
        boolean isFxThread = (boolean) invokeStatic(platformClass, "isFxApplicationThread");
        if (isFxThread) {
            return supplier.get();
        }

        CountDownLatch latch = new CountDownLatch(1);
        AtomicReference<T> valueRef = new AtomicReference<>();
        AtomicReference<RuntimeException> errorRef = new AtomicReference<>();
        Runnable runnable = () -> {
            try {
                valueRef.set(supplier.get());
            } catch (RuntimeException ex) {
                errorRef.set(ex);
            } finally {
                latch.countDown();
            }
        };
        invokeStatic(platformClass, "runLater", runnable);
        try {
            latch.await();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while waiting for JavaFX task", ex);
        }
        if (errorRef.get() != null) {
            throw errorRef.get();
        }
        return valueRef.get();
    }

    static Object invoke(Object target, String methodName, Object... args) {
        Objects.requireNonNull(target, "target");
        Method method = resolveMethod(target.getClass(), methodName, args);
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("Failed to invoke method " + methodName + " on " + target.getClass().getName(), ex);
        }
    }

    static Object invokeExact(Object target, String methodName, Class<?>[] parameterTypes, Object... args) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(parameterTypes, "parameterTypes");
        try {
            Method method = target.getClass().getMethod(methodName, parameterTypes);
            return method.invoke(target, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("Failed to invoke exact method " + methodName + " on " + target.getClass().getName(), ex);
        }
    }

    static Object invokeOnType(Object target, String ownerClassName, String methodName, Class<?>[] parameterTypes, Object... args) {
        Objects.requireNonNull(target, "target");
        Objects.requireNonNull(ownerClassName, "ownerClassName");
        Objects.requireNonNull(parameterTypes, "parameterTypes");
        try {
            Class<?> owner = loadClass(ownerClassName);
            Method method = owner.getMethod(methodName, parameterTypes);
            return method.invoke(target, args);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException(
                "Failed to invoke method " + ownerClassName + "." + methodName + " on " + target.getClass().getName(),
                ex
            );
        }
    }

    static Object invokeStatic(Object klass, String methodName, Object... args) {
        Class<?> type = klass instanceof Class<?> actual ? actual : loadClass(klass.toString());
        Method method = resolveMethod(type, methodName, args);
        try {
            return method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new IllegalStateException("Failed to invoke static method " + methodName + " on " + type.getName(), ex);
        }
    }

    static String textOf(Object node) {
        try {
            Object value = invoke(node, "getText");
            return value == null ? null : value.toString();
        } catch (IllegalStateException ex) {
            return null;
        }
    }

    static Class<?> loadClass(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException ex) {
            throw new IllegalStateException("Missing class: " + className, ex);
        }
    }

    private static Method resolveMethod(Class<?> type, String methodName, Object... args) {
        for (Method method : type.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            Class<?>[] parameterTypes = method.getParameterTypes();
            if (parameterTypes.length != args.length) {
                continue;
            }
            if (isCompatible(parameterTypes, args)) {
                return method;
            }
        }
        throw new IllegalStateException("Method not found: " + type.getName() + "." + methodName);
    }

    private static boolean isCompatible(Class<?>[] parameterTypes, Object[] args) {
        for (int i = 0; i < parameterTypes.length; i++) {
            Object arg = args[i];
            if (arg == null) {
                continue;
            }
            if (parameterTypes[i].isPrimitive()) {
                if (!wrap(parameterTypes[i]).isAssignableFrom(arg.getClass())) {
                    return false;
                }
                continue;
            }
            if (!parameterTypes[i].isAssignableFrom(arg.getClass())) {
                return false;
            }
        }
        return true;
    }

    private static Class<?> wrap(Class<?> primitive) {
        if (primitive == boolean.class) {
            return Boolean.class;
        }
        if (primitive == int.class) {
            return Integer.class;
        }
        if (primitive == double.class) {
            return Double.class;
        }
        if (primitive == long.class) {
            return Long.class;
        }
        if (primitive == float.class) {
            return Float.class;
        }
        if (primitive == short.class) {
            return Short.class;
        }
        if (primitive == byte.class) {
            return Byte.class;
        }
        if (primitive == char.class) {
            return Character.class;
        }
        return primitive;
    }
}
