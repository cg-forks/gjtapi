package net.sourceforge.gjtapi.util;
/**
 * This is a queue that accepts objects manages a collection of event handlers.
 * A user will create and instance of the EventHandler and pass it into this event collector.
 * Subclasses include an Ordered and Parallel version.
 * Creation date: (2000-02-24 17:01:26)
 * @author: Richard Deadman
 */
public abstract class EventManager extends BlockManager {
	protected EventHandler handler;			// shared handler for the handler threads
/**
 * Create myself with a event handler delegate
 * Creation date: (2000-02-25 7:26:06)
 * @author: Richard Deadman
 * @param eh com.uforce.util.EventHandler
 * @param exh A handler for RuntimeEvents from eh.process() RuntimeExceptions.
 */
public EventManager(EventHandler eh, ExceptionHandler exh) {
	super(exh);
	
	// record this first thread as a template for others
	this.handler = eh;

}
/**
 * Add a new object to the queue.  If anyone is waiting for objects they are activated.
 * This opens the access rights to the base put method.
 * Creation date: (2000-02-24 17:05:21)
 * @author: Richard Deadman
 * @param o The object to add
 */
public void put(EventHandler o) {
	super.put(o);
}
}
