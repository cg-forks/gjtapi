/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import javax.csapi.cc.jcc.JccEvent;

/**
This interface is related to events on the JcatTerminalConnection.
 */
public interface JcatTerminalConnectionEvent extends JccEvent {

	// don't know final values yet
	public static final int CAUSE_PARK = 25;
	public static final int CAUSE_UNHOLD = 26;
	public static final int TERMINALCONNECTION_IDLE = 27;
	public static final int TERMINALCONNECTION_RINGING = 28;
	public static final int TERMINALCONNECTION_DROPPED = 29;
	public static final int TERMINALCONNECTION_BRIDGED = 30;
	public static final int TERMINALCONNECTION_TALKING = 31;
	public static final int TERMINALCONNECTION_INUSE = 32;
	public static final int TERMINALCONNECTION_HELD = 33;
	public static final int TERMINALCONNECTION_EVENT_TRANSMISSION_ENDED = 34;
	
	JcatTerminalConnection getTerminalConnection();
}
