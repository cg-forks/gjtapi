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
import net.sourceforge.gjtapi.FreeCall;

import javax.telephony.TerminalConnectionListener;
import javax.telephony.events.*;

/**
 * A connection connection event.
 * This is made by a Connection Event to satisfy old-style observers
 * Creation date: (2000-02-15 13:23:45)
 * @author: Richard Deadman
 */
public class FreeConnConnectedEv extends FreeConnectionEvent implements ConnConnectedEv {
/**
 * FreeConnConnectedEv constructor comment.
 * @param cause The cause code for the event from javax.te.ephony.Event
 * @param metaCode The Observer-style (Ev) metaCode
 * @param isNewMetaEvent Is this part of a higher-level MetaEvent (Event Listener)
 * @param conn The connection the event applies to
 */
public FreeConnConnectedEv(int cause, int metaCode, boolean isNewMetaEvent, net.sourceforge.gjtapi.FreeConnection conn) {
	super(cause, metaCode, isNewMetaEvent, conn);
}
/**
 * FreeConnConnectedEv constructor comment.
 * @param cause The cause code for the event from javax.te.ephony.Event
 * @param conn The connection the event applies to
 */
public FreeConnConnectedEv(int cause, net.sourceforge.gjtapi.FreeConnection conn) {
	this(cause, Ev.META_UNKNOWN, false, conn);
}
/**
 * Define how an event dispatches itself to registered clients.
 * Creation date: (2000-04-26 10:48:23)
 * @author: Richard Deadman
 */
public void dispatch() {
    super.dispatch();	// send to Obsersers

    // now send to listeners
    final FreeCall freeCall = (FreeCall) getCall();
    final TerminalConnectionListener listener = freeCall.getListener();
    listener.connectionConnected(this);
}

/**
 * Return the observer-style ConnEv subtype id.
 */
public int getID() {
	return ConnConnectedEv.ID;
}
}
