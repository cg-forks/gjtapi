package net.sourceforge.gjtapi.util;
/**
 * This is a queue that accepts objects and processes these objects.
 * A user will create and instance of the EventHandler and pass it into this event collector.
 * Subclasses include an Ordered and Parallel Event version for when the objects are visitors to a common
 * handler, and Ordered Block version for when the common held object visits each queue item.
 * Creation date: (2000-02-24 17:01:26)
 * @author: Richard Deadman
 */
public abstract class BlockManager extends BaseManager {
	protected ExceptionHandler exHandler;
/**
 * Create a BlockManager with the ExceptionHandler that defines how to handle EventHandler.process()
 * RuntimeExceptions.
 * Creation date: (2000-05-10 11:23:13)
 * @author: Richard Deadman
 * @param exh A handler object for processing the exceptions.
 */
public BlockManager(ExceptionHandler exh) {
	this.exHandler = exh;
}
/**
 * Add a new object to the queue after testing for the correct type.  If anyone is waiting for objects they are activated.
 * Creation date: (2000-02-24 17:05:21)
 * @author: Richard Deadman
 * @param o The EventHandler to add
 */
public void put(EventHandler o) {
	super.put(o);
}
}
