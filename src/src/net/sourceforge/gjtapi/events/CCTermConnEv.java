package net.sourceforge.gjtapi.events;

/*
	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 

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
import javax.telephony.*;
import net.sourceforge.gjtapi.*;
import javax.telephony.callcontrol.events.*;
import javax.telephony.callcontrol.CallControlTerminalConnectionEvent;
/**
 * This is a parent class for all CallControl TerminalConnection events.
 * <P>These are parallel events to those generated for the base TerminalConnection.  These are sent
 * to Observers that implement the CallControlCallObserver interface.
 * Creation date: (2000-08-03 11:45:53)
 * @author: Richard Deadman
 */
public abstract class CCTermConnEv extends CCCallEv implements CallCtlTermConnEv, CallControlTerminalConnectionEvent {
	private net.sourceforge.gjtapi.FreeTerminalConnection termConn = null;
/**
 * Construct a CallControl TerminalConnection event.
 * @param cause The CallCtl cause.
 * @param metaCode A MetaCode id
 * @param isNewMetaEvent Is this the beginning of a MetaCode set of events?
 * @param tc A Generic Framework FreeTerminalConnection
 */
public CCTermConnEv(int cause, int metaCode, boolean isNewMetaEvent, FreeTerminalConnection tc) {
	super(cause, metaCode, isNewMetaEvent, (FreeCall)tc.getConnection().getCall());

	this.setTermConn(tc);
}
/**
 * Construct a CallControl TerminalConnection event.
 * @param cause The CallCtl cause.
 * @param tc A Generic Framework FreeTerminalConnection
 */
public CCTermConnEv(int cause, FreeTerminalConnection tc) {
	super(cause, (FreeCall)tc.getConnection().getCall());

	this.setTermConn(tc);
}
/**
 * Return the TerminalConnection this CallControl Event is associated with.
 */
public TerminalConnection getTerminalConnection() {
	return this.termConn;
}
/**
 * Internal setter for the TerminalConnection this CallControl event is associated with.
 * Creation date: (2000-08-03 12:01:31)
 * @param newTermConn A FreeTerminalConnection associated with the event.
 */
private void setTermConn(FreeTerminalConnection newTermConn) {
	termConn = newTermConn;
}
}
