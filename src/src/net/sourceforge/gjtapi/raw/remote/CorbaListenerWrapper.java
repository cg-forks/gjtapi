package net.sourceforge.gjtapi.raw.remote;

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
import java.io.NotSerializableException;
import java.io.Serializable;
import javax.telephony.media.Symbol;
import net.sourceforge.gjtapi.*;
/**
 * This wraps a CorbaListener and makes it look like a TelephonyListener again. The TelephonyProvider can then
 * deal with TelephonyListenerss as if they are local.
 * Creation date: (2000-02-17 13:36:23)
 * @author: Richard Deadman
 */
public class CorbaListenerWrapper implements TelephonyListener {
	private net.sourceforge.gjtapi.raw.remote.corba.CorbaListener delegate;
	private CallMapper refMapper = null;
	private org.omg.CORBA.ORB orb = null;
/**
 * Create a wrapper TelephonyListener that delegates of to a CorbaListener.
 * Creation date: (2000-02-17 13:36:49)
 * @author: Richard Deadman
 * @param rl The wrapped CorbaListener stub
 * @param mapper A map between RP call ids and serializable call ids.
 */
public CorbaListenerWrapper(net.sourceforge.gjtapi.raw.remote.corba.CorbaListener cl, CallMapper mapper, org.omg.CORBA.ORB orb) {
	super();

	if (cl == null)
		throw new NullPointerException();
	else
		this.setDelegate(cl);

	this.setRefMapper(mapper);
	this.setOrb(orb);
}
/**
 * addressPrivateData method comment.
 */
public void addressPrivateData(String address, Serializable data, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().addressPrivateData(address, CorbaProvider.convertToAny(this.getOrb(), data), cause);
	} catch (NotSerializableException nse) {
		// eat
		System.err.println("Cannot push private data back to client: ");
		nse.printStackTrace(System.err);
	}
}
/**
 * callActive method comment.
 */
public void callActive(net.sourceforge.gjtapi.CallId id, int cause) {
	// Try to send the event to the client side
	this.getDelegate().callActive(this.getRefMapper().callToInt(id), cause);
}
/**
 * callInvalid method comment.
 */
public void callInvalid(net.sourceforge.gjtapi.CallId id, int cause) {
	// Try to send the event to the client side
	this.getDelegate().callInvalid(this.getRefMapper().callToInt(id), cause);
}
/**
 * callOverloadCeased method comment.
 */
public void callOverloadCeased(java.lang.String address) {
	this.getDelegate().callOverloadCeased(address);
}
/**
 * callOverloadEncountered method comment.
 */
public void callOverloadEncountered(java.lang.String address) {
	this.getDelegate().callOverloadEncountered(address);
}
/**
 * callPrivateData method comment.
 */
public void callPrivateData(CallId call, Serializable data, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().callPrivateData(this.getRefMapper().callToInt(call), CorbaProvider.convertToAny(this.getOrb(), data), cause);
	} catch (NotSerializableException nse) {
		// eat
		System.err.println("Cannot push private data back to client: ");
		nse.printStackTrace(System.err);
	}
}
/**
 * connectionAddressAnalyse method comment.
 */
public void connectionAddressAnalyse(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionAddressAnalyse(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionAddressCollect method comment.
 */
public void connectionAddressCollect(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionAddressCollect(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionAlerting method comment.
 */
public void connectionAlerting(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionAlerting(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionAuthorizeCallAttempt method comment.
 */
public void connectionAuthorizeCallAttempt(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionAuthorizeCallAttempt(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionCallDelivery method comment.
 */
public void connectionCallDelivery(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionCallDelivery(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionConnected method comment.
 */
public void connectionConnected(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionConnected(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionDisconnected method comment.
 */
public void connectionDisconnected(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionDisconnected(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionFailed method comment.
 */
public void connectionFailed(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionFailed(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionInProgress method comment.
 */
public void connectionInProgress(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionInProgress(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * connectionSuspended method comment.
 */
public void connectionSuspended(CallId id, String address, int cause) {
	// Try to send the event to the client side
	this.getDelegate().connectionSuspended(this.getRefMapper().callToInt(id), address, cause);
}
/**
 * Sees if the two objects hold equal delegates.
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 */
public boolean equals(Object obj) {
	if (obj instanceof CorbaListenerWrapper) {
		CorbaListenerWrapper clw = (CorbaListenerWrapper)obj;
		return this.getDelegate().equals(clw.getDelegate());
	}
	return false;
}
/**
 * Internal accessor for the stub of the Corba Listener.
 * Creation date: (2000-08-24 13:38:44)
 * @author: Richard Deadman
 * @return The remote stub that implements the CorbaListener interface.
 */
private net.sourceforge.gjtapi.raw.remote.corba.CorbaListener getDelegate() {
	return delegate;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 15:57:36)
 * @author: Richard Deadman
 * @return org.omg.CORBA.ORB
 */
private org.omg.CORBA.ORB getOrb() {
	return orb;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-04-25 0:15:49)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.remote.CallMapper
 */
private CallMapper getRefMapper() {
	return refMapper;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getDelegate().hashCode();
}
/**
 * mediaPlayPause method comment.
 */
public void mediaPlayPause(java.lang.String terminal, int index, int offset, javax.telephony.media.Symbol trigger) {
	// Try to send the event to the client side
	this.getDelegate().mediaPlayPause(terminal, index, offset, (int)trigger.hashCode());
}
/**
 * mediaPlayResume method comment.
 */
public void mediaPlayResume(java.lang.String terminal, javax.telephony.media.Symbol trigger) {
	// Try to send the event to the client side
	this.getDelegate().mediaPlayResume(terminal, (int)trigger.hashCode());
}
/**
 * mediaRecorderPause method comment.
 */
public void mediaRecorderPause(java.lang.String terminal, int duration, javax.telephony.media.Symbol trigger) {
	// Try to send the event to the client side
	this.getDelegate().mediaRecorderPause(terminal, duration, (int)trigger.hashCode());
}
/**
 * mediaRecorderResume method comment.
 */
public void mediaRecorderResume(java.lang.String terminal, javax.telephony.media.Symbol trigger) {
	// Try to send the event to the client side
	this.getDelegate().mediaRecorderResume(terminal, (int)trigger.hashCode());
}
/**
 * mediaSignalDetectorDetected method comment.
 */
public void mediaSignalDetectorDetected(java.lang.String terminal, Symbol[] sigs) {
	// Try to send the event to the client side
	this.getDelegate().mediaSDDetected(terminal, CorbaProvider.toLongArray(sigs));
}
/**
 * mediaSignalDetectorOverflow method comment.
 */
public void mediaSignalDetectorOverflow(java.lang.String terminal, javax.telephony.media.Symbol[] sigs) {
	// Try to send the event to the client side
	this.getDelegate().mediaSDOverflow(terminal, CorbaProvider.toLongArray(sigs));
}
/**
 * mediaSignalDetectorPatternMatched method comment.
 */
public void mediaSignalDetectorPatternMatched(java.lang.String terminal, javax.telephony.media.Symbol[] sigs, int index) {
	// Try to send the event to the client side
	this.getDelegate().mediaSDPatternMatched(terminal, CorbaProvider.toLongArray(sigs), index);
}
/**
 * providerPrivateData method comment.
 */
public void providerPrivateData(Serializable data, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().providerPrivateData(CorbaProvider.convertToAny(this.getOrb(), data), cause);
	} catch (NotSerializableException nse) {
		// eat
		System.err.println("Cannot push private data back to client: ");
		nse.printStackTrace(System.err);
	}
}
/**
 * Internal settor for the CorbaListener stub.
 * Creation date: (2000-08-24 13:38:44)
 * @author: Richard Deadman
 * @param newDelegate Corba stub I delegate to.
 */
private void setDelegate(net.sourceforge.gjtapi.raw.remote.corba.CorbaListener newDelegate) {
	delegate = newDelegate;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 15:57:36)
 * @author: Richard Deadman
 * @param newOrb org.omg.CORBA.ORB
 */
private void setOrb(org.omg.CORBA.ORB newOrb) {
	orb = newOrb;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-04-25 0:15:49)
 * @author: 
 * @param newRefMapper net.sourceforge.gjtapi.raw.remote.CallMapper
 */
private void setRefMapper(CallMapper newRefMapper) {
	refMapper = newRefMapper;
}
/**
 * terminalConnectionCreated method comment.
 */
public void terminalConnectionCreated(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	this.getDelegate().terminalConnectionCreated(this.getRefMapper().callToInt(id), address, terminal, cause);
}
/**
 * terminalConnectionDropped method comment.
 */
public void terminalConnectionDropped(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	this.getDelegate().terminalConnectionDropped(this.getRefMapper().callToInt(id), address, terminal, cause);
}
/**
 * terminalConnectionHeld method comment.
 */
public void terminalConnectionHeld(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	this.getDelegate().terminalConnectionHeld(this.getRefMapper().callToInt(id), address, terminal, cause);
}
/**
 * terminalConnectionRinging method comment.
 */
public void terminalConnectionRinging(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	this.getDelegate().terminalConnectionRinging(this.getRefMapper().callToInt(id), address, terminal, cause);
}
/**
 * terminalConnectionTalking method comment.
 */
public void terminalConnectionTalking(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	this.getDelegate().terminalConnectionTalking(this.getRefMapper().callToInt(id), address, terminal, cause);
}
/**
 * terminalPrivateData method comment.
 */
public void terminalPrivateData(String terminal, Serializable data, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().terminalPrivateData(terminal, CorbaProvider.convertToAny(this.getOrb(), data), cause);
	} catch (NotSerializableException nse) {
		// eat
		System.err.println("Cannot push private data back to client: ");
		nse.printStackTrace(System.err);
	}
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "RawObserver wrapper for CorbaListener: " + this.getDelegate().toString();
}
}
