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
import net.sourceforge.gjtapi.*;
import javax.telephony.*;
import javax.telephony.events.*;
/**
 * This is a Raw TerminalConnectionEvent that a RawProvider can partially initialize and pass up
 * to the JTAPI layer for completion and dispatch.
 * Creation date: (2000-02-15 11:02:42)
 * @author: Richard Deadman
 */
public abstract class FreeTerminalConnectionEvent extends FreeCallEvent implements TermConnEv, TerminalConnectionEvent {
	private FreeTerminalConnection terminalConnection = null;
/**
 * TerminalConnectionEvent constructor hooked to JTAPI objects.
 * @param cause Cause identifier (see javax.telephony.Event)
 * @param metaCode The Observer-style MetaCode ohigher-level description
 * @param isNewMetaEvent Is this a MetaEvent?
 * @param tc The terminal connection the event applies to
 */
public FreeTerminalConnectionEvent(int cause, int metaCode, boolean isNewMetaEvent, FreeTerminalConnection tc) {
	super(cause, metaCode, isNewMetaEvent, (FreeCall)tc.getConnection().getCall());

	this.setTerminalConnection(tc);
}
/**
 * TerminalConnectionEvent constructor hooked to JTAPI objects.
 * @param cause Cause identifier (see javax.telephony.Event)
 * @param tc The terminal connection the event applies to
 */
public FreeTerminalConnectionEvent(int cause, FreeTerminalConnection tc) {
	this(cause, 0, false, tc);
}
/**
 * Return the Terminal Connection that the event applies to.
 * Creation date: (2000-02-15 12:06:02)
 * @author: Richard Deadman
 * @return The event's TerminalConnection
 */
public TerminalConnection getTerminalConnection() {
	return terminalConnection;
}
/**
 * Internal setter
 * Creation date: (2000-02-15 12:06:02)
 * @author: Richard Deadman
 * @param newTerminalConnection The associated TerminalConnection for the event
 * @return the new TerminalConnection (useful for cascading)
 */
private FreeTerminalConnection setTerminalConnection(FreeTerminalConnection newTerminalConnection) {
	return terminalConnection = newTerminalConnection;
}
}
