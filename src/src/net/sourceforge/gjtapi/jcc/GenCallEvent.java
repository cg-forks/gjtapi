package net.sourceforge.gjtapi.jcc;

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
import javax.csapi.cc.jcc.*;
import javax.telephony.*;
/**
 * This is a wrapper event that makes a JTAPI CallEvent appear as a JccCallEvent.
 * Creation date: (2000-10-30 10:41:18)
 * @author: Richard Deadman
 */
public class GenCallEvent implements JccCallEvent {
	/**
	 * The provider that ccan be used to translate the call object
	 **/
	private Provider prov = null;
	/**
	 * The real JTAPI event I am wrapping
	 **/
	private CallEvent realEvent = null;
/**
 * GenCallEvent constructor comment.
 */
public GenCallEvent(Provider prov, CallEvent event) {
	super();

	this.setProv(prov);
	this.setRealEvent(event);
}
/**
 * Compares two objects for equality. Returns a boolean that indicates
 * whether this object is equivalent to the specified object. This method
 * is used when an object is stored in a hashtable.
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals(Object obj) {
	if (obj instanceof GenCallEvent)
		return this.getRealEvent().equals(((GenCallEvent)obj).getRealEvent());
	else
		return false;
}
/**
 * Ask the Provider to find the call, lazily if necessary, that wraps the JTAPI call.
 */
public JccCall getCall() {
	return this.getProv().findCall((FreeCall)this.getRealEvent().getCall());
}
/**
 * getCause method comment.
 */
public int getCause() {
	int jCause = this.getRealEvent().getCause();

	switch (jCause) {
			// must use JcpCallEvent constants due to error in Jcc 1.0b
		case CallEvent.CAUSE_CALL_CANCELLED: {
			return JccCallEvent.CAUSE_CALL_CANCELLED;
		} 
		case CallEvent.CAUSE_DEST_NOT_OBTAINABLE: {
			return JccCallEvent.CAUSE_DEST_NOT_OBTAINABLE;
		} 
		case CallEvent.CAUSE_INCOMPATIBLE_DESTINATION: {
			return JccCallEvent.CAUSE_INCOMPATIBLE_DESTINATION;
		} 
		case CallEvent.CAUSE_LOCKOUT: {
			return JccCallEvent.CAUSE_LOCKOUT;
		} 
		case CallEvent.CAUSE_NETWORK_CONGESTION: {
			return JccCallEvent.CAUSE_NETWORK_CONGESTION;
		} 
		case CallEvent.CAUSE_NETWORK_NOT_OBTAINABLE: {
			return JccCallEvent.CAUSE_NETWORK_NOT_OBTAINABLE;
		} 
		case CallEvent.CAUSE_NEW_CALL: {
			return JccCallEvent.CAUSE_NEW_CALL;
		} 
		case CallEvent.CAUSE_NORMAL: {
			return JccCallEvent.CAUSE_NORMAL;
		} 
		case CallEvent.CAUSE_RESOURCES_NOT_AVAILABLE: {
			return JccCallEvent.CAUSE_RESOURCES_NOT_AVAILABLE;
		} 
		case CallEvent.CAUSE_SNAPSHOT: {
			return JccCallEvent.CAUSE_SNAPSHOT;
		} 
		case CallEvent.CAUSE_UNKNOWN: {
			return JccCallEvent.CAUSE_UNKNOWN;
		} 
		default: {
			return JccCallEvent.CAUSE_UNKNOWN;
		}
	}
}
/**
 * getID method comment.
 */
public int getID() {
	int jId = this.getRealEvent().getID();
	switch (jId) {
		case CallEvent.CALL_ACTIVE: {
			return JccCall.ACTIVE;
		} 
		case CallEvent.CALL_INVALID: {
			return JccCall.INVALID;
		} 
		default: {
			return JccCall.INVALID;
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:46:28)
 * @return com.uforce.jain.generic.Provider
 */
protected Provider getProv() {
	return prov;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:48:06)
 * @return javax.telephony.CallEvent
 */
private javax.telephony.CallEvent getRealEvent() {
	return realEvent;
}
/**
 * getSource method comment.
 */
public Object getSource() {
	return this.getCall();
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getRealEvent().hashCode();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:46:28)
 * @param newProv com.uforce.jain.generic.Provider
 */
private void setProv(Provider newProv) {
	prov = newProv;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:48:06)
 * @param newRealEvent javax.telephony.CallEvent
 */
private void setRealEvent(javax.telephony.CallEvent newRealEvent) {
	realEvent = newRealEvent;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jcc wrapper for a JTAPI event: " + this.getRealEvent();
}
}
