package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca) 

	All rights reserved. 

	Permission is hereby granted, free of charge, to any person obtaining a 
	copy of this software and associated documentation files (the 
	"Software"), to deal in the Software without restriction, including 
	without limitation the rights to use, copy, modify, merge, publish, 
	distribute, and/or sell copies of the Software, and to permit persons 
	to whom the Software is furnished to do so, provided that the above 
	copyright notice(s) and this permission notice appear in all copies of 
	the Software and that both the above copyright notice(s) and this 
	permission notice appear in supporting documentation. 

	THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS 
	OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF 
	MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT 
	OF THIRD PARTY RIGHTS. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR 
	HOLDERS INCLUDED IN THIS NOTICE BE LIABLE FOR ANY CLAIM, OR ANY SPECIAL 
	INDIRECT OR CONSEQUENTIAL DAMAGES, OR ANY DAMAGES WHATSOEVER RESULTING 
	FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
	NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION 
	WITH THE USE OR PERFORMANCE OF THIS SOFTWARE. 

	Except as contained in this notice, the name of a copyright holder 
	shall not be used in advertising or otherwise to promote the sale, use 
	or other dealings in this Software without prior written authorization 
	of the copyright holder.
*/
import net.sourceforge.gjtapi.FreeTerminalConnection;

import javax.jcat.JcatTerminalConnection;
import javax.jcat.JcatTerminalConnectionEvent;
import javax.telephony.*;
/**
 * This is a wrapper event that makes a JTAPI TerminalConnectionEvent appear as a JcatTerminalConnectionEvent.
 * Creation date: (2003-10-30 10:41:18)
 * @author: Richard Deadman
 */
public class GenTerminalConnectionEvent extends GenCallEvent implements JcatTerminalConnectionEvent {
	/**
	 * The real JTAPI event I am wrapping
	 **/
	private TerminalConnectionEvent realEvent = null;
/**
 * GenCallEvent constructor comment.
 */
public GenTerminalConnectionEvent(Provider prov, TerminalConnectionEvent event) {
	super(prov, event);

	this.setRealEvent(event);
}
/**
 * Ask the Provider to find the call, lazily if necessary, that wraps the JTAPI call.
 */
public JcatTerminalConnection getTerminalConnection() {
	return this.getProv().findTerminalConnection((FreeTerminalConnection)this.getRealEvent().getTerminalConnection());
}
/**
 * getID method comment.
 */
public int getID() {
	int jId = this.getRealEvent().getID();
	switch (jId) {
		case TerminalConnectionEvent.TERMINAL_CONNECTION_ACTIVE: {
			return JcatTerminalConnectionEvent.TERMINALCONNECTION_TALKING;
		} 
		case TerminalConnectionEvent.TERMINAL_CONNECTION_CREATED: {
			return JcatTerminalConnectionEvent.TERMINALCONNECTION_IDLE;
		} 
		case TerminalConnectionEvent.TERMINAL_CONNECTION_DROPPED: {
			return JcatTerminalConnectionEvent.TERMINALCONNECTION_DROPPED;
		} 
		case TerminalConnectionEvent.TERMINAL_CONNECTION_PASSIVE: {
			return JcatTerminalConnectionEvent.TERMINALCONNECTION_HELD;
		} 
		case TerminalConnectionEvent.TERMINAL_CONNECTION_RINGING: {
			return JcatTerminalConnectionEvent.TERMINALCONNECTION_RINGING;
		} 
		case TerminalConnectionEvent.TERMINAL_CONNECTION_UNKNOWN: {
			return JcatTerminalConnectionEvent.TERMINALCONNECTION_INUSE;
		} 
		default: {
			return JcatTerminalConnectionEvent.TERMINALCONNECTION_INUSE;
		}
	}
}
/**
 * Get the real event that I wrap.
 * Creation date: (2003-10-30 10:48:06)
 * @return javax.telephony.TerminalEvent
 */
private javax.telephony.TerminalConnectionEvent getRealEvent() {
	return realEvent;
}
/**
 * getSource method comment.
 */
public Object getSource() {
	return this.getTerminalConnection();
}
/**
 * Set the real TerminalConnectionEvent that I wrap.
 * Creation date: (2003-10-30 10:48:06)
 * @param newRealEvent javax.telephony.CallEvent
 */
private void setRealEvent(javax.telephony.TerminalConnectionEvent newRealEvent) {
	realEvent = newRealEvent;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jcat wrapper for a JTAPI event: " + this.getRealEvent();
}
}
