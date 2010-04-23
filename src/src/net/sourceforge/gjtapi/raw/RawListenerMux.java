package net.sourceforge.gjtapi.raw;

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
import java.util.*;
/**
 * This is a simple RawListener broadcaster that allows multiple remote RawListeners to appear as one
 * to the RawProvider.  This simplifies RawProvider construction since they now only have to worry about
 * holding onto one RawListener, not a set.
 * Creation date: (2000-04-26 15:43:02)
 * @author: Richard Deadman
 */
public class RawListenerMux implements TelephonyListener {
	private Set<TelephonyListener> listeners = new HashSet<TelephonyListener>();
/**
 * Add to the set of Raw Listeners I broadcast to.
 * Creation date: (2000-04-26 16:01:55)
 * @author: Richard Deadman
 * @param l A new TelphonyListener to receive callbacks
 */
public void addListener(TelephonyListener l) {
	this.getListeners().add(l);
}
/**
 * addressPrivateData method comment.
 */
public void addressPrivateData(String address, Serializable data, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().addressPrivateData(address, data, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void callActive(CallId id, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().callActive(id, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void callInvalid(CallId id, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().callInvalid(id, cause);
	}
}
/**
 * callOverloadCeased method comment.
 */
public void callOverloadCeased(java.lang.String address) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().callOverloadCeased(address);
	}
}
/**
 * callOverloadEncountered method comment.
 */
public void callOverloadEncountered(java.lang.String address) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().callOverloadEncountered(address);
	}
}
/**
 * callPrivateData method comment.
 */
public void callPrivateData(CallId call, Serializable data, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().callPrivateData(call, data, cause);
	}
}
/**
 * connectionAddressAnalyse method comment.
 */
public void connectionAddressAnalyse(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionAddressAnalyse(id, address, cause);
	}
}
/**
 * connectionAddressCollect method comment.
 */
public void connectionAddressCollect(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionAddressCollect(id, address, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void connectionAlerting(CallId id, String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionAlerting(id, address, cause);
	}
}
/**
 * connectionAuthorizeCallAttempt method comment.
 */
public void connectionAuthorizeCallAttempt(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionAuthorizeCallAttempt(id, address, cause);
	}
}
/**
 * connectionCallDelivery method comment.
 */
public void connectionCallDelivery(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionCallDelivery(id, address, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void connectionConnected(CallId id, String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionConnected(id, address, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void connectionDisconnected(CallId id, String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionDisconnected(id, address, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void connectionFailed(CallId id, String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionFailed(id, address, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void connectionInProgress(CallId id, String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionInProgress(id, address, cause);
	}
}
/**
 * connectionSuspended method comment.
 */
public void connectionSuspended(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().connectionSuspended(id, address, cause);
	}
}
/**
 * Internal accessor for the real listeners.
 */
private Set<TelephonyListener> getListeners() {
	return this.listeners;
}
/**
 * Broadcast off to all listeners.
 */
public void mediaPlayPause(String terminal, int index, int offset, javax.telephony.media.Symbol trigger) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().mediaPlayPause(terminal, index, offset, trigger);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void mediaPlayResume(String terminal, javax.telephony.media.Symbol trigger) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().mediaPlayResume(terminal, trigger);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void mediaRecorderPause(String terminal, int duration, javax.telephony.media.Symbol trigger) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().mediaRecorderPause(terminal, duration, trigger);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void mediaRecorderResume(String terminal, javax.telephony.media.Symbol trigger) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().mediaRecorderResume(terminal, trigger);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void mediaSignalDetectorDetected(String terminal, javax.telephony.media.Symbol[] sigs) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().mediaSignalDetectorDetected(terminal, sigs);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void mediaSignalDetectorOverflow(String terminal, javax.telephony.media.Symbol[] sigs) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().mediaSignalDetectorOverflow(terminal, sigs);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void mediaSignalDetectorPatternMatched(String terminal, javax.telephony.media.Symbol[] sigs, int index) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().mediaSignalDetectorPatternMatched(terminal, sigs, index);
	}
}
/**
 * providerPrivateData method comment.
 */
public void providerPrivateData(Serializable data, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().providerPrivateData(data, cause);
	}
}
/**
 * Remove a Raw Listener from the set I broadcast to.
 * Creation date: (2000-04-26 16:01:55)
 * @author: Richard Deadman
 * @param l A new RawListener to remove
 */
public void removeListener(TelephonyListener l) {
	this.getListeners().remove(l);
}
/**
 * Broadcast off to all listeners.
 */
public void terminalConnectionCreated(CallId id, String address, String terminal, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().terminalConnectionCreated(id, address, terminal, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void terminalConnectionDropped(CallId id, String address, String terminal, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().terminalConnectionDropped(id, address, terminal, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void terminalConnectionHeld(CallId id, String address, String terminal, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().terminalConnectionHeld(id, address, terminal, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void terminalConnectionRinging(CallId id, String address, String terminal, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().terminalConnectionRinging(id, address, terminal, cause);
	}
}
/**
 * Broadcast off to all listeners.
 */
public void terminalConnectionTalking(CallId id, String address, String terminal, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().terminalConnectionTalking(id, address, terminal, cause);
	}
}
/**
 * terminalPrivateData method comment.
 */
public void terminalPrivateData(String terminal, Serializable data, int cause) {
	Iterator<TelephonyListener> it = this.getListeners().iterator();
	while (it.hasNext()) {
		it.next().terminalPrivateData(terminal, data, cause);
	}
}
}
