package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2002 Deadman Consulting (www.deadman.ca) 

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
import net.sourceforge.gjtapi.FreeConnection;
import javax.csapi.cc.jcc.*;
import javax.telephony.*;
/**
 * This is a wrapper that adapts a JTAPI Connection event into a JccConnectionEvent.
 * Creation date: (2000-10-30 11:01:35)
 * @author: Richard Deadman
 */
public class GenConnEvent extends GenCallEvent implements JccConnectionEvent {
	private ConnectionEvent realEvent = null;
/**
 * GennConnEvent constructor comment.
 * @param prov com.uforce.jain.generic.Provider
 * @param event javax.telephony.CallEvent
 */
public GenConnEvent(net.sourceforge.gjtapi.jcc.Provider prov, ConnectionEvent event) {
	super(prov, event);

	this.setRealEvent(event);
}
/**
 * getConnection method comment.
 */
public JccConnection getConnection() {
	return this.getProv().findConnection((FreeConnection)this.getRealEvent().getConnection());
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 11:05:21)
 * @return javax.telephony.ConnectionEvent
 */
private javax.telephony.ConnectionEvent getRealEvent() {
	return realEvent;
}
/**
 * getSource method comment.
 */
public Object getSource() {
	return this.getConnection();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 11:05:21)
 * @param newRealEvent javax.telephony.ConnectionEvent
 */
private void setRealEvent(javax.telephony.ConnectionEvent newRealEvent) {
	realEvent = newRealEvent;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jcc Adapter for a JTAPI Connection event: " + this.getRealEvent();
}
}
