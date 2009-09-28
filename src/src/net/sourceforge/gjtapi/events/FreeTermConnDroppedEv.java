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

import javax.telephony.events.TermConnDroppedEv;
/**
 * An Observer-style TermConnEv subclass
 * Creation date: (2000-02-15 14:51:24)
 * @author: Richard Deadman
 */
public class FreeTermConnDroppedEv extends FreeTerminalConnectionEvent implements javax.telephony.events.TermConnDroppedEv {
	private FreeTerminal droppedTerminal;
/**
 * Protected FreeTermConnActiveEv constructor comment.
 * @param cause Cause identifier (see javax.telephony.Event)
 * @param metaCode The Observer-style MetaCode ohigher-level description
 * @param isNewMetaEvent Is this a MetaEvent?
 * @param tc The terminal connection the event applies to
 */
public FreeTermConnDroppedEv(int cause, int metaCode, boolean isNewMetaEvent, net.sourceforge.gjtapi.FreeTerminalConnection tc) {
	super(cause, metaCode, isNewMetaEvent, tc);

	droppedTerminal = (FreeTerminal)tc.getTerminal();
}
/**
 * Protected FreeTermConnActiveEv constructor comment.
 * @param cause Cause identifier (see javax.telephony.Event)
 * @param tc The terminal connection the event applies to
 */
public FreeTermConnDroppedEv(int cause, net.sourceforge.gjtapi.FreeTerminalConnection tc) {
	this(cause, javax.telephony.events.Ev.META_UNKNOWN, false, tc);
}
/**
 * Define how an event dispatches itself to registered clients.
 * Creation date: (2000-04-26 10:48:23)
 * @author: Richard Deadman
 */
public void dispatch() {
	// we don't want errors in the observers and listeners to halt our cleanup
	FreeCall c = (FreeCall)this.getCall();
	try {
		super.dispatch();	// send to Obsersers

		// now send to listeners
		c.getListener().terminalConnectionDropped(this);
	} finally {
		// tell the call to remove any Terminal listeners
		c.removeCallListAndObs(this.droppedTerminal);
	}
}
/**
 * Return the observer-style event id.
 */
public int getID() {
	return TermConnDroppedEv.ID;
}
}
