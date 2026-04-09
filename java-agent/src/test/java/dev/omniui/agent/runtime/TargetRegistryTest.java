package dev.omniui.agent.runtime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class TargetRegistryTest {

    private static final String REGISTERED_APP = "RegistryTestApp";
    private static final String MISSING_APP = "RegistryMissingApp";

    @AfterEach
    void tearDown() {
        TargetRegistry.unregister(REGISTERED_APP);
        TargetRegistry.unregister(MISSING_APP);
    }

    @Test
    void resolveReturnsRegisteredTarget() {
        // GIVEN
        AutomationTarget target = new StubAutomationTarget(REGISTERED_APP);
        TargetRegistry.register(target);

        // WHEN
        AutomationTarget resolved = TargetRegistry.resolve(REGISTERED_APP);

        // THEN
        assertSame(target, resolved);
    }

    @Test
    void unregisterRemovesTarget() {
        // GIVEN
        TargetRegistry.register(new StubAutomationTarget(REGISTERED_APP));

        // WHEN
        TargetRegistry.unregister(REGISTERED_APP);

        // THEN
        assertNull(TargetRegistry.resolve(REGISTERED_APP));
    }

    @Test
    void resolveReturnsNullWhenNothingRegisteredOrDiscovered() {
        // THEN
        assertNull(TargetRegistry.resolve(MISSING_APP));
    }
}
