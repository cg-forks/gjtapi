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
import jain.application.services.jcc.*;
import javax.telephony.*;
/**
 * An adapter that converts JTAPI Provider events into Jcp ProviderEvents.
 * Creation date: (2000-10-30 13:36:16)
 * @author: Richard Deadman
 */
public class ProviderListenerAdapter implements ProviderListener {
	private Provider prov;
	private JcpProviderListener realListener;
	private EventFilter filter;
/**
 * ProviderListenerAdapter constructor comment.
 */
public ProviderListenerAdapter(Provider prov, JcpProviderListener listener, EventFilter filter) {
	super();

	this.setProv(prov);
	this.setRealListener(listener);
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
	if (obj instanceof ProviderListenerAdapter)
		return this.getRealListener().equals(((ProviderListenerAdapter)obj).getRealListener());
	else
		return false;
}
/**
 * Insert the method's description here.
 * Creation date: (2001-01-24 8:21:41)
 * @return jain.application.services.jcc.EventFilter
 */
private jain.application.services.jcc.EventFilter getFilter() {
	return filter;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 13:40:03)
 * @return com.uforce.jain.generic.Provider
 */
private Provider getProv() {
	return prov;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 13:40:03)
 * @return jain.application.services.jcp.JcpProviderListener
 */
private jain.application.services.jcp.JcpProviderListener getRealListener() {
	return realListener;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getRealListener().hashCode();
}
/**
 * providerEventTransmissionEnded method comment.
 */
public void providerEventTransmissionEnded(ProviderEvent event) {
	EventFilter filter = this.getFilter();
	JcpProviderEvent ce = new GenProviderEvent(this.getProv(), event);
	if ((filter == null) || (filter.getEventDisposition(ce) != EventFilter.EVENT_DISCARD))
		this.getRealListener().providerEventTransmissionEnded(ce);
}
/**
 * providerEventTransmissionEnded method comment.
 */
public void providerInService(ProviderEvent event) {
	EventFilter filter = this.getFilter();
	JcpProviderEvent ce = new GenProviderEvent(this.getProv(), event);
	if ((filter == null) || (filter.getEventDisposition(ce) != EventFilter.EVENT_DISCARD))
		this.getRealListener().providerInService(ce);
}
/**
 * providerEventTransmissionEnded method comment.
 */
public void providerOutOfService(ProviderEvent event) {
	EventFilter filter = this.getFilter();
	JcpProviderEvent ce = new GenProviderEvent(this.getProv(), event);
	if ((filter == null) || (filter.getEventDisposition(ce) != EventFilter.EVENT_DISCARD))
		this.getRealListener().providerOutOfService(ce);
}
/**
 * providerEventTransmissionEnded method comment.
 */
public void providerShutdown(ProviderEvent event) {
	EventFilter filter = this.getFilter();
	JcpProviderEvent ce = new GenProviderEvent(this.getProv(), event);
	if ((filter == null) || (filter.getEventDisposition(ce) != EventFilter.EVENT_DISCARD))
		this.getRealListener().providerShutdown(ce);
}
/**
 * Insert the method's description here.
 * Creation date: (2001-01-24 8:21:41)
 * @param newFilter jain.application.services.jcc.EventFilter
 */
private void setFilter(jain.application.services.jcc.EventFilter newFilter) {
	filter = newFilter;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 13:40:03)
 * @param newProv com.uforce.jain.generic.Provider
 */
private void setProv(Provider newProv) {
	prov = newProv;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 13:40:03)
 * @param newRealListener jain.application.services.jcp.JcpProviderListener
 */
private void setRealListener(jain.application.services.jcp.JcpProviderListener newRealListener) {
	realListener = newRealListener;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	// Insert code to print the receiver here.
	// This implementation forwards the message to super. You may replace or supplement this.
	return super.toString();
}
}
