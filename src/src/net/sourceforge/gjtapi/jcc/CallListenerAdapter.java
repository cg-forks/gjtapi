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
import javax.jain.services.jcc.*;
import javax.jain.services.jcp.JcpCallListener;
import javax.telephony.*;
import javax.telephony.callcontrol.*;
/**
 * This is a simple adapter that listens for JTAPI Call events and translates
 * them into JccCall events.
 * Creation date: (2000-10-11 15:09:31)
 * @author: Richard Deadman
 */
public class CallListenerAdapter implements CallListener {
	private Provider prov = null;
	private JcpCallListener realCallListener = null;
/**
 * EventConnectionAdapter constructor comment.
 */
public CallListenerAdapter(Provider prov,
			JcpCallListener listener) {
	super();

	this.setProv(prov);
	this.setRealCallListener(listener);
}
/**
 * Forward the event if the filter doesn't tell me to shut the event down.
 */
public void callActive(CallEvent event) {
	JccCallEvent ce = new GenCallEvent(this.getProv(), event);
	this.getRealCallListener().callActive(ce);
}
/**
 * Forward the event if the filter doesn't tell me to shut the event down.
 * <P> This is used in Provider.createCall() to notify Listeners of new calls they are
 * registered with.
 */
void callCreated(JccCallEvent event) {
	this.getRealCallListener().callCreated(event);
}
/**
 * Forward the event if the filter doesn't tell me to shut the event down.
 */
public void callEventTransmissionEnded(CallEvent event) {
	JccCallEvent ce = new GenCallEvent(this.getProv(), event);
	this.getRealCallListener().callEventTransmissionEnded(ce);
}
/**
 * Forward the event if the filter doesn't tell me to shut the event down.
 */
public void callInvalid(CallEvent event) {
	JccCallEvent ce = new GenCallEvent(this.getProv(), event);
	this.getRealCallListener().callInvalid(ce);
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
	if (obj instanceof CallListenerAdapter)
		return this.getRealCallListener().equals(((CallListenerAdapter)obj).getRealCallListener());
	else
		return false;
}
/**
 * Get the provider that I am attached to
 * Creation date: (2000-10-30 10:43:58)
 * @return com.uforce.jain.generic.Provider
 */
Provider getProv() {
	return prov;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:27:59)
 * @return jain.application.services.jcc.JccCallListener
 */
private JcpCallListener getRealCallListener() {
	return realCallListener;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getRealCallListener().hashCode();
}
/**
 * Not supported by Jcc
 */
public void multiCallMetaMergeEnded(MetaEvent event) {}
/**
 * Not supported by Jcc
 */
public void multiCallMetaMergeStarted(MetaEvent event) {}
/**
 * Not supported by Jcc
 */
public void multiCallMetaTransferEnded(MetaEvent event) {}
/**
 * Not supported by Jcc
 */
public void multiCallMetaTransferStarted(MetaEvent event) {}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:43:58)
 * @param newProv com.uforce.jain.generic.Provider
 */
private void setProv(Provider newProv) {
	prov = newProv;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:27:59)
 * @param newRealCallListener jain.application.services.jcc.JccCallListener
 */
private void setRealCallListener(JcpCallListener newRealCallListener) {
	realCallListener = newRealCallListener;
}
/**
 * Not supported by Jcc
 */
public void singleCallMetaProgressEnded(MetaEvent event) {}
/**
 * Not supported by Jcc
 */
public void singleCallMetaProgressStarted(MetaEvent event) {}
/**
 * Not supported by Jcc
 */
public void singleCallMetaSnapshotEnded(MetaEvent event) {}
/**
 * Not supported by Jcc
 */
public void singleCallMetaSnapshotStarted(MetaEvent event) {}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Listener adapter for Jcc Call Listener: " + this.getRealCallListener();
}
}
