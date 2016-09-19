package com.github.alemures.fasttcp;

import java.util.*;

public class Emitter {
    private Map<String, LinkedList<Listener>> callbacks = new HashMap<>();

    Emitter() {
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

    Emitter on(String event, Listener fn) {
        LinkedList<Listener> callbacksList = callbacks.get(event);
        if (callbacksList == null) {
            callbacksList = new LinkedList<>();
            callbacks.put(event, callbacksList);
        }
        callbacksList.add(fn);
        return this;
    }

    Emitter once(final String event, final Listener fn) {
        on(event, new OnceListener(event, fn));
        return this;
    }

    Emitter removeAllListeners(String event) {
        callbacks.remove(event);
        return this;
    }

    Emitter removeListener(String event, Listener fn) {
        LinkedList<Listener> callbacks = this.callbacks.get(event);
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
        LinkedList<Listener> callbacks = this.callbacks.get(event);
        if (callbacks != null) {
            for (Listener fn : callbacks) {
                fn.call(args);
            }
        }
        return this;
    }

    List<Listener> listeners(String event) {
        LinkedList<Listener> callbacks = this.callbacks.get(event);
        return callbacks != null ?
                new ArrayList<Listener>(callbacks) : new ArrayList<Listener>(0);
    }

    boolean hasListeners(String event) {
        LinkedList<Listener> callbacks = this.callbacks.get(event);
        return callbacks != null && !callbacks.isEmpty();
    }

    public interface Listener {
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
            removeListener(event, this);
            fn.call(args);
        }
    }
}