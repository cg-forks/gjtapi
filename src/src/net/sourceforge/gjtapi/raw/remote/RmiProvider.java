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
import java.io.*;
import javax.telephony.media.*;
import net.sourceforge.gjtapi.media.*;
import javax.telephony.*;
import java.rmi.RemoteException;
import net.sourceforge.gjtapi.*;
import java.util.*;
/**
 * This is a pluggable provider that provides access to a remote provider through an RMI pipe
 * Creation date: (2000-02-10 10:53:56)
 * @author: Richard Deadman
 */
public class RmiProvider implements TelephonyProvider {
	private RemoteProvider remote;
/**
 * Forward to remote provider
 */
public void addListener(TelephonyListener rl) {
	try {
		this.getRemote().addListener(new RemoteListenerImpl(rl));
	} catch (RemoteException re) {
		throw new PlatformException("Error creating Remote Observer...");
	}
}
/**
 * Forward to a remote stub
 */
public boolean allocateMedia(String terminal, int type, Dictionary params) {
	try {
		return this.getRemote().allocateMedia(terminal, type, this.toSerializable(params));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Forward answerCall to remote provider
 */
public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
	  MethodNotSupportedException, RawStateException {
	try {
		this.getRemote().answerCall((SerializableCallId)call, address, terminal);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * attachMedia method comment.
 */
public boolean attachMedia(net.sourceforge.gjtapi.CallId call, java.lang.String address, boolean onFlag) {
	try {
		return this.getRemote().attachMedia((SerializableCallId)call, address, onFlag);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * beep method comment.
 */
public void beep(net.sourceforge.gjtapi.CallId call) {
	try {
		this.getRemote().beep((SerializableCallId)call);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Create a call from the given address and terminal to the remote address
 */
public CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException,
	  InvalidPartyException, InvalidArgumentException, RawStateException,
	  MethodNotSupportedException {
	try {
		return this.getRemote().createCall((SerializableCallId)id, address, term, dest);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Delegate to remote stub
 */
public boolean freeMedia(java.lang.String terminal, int type) {
	try {
		return this.getRemote().freeMedia(terminal, type);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Get a set or addresses for a remote provider.
 */
public java.lang.String[] getAddresses() throws ResourceUnavailableException {
	try {
		return this.getRemote().getAddresses();
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Return a set of address names from the remote provider.
 */
public String[] getAddresses(String term) throws InvalidArgumentException {
	try {
		return this.getRemote().getAddresses(term);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * getAddressType method comment.
 */
public int getAddressType(java.lang.String name) {
	try {
		return this.getRemote().getAddressType(name);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Ask the raw TelephonyProvider to give a snapshot of the indicated Call.
 * <P>This will only be called on a TelephonyProvider that "trottle"s call events.
 * <P><B>Note:</B> This implies that the given Call will have events delivered on it until such time
 * as a "TelephonyProvider::releaseCallId(CallId)".
 * Creation date: (2000-06-20 15:22:50)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.CallData
 * @param id net.sourceforge.gjtapi.CallId
 */
public CallData getCall(CallId id) {
	try {
		return this.getRemote().getCall(id);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Ask the raw TelephonyProvider to give a snapshot of all Calls on an Address.
 * <P>This will only be called on a TelephonyProvider that "trottle"s call events.
 * <P><B>Note:</B> This implies that the given Call will have events delivered on it until such time
 * as a "TelephonyProvider::releaseCallId(CallId)".
 * Creation date: (2000-06-20 15:22:50)
 * @author: Richard Deadman
 * @return A set of call data.
 * @param number The Address's logical number
 */
public CallData[] getCallsOnAddress(String number) {
	try {
		return this.getRemote().getCallsOnAddress(number);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Ask the raw TelephonyProvider to give a snapshot of all Calls at a Terminal.
 * <P>This will only be called on a TelephonyProvider that "trottle"s call events.
 * <P><B>Note:</B> This implies that the given Calls will have events delivered on it until such time
 * as a "TelephonyProvider::releaseCallId(CallId)".
 * Creation date: (2000-06-20 15:22:50)
 * @author: Richard Deadman
 * @return A set of call data.
 * @param term The Terminal's logical name
 */
public CallData[] getCallsOnTerminal(String term) {
	try {
		return this.getRemote().getCallsOnTerminal(term);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * getCapabilities method comment.
 */
public java.util.Properties getCapabilities() {
	try {
		return this.getRemote().getCapabilities();
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * getDialledDigits method comment.
 */
public java.lang.String getDialledDigits(net.sourceforge.gjtapi.CallId id, java.lang.String address) {
	try {
		return this.getRemote().getDialledDigits((SerializableCallId)id, address);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Call getPrivateData on remote interface.
 */
public Object getPrivateData(CallId call, String address, String terminal) {
	try {
		return this.getRemote().getPrivateData(call, address, terminal);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (NotSerializableException nse) {
		throw new PlatformException("sendPrivateData result not serializable through remote proxy");
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-17 14:24:48)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.remote.RemoteProvider
 */
private RemoteProvider getRemote() {
	return remote;
}
/**
 * Get a list of available terminals.
 * This may be null if the Telephony (raw) Provider does not support Terminals.
 * If the Terminal set it too large, this will throw a ResourceUnavailableException
 * <P>Since we went to lazy connecting between Addresses and Terminals, this is called so
 * we don't have to follow all Address->Terminal associations to get the full set of Terminals.
 * Creation date: (2000-02-11 12:29:00)
 * @author: Richard Deadman
 * @return An array of terminal names, media type containers.
 * @exception ResourceUnavailableException if the set it too large to be returned dynamically.
 */
public TermData[] getTerminals() throws ResourceUnavailableException {
	try {
		return this.getRemote().getTerminals();
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Return a set of terminal names from the remote provider.
 */
public TermData[] getTerminals(String address) throws InvalidArgumentException {
	try {
		return this.getRemote().getTerminals(address);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Send a hold message for a terminal to a remote provider.
 */
public void hold(CallId call, String term, String address) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	try {
		this.getRemote().hold((SerializableCallId)call, term, address);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Initialize my connection to the remote provider.
 * These properties could be used locally or sent to the server for the creation of a user-session.
 * For now, don't send.
 */
public void initialize(java.util.Map props) throws ProviderUnavailableException {
	// Try to find this server
	try {
		this.setRemote((RemoteProvider)java.rmi.registry.LocateRegistry.getRegistry((String)props.get("server")).lookup((String)props.get("name")));
	} catch (RemoteException rex) {
		throw new PlatformException("Error initializing server");
	} catch (Exception ex) {
		throw new ProviderUnavailableException(ex.getMessage());
	}
}
/**
 * Delegate to the remote stub
 */
public boolean isMediaTerminal(java.lang.String terminal) {
	try {
		return this.getRemote().isMediaTerminal(terminal);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Tell the remote provider to join two calls
 */
public CallId join(CallId call1, CallId call2, String address, String terminal) throws RawStateException, InvalidArgumentException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	try {
		return this.getRemote().join((SerializableCallId)call1,
				(SerializableCallId)call2,
				address,
				terminal);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Put RTCs into serializable holder and send to remote stub
 */
public void play(String terminal,
	String[] streamIds,
	int offset,
	RTC[] rtcs,
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTCHolder[] rtcHolders = new RTCHolder[rtcs.length];
	for (int i = 0; i < rtcs.length; i++) {
		rtcHolders[i] = new RTCHolder(rtcs[i]);
	}
	try {
		this.getRemote().play(terminal, streamIds, offset, rtcHolders, this.toSerializable(optArgs));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Put RTCs into serializable holder and send to remote stub
 */
public void record(String terminal,
	String streamId,
	RTC[] rtcs,
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTCHolder[] rtcHolders = new RTCHolder[rtcs.length];
	for (int i = 0; i < rtcs.length; i++) {
		rtcHolders[i] = new RTCHolder(rtcs[i]);
	}
	try {
		this.getRemote().record(terminal, streamId, rtcHolders, this.toSerializable(optArgs));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Tell the remote provider to release an address from a call.
 */
public void release(String address, CallId call) throws PrivilegeViolationException,
	ResourceUnavailableException, MethodNotSupportedException, RawStateException {
	try {
		this.getRemote().release(address, (SerializableCallId)call);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Release any CallId's that I have reserved.
 */
public void releaseCallId(CallId id) {
	try {
		this.getRemote().releaseCallId((SerializableCallId)id);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Forward removeListener to remote provider.
 */
public void removeListener(TelephonyListener rl) {
	try {
		this.getRemote().removeListener(new RemoteListenerImpl(rl));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Forward to remote stub
 */
public void reportCallsOnAddress(String address, boolean flag) throws InvalidArgumentException, ResourceUnavailableException {
	try {
		this.getRemote().reportCallsOnAddress(address, flag);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Forward to remote stub
 */
public void reportCallsOnTerminal(String terminal, boolean flag) throws InvalidArgumentException, ResourceUnavailableException {
	try {
		this.getRemote().reportCallsOnTerminal(terminal, flag);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Reserve a call id on the remote server.
 */
public CallId reserveCallId(String address) throws InvalidArgumentException {
	try {
		return this.getRemote().reserveCallId(address);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Put RTCs into serializable holder and send to remote stub
 */
public RawSigDetectEvent retrieveSignals(String terminal,
	int num,
	Symbol[] patterns,
	RTC[] rtcs,
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTCHolder[] rtcHolders = new RTCHolder[rtcs.length];
	for (int i = 0; i < rtcs.length; i++) {
		rtcHolders[i] = new RTCHolder(rtcs[i]);
	}
	SymbolHolder[] patHolders = new SymbolHolder[patterns.length];
	for (int i = 0; i < patterns.length; i++) {
		patHolders[i] = new SymbolHolder(patterns[i]);
	}
	try {
		return this.getRemote().retrieveSignals(terminal, num, patHolders, rtcHolders, this.toSerializable(optArgs));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Trigger sendPrivateData against the remote interface.
 */
public Object sendPrivateData(CallId call, String address, String terminal, Object data) {
	if (!(data instanceof Serializable))
		throw new PlatformException("sendPrivateData data is not serializable through remote proxy");

	try {
		return this.getRemote().sendPrivateData(call, address, terminal, (Serializable)data);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (NotSerializableException nse) {
		throw new PlatformException("sendPrivateData result not serializable through remote proxy");
	}
}
/**
 * Put RTCs and Symbols into serializable holder and send to remote stub
 */
public void sendSignals(String terminal,
	Symbol[] syms,
	RTC[] rtcs,
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTCHolder[] rtcHolders = new RTCHolder[rtcs.length];
	for (int i = 0; i < rtcs.length; i++) {
		rtcHolders[i] = new RTCHolder(rtcs[i]);
	}
	SymbolHolder[] symHolders = new SymbolHolder[syms.length];
	for (int i = 0; i < syms.length; i++) {
		symHolders[i] = new SymbolHolder(syms[i]);
	}
	try {
		this.getRemote().sendSignals(terminal, symHolders, rtcHolders, this.toSerializable(optArgs));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * setLoadControl method comment.
 */
public void setLoadControl(java.lang.String startAddr, java.lang.String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws javax.telephony.MethodNotSupportedException {
	try {
		this.getRemote().setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatment);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Send setPrivateData through remote proxy.
 */
public void setPrivateData(CallId call, String address, String terminal, Object data) {
	if (!(data instanceof Serializable))
		throw new PlatformException("setPrivateData data is not serializable through remote proxy");

	try {
		this.getRemote().setPrivateData(call, address, terminal, (Serializable)data);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-17 14:24:48)
 * @author: 
 * @param newRemote net.sourceforge.gjtapi.raw.remote.RemoteProvider
 */
private void setRemote(RemoteProvider newRemote) {
	remote = newRemote;
}
/**
 * Tell the remote provider to shutdown.  It may choose to ignore me.
 */
public void shutdown() {
	try {
		this.getRemote().shutdown();
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Stop any media actions on the remote terminal.
 */
public void stop(String terminal) {
	try {
		this.getRemote().stop(terminal);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Forward to remote stub
 */
public boolean stopReportingCall(CallId call) {
	try {
		return this.getRemote().stopReportingCall((SerializableCallId)call);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Replace each Symbol key or value with a SymbolHolder so that it can be send across a wire.
 * Creation date: (2000-03-13 9:34:39)
 * @author: Richard Deadman
 * @return A clone that is serializable
 * @param dict A dictionary of parameters and values that control the usage of the resource.  This should move to a Map later.
 */
private Dictionary toSerializable(Dictionary dict) {
	if (dict == null)
		return null;
	Hashtable table = new Hashtable();
	Enumeration keys = dict.keys();
	while (keys.hasMoreElements()) {
		Object k = keys.nextElement();
		Object v = dict.get(k);
		if (k instanceof Symbol)
			k = new SymbolHolder((Symbol)k);
		if (v instanceof Symbol)
			v = new SymbolHolder((Symbol)v);
		table.put(k, v);
	}
	return table;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Client proxy for a remote provider";
}
/**
 * Trigger a media runtime control (RTC) action on a remote terminal.
 */
public void triggerRTC(String terminal, javax.telephony.media.Symbol action) {
	try {
		this.getRemote().triggerRTC(terminal, new SymbolHolder(action));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Tell the remote provider to unhold a terminal from a call
 */
public void unHold(CallId call, String address, String term) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	try {
		this.getRemote().unHold((SerializableCallId)call, address, term);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
}
