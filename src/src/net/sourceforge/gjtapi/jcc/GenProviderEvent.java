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
import jain.application.services.jcp.*;
import javax.telephony.*;
/**
 * Wrapper for a JTPAI Provider event that morphs it into a JcpProviderEvent
 * Creation date: (2000-10-30 13:23:05)
 * @author: Richard Deadman
 */
public class GenProviderEvent implements JcpProviderEvent {
	private Provider prov = null;
	private ProviderEvent realEvent = null;
/**
 * ProviderEvent constructor comment.
 */
public GenProviderEvent(Provider prov, ProviderEvent eventToWrap) {
	super();

	this.setProv(prov);
	this.setRealEvent(eventToWrap);
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
	if (obj instanceof GenProviderEvent)
		return this.getProv().equals(((GenProviderEvent)obj).getProv());
	else
		return false;
}
/**
 * getCause method comment.
 */
public int getCause() {
	int jCause = this.getRealEvent().getCause();
	switch (jCause) {
		case ProviderEvent.CAUSE_CALL_CANCELLED: {
			return JcpProviderEvent.CAUSE_CALL_CANCELLED;
		} 
		case ProviderEvent.CAUSE_DEST_NOT_OBTAINABLE: {
			return JcpProviderEvent.CAUSE_DEST_NOT_OBTAINABLE;
		} 
		case ProviderEvent.CAUSE_INCOMPATIBLE_DESTINATION: {
			return JcpProviderEvent.CAUSE_INCOMPATIBLE_DESTINATION;
		} 
		case ProviderEvent.CAUSE_LOCKOUT: {
			return JcpProviderEvent.CAUSE_LOCKOUT;
		} 
		case ProviderEvent.CAUSE_NETWORK_CONGESTION: {
			return JcpProviderEvent.CAUSE_NETWORK_CONGESTION;
		} 
		case ProviderEvent.CAUSE_NETWORK_NOT_OBTAINABLE: {
			return JcpProviderEvent.CAUSE_NETWORK_NOT_OBTAINABLE;
		} 
		case ProviderEvent.CAUSE_NEW_CALL: {
			return JcpProviderEvent.CAUSE_NEW_CALL;
		} 
		case ProviderEvent.CAUSE_NORMAL: {
			return JcpProviderEvent.CAUSE_NORMAL;
		} 
		case ProviderEvent.CAUSE_RESOURCES_NOT_AVAILABLE: {
			return JcpProviderEvent.CAUSE_RESOURCES_NOT_AVAILABLE;
		} 
		case ProviderEvent.CAUSE_SNAPSHOT: {
			return JcpProviderEvent.CAUSE_SNAPSHOT;
		} 
		default: {
			return JcpProviderEvent.CAUSE_UNKNOWN;
		}
	}
}
/**
 * getID method comment.
 */
public int getID() {
	int jId = this.getRealEvent().getID();
	switch (jId) {
		case ProviderEvent.PROVIDER_EVENT_TRANSMISSION_ENDED: {
			return JcpProviderEvent.PROVIDER_EVENT_TRANSMISSION_ENDED;
		} 
		case ProviderEvent.PROVIDER_IN_SERVICE: {
			return JcpProviderEvent.PROVIDER_IN_SERVICE;
		} 
		case ProviderEvent.PROVIDER_OUT_OF_SERVICE: {
			return JcpProviderEvent.PROVIDER_OUT_OF_SERVICE;
		} 
		case ProviderEvent.PROVIDER_SHUTDOWN: {
			return JcpProviderEvent.PROVIDER_SHUTDOWN;
		} 
		default: {
			return JcpProviderEvent.PROVIDER_OUT_OF_SERVICE;
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 13:25:01)
 * @return com.uforce.jain.generic.Provider
 */
private Provider getProv() {
	return prov;
}
/**
 * getProvider method comment.
 */
public JcpProvider getProvider() {
	return this.getProv();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 13:25:01)
 * @return javax.telephony.ProviderEvent
 */
private javax.telephony.ProviderEvent getRealEvent() {
	return realEvent;
}
/**
 * getSource method comment.
 */
public Object getSource() {
	return this.getProv();
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
 * Creation date: (2000-10-30 13:25:01)
 * @param newProv com.uforce.jain.generic.Provider
 */
private void setProv(Provider newProv) {
	prov = newProv;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 13:25:01)
 * @param newRealEvent javax.telephony.ProviderEvent
 */
private void setRealEvent(javax.telephony.ProviderEvent newRealEvent) {
	realEvent = newRealEvent;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "JccProviderEvent wrapper for JTAPI event: " + this.getRealEvent();
}
}
