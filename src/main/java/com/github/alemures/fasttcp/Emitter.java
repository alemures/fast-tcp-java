package com.github.alemures.fasttcp;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

class Emitter {
    private ConcurrentMap<String, ConcurrentLinkedQueue<Listener>> callbacks = new ConcurrentHashMap<>();

    Emitter on(String event, Listener fn) {
        ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
        if (callbacks == null) {
            callbacks = new ConcurrentLinkedQueue<>();
            ConcurrentLinkedQueue<Listener> tempCallbacks = this.callbacks.putIfAbsent(event, callbacks);
            if (tempCallbacks != null) {
                callbacks = tempCallbacks;
            }
        }
        callbacks.add(fn);
        return this;
    }

    Emitter once(final String event, final Listener fn) {
        this.on(event, new OnceListener(event, fn));
        return this;
    }

    Emitter off() {
        this.callbacks.clear();
        return this;
    }

    Emitter off(String event) {
        this.callbacks.remove(event);
        return this;
    }

    Emitter off(String event, Listener fn) {
        ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
        if (callbacks != null) {
            Iterator<Listener> it = callbacks.iterator();
            while (it.hasNext()) {
                Listener internal = it.next();
                if (Emitter.sameAs(fn, internal)) {
                    it.remove();
                    break;
                }
            }
        }
        return this;
    }

    Emitter emit(String event, Object... args) {
        ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
        if (callbacks != null) {
            for (Listener fn : callbacks) {
                fn.call(args);
            }
        }
        return this;
    }

    List<Listener> listeners(String event) {
        ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
        return callbacks != null ?
                new ArrayList<Listener>(callbacks) : new ArrayList<Listener>(0);
    }

    boolean hasListeners(String event) {
        ConcurrentLinkedQueue<Listener> callbacks = this.callbacks.get(event);
        return callbacks != null && !callbacks.isEmpty();
    }

    private static boolean sameAs(Listener fn, Listener internal) {
        if (fn.equals(internal)) {
            return true;
        } else if (internal instanceof OnceListener) {
            return fn.equals(((OnceListener) internal).fn);
        } else {
            return false;
        }
    }

    interface Listener {
        void call(Object... args);
    }

    private class OnceListener implements Listener {
        private final String event;
        private final Listener fn;

        private OnceListener(String event, Listener fn) {
            this.event = event;
            this.fn = fn;
        }

        @Override
        public void call(Object... args) {
            Emitter.this.off(this.event, this);
            this.fn.call(args);
        }
    }
}