package dev.omniui.agent.runtime;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectiveJavaFxSupportTest {

    @Test
    void isJavaFxPresentReturnsFalseWhenJavaFxClassesAreUnavailable() {
        assertFalse(ReflectiveJavaFxSupport.isJavaFxPresent());
    }

    @Test
    void onFxThreadExecutesSupplierDirectlyWhenJavaFxIsUnavailable() {
        // WHEN
        String value = ReflectiveJavaFxSupport.onFxThread(() -> "ready");

        // THEN
        assertEquals("ready", value);
    }

    @Test
    void onFxThreadPropagatesSupplierExceptionWithoutJavaFx() {
        RuntimeException error = assertThrows(
            RuntimeException.class,
            () -> ReflectiveJavaFxSupport.onFxThread(() -> {
                throw new RuntimeException("boom");
            })
        );

        assertEquals("boom", error.getMessage());
    }

    @Test
    void invokeResolvesCompatiblePrimitiveWrapperArguments() {
        // GIVEN
        SampleTarget target = new SampleTarget();

        // WHEN
        Object value = ReflectiveJavaFxSupport.invoke(target, "join", "count", 3);

        // THEN
        assertEquals("count:3", value);
    }

    @Test
    void invokeExactCallsMethodWithExplicitParameterTypes() {
        // GIVEN
        SampleTarget target = new SampleTarget();

        // WHEN
        Object value = ReflectiveJavaFxSupport.invokeExact(
            target,
            "exactJoin",
            new Class<?>[]{String.class, Integer.class},
            "size",
            7
        );

        // THEN
        assertEquals("size=7", value);
    }

    @Test
    void invokeOnTypeCallsMethodDeclaredOnInterfaceOwner() {
        // GIVEN
        SampleTarget target = new SampleTarget();

        // WHEN
        Object value = ReflectiveJavaFxSupport.invokeOnType(
            target,
            NamedContract.class.getName(),
            "displayName",
            new Class<?>[0]
        );

        // THEN
        assertEquals("sample-target", value);
    }

    @Test
    void invokeStaticCallsStaticMethodOnClassObject() {
        // WHEN
        Object value = ReflectiveJavaFxSupport.invokeStatic(StaticHelpers.class, "echo", "hello");

        // THEN
        assertEquals("hello", value);
    }

    @Test
    void invokeStaticLoadsClassByNameWhenGivenString() {
        // WHEN
        Object value = ReflectiveJavaFxSupport.invokeStatic(
            StaticHelpers.class.getName(),
            "add",
            2,
            5
        );

        // THEN
        assertEquals(7, value);
    }

    @Test
    void textOfReturnsNullWhenGetTextMethodIsMissing() {
        assertNull(ReflectiveJavaFxSupport.textOf(new Object()));
    }

    @Test
    void textOfReturnsStringValueWhenMethodExists() {
        assertEquals("visible text", ReflectiveJavaFxSupport.textOf(new TextNode()));
    }

    @Test
    void loadClassReturnsRequestedType() {
        assertEquals(String.class, ReflectiveJavaFxSupport.loadClass("java.lang.String"));
    }

    @Test
    void loadClassThrowsHelpfulErrorWhenMissing() {
        IllegalStateException error = assertThrows(
            IllegalStateException.class,
            () -> ReflectiveJavaFxSupport.loadClass("missing.TypeName")
        );

        assertTrue(error.getMessage().contains("Missing class: missing.TypeName"));
    }

    @Test
    void invokeThrowsHelpfulErrorWhenMethodIsMissing() {
        IllegalStateException error = assertThrows(
            IllegalStateException.class,
            () -> ReflectiveJavaFxSupport.invoke(new Object(), "missingMethod")
        );

        assertTrue(error.getMessage().contains("Method not found"));
    }

    interface NamedContract {
        String displayName();
    }

    static final class SampleTarget implements NamedContract {
        public String join(String label, int count) {
            return label + ":" + count;
        }

        public String exactJoin(String label, Integer count) {
            return label + "=" + count;
        }

        @Override
        public String displayName() {
            return "sample-target";
        }
    }

    static final class StaticHelpers {
        public static String echo(String value) {
            return value;
        }

        public static int add(int left, int right) {
            return left + right;
        }
    }

    static final class TextNode {
        public String getText() {
            return "visible text";
        }
    }
}
