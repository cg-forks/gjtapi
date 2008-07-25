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
import net.sourceforge.gjtapi.media.*;
import javax.telephony.media.*;
import javax.telephony.*;
import java.rmi.RemoteException;
import java.rmi.server.*;
import java.util.*;
import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.raw.*;
/**
 * This is a server-side implementation for a RemoteProvider.  It finishes the job off of
 * delegating requests from the client to a real RawProvider.
 * Creation date: (2000-02-17 13:07:36)
 * @author: Richard Deadman
 */
public class RemoteProviderImpl extends UnicastRemoteObject implements Unreferenced, RemoteProvider {
	static final long serialVersionUID = 4108831180888441168L;
	
	private TelephonyProvider delegate;
	private CallMapper refMapper = new CallMapper();
	private RawListenerMux callbackMux = new RawListenerMux();
/**
 * Create a new instance of the remote service, wrapping a RawProvider.
 * Creation date: (2000-02-17 13:32:03)
 * @author: Richard Deadman
 * @param rp net.sourceforge.gjtapi.RawProvider
 * @exception RawException The provider was null.
 * @exception RemoteException Error creating remote object.
 */
public RemoteProviderImpl(TelephonyProvider rp) throws RemoteException {
	super();
	
	if (rp == null)
		throw new NullPointerException();
	else
		this.setDelegate(rp);

	// register my listener (wrappedd in a event dispatch pool)
	rp.addListener(new RawListenerPool(this.getCallbackMux()));
}
/**
 * Delegate the add observer command off to the real provider.
 */
public void addListener(RemoteListener rl)  {
	this.getCallbackMux().addListener(new RemoteListenerWrapper(rl, this.getRefMapper()));
}
/**
 * Forward the request on to the local RawProvider
 */
public boolean allocateMedia(String terminal, int type, Dictionary params) {
	return this.getDelegate().allocateMedia(terminal, type, this.fromSerializable(params));
}
/**
 * Delegate off to remote provider
 */
public void answerCall(SerializableCallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
	  MethodNotSupportedException, RawStateException {

	this.getDelegate().answerCall(this.getProvCall(call), address, terminal);
}
/**
 * attachMedia method comment.
 */
public boolean attachMedia(SerializableCallId call, java.lang.String address, boolean onFlag) throws java.rmi.RemoteException {

	return this.getDelegate().attachMedia(this.getProvCall(call), address, onFlag);
}
/**
 * beep method comment.
 */
public void beep(SerializableCallId call) throws java.rmi.RemoteException {

	this.getDelegate().beep(this.getProvCall(call));
}
/**
 * delegate on createCall
 */
public SerializableCallId createCall(SerializableCallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException,
	  InvalidPartyException, InvalidArgumentException, MethodNotSupportedException,
	  RawStateException {
	TelephonyProvider rp = this.getDelegate();
	
	return this.getRefMapper().swapId(rp.createCall(this.getProvCall(id), address, term, dest));
}
/**
 * Clean up
 * Creation date: (2000-04-26 16:09:25)
 * @author: Richard Deadman
 */
public void finalize() {
	this.unreferenced();
}
/**
 * Forward the request on to the local RawProvider
 */
public boolean freeMedia(String terminal, int type) {
	return this.getDelegate().freeMedia(terminal, type);
}
/**
 * Replace each serializable SymbolHolder key or value with its held Symbol.
 * Creation date: (2000-03-13 9:34:39)
 * @author: Richard Deadman
 * @return A clone that now holds non-serializable Symbols again.
 * @param dict A dictionary of parameters and values that control the usage of the resource.  This should move to a Map later.
 */
private Dictionary fromSerializable(Dictionary dict) {
	if (dict == null)
		return null;
	Hashtable table = new Hashtable();
	Enumeration keys = dict.keys();
	while (keys.hasMoreElements()) {
		Object k = keys.nextElement();
		Object v = dict.get(k);
		if (k instanceof SymbolHolder)
			k = ((SymbolHolder)k).getSymbol();
		if (v instanceof SymbolHolder)
			v = ((SymbolHolder)v).getSymbol();
		table.put(k, v);
	}
	return table;
}
/**
 * Delegate remote getAddresses to real provider.
 */
public String[] getAddresses() throws RemoteException, ResourceUnavailableException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		return rp.getAddresses();
	else
		throw new RemoteException();
}
/**
 * Delegate remote getTerminals method to real provider.
 */
public String[] getAddresses(String terminal) throws RemoteException, InvalidArgumentException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		return rp.getAddresses(terminal);
	else
		throw new RemoteException();
}
/**
 * getAddressType method comment.
 */
public int getAddressType(java.lang.String name) throws java.rmi.RemoteException {

	return this.getDelegate().getAddressType(name);
}
/**
 * Delegate off to real TelephonyProvider.
 */
public CallData getCall(CallId id) throws RemoteException  {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null) {
		CallData cd = rp.getCall(id);
		return new CallData(this.getRefMapper().swapId(cd.id), cd.callState, cd.connections);
	}else
		throw new RemoteException();
}
/**
 * Return the Listener that handles the multiplexing of events to multiple remote clients.
 * Creation date: (2000-04-26 16:07:39)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.remote.RawListenerMux
 */
private RawListenerMux getCallbackMux() {
	return callbackMux;
}
/**
 * getCallsOnAddress method comment.
 */
public net.sourceforge.gjtapi.CallData[] getCallsOnAddress(java.lang.String number) throws java.rmi.RemoteException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		return this.toSerialCallData(rp.getCallsOnAddress(number));
	else
		throw new RemoteException();
}
/**
 * Delegate on to real TelephonyProvider.
 */
public CallData[] getCallsOnTerminal(String name) throws RemoteException  {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null) {
		return this.toSerialCallData(rp.getCallsOnTerminal(name));
	} else
		throw new RemoteException();
}

/**
 * Helper method to turn an array of CallData objects into a Serialized version.
 * @param cds The CallData objects to make serializable
 * @return CallData[] A clone with the internal CallIds replaced with their SerializableCallIds.
 */
private CallData[] toSerialCallData(CallData[] cds) {
	if (cds == null)
		return null;
	int len = cds.length;
	CallData[] newCds = new CallData[len];
	for (int i = 0; i < len; i++) {
		CallData cd = cds[i];
		newCds[i] = new CallData(this.getRefMapper().swapId(cd.id), cd.callState, cd.connections);
	}
	return newCds;
}

/**
 * getCapabilities method comment.
 */
public java.util.Properties getCapabilities() throws java.rmi.RemoteException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		return rp.getCapabilities();
	else
		throw new RemoteException();
}
/**
 * Get the delegate that actually performs the TelephonyProvider operations.
 * Creation date: (2000-02-17 13:24:02)
 * @author: Richard Deadman
 * @return A TelephonyProvider
 */
protected TelephonyProvider getDelegate() {
	return delegate;
}
/**
 * Return the dialed digits for a connection identified by a CallId and address.
 */
public String getDialledDigits(SerializableCallId id, String address) throws java.rmi.RemoteException {

	return this.getDelegate().getDialledDigits(this.getProvCall(id), address);
}
/**
 * Forward privateData access call to real TPI Provider.
 */
public Serializable getPrivateData(CallId call, String address, String terminal) throws NotSerializableException, RemoteException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null) {
		Object o = rp.getPrivateData(call, address, terminal);
		if (!(o instanceof Serializable))
			throw new NotSerializableException();
		return (Serializable)o;
	} else
		throw new RemoteException();
}
/**
 * Simple utility function for looking up a provider id
 * Creation date: (2000-02-18 0:16:44)
 * @author: Richard Deadman
 * @return The provider id
 * @param id The transmission SerializableCallId proxy reference
 */
private CallId getProvCall(SerializableCallId id) {
	return this.getRefMapper().providerId((SerializableCallId)id);
}
/**
 * Get the manager of Serialized References
 * Creation date: (2000-02-18 0:08:04)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.CallMapper
 */
private CallMapper getRefMapper() {
	return refMapper;
}
/**
 * Delegate on to real TelephonyProvider.
 */
public TermData[] getTerminals() throws RemoteException, ResourceUnavailableException  {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		return rp.getTerminals();
	else
		throw new RemoteException();
}
/**
 * Delegate remote getTerminals method to real provider.
 */
public TermData[] getTerminals(String address) throws RemoteException, InvalidArgumentException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		return rp.getTerminals(address);
	else
		throw new RemoteException();
}
/**
 * Delegate remote hold message to real provider.
 */
public void hold(SerializableCallId call, String address, String term) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {

	this.getDelegate().hold(this.getProvCall(call), address, term);
}
/**
 * delegate remote initialize message to real provider.
 */
public void initialize(java.util.Map props) throws ProviderUnavailableException {

	this.getDelegate().initialize(props);
}
/**
 * Forward the request on to the local RawProvider
 */
public boolean isMediaTerminal(String terminal) {
	return this.getDelegate().isMediaTerminal(terminal);
}
/**
 * Delegate remote join message to real provider.
 */
public CallId join(SerializableCallId call1, SerializableCallId call2, String address, String terminal) throws RawStateException, InvalidArgumentException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	
	return this.getRefMapper().swapId(this.getDelegate().join(this.getProvCall(call1),
			this.getProvCall(call2),
			address,
			terminal));
}
/**
 * Forward the request on to the local RawProvider
 */
public void play(String terminal, String[] streamIds, int offset, net.sourceforge.gjtapi.media.RTCHolder[] holders, Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTC[] rtcs = new RTC[holders.length];
	for (int i = 0; i < holders.length; i++) {
		rtcs[i] = holders[i].getRTC();
	}
	this.getDelegate().play(terminal, streamIds, offset, rtcs, this.fromSerializable(optArgs));
}
/**
 * Forward the request on to the local RawProvider
 */
public void record(String terminal,
	String streamId,
	net.sourceforge.gjtapi.media.RTCHolder[] holders,
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTC[] rtcs = new RTC[holders.length];
	for (int i = 0; i < holders.length; i++) {
		rtcs[i] = holders[i].getRTC();
	}
	this.getDelegate().record(terminal, streamId, rtcs, this.fromSerializable(optArgs));
}
/**
 * Delegate remote release message to real provider.
 */
public void release(String address, SerializableCallId call) throws PrivilegeViolationException, ResourceUnavailableException, MethodNotSupportedException, RawStateException {

	this.getDelegate().release(address, this.getProvCall(call));
				// free the reference from my maps
	this.getRefMapper().freeId((SerializableCallId)call);
}
/**
 * releaseCallId method comment.
 */
public void releaseCallId(SerializableCallId id) throws RemoteException {
	this.getDelegate().releaseCallId(this.getProvCall(id));

	// now release my handle to it
	this.getRefMapper().freeId((SerializableCallId)id);
}
/**
 * Delegate removeObserver message to real provider
 */
public void removeListener(RemoteListener rl) {
	this.getCallbackMux().removeListener(new RemoteListenerWrapper(rl, this.getRefMapper()));
}
/**
 * Forward the request on to the local RawProvider
 * <P>Since we don't track which Frameworks monitor a call, only allow turning this on.  Note
 * that multiple Generic Framework clients may have registered for reporting of this call.
 */
public void reportCallsOnAddress(String address, boolean flag)
throws InvalidArgumentException, ResourceUnavailableException {
	if (flag)
		this.getDelegate().reportCallsOnAddress(address, flag);
}
/**
 * Forward the request on to the local RawProvider
 * <P>Since we don't track which Frameworks monitor a call, only allow turning this on.  Note
 * that multiple Generic Framework clients may have registered for reporting of this call.
 */
public void reportCallsOnTerminal(String terminal, boolean flag)
throws InvalidArgumentException, ResourceUnavailableException {
	if (flag)
		this.getDelegate().reportCallsOnTerminal(terminal, flag);
}
/**
 * Delegate remote reserveCallId message to real provider.
 */
public SerializableCallId reserveCallId(String address) throws RemoteException, InvalidArgumentException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		return this.getRefMapper().swapId(rp.reserveCallId(address));
	else
		throw new RemoteException();
}
/**
 * Forward the request on to the local RawProvider
 */
public RawSigDetectEvent retrieveSignals(String terminal,
	int num,
	SymbolHolder[] patHolders,
	RTCHolder[] rtcHolders,
	Dictionary optArgs) throws MediaResourceException {
	RTC[] rtcs = new RTC[rtcHolders.length];
	for (int i = 0; i < rtcHolders.length; i++) {
		rtcs[i] = rtcHolders[i].getRTC();
	}
	Symbol[] patterns = new Symbol[patHolders.length];
	for (int i = 0; i < patHolders.length; i++) {
		patterns[i] = patHolders[i].getSymbol();
	}

	return this.getDelegate().retrieveSignals(terminal, num, patterns, rtcs, this.fromSerializable(optArgs));
}
/**
 * Forward sendPrivateData method to real TPI Provider.
 */
public Serializable sendPrivateData(CallId call, String address, String terminal, Serializable data) throws NotSerializableException, RemoteException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null) {
		Object o = rp.sendPrivateData(call, address, terminal, data);
		if (!(o instanceof Serializable))
			throw new NotSerializableException();
		return (Serializable)o;
	} else
		throw new RemoteException();
}
/**
 * Forward the request on to the local RawProvider
 */
public void sendSignals(String terminal,
	SymbolHolder[] symHolders,
	RTCHolder[] rtcHolders,
	Dictionary optArgs) throws MediaResourceException {
	RTC[] rtcs = new RTC[rtcHolders.length];
	for (int i = 0; i < rtcHolders.length; i++) {
		rtcs[i] = rtcHolders[i].getRTC();
	}
	Symbol[] syms = new Symbol[symHolders.length];
	for (int i = 0; i < symHolders.length; i++) {
		syms[i] = symHolders[i].getSymbol();
	}

	this.getDelegate().sendSignals(terminal,syms, rtcs, this.fromSerializable(optArgs));
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-17 13:24:02)
 * @author: 
 * @param newDelegate net.sourceforge.gjtapi.RawProvider
 */
private void setDelegate(net.sourceforge.gjtapi.TelephonyProvider newDelegate) {
	delegate = newDelegate;
}
/**
 * setLoadControl method comment.
 */
public void setLoadControl(java.lang.String startAddr, java.lang.String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws java.rmi.RemoteException, javax.telephony.MethodNotSupportedException {

	this.getDelegate().setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatment);
}
/**
 * Forward setPrivateData method to real TPI Provider.
 */
public void setPrivateData(CallId call, String address, String terminal, Serializable data) throws RemoteException {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		rp.setPrivateData(call, address, terminal, data);
	else
		throw new RemoteException();
}
/**
 * Eat remote shutdown message -- since more than one client may be connected.
 * The provider should handle shutdown from finalize or Unreferenced.
 */
public void shutdown() {}
/**
 * Receive a remote stop media request
 */
public void stop(String terminal) {
	this.getDelegate().stop(terminal);
}
/**
 * Eat request to the local TelephonyProvider
 * <P>Since we don't track which Frameworks monitor a call, we should eat this message, even though that will mean
 * any call once tracked will always be reported on.  Note
 * that multiple Generic Framework clients may have registered for reporting of this call.
 */
public boolean stopReportingCall(SerializableCallId call)  {
	/*TelephonyProvider rp = this.getDelegate();
	
	if (rp != null)
		return rp.stopReportingCall(call);
	else
		throw new RemoteException();
	*/
	return true;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "RemoteProvider implementation wrapping a RawProvider: " + this.getDelegate();
}
/**
 * receive and forware a triggerRTC remote command.
 */
public void triggerRTC(String terminal, SymbolHolder action) {
	this.getDelegate().triggerRTC(terminal, action.getSymbol());
}
/**
 * Delegate remote unHold message to real provider
 */
public void unHold(SerializableCallId call, String address, String term) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {

	this.getDelegate().unHold(this.getProvCall(call), address, term);
}
/**
 * Clean up when the clients are all removed.
 */
public void unreferenced() {
	TelephonyProvider rp = this.getDelegate();
	
	if (rp != null) {
		rp.removeListener(this.getCallbackMux());
		rp.shutdown();
	}
}
}
