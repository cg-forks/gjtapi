package net.sourceforge.gjtapi.util;
/**
 * This is an interface that describes the event processing actions of an event.
 * The implementor should be re-entrant.
 * Creation date: (2000-02-25 7:15:02)
 * @author: Richard Deadman
 */
public interface EventHandler {
/**
 * This defines the method that the queued event object must implement.
 */
public abstract void process(Object ev);
}
