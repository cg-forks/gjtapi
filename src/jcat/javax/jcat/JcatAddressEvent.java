/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import javax.csapi.cc.jcc.JccEvent;

/**
 * This interface is associated with JcatAddressEvents. 
 */
public interface JcatAddressEvent extends JccEvent {

	// don't know final values yet.
	public static final int TERMINAL_REGISTERED = 1;
	public static final int TERMINAL_DEREGISTERED = 2;
	public static final int ADDRESS_EVENT_TRANSMISSION_ENDED = 4;
	
	JcatTerminal getTerminal();
	
	JcatAddress getAddress();
}
