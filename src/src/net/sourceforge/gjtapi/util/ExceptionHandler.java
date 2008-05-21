package net.sourceforge.gjtapi.util;
/**
 * This is a class that encapsulates how to handle thread running exceptions.
 * <P>The base implementation is a NOP -- the exception is rethrown.
 * Creation date: (2000-05-10 7:15:02)
 * @author: Richard Deadman
 */
public class ExceptionHandler {
/**
 * The default ExceptionHandler just rethrows the Exception
 * Creation date: (2000-05-10 11:04:10)
 * @author: Richard Deadman
 * @param eh The EventHandler being invoked when the exception occurred.
 * @param ex The exception that occured.
 * @param other The parameter passed to the EventHandler
 * @exception java.lang.Exception the same exception.
 */
public void handleException(EventHandler eh, RuntimeException ex, Object other) throws RuntimeException {
	throw ex;
}
}
