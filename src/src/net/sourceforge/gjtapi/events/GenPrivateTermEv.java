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
import net.sourceforge.gjtapi.FreeTerminal;
import javax.telephony.events.Ev;
import javax.telephony.privatedata.events.PrivateTermEv;
/**
 * An event for returning PrivateData events to registered observers.
 * Creation date: (2000-08-09 10:45:32)
 * @author: Richard Deadman
 */
public class GenPrivateTermEv extends FreeTerminalEvent implements PrivateTermEv {
	private Object privateData;
/**
 * GenPrivateTermEv constructor comment.
 * @param term The terminal for the event
 * @param cause The EV.cause field that describes the event.
 * @data The asynchronous private data associated with the event.
 */
public GenPrivateTermEv(FreeTerminal term, int cause, Object data) {
	super(cause, Ev.META_UNKNOWN, false, term);

	this.setPrivateData(data);
}
/**
 * Return the PrivateTermEv id for switch statement identification.
 */
public int getID() {
	return PrivateTermEv.ID;
}
/**
 * Return the PrivateData associated with this event.
 * Creation date: (2000-08-09 10:52:03)
 * @author: Richard Deadman
 * @return java.lang.Object
 */
public Object getPrivateData() {
	return this.privateData;
}
/**
 * Store my PrivateData
 * Creation date: (2000-08-09 10:52:03)
 * @author: Richard Deadman
 * @param newPrivateData The private data for the Provider that is being returned asynchronously.
 */
private void setPrivateData(Object newPrivateData) {
	this.privateData = newPrivateData;
}
}
