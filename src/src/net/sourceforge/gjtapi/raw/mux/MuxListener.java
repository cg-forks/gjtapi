package net.sourceforge.gjtapi.raw.mux;

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
import java.io.Serializable;
import net.sourceforge.gjtapi.*;
/**
 * This is a simple event interceptor that allows the MuxProvider to be updated with new CallId
 * information mapped to each sub provider.
 * Creation date: (2000-02-22 15:48:52)
 * @author: Richard Deadman
 */
public class MuxListener implements TelephonyListener {
	private net.sourceforge.gjtapi.TelephonyListener real;
	private MuxProvider mux;
	private net.sourceforge.gjtapi.TelephonyProvider sub;
/**
 * Create the MuxObserver
 * Creation date: (2000-02-22 15:51:30)
 * @author: Richard Deadman
 * @param rl The real observer I intercept for
 * @param mp The MuxProvider containing the mapping tables.
 * @param sub The sub RawProvider I receive events from.
 */
MuxListener(TelephonyListener rl, MuxProvider mp, TelephonyProvider sub) {
	super();
	
	this.setReal(rl);
	this.setMux(mp);
	this.setSub(sub);
}
/**
 * addressPrivateData method comment.
 */
public void addressPrivateData(String address, Serializable data, int cause) {
	this.noteAddress(address);
	this.getReal().addressPrivateData(address, data, cause);
}
/**
 * Note CallId mapping and delegate the event on.
 */
public void callActive(CallId id, int cause) {
	this.getReal().callActive(this.morphCall(id), cause);
}
/**
 * One sub-call has died.  Kill this call's legs and see if that any other
 * branches of the call are still alive.
 */
public void callInvalid(CallId id, int cause) {
	CallHolder ch = new CallHolder(id, this.getSub());
	MuxCallId logicalCall = this.getMux().findCall(ch);
	if (logicalCall != null) {
		if (logicalCall.removeCall(ch))		// true if last sub-call
			this.getReal().callInvalid(logicalCall, cause);
			this.getMux().removeCall(logicalCall);
	}
}
/**
 * callOverloadCeased method comment.
 */
public void callOverloadCeased(java.lang.String address) {
	this.noteAddress(address);
	this.getReal().callOverloadCeased(address);
}
/**
 * callOverloadEncountered method comment.
 */
public void callOverloadEncountered(java.lang.String address) {
	this.noteAddress(address);
	this.getReal().callOverloadEncountered(address);
}
/**
 * callPrivateData method comment.
 */
public void callPrivateData(CallId call, Serializable data, int cause) {
	this.getReal().callPrivateData(this.morphCall(call), data, cause);
}
/**
 * connectionAddressAnalyse method comment.
 */
public void connectionAddressAnalyse(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	this.noteAddress(address);
	this.getReal().connectionAddressAnalyse(this.morphCall(id), address, cause);
}
/**
 * connectionAddressCollect method comment.
 */
public void connectionAddressCollect(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	this.noteAddress(address);
	this.getReal().connectionAddressCollect(this.morphCall(id), address, cause);
}
/**
 * Note CallId mapping and delegate the event on.
 */
public void connectionAlerting(CallId id, String address, int cause) {
	this.getReal().connectionAlerting(this.morphConnection(id, address), address, cause);
}
/**
 * connectionAuthorizeCallAttempt method comment.
 */
public void connectionAuthorizeCallAttempt(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	this.noteAddress(address);
	this.getReal().connectionAuthorizeCallAttempt(this.morphCall(id), address, cause);
}
/**
 * connectionCallDelivery method comment.
 */
public void connectionCallDelivery(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	this.noteAddress(address);
	this.getReal().connectionCallDelivery(this.morphCall(id), address, cause);
}
/**
 * Note CallId mapping and delegate the event on.
 */
public void connectionConnected(CallId id, String address, int cause) {
	this.getReal().connectionConnected(this.morphConnection(id, address), address, cause);
}
/**
 * Note CallId mapping and delegate the event on.
 */
public void connectionDisconnected(CallId id, String address, int cause) {
	this.getReal().connectionDisconnected(this.morphConnection(id, address), address, cause);
}
/**
 * Note CallId mapping and delegate the event on.
 */
public void connectionFailed(CallId id, String address, int cause) {
	this.getReal().connectionFailed(this.morphConnection(id, address), address, cause);
}
/**
 * Note CallId mapping and delegate the event on.
 */
public void connectionInProgress(CallId id, String address, int cause) {
	this.getReal().connectionInProgress(this.morphConnection(id, address), address, cause);
}
/**
 * connectionSuspended method comment.
 */
public void connectionSuspended(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	this.noteAddress(address);
	this.getReal().connectionSuspended(this.morphCall(id), address, cause);
}
/**
 * Sees if the two objects hold equal delegates.
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 */
public boolean equals(Object obj) {
	if (obj instanceof MuxListener) {
		MuxListener other = (MuxListener)obj;
		return (this.getReal().equals(other.getReal()) &&
			this.getMux().equals(other.getMux()) &&
			this.getSub().equals(other.getSub()));
	}
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-22 15:53:03)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.mux.MuxProvider
 */
private MuxProvider getMux() {
	return mux;
}
/**
 * Get the real listener I intercept for.
 * Creation date: (2000-02-22 15:49:58)
 * @author: Richard Deadman
 * @return The Framework event listener
 */
private net.sourceforge.gjtapi.TelephonyListener getReal() {
	return real;
}
/**
 * Get the low-level telephony provider I receive events for
 * Creation date: (2000-02-22 15:54:44)
 * @author: Richard Deadman
 * @return A low-level telephony provider
 */
private net.sourceforge.gjtapi.TelephonyProvider getSub() {
	return sub;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getReal().hashCode() + this.getMux().hashCode() + this.getSub().hashCode();
}
/**
 * Delegate on.
 */
public void mediaPlayPause(String terminal, int index, int offset, javax.telephony.media.Symbol trigger) {
	this.getReal().mediaPlayPause(terminal, index, offset, trigger);
}
/**
 * Delegate on.
 */
public void mediaPlayResume(String terminal, javax.telephony.media.Symbol trigger) {
	this.getReal().mediaPlayResume(terminal, trigger);
}
/**
 * Delegate on.
 */
public void mediaRecorderPause(String terminal, int duration, javax.telephony.media.Symbol trigger) {
	this.getReal().mediaRecorderPause(terminal, duration, trigger);
}
/**
 * Delegate on.
 */
public void mediaRecorderResume(String terminal, javax.telephony.media.Symbol trigger) {
	this.getReal().mediaRecorderResume(terminal, trigger);
}
/**
 * Delegate on.
 */
public void mediaSignalDetectorDetected(String terminal, javax.telephony.media.Symbol[] sigs) {
	this.getReal().mediaSignalDetectorDetected(terminal, sigs);
}
/**
 * Delegate on.
 */
public void mediaSignalDetectorOverflow(String terminal, javax.telephony.media.Symbol[] sigs) {
	this.getReal().mediaSignalDetectorOverflow(terminal, sigs);
}
/**
 * Delegate on.
 */
public void mediaSignalDetectorPatternMatched(String terminal, javax.telephony.media.Symbol[] sigs, int index) {
	this.getReal().mediaSignalDetectorPatternMatched(terminal, sigs, index);
}
/**
 * Tell the MuxProvider about a CallId reference from a sub-provider.
 * Creation date: (2000-04-25 0:38:22)
 * @author: Richard Deadman
 * @param id A handle to a call id to be mapped to my sub-provider.
 * @return The MuxCallId that wraps the low-level CallIds in possibley bridged call.s
 */
private MuxCallId morphCall(CallId id) {
	return this.getMux().locateCall(id, this.getSub());
}
/**
 * Tell the MuxProvider about a CallId and Address reference from a sub-provider.
 * This is useful if the address set is not statically returned during initialization.
 * Creation date: (2000-06-06 0:38:22)
 * @author: Richard Deadman
 * @param id A handle to a call id to be mapped to my sub-provider.
 * @param addr A reference to an Address
 */
private MuxCallId morphConnection(CallId id, String addr) {
	this.getMux().noteAddress(addr, this.getSub());
	this.noteAddress(addr);
	return this.morphCall(id);
}
/**
 * Tell the MuxProvider about a CallId, Address and Terminal reference from a sub-provider.
 * This is useful if the address set is not statically returned during initialization.
 * Creation date: (2000-06-06 0:38:22)
 * @author: Richard Deadman
 * @param id A handle to a call id to be mapped to my sub-provider.
 * @param addr A reference to an Address
 * @param term A reference to a Terminal
 */
private MuxCallId morphTermConn(CallId id, String addr, String term) {
	this.getMux().noteTerminal(term, this.getSub());
	this.noteTerminal(term);
	return this.morphConnection(id, addr);
}
/**
 * Tell the MuxProvider about an Address name from a sub-provider.
 * Creation date: (2000-08-09 0:38:22)
 * @author: Richard Deadman
 * @param id A name of a sub-provider's Address
 */
private void noteAddress(String addressName) {
	this.getMux().noteAddress(addressName, this.getSub());
}
/**
 * Tell the MuxProvider about a Terminal name from a sub-provider.
 * Creation date: (2000-08-09 0:38:22)
 * @author: Richard Deadman
 * @param id A name of a sub-provider's Terminal
 */
private void noteTerminal(String terminalName) {
	this.getMux().noteTerminal(terminalName, this.getSub());
}
/**
 * providerPrivateData method comment.
 */
public void providerPrivateData(Serializable data, int cause) {
	this.getReal().providerPrivateData(data, cause);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-22 15:53:03)
 * @author: 
 * @param newMux net.sourceforge.gjtapi.raw.mux.MuxProvider
 */
private void setMux(MuxProvider newMux) {
	mux = newMux;
}
/**
 * Set the real listener that I intercept for.
 * Creation date: (2000-02-22 15:49:58)
 * @author: Richard Deadman
 * @param newReal The Framework listener.
 */
private void setReal(net.sourceforge.gjtapi.TelephonyListener newReal) {
	real = newReal;
}
/**
 * Set the low-level provider I listen for events on.
 * Creation date: (2000-02-22 15:54:44)
 * @author: Richard Deadman
 * @param newSub net.sourceforge.gjtapi.TelephonyProvider
 */
private void setSub(net.sourceforge.gjtapi.TelephonyProvider newSub) {
	sub = newSub;
}
/**
 * Note CallId and delegate on.
 */
public void terminalConnectionCreated(CallId id, String address, String terminal, int cause) {
	this.getReal().terminalConnectionCreated(this.morphTermConn(id, address, terminal), address, terminal, cause);
}
/**
 * Note CallId and delegate on.
 */
public void terminalConnectionDropped(CallId id, String address, String terminal, int cause) {
	this.getReal().terminalConnectionDropped(this.morphTermConn(id, address, terminal), address, terminal, cause);
}
/**
 * Note CallId and delegate on.
 */
public void terminalConnectionHeld(CallId id, String address, String terminal, int cause) {
	this.getReal().terminalConnectionHeld(this.morphTermConn(id, address, terminal), address, terminal, cause);
}
/**
 * Note CallId and delegate on.
 */
public void terminalConnectionRinging(CallId id, String address, String terminal, int cause) {
	this.getReal().terminalConnectionRinging(this.morphTermConn(id, address, terminal), address, terminal, cause);
}
/**
 * Note CallId and delegate on.
 */
public void terminalConnectionTalking(CallId id, String address, String terminal, int cause) {
	this.getReal().terminalConnectionTalking(this.morphTermConn(id, address, terminal), address, terminal, cause);
}
/**
 * terminalPrivateData method comment.
 */
public void terminalPrivateData(String terminal, Serializable data, int cause) {
	this.noteTerminal(terminal);
	this.getReal().terminalPrivateData(terminal, data, cause);
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Mux observer for the real observer: " + this.getReal();
}
}
