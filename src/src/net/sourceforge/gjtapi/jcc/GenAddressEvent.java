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
import net.sourceforge.gjtapi.FreeAddress;
import javax.jcat.JcatAddress;
import javax.jcat.JcatTerminal;
import javax.jcat.JcatAddressEvent;
import javax.telephony.*;
/**
 * This is a wrapper event that makes a JTAPI AddressEvent appear as a JcatAddressEvent.
 * Creation date: (2003-10-30 10:41:18)
 * @author: Richard Deadman
 */
public class GenAddressEvent implements JcatAddressEvent {
	/**
	 * The provider that ccan be used to translate the call object
	 **/
	private Provider prov = null;
	/**
	 * The real JTAPI event I am wrapping
	 **/
	private AddressEvent realEvent = null;
/**
 * GenCallEvent constructor comment.
 */
public GenAddressEvent(Provider prov, AddressEvent event) {
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
	if (obj instanceof GenAddressEvent)
		return this.getRealEvent().equals(((GenAddressEvent)obj).getRealEvent());
	else
		return false;
}
/**
 * Ask the Provider to find the call, lazily if necessary, that wraps the JTAPI call.
 */
public JcatAddress getAddress() {
	return this.getProv().findAddress((FreeAddress)this.getRealEvent().getAddress());
}
/**
 * getCause method comment.
 */
public int getCause() {
	int jCause = this.getRealEvent().getCause();

	switch (jCause) {
		case AddressEvent.CAUSE_NORMAL: {
			return JcatAddressEvent.CAUSE_NORMAL;
		} 
		case AddressEvent.CAUSE_RESOURCES_NOT_AVAILABLE: {
			return JcatAddressEvent.CAUSE_RESOURCES_NOT_AVAILABLE;
		} 
		case AddressEvent.CAUSE_SNAPSHOT: {
			return JcatAddressEvent.CAUSE_SNAPSHOT;
		} 
		case AddressEvent.CAUSE_UNKNOWN: {
			return JcatAddressEvent.CAUSE_UNKNOWN;
		} 
		default: {
			return JcatAddressEvent.CAUSE_UNKNOWN;
		}
	}
}
/**
 * getID method comment.
 */
public int getID() {
	int jId = this.getRealEvent().getID();
	switch (jId) {
		case AddressEvent.ADDRESS_EVENT_TRANSMISSION_ENDED: {
			return JcatAddressEvent.ADDRESS_EVENT_TRANSMISSION_ENDED;
		} 
		default: {
			return JcatAddressEvent.ADDRESS_EVENT_TRANSMISSION_ENDED;
		}
	}
}
/**
 * Package-protected accessor for the Jcat Provider implementation.
 * Creation date: (2003-10-30 10:46:28)
 * @return com.uforce.jain.generic.Provider
 */
protected Provider getProv() {
	return prov;
}
/**
 * Get the real event that I wrap.
 * Creation date: (2003-10-30 10:48:06)
 * @return javax.telephony.AddressEvent
 */
private javax.telephony.AddressEvent getRealEvent() {
	return realEvent;
}
/**
 * getSource method comment.
 */
public Object getSource() {
	return this.getAddress();
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
 * Set the provider that manages me.
 * Creation date: (2003-10-30 10:46:28)
 * @param newProv com.uforce.jain.generic.Provider
 */
private void setProv(Provider newProv) {
	prov = newProv;
}
/**
 * Set the real AddressEvent that I wrap.
 * Creation date: (2003-10-30 10:48:06)
 * @param newRealEvent javax.telephony.CallEvent
 */
private void setRealEvent(javax.telephony.AddressEvent newRealEvent) {
	realEvent = newRealEvent;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jcat wrapper for a JTAPI event: " + this.getRealEvent();
}
	/* (non-Javadoc)
	 * @see javax.jcat.JcatAddressEvent#getTerminal()
	 */
	public JcatTerminal getTerminal() {
		// TODO Auto-generated method stub
		return null;
	}

}
