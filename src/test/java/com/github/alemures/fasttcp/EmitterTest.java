package com.github.alemures.fasttcp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class EmitterTest {
    public Emitter emitter;

    @Before
    public void resetEmitterInstance() {
        emitter = new Emitter();
    }

    @Test
    public void shouldAddAListener() {
        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });

        Assert.assertTrue("Should have one listener", emitter.hasListeners("testEvent"));
    }

    @Test
    public void shouldAddAOnceListener() {
        emitter.once("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });

        emitter.emit("testEvent");
        Assert.assertFalse("Should not have any listener", emitter.hasListeners("testEvent"));
    }

    @Test
    public void shouldEmitAEvent() {
        final AtomicBoolean executed = new AtomicBoolean(false);
        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                executed.set(true);
            }
        });

        emitter.emit("testEvent", "testData");
        Assert.assertTrue("The listener should be called", executed.get());
    }

    @Test
    public void shouldRemoveAllListeners() {
        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });

        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });

        emitter.removeAllListeners("testEvent");
        Assert.assertFalse("Should not have any listener", emitter.hasListeners("testEvent"));
    }

    @Test
    public void shouldRemoveAListener() {
        Emitter.Listener listener = new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        };

        emitter.on("testEvent", listener);

        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });

        emitter.removeListener("testEvent", listener);
        int count = emitter.listenerCount("testEvent");
        Assert.assertEquals("Should have one listener", 1, count);
    }

    @Test
    public void shouldReturnListenerCount() {
        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });

        int count = emitter.listenerCount("testEvent");
        Assert.assertEquals("Should have one listener", 1, count);
    }

    @Test
    public void shouldReturnIfHasListeners() {
        Assert.assertFalse("Should not have listeners", emitter.hasListeners("testEvent"));
        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });
        Assert.assertTrue("Should have listeners", emitter.hasListeners("testEvent"));
    }
}
