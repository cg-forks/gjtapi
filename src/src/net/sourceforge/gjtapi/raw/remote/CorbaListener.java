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
import java.io.Serializable;
import net.sourceforge.gjtapi.*;
import javax.telephony.media.Symbol;
/**
 * This is a CORBA asynchronous callback object.
 * Creation date: (2000-08-18 14:32:01)
 * @author: Richard Deadman
 */
public class CorbaListener extends net.sourceforge.gjtapi.raw.remote.corba._CorbaListenerImplBase {
	static final long serialVersionUID = -2688204344061941227L;
	
	  private TelephonyListener real;
/**
 * Create a CorbaListener that delegates off to the Generic JTAPI TelephonyListener.
 * Creation date: (2000-08-18 15:01:33)
 * @author: Richard Deadman
 * @param tl A TelephonyListener to delegate calls to.
 */
protected CorbaListener(TelephonyListener tl) {
	super();
	
	this.setReal(tl);
}
/**
 * addressPrivateData method comment.
 */
public void addressPrivateData(java.lang.String address, org.omg.CORBA.Any data, int cause) {
	this.getReal().addressPrivateData(address, (Serializable)CorbaProvider.convertAny(data), cause);
}
/**
 * callActive method comment.
 */
public void callActive(int callId, int cause) {
	this.getReal().callActive(new SerializableCallId(callId), cause);
}
/**
 * callInvalid method comment.
 */
public void callInvalid(int callId, int cause) {
	this.getReal().callInvalid(new SerializableCallId(callId), cause);
}
/**
 * callOverloadCeased method comment.
 */
public void callOverloadCeased(java.lang.String address) {
	this.getReal().callOverloadCeased(address);
}
/**
 * callOverloadEncountered method comment.
 */
public void callOverloadEncountered(java.lang.String address) {
	this.getReal().callOverloadEncountered(address);
}
/**
 * callPrivateData method comment.
 */
public void callPrivateData(int callId, org.omg.CORBA.Any data, int cause) {
	this.getReal().callPrivateData(new SerializableCallId(callId), (Serializable)CorbaProvider.convertAny(data), cause);
}
/**
 * connectionAddressAnalyse method comment.
 */
public void connectionAddressAnalyse(int callId, String address, int cause) {
	this.getReal().connectionAddressAnalyse(new SerializableCallId(callId), address, cause);
}
/**
 * connectionAddressCollect method comment.
 */
public void connectionAddressCollect(int callId, java.lang.String address, int cause) {
	this.getReal().connectionAddressCollect(new SerializableCallId(callId), address, cause);
}
/**
 * connectionAlerting method comment.
 */
public void connectionAlerting(int callId, String address, int cause) {
	this.getReal().connectionAlerting(new SerializableCallId(callId), address,  cause);
}
/**
 * connectionAuthorizeCallAttempt method comment.
 */
public void connectionAuthorizeCallAttempt(int callId, java.lang.String address, int cause) {
	this.getReal().connectionAuthorizeCallAttempt(new SerializableCallId(callId), address, cause);
}
/**
 * connectionCallDelivery method comment.
 */
public void connectionCallDelivery(int callId, java.lang.String address, int cause) {
	this.getReal().connectionCallDelivery(new SerializableCallId(callId), address, cause);
}
/**
 * connectionConnected method comment.
 */
public void connectionConnected(int callId, java.lang.String address, int cause) {
	this.getReal().connectionConnected(new SerializableCallId(callId), address,  cause);
}
/**
 * connectionDisconnected method comment.
 */
public void connectionDisconnected(int callId, java.lang.String address, int cause) {
	this.getReal().connectionDisconnected(new SerializableCallId(callId), address,  cause);
}
/**
 * connectionFailed method comment.
 */
public void connectionFailed(int callId, java.lang.String address, int cause) {
	this.getReal().connectionFailed(new SerializableCallId(callId), address,  cause);
}
/**
 * connectionInProgress method comment.
 */
public void connectionInProgress(int callId, java.lang.String address, int cause) {
	this.getReal().connectionInProgress(new SerializableCallId(callId), address,  cause);
}
/**
 * connectionSuspended method comment.
 */
public void connectionSuspended(int callId, java.lang.String address, int cause) {
	this.getReal().connectionSuspended(new SerializableCallId(callId), address, cause);
}
/**
 * Internal accessor.
 * Creation date: (2000-08-18 15:02:49)
 * @author: Richard Deadman
 * @return The Generic JTAPI Framework event listener to delegate events to.
 */
private net.sourceforge.gjtapi.TelephonyListener getReal() {
	return real;
}
/**
 * mediaPlayPause method comment.
 */
public void mediaPlayPause(String terminal, int index, int offset, int trigger) {
	this.getReal().mediaPlayPause(terminal, index, offset, Symbol.getSymbol(trigger));
}
/**
 * mediaPlayResume method comment.
 */
public void mediaPlayResume(String terminal, int trigger) {
	this.getReal().mediaPlayResume(terminal, Symbol.getSymbol(trigger));
}
/**
 * mediaRecorderPause method comment.
 */
public void mediaRecorderPause(String terminal, int duration, int trigger) {
	this.getReal().mediaRecorderPause(terminal, duration, Symbol.getSymbol(trigger));
}
/**
 * mediaRecorderResume method comment.
 */
public void mediaRecorderResume(String terminal, int trigger) {
	this.getReal().mediaRecorderResume(terminal, Symbol.getSymbol(trigger));
}
/**
 * mediaSDDetected method comment.
 */
public void mediaSDDetected(String terminal, int[] sigs) {
	this.getReal().mediaSignalDetectorDetected(terminal, CorbaProvider.toSymbolArray(sigs));
}
/**
 * mediaSDOverflow method comment.
 */
public void mediaSDOverflow(String terminal, int[] sigs) {
	this.getReal().mediaSignalDetectorOverflow(terminal, CorbaProvider.toSymbolArray(sigs));
}
/**
 * mediaSDPatternMatched method comment.
 */
public void mediaSDPatternMatched(String terminal, int[] sigs, int index) {
	this.getReal().mediaSignalDetectorPatternMatched(terminal, CorbaProvider.toSymbolArray(sigs), index);
}
/**
 * providerPrivateData method comment.
 */
public void providerPrivateData(org.omg.CORBA.Any data, int cause) {
	this.getReal().providerPrivateData((Serializable)CorbaProvider.convertAny(data), cause);
}
/**
 * Internal settor.
 * Creation date: (2000-08-18 15:02:49)
 * @author: Richard Deadman
 * @param newReal The Generic JTAPI Framework event listener to delegate events to.
 */
private void setReal(net.sourceforge.gjtapi.TelephonyListener newReal) {
	real = newReal;
}
/**
 * terminalConnectionCreated method comment.
 */
public void terminalConnectionCreated(int callId, String address, String terminal, int cause) {
	this.getReal().terminalConnectionCreated(new SerializableCallId(callId), address, terminal, cause);
}
/**
 * terminalConnectionDropped method comment.
 */
public void terminalConnectionDropped(int callId, java.lang.String address, java.lang.String terminal, int cause) {
	this.getReal().terminalConnectionDropped(new SerializableCallId(callId), address, terminal, cause);
}
/**
 * terminalConnectionHeld method comment.
 */
public void terminalConnectionHeld(int callId, java.lang.String address, java.lang.String terminal, int cause) {
	this.getReal().terminalConnectionHeld(new SerializableCallId(callId), address, terminal, cause);
}
/**
 * terminalConnectionRinging method comment.
 */
public void terminalConnectionRinging(int callId, java.lang.String address, java.lang.String terminal, int cause) {
	this.getReal().terminalConnectionRinging(new SerializableCallId(callId), address, terminal, cause);
}
/**
 * terminalConnectionTalking method comment.
 */
public void terminalConnectionTalking(int callId, String address, String terminal, int cause) {
	this.getReal().terminalConnectionTalking(new SerializableCallId(callId), address, terminal, cause);
}
/**
 * terminalPrivateData method comment.
 */
public void terminalPrivateData(String terminal, org.omg.CORBA.Any data, int cause) {
	this.getReal().terminalPrivateData(terminal, (Serializable)CorbaProvider.convertAny(data), cause);
}
}
