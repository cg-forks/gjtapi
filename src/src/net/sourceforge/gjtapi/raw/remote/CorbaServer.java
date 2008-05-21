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
import net.sourceforge.gjtapi.media.*;
import javax.telephony.media.RTC;
import javax.telephony.media.MediaResourceException;
import javax.telephony.*;
import net.sourceforge.gjtapi.raw.remote.corba.*;
import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.raw.*;
import java.util.*;
import javax.telephony.media.Symbol;
import net.sourceforge.gjtapi.raw.remote.corba.LongEntry;
/**
 * This is a server class for that translates the CORBA skeleton's calls to a TelephonyProvider.
 * Creation date: (2000-08-18 14:39:05)
 * @author: Richard Deadman
 */
public class CorbaServer extends _CorbaProviderImplBase {
	static final long serialVersionUID = 7075798562180704373L;
	
	private CallMapper refMapper = new CallMapper();
	private RawListenerMux callbackMux = new RawListenerMux();
	private TelephonyProvider real;
	private org.omg.CORBA.ORB orb;
/**
 * Create a server-side wrapper for a real TelephonyProvider
 * Creation date: (2000-08-24 15:29:45)
 * @author: Richard Deadman
 * @param tl net.sourceforge.gjtapi.TelephonyProvider
 */
public CorbaServer(TelephonyProvider tp, org.omg.CORBA.ORB orb) {
	super();
	
	if (tp == null)
		throw new NullPointerException();
	else
		this.setReal(tp);

	// register my listener (wrappedd in a event dispatch pool)
	tp.addListener(this.getCallbackMux());

	// Note the ORB I'm going through
	this.setOrb(orb);
}
/**
 * addListener method comment.
 */
public void addListener(net.sourceforge.gjtapi.raw.remote.corba.CorbaListener cl) {
	this.getCallbackMux().addListener(new CorbaListenerWrapper(cl, this.getRefMapper(), this.getOrb()));
}
/**
 * allocateMedia method comment.
 */
public boolean allocateMedia(String term, int type, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] parameters) {
	return this.getReal().allocateMedia(term, type, this.toSymbolDictionary(parameters));
}
/**
 * answerCall method comment.
 */
public void answerCall(int callId, String address, String terminal) throws ResourceUnavailableEx, PrivilegeViolationEx, RawStateEx, MethodNotSupportedEx {
	try {
		this.getReal().answerCall(this.getRefMapper().intToCall(callId), address, terminal);
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw this.createResourceUnavailableEx(rue);
	} catch (PrivilegeViolationException pve) {
		throw createPrivilegeViolationEx(pve);
	} catch (RawStateException rse) {
		throw createRawStateEx(rse);
	} catch (MethodNotSupportedException nse) {
		throw createMethodNotSupportedEx(nse);
	}
}
/**
 * attachMedia method comment.
 */
public boolean attachMedia(int call, java.lang.String address, boolean onFlag) {
	return this.getReal().attachMedia(this.getRefMapper().intToCall(call), address, onFlag);
}
/**
 * beep method comment.
 */
public void beep(int call) {
	this.getReal().beep(this.getRefMapper().intToCall(call));
}
/**
 * Convert a Generic JTAPI CallData array to a CORBA CallData array.
 * Creation date: (2000-08-23 16:14:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.CallData[]
 * @param ca net.sourceforge.gjtapi.CallData[]
 */
private net.sourceforge.gjtapi.raw.remote.corba.CallData[] convertCallArray(net.sourceforge.gjtapi.CallData[] ca) {
	int size = ca.length;
	net.sourceforge.gjtapi.raw.remote.corba.CallData[] cda = new net.sourceforge.gjtapi.raw.remote.corba.CallData[size];
	for (int i = 0; i < size; i++) {
		cda[i] = this.toCallData(ca[i]);
	}
	return cda;
}
/**
 * Convert a Generic JTAPI TermData array to a CORBA TermData array.
 * Creation date: (2000-08-23 16:14:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.TermData[]
 * @param ca net.sourceforge.gjtapi.TermData[]
 */
private net.sourceforge.gjtapi.raw.remote.corba.TermData[] convertTermDataArray(net.sourceforge.gjtapi.TermData[] td) {
	int size = td.length;
	net.sourceforge.gjtapi.raw.remote.corba.TermData[] tda = new net.sourceforge.gjtapi.raw.remote.corba.TermData[size];
	for (int i = 0; i < size; i++) {
		tda[i] = this.toTermData(td[i]);
	}
	return tda;
}
/**
 * createCall method comment.
 */
public int createCall(int callId, String address, String terminal, String destination) throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx, net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx, net.sourceforge.gjtapi.raw.remote.corba.InvalidPartyEx, net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx, net.sourceforge.gjtapi.raw.remote.corba.RawStateEx, net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx {
	CallMapper cm = this.getRefMapper();
	try {
		return cm.callToInt(this.getReal().createCall(cm.intToCall(callId), address, terminal, destination));
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw this.createResourceUnavailableEx(rue);
	} catch (PrivilegeViolationException pve) {
		throw createPrivilegeViolationEx(pve);
	} catch (RawStateException rse) {
		throw createRawStateEx(rse);
	} catch (MethodNotSupportedException nse) {
		throw createMethodNotSupportedEx(nse);
	} catch (InvalidPartyException ipe) {
		throw createInvalidPartyEx(ipe);
	} catch (InvalidArgumentException iae) {
		throw createInvalidArgumentEx(iae);
	}

}
/**
 * Factory to create the appropriate ResourceEvent based on the CORBA resource event enumeration
 */
private net.sourceforge.gjtapi.raw.remote.corba.ResourceEvent createEvent(javax.telephony.media.ResourceEvent re) {
	net.sourceforge.gjtapi.raw.remote.corba.ResourceUnion ru = new ResourceUnion();
	if (re instanceof GenericPlayerEvent) {
		GenericPlayerEvent pe = (GenericPlayerEvent)re;
		ru.playEv(new PlayerEvent((int)pe.getChangeType().hashCode(),
			pe.getIndex(),
			pe.getOffset()));
	}
	if (re instanceof GenericRecorderEvent) {
		GenericRecorderEvent ev = (GenericRecorderEvent)re;
		ru.recEv(new RecorderEvent(ev.getDuration()));
	}
	if (re instanceof GenericSignalDetectorEvent) {
		GenericSignalDetectorEvent ev = (GenericSignalDetectorEvent)re;
		ru.sdEv(new SigDetectorEvent(ev.getPatternIndex(),
			CorbaProvider.toLongArray(ev.getSignalBuffer())));
	}
	return new ResourceEvent((int)re.getError().hashCode(),
		(int)re.getEventID().hashCode(),
		re.getMediaService().getTerminal().getName(),
		(int)re.getQualifier().hashCode(),
		(int)re.getRTCTrigger().hashCode(),
		ru);
}
/**
 * Create a Corba InvalidArgumentEx from its pair
 * Creation date: (2000-08-24 18:34:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx
 * @param rue javax.telephony.InvalidArgumentException
 */
private InvalidArgumentEx createInvalidArgumentEx(javax.telephony.InvalidArgumentException iae) {
	return new InvalidArgumentEx(iae.getMessage());
}
/**
 * Create a Corba InvalidPartyEx from its pair
 * Creation date: (2000-08-24 18:34:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.InvalidPartyEx
 * @param rue javax.telephony.InvalidPartyException
 */
private InvalidPartyEx createInvalidPartyEx(javax.telephony.InvalidPartyException ipe) {
	return new InvalidPartyEx(ipe.getType(), ipe.getMessage());
}
/**
 * Create a Corba MethodNotSupportedEx from its pair
 * Creation date: (2000-08-24 18:34:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx
 * @param rue javax.telephony.MethodNotSupportedException
 */
private MethodNotSupportedEx createMethodNotSupportedEx(javax.telephony.MethodNotSupportedException nse) {
	return new MethodNotSupportedEx(nse.getMessage());
}
/**
 * Create a Corba PrivilegeViolationEx from its pair
 * Creation date: (2000-08-24 18:34:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx
 * @param rue javax.telephony.ResourceUnavailableException
 */
private PrivilegeViolationEx createPrivilegeViolationEx(javax.telephony.PrivilegeViolationException pve) {
	return new PrivilegeViolationEx(pve.getType(), pve.getMessage());
}
/**
 * Create a Corba RawStateEx from its pair
 * Creation date: (2000-08-24 18:34:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx
 * @param rue javax.telephony.ResourceUnavailableException
 */
private RawStateEx createRawStateEx(RawStateException rse) {
	RawStateEx re = new RawStateEx();
	re.type = rse.getObjectType();
	re.info = rse.getMessage();
	re.state = rse.getState();
	Object o = rse.getObject();
	if (o instanceof Address) {
		re.address = ((Address)o).getName();
	}
	if (o instanceof Terminal) {
		re.terminal = ((Terminal)o).getName();
	}
	if (o instanceof Call) {
		re.callId = (int)((SerializableCallId)((FreeCall)o).getCallID()).getId();
	}
	if (o instanceof Connection) {
		re.callId = (int)((SerializableCallId)((FreeCall)((Connection)o).getCall()).getCallID()).getId();
		re.address = ((Connection)o).getAddress().getName();
	}
	if (o instanceof TerminalConnection) {
		Connection conn = ((TerminalConnection)o).getConnection();
		re.callId = (int)((SerializableCallId)((FreeCall)conn.getCall()).getCallID()).getId();
		re.address = conn.getAddress().getName();
		re.terminal = ((TerminalConnection)o).getTerminal().getName();
	}
	return re;
}
/**
 * Create a Corba ResourceUnavailableEx from its pair
 * Creation date: (2000-08-24 18:34:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx
 * @param rue javax.telephony.ResourceUnavailableException
 */
private ResourceUnavailableEx createResourceUnavailableEx(javax.telephony.ResourceUnavailableException rue) {
	return new ResourceUnavailableEx(rue.getType(), rue.getMessage());
}
/**
 * Clean up when no longer held by anything.
 * Creation date: (2000-08-25 1:45:07)
 * @author: Richard Deadman
 */
public void finalize() {
	this.getReal().shutdown();
}
/**
 * freeMedia method comment.
 */
public boolean freeMedia(String terminal, int type) {
	return this.getReal().freeMedia(terminal, type);
}
/**
 * getAddresses method comment.
 */
public java.lang.String[] getAddresses() throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx {
	try {
		return this.getReal().getAddresses();
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw this.createResourceUnavailableEx(rue);
	}
}
/**
 * getAddressesForTerm method comment.
 */
public java.lang.String[] getAddressesForTerm(String terminal) throws net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx {
	try {
		return this.getReal().getAddresses(terminal);
	} catch (InvalidArgumentException iae) {
		throw this.createInvalidArgumentEx(iae);
	}
}
/**
 * getAddressType method comment.
 */
public int getAddressType(String name) {
	return this.getReal().getAddressType(name);
}
/**
 * getCall method comment.
 */
public net.sourceforge.gjtapi.raw.remote.corba.CallData getCall(int callId) {
	return this.toCallData(this.getReal().getCall(this.getRefMapper().intToCall(callId)));
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 15:33:55)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.RawListenerMux
 */
private net.sourceforge.gjtapi.raw.RawListenerMux getCallbackMux() {
	return callbackMux;
}
/**
 * getCallsOnAddress method comment.
 */
public net.sourceforge.gjtapi.raw.remote.corba.CallData[] getCallsOnAddress(String address) {
	return this.convertCallArray(this.getReal().getCallsOnAddress(address));
}
/**
 * getCallsOnTerminal method comment.
 */
public net.sourceforge.gjtapi.raw.remote.corba.CallData[] getCallsOnTerminal(String terminal) {
	return this.convertCallArray(this.getReal().getCallsOnTerminal(terminal));
}
/**
 * getCapabilities method comment.
 */
public net.sourceforge.gjtapi.raw.remote.corba.StringEntry[] getCapabilities() {
	Properties props = this.getReal().getCapabilities();
	StringEntry[] sEntries = new StringEntry[props.size()];
	int i = 0;
	Enumeration e = props.keys();
	while (e.hasMoreElements()) {
		Object key = e.nextElement();
		sEntries[i] = new StringEntry((String)key, (String)props.get(key));
		i++;
	}
	return sEntries;
}
/**
 * getDialledDigits method comment.
 */
public java.lang.String getDialledDigits(int id, String address) {
	return this.getReal().getDialledDigits(this.getRefMapper().intToCall(id), address);
}
/**
 * Return the ORB that is handling my remoteness.
 * Creation date: (2000-08-24 15:54:40)
 * @author: Richard Deadman
 * @return org.omg.CORBA.ORB
 */
private org.omg.CORBA.ORB getOrb() {
	return orb;
}
/**
 * getPrivateData method comment.
 */
public org.omg.CORBA.Any getPrivateData(int callId, String address, String terminal) throws net.sourceforge.gjtapi.raw.remote.corba.NotSerializableEx {
	Object res = this.getReal().getPrivateData(this.getRefMapper().intToCall(callId), address, terminal);
	try {
		return CorbaProvider.convertToAny(this.getOrb(), res);
	} catch (java.io.NotSerializableException nse) {
		throw new NotSerializableEx(nse.getMessage());
	}
}
/**
 * Internal accessor for the real TelephonyProvider I am making remote.
 * Creation date: (2000-08-24 15:33:56)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.TelephonyProvider
 */
private net.sourceforge.gjtapi.TelephonyProvider getReal() {
	return real;
}
/**
 * This maps TelephonyProvider callIds to SerializableCallIds that have an integer id that is passed
 * to the remote client.
 * Creation date: (2000-08-24 15:33:56)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.CallMapper
 */
private CallMapper getRefMapper() {
	return refMapper;
}
/**
 * getTerminals method comment.
 */
public net.sourceforge.gjtapi.raw.remote.corba.TermData[] getTerminals() throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx {
	try {
		return this.convertTermDataArray(this.getReal().getTerminals());
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw createResourceUnavailableEx(rue);
	}
}
/**
 * getTerminalsForAddr method comment.
 */
public net.sourceforge.gjtapi.raw.remote.corba.TermData[] getTerminalsForAddr(String address) throws net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx {
	try {
		return this.convertTermDataArray(this.getReal().getTerminals(address));
	} catch (InvalidArgumentException iae) {
		throw this.createInvalidArgumentEx(iae);
	}
}
/**
 * hold method comment.
 */
public void hold(int callId, String address, String terminal) throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx, net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx, net.sourceforge.gjtapi.raw.remote.corba.RawStateEx, net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx {
	try {
		this.getReal().hold(this.getRefMapper().intToCall(callId), address, terminal);
	} catch (PrivilegeViolationException pve) {
		throw this.createPrivilegeViolationEx(pve);
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw this.createResourceUnavailableEx(rue);
	} catch (MethodNotSupportedException mnse) {
		throw this.createMethodNotSupportedEx(mnse);
	} catch (RawStateException rse) {
		throw this.createRawStateEx(rse);
	}
}
/**
 * initialize method comment.
 */
public void initialize(net.sourceforge.gjtapi.raw.remote.corba.StringEntry[] propArray) throws net.sourceforge.gjtapi.raw.remote.corba.ProviderUnavailableEx {
	Properties props = new Properties();
	int size = propArray.length;
	for (int i = 0; i < size; i++) {
		props.put(propArray[i].key, propArray[i].value);
	}
	this.getReal().initialize(props);
}
/**
 * isMediaTerminal method comment.
 */
public boolean isMediaTerminal(String terminal) {
	return this.getReal().isMediaTerminal(terminal);
}
/**
 * join method comment.
 */
public int join(int callId1, int callId2, String address, String terminal) throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx, net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx, net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx, net.sourceforge.gjtapi.raw.remote.corba.RawStateEx, net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx {
	CallMapper cm = this.getRefMapper();
	try {
		return cm.callToInt(this.getReal().join(cm.intToCall(callId1),
				cm.intToCall(callId2),
				address,
				terminal));
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw this.createResourceUnavailableEx(rue);
	} catch (PrivilegeViolationException pve) {
		throw createPrivilegeViolationEx(pve);
	} catch (RawStateException rse) {
		throw createRawStateEx(rse);
	} catch (MethodNotSupportedException nse) {
		throw createMethodNotSupportedEx(nse);
	} catch (InvalidArgumentException iae) {
		throw createInvalidArgumentEx(iae);
	}
}
/**
 * play method comment.
 */
public void play(String terminal, java.lang.String[] streamIds, int offset, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] rtcs, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] optArgs) throws net.sourceforge.gjtapi.raw.remote.corba.MediaResourceEx {
	try {
		this.getReal().play(terminal, streamIds, offset, this.toRTCArray(rtcs), this.toSymbolDictionary(optArgs));
	} catch (MediaResourceException mre) {
		throw new MediaResourceEx(mre.getMessage(),
			this.createEvent(mre.getResourceEvent()));
	}
}
/**
 * record method comment.
 */
public void record(String terminal, String streamId, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] rtcs, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] optArgs) throws net.sourceforge.gjtapi.raw.remote.corba.MediaResourceEx {
	try {
		this.getReal().record(terminal, streamId, this.toRTCArray(rtcs), this.toSymbolDictionary(optArgs));
	} catch (MediaResourceException mre) {
		throw new MediaResourceEx(mre.getMessage(),
			this.createEvent(mre.getResourceEvent()));
	}
}
/**
 * release method comment.
 */
public void release(String address, int callId) throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx, net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx, net.sourceforge.gjtapi.raw.remote.corba.RawStateEx, net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx {
	try {
		this.getReal().release(address, this.getRefMapper().intToCall(callId));
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw createResourceUnavailableEx(rue);
	} catch (PrivilegeViolationException pve) {
		throw createPrivilegeViolationEx(pve);
	} catch (RawStateException rse) {
		throw createRawStateEx(rse);
	} catch (MethodNotSupportedException nse) {
		throw createMethodNotSupportedEx(nse);
	}
}
/**
 * releaseCallId method comment.
 */
public void releaseCallId(int callId) {
	this.getReal().releaseCallId(this.getRefMapper().intToCall(callId));
}
/**
 * removeListener method comment.
 */
public void removeListener(net.sourceforge.gjtapi.raw.remote.corba.CorbaListener cl) {
	this.getCallbackMux().removeListener(new CorbaListenerWrapper(cl, this.getRefMapper(), this.getOrb()));
}
/**
 * reportCallsOnAddress method comment.
 */
public void reportCallsOnAddress(String address, boolean flag) throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx, net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx {
	try {
		this.getReal().reportCallsOnAddress(address, flag);
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw this.createResourceUnavailableEx(rue);
	} catch (InvalidArgumentException iae) {
		throw createInvalidArgumentEx(iae);
	}
}
/**
 * reportCallsOnTerminal method comment.
 */
public void reportCallsOnTerminal(String terminal, boolean flag) throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx, net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx {
	try {
		this.getReal().reportCallsOnTerminal(terminal, flag);
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw this.createResourceUnavailableEx(rue);
	} catch (InvalidArgumentException iae) {
		throw createInvalidArgumentEx(iae);
	}
}
/**
 * reserveCallId method comment.
 */
public int reserveCallId(String address) throws net.sourceforge.gjtapi.raw.remote.corba.InvalidArgumentEx {
	try {
		return this.getRefMapper().callToInt(this.getReal().reserveCallId(address));
	} catch (InvalidArgumentException iae) {
		throw this.createInvalidArgumentEx(iae);
	}
}
/**
 * retrieveSignals method comment.
 */
public net.sourceforge.gjtapi.raw.remote.corba.DetectEvent retrieveSignals(String terminal, int num, int[] patterns, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] rtcs, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] optArgs) throws net.sourceforge.gjtapi.raw.remote.corba.MediaResourceEx {
	try {
		RawSigDetectEvent de = this.getReal().retrieveSignals(terminal, num, CorbaProvider.toSymbolArray(patterns), this.toRTCArray(rtcs), this.toSymbolDictionary(optArgs));
		ResourceUnion ru = new ResourceUnion();
		int[] buf = CorbaProvider.toLongArray(SymbolHolder.decode(de.getSigs()));
		ru.sdEv(new SigDetectorEvent(de.getPatternIndex(), buf));
		ResourceEvent re = new ResourceEvent((int)de.getErr().getSymbol().hashCode(),
			(int)javax.telephony.media.SignalDetector.ev_RetrieveSignals.hashCode(),
			de.getTerminal(),
			(int)de.getQualifier().getSymbol().hashCode(),
			(int)de.getTrigger().getSymbol().hashCode(),
			ru);
		return new DetectEvent(de.getPatternIndex(), buf, de.getTerminal(), re);
	} catch (MediaResourceException mre) {
		throw new MediaResourceEx(mre.getMessage(),
			this.createEvent(mre.getResourceEvent()));
	}
}
/**
 * sendPrivateData method comment.
 */
public org.omg.CORBA.Any sendPrivateData(int callId, String address, String terminal, org.omg.CORBA.Any data) throws net.sourceforge.gjtapi.raw.remote.corba.NotSerializableEx {
	try {
		return CorbaProvider.convertToAny(this.getOrb(), this.getReal().sendPrivateData(this.getRefMapper().intToCall(callId), address, terminal, CorbaProvider.convertAny(data)));
	} catch (java.io.NotSerializableException nsx) {
		throw new PlatformException("sendPrivateData data not serializable through remote proxy" + nsx.getMessage());
	}
}
/**
 * sendSignals method comment.
 */
public void sendSignals(String terminal, int[] syms, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] rtcs, net.sourceforge.gjtapi.raw.remote.corba.LongEntry[] optArgs) throws net.sourceforge.gjtapi.raw.remote.corba.MediaResourceEx {
	try {
		this.getReal().sendSignals(terminal, CorbaProvider.toSymbolArray(syms), this.toRTCArray(rtcs), this.toSymbolDictionary(optArgs));
	} catch (MediaResourceException mre) {
		throw new MediaResourceEx(mre.getMessage(),
			this.createEvent(mre.getResourceEvent()));
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 15:33:55)
 * @author: Richard Deadman
 * @param newCallbackMux net.sourceforge.gjtapi.raw.RawListenerMux
 */
/*private void setCallbackMux(net.sourceforge.gjtapi.raw.RawListenerMux newCallbackMux) {
	callbackMux = newCallbackMux;
}*/
/**
 * setLoadControl method comment.
 */
public void setLoadControl(String startAddr, String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws MethodNotSupportedEx {
	try {
		this.getReal().setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatment);
	} catch (MethodNotSupportedException mnse) {
		throw createMethodNotSupportedEx(mnse);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 15:54:40)
 * @author: Richard Deadman
 * @param newOrb org.omg.CORBA.ORB
 */
private void setOrb(org.omg.CORBA.ORB newOrb) {
	orb = newOrb;
}
/**
 * setPrivateData method comment.
 */
public void setPrivateData(int callId, String address, String terminal, org.omg.CORBA.Any data) {
	this.getReal().setPrivateData(this.getRefMapper().intToCall(callId), address, terminal, CorbaProvider.convertAny(data));
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 15:33:56)
 * @author: Richard Deadman
 * @param newReal net.sourceforge.gjtapi.TelephonyProvider
 */
private void setReal(net.sourceforge.gjtapi.TelephonyProvider newReal) {
	real = newReal;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 15:33:56)
 * @author: Richard Deadman
 * @param newRefMapper net.sourceforge.gjtapi.raw.remote.CallMapper
 */
/*private void setRefMapper(CallMapper newRefMapper) {
	refMapper = newRefMapper;
}*/
/**
 * Eat remote shutdown message -- since more than one client may be connected.
 * The provider should handle shutdown from finalize or Unreferenced.
 */
public void shutdown() {}
/**
 * stop method comment.
 */
public void stop(String terminal) {
	this.getReal().stop(terminal);
}
/**
 * stopReportingCall method comment.
 */
public boolean stopReportingCall(int callId) {
	return this.getReal().stopReportingCall(this.getRefMapper().intToCall(callId));
}
/**
 * Translate a JTAPI CallData CallData holder to a Generic CORBA holder.
 * Creation date: (2000-08-23 15:55:52)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.CallData
 * @param cd net.sourceforge.gjtapi.CallData
 */
private net.sourceforge.gjtapi.raw.remote.corba.CallData toCallData(net.sourceforge.gjtapi.CallData cd) {
	return new net.sourceforge.gjtapi.raw.remote.corba.CallData(this.getRefMapper().callToInt(cd.id), cd.callState,
		this.toConnectionData(cd.connections));
}
/**
 * Convert Generic JTAPI ConnectionData holder to CORBA holder
 * Creation date: (2000-08-23 16:00:40)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.ConnectionData[]
 * @param connData net.sourceforge.gjtapi.ConnectionData[]
 */
private net.sourceforge.gjtapi.raw.remote.corba.ConnectionData[] toConnectionData(net.sourceforge.gjtapi.ConnectionData[] connData) {
	int size = connData.length;
	net.sourceforge.gjtapi.raw.remote.corba.ConnectionData[] cd = new net.sourceforge.gjtapi.raw.remote.corba.ConnectionData[size];
	for (int i = 0; i < size; i++) {
		cd[i] = new net.sourceforge.gjtapi.raw.remote.corba.ConnectionData(connData[i].connState,
			connData[i].address, connData[i].isLocal, this.toTCData(connData[i].terminalConnections));
	}
	return cd;
}
/**
 * Convert a Symbol dictionary to a LongEntry array
 */
//private LongEntry[] toLongEntryArray(Dictionary params) {
//	Set longEntrySet = new HashSet();
//	int size = 0;
//	Enumeration e = params.keys();
//	while (e.hasMoreElements()) {
//		Object key = e.nextElement();
//		if (key instanceof Symbol) {
//			Symbol skey = (Symbol)key;
//			Object value = params.get(key);
//			if (value instanceof Symbol) {
//				longEntrySet.add(new LongEntry(skey.hashCode(), value.hashCode()));
//				size++;
//			}
//		}
//	}
//	return (LongEntry[])longEntrySet.toArray(new LongEntry[size]);
//}
/**
 * Convert a LongEntry array to an RTC array
 */
private RTC[] toRTCArray(LongEntry[] rtcs) {
	int size = rtcs.length;
	RTC[] array = new RTC[size];
	for (int i = 0; i < size; i++) {
		array[i] = new RTC(Symbol.getSymbol(rtcs[i].key), Symbol.getSymbol(rtcs[i].value));
	}
	return array;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Remote Skeleton for: " + this.getReal().toString();
}
/**
 * Convert a LongEntry array to an Symbol Dictionary
 */
private Dictionary toSymbolDictionary(LongEntry[] rtcs) {
	int size = rtcs.length;
	Dictionary dict = new Hashtable();
	for (int i = 0; i < size; i++) {
		dict.put(Symbol.getSymbol(rtcs[i].key), Symbol.getSymbol(rtcs[i].value));
	}
	return dict;
}
/**
 * Convert Generic JTAPI TCData holder to CORBA holder
 * Creation date: (2000-08-23 16:00:40)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.TCData[]
 * @param connData net.sourceforge.gjtapi.TCData[]
 */
 private net.sourceforge.gjtapi.raw.remote.corba.TCData[] toTCData(net.sourceforge.gjtapi.TCData[] tcData) {
	int size = tcData.length;
	net.sourceforge.gjtapi.raw.remote.corba.TCData[] tcd = new net.sourceforge.gjtapi.raw.remote.corba.TCData[size];
	for (int i = 0; i < size; i++) {
		tcd[i] = new net.sourceforge.gjtapi.raw.remote.corba.TCData(tcData[i].tcState,
			this.toTermData(tcData[i].terminal));
	}
	return tcd;
}
/**
 * Translate a Generic JTAPI TermData holder to a CORBA TermData holder.
 * Creation date: (2000-08-23 15:55:52)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.corba.TermData
 * @param cd net.sourceforge.gjtapi.TermData
 */
private net.sourceforge.gjtapi.raw.remote.corba.TermData toTermData(net.sourceforge.gjtapi.TermData td) {
	return new net.sourceforge.gjtapi.raw.remote.corba.TermData(td.terminal, td.isMedia);
}
/**
 * triggerRTC method comment.
 */
public void triggerRTC(String terminal, int action) {
	this.getReal().triggerRTC(terminal, Symbol.getSymbol(action));
}
/**
 * unHold method comment.
 */
public void unHold(int callId, String address, String term) throws net.sourceforge.gjtapi.raw.remote.corba.ResourceUnavailableEx, net.sourceforge.gjtapi.raw.remote.corba.PrivilegeViolationEx, net.sourceforge.gjtapi.raw.remote.corba.RawStateEx, net.sourceforge.gjtapi.raw.remote.corba.MethodNotSupportedEx {
	try {
		this.getReal().unHold(this.getRefMapper().intToCall(callId), address, term);
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw this.createResourceUnavailableEx(rue);
	} catch (PrivilegeViolationException pve) {
		throw createPrivilegeViolationEx(pve);
	} catch (RawStateException rse) {
		throw createRawStateEx(rse);
	} catch (MethodNotSupportedException nse) {
		throw createMethodNotSupportedEx(nse);
	}
}
}
