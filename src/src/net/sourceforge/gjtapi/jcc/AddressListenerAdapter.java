package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2003, Richard Deadman, Deadman Consulting (www.deadman.ca) 

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
import javax.csapi.cc.jcc.EventFilter;
import javax.jcat.JcatAddressEvent;
import javax.jcat.JcatAddressListener;
import javax.telephony.*;
/**
 * This is a simple adapter that listens for JTAPI Address events and translates
 * them into JcatAddress events.
 * Creation date: (2003-10-11 15:09:31)
 * @author: Richard Deadman
 */
public class AddressListenerAdapter implements AddressListener {
	private Provider prov = null;
	private JcatAddressListener realAddressListener = null;
	private EventFilter filter = null;
/**
 * EventConnectionAdapter constructor comment.
 */
public AddressListenerAdapter(Provider prov,
		JcatAddressListener listener,
		EventFilter filter) {
	super();

	this.setProv(prov);
	this.setRealAddressListener(listener);
	this.setFilter(filter);
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
	if (obj instanceof AddressListenerAdapter) {
		// test that they have the same real Listener
		return this.getRealAddressListener().equals(((AddressListenerAdapter)obj).getRealAddressListener());
	}
	return false;
}
/**
 * Get the provider that I am attached to
 * Creation date: (2003-10-30 10:43:58)
 * @return com.uforce.jain.generic.Provider
 */
Provider getProv() {
	return prov;
}
/**
 * Getter for the Jcat AddressListener I wrap
 * Creation date: (2003-10-30 10:27:59)
 * @return jain.application.services.jcc.JccCallListener
 */
private JcatAddressListener getRealAddressListener() {
	return this.realAddressListener;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getRealAddressListener().hashCode();
}
/**
 * Insert the method's description here.
 * Creation date: (2003-10-30 10:43:58)
 * @param newProv com.uforce.jain.generic.Provider
 */
private void setProv(Provider newProv) {
	prov = newProv;
}
/**
 * Setter for the Jcat AddressListener I wrap
 * Creation date: (2003-10-30 10:27:59)
 * @param newRealCallListener jain.application.services.jcc.JccCallListener
 */
private void setRealAddressListener(JcatAddressListener newRealAddressListener) {
	this.realAddressListener = newRealAddressListener;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Listener adapter for Jcc Address Listener: " + this.getRealAddressListener();
}
	/**
	 * Forward on the listening ended event from JTAPI to Jcc
	 * @see javax.telephony.AddressListener#addressListenerEnded(javax.telephony.AddressEvent)
	 */
	public void addressListenerEnded(AddressEvent event) {
		JcatAddressEvent ev = new GenAddressEvent(this.getProv(), event);
		EventFilter ef = this.getFilter();
		int disposition = EventFilter.EVENT_NOTIFY;
		if (ef != null)
			disposition = ef.getEventDisposition(ev);
		if (disposition == EventFilter.EVENT_NOTIFY)
			this.getRealAddressListener().addressEventTransmissionEnded(ev);

	}

	/**
	 * Get the filter for my events.
	 * @return an EventFilter that determines how events are propagated.
	 */
	private EventFilter getFilter() {
		return filter;
	}

	/**
	 * Set my filter that determines if events are propagated
	 * @param filter The event filter.
	 */
	private void setFilter(EventFilter filter) {
		this.filter = filter;
	}

}
