package com.github.alemures.fasttcp;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
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

        Assert.assertEquals("The number of listeners should be 1", 1, emitter.listeners("testEvent").size());
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

        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });

        emitter.removeAllListeners("testEvent");
        Assert.assertEquals("The number of listeners should be 0", 0, emitter.listeners("testEvent").size());
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
        Assert.assertEquals("The number of listeners should be 1", 1, emitter.listeners("testEvent").size());
    }

    @Test
    public void shouldReturnAListenerList() {
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

        List<Emitter.Listener> list = emitter.listeners("testEvent");
        Assert.assertEquals("The number of listeners should be 2", 2, list.size());
    }

    @Test
    public void shouldReturnIfHasListeners() {
        emitter.on("testEvent", new Emitter.Listener() {
            @Override
            public void call(Object... args) {
            }
        });

        boolean hasListeners = emitter.hasListeners("testEvent");
        Assert.assertTrue("The result should be true", hasListeners);
    }
}
