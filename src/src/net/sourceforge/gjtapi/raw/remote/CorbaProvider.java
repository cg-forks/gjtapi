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
import org.omg.CosNaming.*;
import org.omg.CORBA.ORB;
import org.omg.CORBA.TypeCode;
import org.omg.CORBA.TCKind;
import org.omg.CORBA.Any;
import net.sourceforge.gjtapi.raw.remote.corba.*;
import java.io.*;
import javax.telephony.media.*;
import net.sourceforge.gjtapi.media.*;
import javax.telephony.*;
import net.sourceforge.gjtapi.*;
import java.util.*;
/**
 * This is a pluggable provider that provides access to a remote provider through CORBA
 * Creation date: (2000-08-18 10:53:56)
 * @author: Richard Deadman
 */
public class CorbaProvider implements TelephonyProvider {
	private net.sourceforge.gjtapi.raw.remote.corba.CorbaProvider remote;
	private org.omg.CORBA.ORB orb;
	private CorbaListener listener;
/**
 * Forward to remote provider.
 * Should only be called once.
 */
public synchronized void addListener(TelephonyListener rl) {
	CorbaListener cl = new CorbaListener(rl);
	//this.getOrb().connect(cl);
	this.getRemote().addListener(cl);
	this.setListener(cl);
}
/**
 * Forward to a remote stub
 */
public boolean allocateMedia(String terminal, int type, Dictionary params) {
	return this.getRemote().allocateMedia(terminal, type, this.toLongEntryArray(params));
}
/**
 * Forward answerCall to remote provider
 */
public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
	  MethodNotSupportedException, RawStateException {
	try {
		this.getRemote().answerCall((int)((SerializableCallId)call).getId(), address, terminal);
	} catch (PrivilegeViolationEx pve) {
		throw new PrivilegeViolationException(pve.type, pve.reason);
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	} catch (MethodNotSupportedEx mnse) {
		throw new MethodNotSupportedException(mnse.reason);
	} catch (RawStateEx rse) {
		throw new RawStateException(new SerializableCallId(rse.callId), rse.address, rse.terminal,
			rse.type, rse.state, rse.info);
	}
}
/**
 * attachMedia method comment.
 */
public boolean attachMedia(CallId call, String address, boolean onFlag) {
	return this.getRemote().attachMedia((int)((SerializableCallId)call).getId(), address, onFlag);
}
/**
 * beep method comment.
 */
public void beep(net.sourceforge.gjtapi.CallId call) {
	this.getRemote().beep((int)((SerializableCallId)call).getId());
}
/**
 * Turn an Object to a CORBA ANY object
 * Creation date: (2000-08-24 10:14:47)
 * @author: Richard Deadman
 * @return org.omg.CORBA.Any
 * @param any java.lang.Object
 */
private Any convertAny(Object o) throws NotSerializableException {
	return convertToAny(this.getOrb(), o);
}
/**
 * Turn a CORBA ANY object into a Java object
 * Creation date: (2000-08-24 10:14:47)
 * @author: Richard Deadman
 * @return java.lang.Object
 * @param any org.omg.CORBA.Any
 */
static Object convertAny(Any any) {
	TypeCode type = any.type();
	switch (type.kind().value()) {
		case TCKind._tk_boolean: {
			return new Boolean(any.extract_boolean());
		} 
		case TCKind._tk_char: {
			return new Character(any.extract_char());
		} 
		case TCKind._tk_octet: {
			return new Byte(any.extract_octet());
		} 
		case TCKind._tk_double: {
			return new Double(any.extract_double());
		} 
		case TCKind._tk_float: {
			return new Float(any.extract_float());
		} 
		case TCKind._tk_long: {
			return new Integer(any.extract_long());
		} 
		case TCKind._tk_longlong: {
			return new Long(any.extract_longlong());
		} 
		case TCKind._tk_short: {
			return new Short(any.extract_short());
		} 
		case TCKind._tk_string: {
			return any.extract_string();
		} 
		case TCKind._tk_ulong: {
			return new Integer(any.extract_ulong());
		} 
		case TCKind._tk_ulonglong: {
			return new Long(any.extract_ulonglong());
		} 
		case TCKind._tk_ushort: {
			return new Short(any.extract_ushort());
		} 
		case TCKind._tk_wchar: {
			return new Character(any.extract_wchar());
		} 
		case TCKind._tk_wstring: {
			return any.extract_wstring();
		} 
		default: {
			return null;
		}
	}
}
/**
 * Convert a CORBA CallData array to a Generic JTAPI CallData array.
 * Creation date: (2000-08-23 16:14:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.CallData[]
 * @param ca net.sourceforge.gjtapi.raw.remote.corba.CallData[]
 */
private net.sourceforge.gjtapi.CallData[] convertCallArray(net.sourceforge.gjtapi.raw.remote.corba.CallData[] ca) {
	int size = ca.length;
	net.sourceforge.gjtapi.CallData[] cda = new net.sourceforge.gjtapi.CallData[size];
	for (int i = 0; i < size; i++) {
		cda[i] = this.toCallData(ca[i]);
	}
	return cda;
}
/**
 * Convert a CORBA TermData array to a Generic JTAPI TermData array.
 * Creation date: (2000-08-23 16:14:19)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.TermData[]
 * @param ca net.sourceforge.gjtapi.raw.remote.corba.TermData[]
 */
private net.sourceforge.gjtapi.TermData[] convertTermDataArray(net.sourceforge.gjtapi.raw.remote.corba.TermData[] td) {
	int size = td.length;
	net.sourceforge.gjtapi.TermData[] tda = new net.sourceforge.gjtapi.TermData[size];
	for (int i = 0; i < size; i++) {
		tda[i] = this.toTermData(td[i]);
	}
	return tda;
}
/**
 * Turn an Object to a CORBA ANY object
 * Creation date: (2000-08-24 10:14:47)
 * @author: Richard Deadman
 * @return org.omg.CORBA.Any
 * @param any java.lang.Object
 */
static Any convertToAny(ORB orb, Object o) throws NotSerializableException {
	Any any = orb.create_any();
	if (o instanceof Boolean) {
		any.insert_boolean(((Boolean)o).booleanValue());
	} else if (o instanceof Character) {
		any.insert_char(((Character)o).charValue());
	} else if (o instanceof Double) {
		any.insert_double(((Double)o).doubleValue());
	} else if (o instanceof Float) {
		any.insert_float(((Float)o).floatValue());
	} else if (o instanceof Integer) {
		any.insert_long(((Integer)o).intValue());
	} else if (o instanceof Long) {
		any.insert_longlong(((Long)o).longValue());
	} else if (o instanceof Byte) {
		any.insert_octet(((Byte)o).byteValue());
	} else if (o instanceof Short) {
		any.insert_short(((Short)o).shortValue());
	} else {
		throw new NotSerializableException("No CORBA ANY correspondence for: " + o.toString());
	}
	return any;
}
/**
 * Create a call from the given address and terminal to the remote address
 */
public CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException,
	  InvalidPartyException, InvalidArgumentException, RawStateException,
	  MethodNotSupportedException {
	try {
		return new SerializableCallId(this.getRemote().createCall((int)((SerializableCallId)id).getId(), address, term, dest));
	} catch (PrivilegeViolationEx pve) {
		throw new PrivilegeViolationException(pve.type, pve.reason);
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	} catch (MethodNotSupportedEx mnse) {
		throw new MethodNotSupportedException(mnse.reason);
	} catch (RawStateEx rse) {
		throw new RawStateException(new SerializableCallId(rse.callId), rse.address, rse.terminal,
			rse.type, rse.state, rse.info);
	} catch (InvalidPartyEx ipe) {
		throw new InvalidPartyException(ipe.type, ipe.reason);
	} catch (InvalidArgumentEx iae) {
		throw new InvalidArgumentException(iae.reason);
	}
}
/**
 * Factory to create the appropriate ResourceEvent based on the CORBA resource event enumeration
 */
private GenericResourceEvent createEvent(String terminal, net.sourceforge.gjtapi.raw.remote.corba.ResourceEvent re) {
	net.sourceforge.gjtapi.raw.remote.corba.ResourceUnion ru = re.resUnion;
	switch (ru.discriminator().value()) {
		case ResourceType._player: {
			net.sourceforge.gjtapi.raw.remote.corba.PlayerEvent pe = ru.playEv();
			return new GenericPlayerEvent(CorbaProvider.createSymbol(re.eventId),
				terminal,
				CorbaProvider.createSymbol(re.error),
				CorbaProvider.createSymbol(re.qualifier),
				CorbaProvider.createSymbol(re.trigger),
				CorbaProvider.createSymbol(pe.change),
				pe.index,
				pe.offset);
		}
		case ResourceType._recorder: {
			net.sourceforge.gjtapi.raw.remote.corba.RecorderEvent rec = ru.recEv();
			return new GenericRecorderEvent(CorbaProvider.createSymbol(re.eventId),
				terminal,
				CorbaProvider.createSymbol(re.error),
				CorbaProvider.createSymbol(re.qualifier),
				CorbaProvider.createSymbol(re.trigger),
				rec.duration);
		}
		case ResourceType._sigDetector: {
			net.sourceforge.gjtapi.raw.remote.corba.SigDetectorEvent sde = ru.sdEv();
			return new GenericSignalDetectorEvent(CorbaProvider.createSymbol(re.eventId),
				terminal,
				CorbaProvider.createSymbol(re.error),
				CorbaProvider.createSymbol(re.qualifier),
				CorbaProvider.createSymbol(re.trigger),
				sde.index,
				CorbaProvider.toSymbolArray(sde.buffer));
		}
		case ResourceType._sigGenerator: {
			return new GenericSignalGeneratorEvent(CorbaProvider.createSymbol(re.eventId),
				terminal,
				CorbaProvider.createSymbol(re.error),
				CorbaProvider.createSymbol(re.qualifier),
				CorbaProvider.createSymbol(re.trigger));
		}
	}
	// should never get here
	return null;
}
/**
 * Helper method to create Symbols from their ids
 * Creation date: (2000-08-24 0:13:51)
 * @author: Richard Deadman
 * @return javax.telephony.media.Symbol
 * @param id int
 */
private static Symbol createSymbol(int id) {
	if (id != 0)
		return Symbol.getSymbol(id);
	else
		return null;
}
/**
 * Delegate to remote stub
 */
public boolean freeMedia(java.lang.String terminal, int type) {
	return this.getRemote().freeMedia(terminal, type);
}
/**
 * Get a set or addresses for a remote provider.
 */
public java.lang.String[] getAddresses() throws ResourceUnavailableException {
	try {
		return this.getRemote().getAddresses();
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	}
}
/**
 * Return a set of address names from the remote provider.
 */
public String[] getAddresses(String term) throws InvalidArgumentException {
	try {
		return this.getRemote().getAddressesForTerm(term);
	} catch (InvalidArgumentEx iae) {
		throw new InvalidArgumentException(iae.reason);
	}
}
/**
 * getAddressType method comment.
 */
public int getAddressType(java.lang.String name) {
	return this.getRemote().getAddressType(name);
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
public net.sourceforge.gjtapi.CallData getCall(CallId id) {
	net.sourceforge.gjtapi.raw.remote.corba.CallData cd = this.getRemote().getCall((int)((SerializableCallId)id).getId());
	return this.toCallData(cd);
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
public net.sourceforge.gjtapi.CallData[] getCallsOnAddress(String number) {
	return this.convertCallArray(this.getRemote().getCallsOnAddress(number));
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
public net.sourceforge.gjtapi.CallData[] getCallsOnTerminal(String term) {
	return this.convertCallArray(this.getRemote().getCallsOnTerminal(term));
}
/**
 * getCapabilities method comment.
 */
public java.util.Properties getCapabilities() {
	StringEntry[] sEntries = this.getRemote().getCapabilities();
	Properties props = new Properties();
	int size = sEntries.length;
	for (int i = 0; i < size;i++) {
		props.put(sEntries[i].key, sEntries[i].value);
	}
	return props;
}
/**
 * getDialledDigits method comment.
 */
public java.lang.String getDialledDigits(CallId id, String address) {
	return this.getRemote().getDialledDigits((int)((SerializableCallId)id).getId(), address);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 12:01:45)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.CorbaListener
 */
private CorbaListener getListener() {
	return listener;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 11:09:53)
 * @author: Richard Deadman
 * @return org.omg.CORBA.ORB
 */
private org.omg.CORBA.ORB getOrb() {
	return orb;
}
/**
 * Call getPrivateData on remote interface.
 */
public Object getPrivateData(CallId call, String address, String terminal) {
	try {
		Any res = this.getRemote().getPrivateData((int)((SerializableCallId)call).getId(), address, terminal);
		return CorbaProvider.convertAny(res);
	} catch (NotSerializableEx nse) {
		throw new PlatformException("sendPrivateData result not serializable through remote proxy");
	}
}
/**
 * Return the CORBA Provider's stub.
 * Creation date: (2000-08-18 14:24:48)
 * @author: Richard Deadman
 * @return The CORBA stub that implements the remote CORBA interface.
 */
private net.sourceforge.gjtapi.raw.remote.corba.CorbaProvider getRemote() {
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
public net.sourceforge.gjtapi.TermData[] getTerminals() throws ResourceUnavailableException {
	try {
		return this.convertTermDataArray(this.getRemote().getTerminals());
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	}
}
/**
 * Return a set of terminal names from the remote provider.
 */
public net.sourceforge.gjtapi.TermData[] getTerminals(String address) throws InvalidArgumentException {
	try {
		return this.convertTermDataArray(this.getRemote().getTerminalsForAddr(address));
	} catch (InvalidArgumentEx iae) {
		throw new InvalidArgumentException(iae.reason);
	}
}
/**
 * Send a hold message for a terminal to a remote provider.
 */
public void hold(CallId call, String address, String terminal) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	try {
		this.getRemote().hold((int)((SerializableCallId)call).getId(), address, terminal);
	} catch (PrivilegeViolationEx pve) {
		throw new PrivilegeViolationException(pve.type, pve.reason);
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	} catch (MethodNotSupportedEx mnse) {
		throw new MethodNotSupportedException(mnse.reason);
	} catch (RawStateEx rse) {
		throw new RawStateException(new SerializableCallId(rse.callId), rse.address, rse.terminal,
			rse.type, rse.state, rse.info);
	}
}
/**
 * Initialize my connection to the remote provider.
 * These properties could be used locally or sent to the server for the creation of a user-session.
 * For now, don't send.
 */
public void initialize(java.util.Map props) throws ProviderUnavailableException {
	// check if required properties are set
	String serviceName = (String)props.get("name");
	if (serviceName == null)
		throw new ProviderUnavailableException(ProviderUnavailableException.CAUSE_INVALID_ARGUMENT,
			"Remote CORBA service name not set: \"name\".");
	String hostName = (String)props.get("server");
	if (hostName == null)
		throw new ProviderUnavailableException(ProviderUnavailableException.CAUSE_INVALID_ARGUMENT,
			"Remote CORBA host name not set: \"server\".");

	// Try to find this server
	try {
		// create and initialize the ORB
		Properties orbProps = new Properties();
		//orbProps.put("org.omg.CORBA.ORBInitialPort", "1050");
		orbProps.put("org.omg.CORBA.ORBInitialHost", hostName);
		ORB orb = ORB.init((String[])null, orbProps);
		this.setOrb(orb);

		// get the root naming context
		org.omg.CORBA.Object objRef = orb.resolve_initial_references("NameService");
		NamingContext ncRef = NamingContextHelper.narrow(objRef);

		// resolve the Object Reference in Naming
		NameComponent nc = new NameComponent(serviceName, "");
		NameComponent path[] = {nc};
		this.setRemote(CorbaProviderHelper.narrow(ncRef.resolve(path)));
	} catch (Exception ex) {
		throw new ProviderUnavailableException(ex.getMessage());
	}
}
/**
 * Delegate to the remote stub
 */
public boolean isMediaTerminal(java.lang.String terminal) {
	return this.getRemote().isMediaTerminal(terminal);
}
/**
 * Tell the remote provider to join two calls
 */
public CallId join(CallId call1, CallId call2, String address, String terminal) throws RawStateException, InvalidArgumentException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	try {
		return new SerializableCallId(this.getRemote().join((int)((SerializableCallId)call1).getId(),
			(int)((SerializableCallId)call2).getId(),
			address,
			terminal));
	} catch (PrivilegeViolationEx pve) {
		throw new PrivilegeViolationException(pve.type, pve.reason);
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	} catch (MethodNotSupportedEx mnse) {
		throw new MethodNotSupportedException(mnse.reason);
	} catch (RawStateEx rse) {
		throw new RawStateException(new SerializableCallId(rse.callId), rse.address, rse.terminal,
			rse.type, rse.state, rse.info);
	} catch (InvalidArgumentEx iae) {
		throw new InvalidArgumentException(iae.reason);
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
	try {
		this.getRemote().play(terminal, streamIds, offset, this.toLongEntryArray(rtcs), this.toLongEntryArray(optArgs));
	} catch (MediaResourceEx mre) {
		throw new MediaResourceException(mre.reason,
			this.createEvent(terminal, mre.event));
	}
}
/**
 * Put RTCs into serializable holder and send to remote stub
 */
public void record(String terminal,
	String streamId,
	RTC[] rtcs,
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	try {
		this.getRemote().record(terminal, streamId, this.toLongEntryArray(rtcs), this.toLongEntryArray(optArgs));
	} catch (MediaResourceEx mre) {
		throw new MediaResourceException(mre.reason,
			this.createEvent(terminal, mre.event));
	}
}
/**
 * Tell the remote provider to release an address from a call.
 */
public void release(String address, CallId call) throws PrivilegeViolationException,
	ResourceUnavailableException, MethodNotSupportedException, RawStateException {
	try {
		this.getRemote().release(address, (int)((SerializableCallId)call).getId());
	} catch (PrivilegeViolationEx pve) {
		throw new PrivilegeViolationException(pve.type, pve.reason);
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	} catch (MethodNotSupportedEx mnse) {
		throw new MethodNotSupportedException(mnse.reason);
	} catch (RawStateEx rse) {
		throw new RawStateException(new SerializableCallId(rse.callId), rse.address, rse.terminal,
			rse.type, rse.state, rse.info);
	}
}
/**
 * Release any CallId's that I have reserved.
 */
public void releaseCallId(CallId id) {
	this.getRemote().releaseCallId((int)((SerializableCallId)id).getId());
}
/**
 * Forward removeListener to remote provider.
 */
public synchronized void removeListener(TelephonyListener rl) {
	this.getRemote().removeListener(this.getListener());
}
/**
 * Forward to remote stub
 */
public void reportCallsOnAddress(String address, boolean flag) throws InvalidArgumentException, ResourceUnavailableException {
	try {
		this.getRemote().reportCallsOnAddress(address, flag);
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	} catch (InvalidArgumentEx iae) {
		throw new InvalidArgumentException(iae.reason);
	}
}
/**
 * Forward to remote stub
 */
public void reportCallsOnTerminal(String terminal, boolean flag) throws InvalidArgumentException, ResourceUnavailableException {
	try {
		this.getRemote().reportCallsOnTerminal(terminal, flag);
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	} catch (InvalidArgumentEx iae) {
		throw new InvalidArgumentException(iae.reason);
	}
}
/**
 * Reserve a call id on the remote server.
 */
public CallId reserveCallId(String address) throws InvalidArgumentException {
	try {
		return new SerializableCallId(this.getRemote().reserveCallId(address));
	} catch (InvalidArgumentEx iae) {
		throw new InvalidArgumentException(iae.reason);
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
	try {
		DetectEvent de = this.getRemote().retrieveSignals(terminal, num, CorbaProvider.toLongArray(patterns), this.toLongEntryArray(rtcs), this.toLongEntryArray(optArgs));
		return RawSigDetectEvent.create(terminal, de.event.qualifier, de.sigs, de.pattern, de.event.trigger, de.event.error);
	} catch (MediaResourceEx mre) {
		throw new MediaResourceException(mre.reason,
			this.createEvent(terminal, mre.event));
	}
}
/**
 * Trigger sendPrivateData against the remote interface.
 */
public Object sendPrivateData(CallId call, String address, String terminal, Object data) {
	if (!(data instanceof Serializable))
		throw new PlatformException("sendPrivateData data is not serializable through remote proxy");

	try {
		return CorbaProvider.convertAny(this.getRemote().sendPrivateData((int)((SerializableCallId)call).getId(), address, terminal, this.convertAny(data)));
	} catch (NotSerializableException nsx) {
		throw new PlatformException("sendPrivateData data not serializable through remote proxy" + nsx.getMessage());
	} catch (NotSerializableEx nse) {
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
	try {
		this.getRemote().sendSignals(terminal, CorbaProvider.toLongArray(syms), this.toLongEntryArray(rtcs), this.toLongEntryArray(optArgs));
	} catch (MediaResourceEx mre) {
		throw new MediaResourceException(mre.reason,
			this.createEvent(terminal, mre.event));
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 12:01:45)
 * @author: Richard Deadman
 * @param newListener net.sourceforge.gjtapi.raw.remote.CorbaListener
 */
private void setListener(CorbaListener newListener) {
	listener = newListener;
}
/**
 * setLoadControl method comment.
 */
public void setLoadControl(String startAddr, String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws MethodNotSupportedException {
	try {
		this.getRemote().setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatment);
	} catch (MethodNotSupportedEx mnse) {
		throw new MethodNotSupportedException(mnse.reason);
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-24 11:09:53)
 * @author: Richard Deadman
 * @param newOrb org.omg.CORBA.ORB
 */
private void setOrb(org.omg.CORBA.ORB newOrb) {
	orb = newOrb;
}
/**
 * Send setPrivateData through remote proxy.
 */
public void setPrivateData(CallId call, String address, String terminal, Object data) {
	if (!(data instanceof Serializable))
		throw new PlatformException("setPrivateData data is not serializable through remote proxy");

	try {
		this.getRemote().setPrivateData((int)((SerializableCallId)call).getId(), address, terminal, this.convertAny(data));
	} catch (NotSerializableException nse) {
		throw new PlatformException("sendPrivateData data not serializable through remote proxy" + nse.getMessage());
	}
}
/**
 * Assign the remote proxy.
 * Creation date: (2000-02-17 14:24:48)
 * @author: Richard Deadman
 * @param newRemote net.sourceforge.gjtapi.raw.remote.corba.CorbaProvider
 */
private void setRemote(net.sourceforge.gjtapi.raw.remote.corba.CorbaProvider newRemote) {
	remote = newRemote;
}
/**
 * Tell the remote provider to shutdown.  It may choose to ignore me.
 */
public void shutdown() {
	this.getRemote().shutdown();
}
/**
 * Stop any media actions on the remote terminal.
 */
public void stop(String terminal) {
	this.getRemote().stop(terminal);
}
/**
 * Forward to remote stub
 */
public boolean stopReportingCall(CallId call) {
	return this.getRemote().stopReportingCall((int)((SerializableCallId)call).getId());
}
/**
 * Translate a CORBA CallData holder to a Generic JTAPI CallData holder.
 * Creation date: (2000-08-23 15:55:52)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.CallData
 * @param cd net.sourceforge.gjtapi.raw.remote.corba.CallData
 */
private net.sourceforge.gjtapi.CallData toCallData(net.sourceforge.gjtapi.raw.remote.corba.CallData cd) {
	return new net.sourceforge.gjtapi.CallData(new SerializableCallId(cd.callId), cd.state,
		this.toConnectionData(cd.connections));
}
/**
 * Convert CORBA ConnectionData holder to Generic JTAPI holder
 * Creation date: (2000-08-23 16:00:40)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.ConnectionData[]
 * @param connData net.sourceforge.gjtapi.raw.remote.corba.ConnectionData[]
 */
private net.sourceforge.gjtapi.ConnectionData[] toConnectionData(net.sourceforge.gjtapi.raw.remote.corba.ConnectionData[] connData) {
	int size = connData.length;
	net.sourceforge.gjtapi.ConnectionData[] cd = new net.sourceforge.gjtapi.ConnectionData[size];
	for (int i = 0; i < size; i++) {
		cd[i] = new net.sourceforge.gjtapi.ConnectionData(connData[i].state,
			connData[i].address, connData[i].isLocal, this.toTCData(connData[i].tcs));
	}
	return cd;
}
/**
 * Convert a Symbol array to an int array
 */
static int[] toLongArray(Symbol[] syms) {
	int size = syms.length;
	int[] ia = new int[size];
	for (int i = 0; i < size; i++) {
		ia[i] = (int)syms[i].hashCode();
	}
	return ia;
}
/**
 * Convert a Symbol dictionary to a LongEntry array
 */
private LongEntry[] toLongEntryArray(RTC[] rtcs) {
	int size = rtcs.length;
	LongEntry[] le = new LongEntry[size];
	for (int i = 0; i < size; i++) {
		le[i].key = (int)rtcs[i].getTrigger().hashCode();
		le[i].value = (int)rtcs[i].getAction().hashCode();
	}
	return le;
}
/**
 * Convert a Symbol dictionary to a LongEntry array
 */
private LongEntry[] toLongEntryArray(Dictionary params) {
	Set longEntrySet = new HashSet();
	int size = 0;
	Enumeration e = params.keys();
	while (e.hasMoreElements()) {
		Object key = e.nextElement();
		if (key instanceof Symbol) {
			Symbol skey = (Symbol)key;
			Object value = params.get(key);
			if (value instanceof Symbol) {
				longEntrySet.add(new LongEntry(skey.hashCode(), value.hashCode()));
				size++;
			}
		}
	}
	return (LongEntry[])longEntrySet.toArray(new LongEntry[size]);
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Client proxy for a remote provider";
}
/**
 * Convert an integer array to a Symbol array
 */
static Symbol[] toSymbolArray(int[] ids) {
	int size = ids.length;
	Symbol[] sa = new Symbol[size];
	for (int i = 0; i < size; i++) {
		sa[i] = Symbol.getSymbol(ids[i]);
	}
	return sa;
}
/**
 * Convert CORBA TCData holder to Generic JTAPI holder
 * Creation date: (2000-08-23 16:00:40)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.TCData[]
 * @param connData net.sourceforge.gjtapi.raw.remote.corba.TCData[]
 */
 private net.sourceforge.gjtapi.TCData[] toTCData(net.sourceforge.gjtapi.raw.remote.corba.TCData[] tcData) {
	int size = tcData.length;
	net.sourceforge.gjtapi.TCData[] tcd = new net.sourceforge.gjtapi.TCData[size];
	for (int i = 0; i < size; i++) {
		tcd[i] = new net.sourceforge.gjtapi.TCData(tcData[i].state,
			this.toTermData(tcData[i].terminal));
	}
	return tcd;
}
/**
 * Translate a CORBA TermData holder to a Generic JTAPI TermData holder.
 * Creation date: (2000-08-23 15:55:52)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.TermData
 * @param cd net.sourceforge.gjtapi.raw.remote.corba.TermData
 */
private net.sourceforge.gjtapi.TermData toTermData(net.sourceforge.gjtapi.raw.remote.corba.TermData td) {
	return new net.sourceforge.gjtapi.TermData(td.terminal, td.isMedia);
}
/**
 * Trigger a media runtime control (RTC) action on a remote terminal.
 */
public void triggerRTC(String terminal, javax.telephony.media.Symbol action) {
	this.getRemote().triggerRTC(terminal, (int)action.hashCode());
}
/**
 * Tell the remote provider to unhold a terminal from a call
 */
public void unHold(CallId call, String address, String term) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	try {
		this.getRemote().unHold((int)((SerializableCallId)call).getId(), address, term);
	} catch (PrivilegeViolationEx pve) {
		throw new PrivilegeViolationException(pve.type, pve.reason);
	} catch (ResourceUnavailableEx rue) {
		throw new ResourceUnavailableException(rue.type, rue.reason);
	} catch (MethodNotSupportedEx mnse) {
		throw new MethodNotSupportedException(mnse.reason);
	} catch (RawStateEx rse) {
		throw new RawStateException(new SerializableCallId(rse.callId), rse.address, rse.terminal,
			rse.type, rse.state, rse.info);
	}
}
}
