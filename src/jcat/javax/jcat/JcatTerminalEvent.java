/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import javax.csapi.cc.jcc.JccEvent;

/**
 * This interface is associated with JcatTerminal events. 
 */
public interface JcatTerminalEvent extends JccEvent {

	public static final int ADDRESS_REGISTERED = 35;
	public static final int ADDRESS_DEREGISTERED = 36;
	public static final int TERMINAL_EVENT_TRANSMISSION_ENDED = 37;
	
	JcatAddress getAddress();
	JcatTerminal getTerminal();
}