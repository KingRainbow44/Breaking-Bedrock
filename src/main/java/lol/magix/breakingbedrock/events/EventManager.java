package lol.magix.breakingbedrock.events;

import java.beans.EventHandler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class EventManager {
    private final Map<Class<? extends Event>, List<EventHandler<?>>> listeners
            = new HashMap<>();

    /**
     * Registers a new event listener.
     *
     * @param event The event to listen for.
     * @param handler The handler to run when the event is fired.
     */
    public <T extends Event> void registerListener(Class<T> event, EventHandler<T> handler) {
        this.listeners.computeIfAbsent(event, k -> new ArrayList<>()).add(handler);
    }

    /**
     * Removes an event listener.
     *
     * @param event The event to remove the listener from.
     * @param handler The handler to remove.
     */
    public <T extends Event> void removeListener(Class<T> event, EventHandler<T> handler) {
        var listeners = this.listeners.get(event);
        if (listeners == null) return;

        listeners.remove(handler);
    }

    /**
     * Invokes all listeners for the given event.
     *
     * @param event The event to invoke.
     */
    public void call(Event event) {
        var listeners = this.listeners.get(event.getClass());
        if (listeners == null) return;

        listeners.forEach(listener -> listener.run(event));
    }

    @SuppressWarnings("unchecked")
    public interface EventHandler<T extends Event> {
        default void run(Object event) {
            this.run((T) event);
        }

        void run(T event);
    }
}
