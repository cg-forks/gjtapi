package net.sourceforge.gjtapi.raw.invert;

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
import javax.telephony.privatedata.PrivateData;
import javax.telephony.callcontrol.*;
import javax.telephony.callcontrol.capabilities.CallControlTerminalConnectionCapabilities;
import javax.telephony.capabilities.*;
import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.capabilities.*;
import javax.telephony.*;
import javax.telephony.media.*;
import java.util.*;
/**
 * This is an Generic JTAPI "raw" Telephony Provider that wraps a JTAPI Provider and allows
 * the Generic JTAPI Framework to act as a decorator to a JTAPI Provider.
 * Creation date: (2000-05-31 17:42:58)
 * @author: Richard Deadman
 */
public abstract class InverterProvider implements net.sourceforge.gjtapi.raw.FullJtapiTpi {
	private final static String RESOURCE_NAME = "Inverter.props";
	private final static String PEER_KEY = "PeerClassName";
	private final static String PROV_KEY = "ProviderString";
	private final static String MEDIA_FREE_RELEASE = "mediaFreeRelease";
	
	private javax.telephony.Provider jtapiProv;
	private Properties provProps;					// temporary map holder
	private InverterListener listener = null;	// an adapter to delegate JTAPI events to TelephonyEvents
	private IdMapper callMap = new IdMapper();	// CallId <-> Call map.
	
	// do we release a MediaService when media is freed from a Terminal?
	private boolean mediaFreeRelease = false;
	
/**
 * Raw constructor used by the GenericJtapiPeer factory
 * Creation date: (2000-02-10 10:28:55)
 * @author: Richard Deadman
 */
public InverterProvider() {
	super();

	// read provider details and load the resources, if available
	this.setProvProps(this.loadResources(InverterProvider.RESOURCE_NAME));

	this.setListener(new InverterListener(this.getCallMap()));
}
/**
 * Set the raw TelephonyListener event receiver.
 * <P>Note that the framework is assumed to set this before any events occur.  Otherwise we will throw
 * null-pointer exceptions back to JTAPI.
 */
public void addListener(TelephonyListener ro) {
	this.getListener().setTListener(ro);
}
/**
 * answerCall method comment.
 */
public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, MethodNotSupportedException, ResourceUnavailableException, RawStateException {
	TerminalConnection tc = this.getTc(call, address, terminal);
	
	if (tc != null) {	// we found the connection
		try {
			tc.answer();
		} catch (InvalidStateException ise) {
			throw new RawStateException(call, address, terminal,
				ise.getObjectType(),
				ise.getState(),
				ise.getMessage());
		}
	} else {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "Could not find terminal connection");
	}
}
/**
 * Connect a call referenced by an id from a terminal connection to a destination address.
 *
 * @param id The id for the already created idle call
 * @param address The name of to originating address
 * @param terminal The name of the originating terminal
 * @param dest The name of the destination address
 * @return The call id.
 */
public CallId createCall(CallId id, String address, String terminal, String dest) throws MethodNotSupportedException, RawStateException, ResourceUnavailableException, InvalidPartyException, InvalidArgumentException, PrivilegeViolationException {
	Call c = this.getCallMap().jtapiCall(id);
	Provider prov = this.getJtapiProv();
	Address addr = prov.getAddress(address);
	Terminal term = prov.getTerminal(terminal);

	try {
		c.connect(term, addr, dest);
	} catch (InvalidStateException ise) {
		throw new RawStateException(id, address, terminal,
			ise.getObjectType(),
			ise.getState(),
			ise.getMessage());
	}
	return id;
}
/**
 * Code to perform when this object is garbage collected.
 * 
 * Any exception thrown by a finalize method causes the finalization to
 * halt. But otherwise, it is ignored.
 */
protected void finalize() throws Throwable {
	try {
		this.getJtapiProv().shutdown();
	} catch (Exception ex) {
		// ignore
	}
}
/**
 * getAddresses method comment.
 */
public java.lang.String[] getAddresses() throws ResourceUnavailableException {
	Address[] addrs = this.getJtapiProv().getAddresses();
	String[] result = new String[addrs.length];

	for (int i = 0; i < addrs.length; i++) {
		result[i] = addrs[i].getName();
	}

	return result;
}
/**
 * Get the Address names associated with a Terminal name.
 */
public String[] getAddresses(String terminal) throws javax.telephony.InvalidArgumentException {
	Address[] addrs = this.getJtapiProv().getTerminal(terminal).getAddresses();
	String[] result = new String[addrs.length];

	for (int i = 0; i < addrs.length; i++) {
		result[i] = addrs[i].getName();
	}

	return result;
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
	// find the call
	Call call = this.getCallMap().jtapiCall(id);
	
	// ensure we are still listening to the call
	try {
		this.register(call);
	} catch (MethodNotSupportedException mnse) {
		// we can't monitor the call!
	} catch (ResourceUnavailableException rue) {
		// we can't monitor the call!
	}

	return this.toCallData(call);
}
/**
 * Accessor for CallId to Call map.
 * Creation date: (2000-06-06 13:04:17)
 * @author: Richard Deadman
 * @return A map of Call to CallId objects.
 */
private IdMapper getCallMap() {
	return callMap;
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
	Address addr = null;
	try {
		addr = this.getJtapiProv().getAddress(number);
	} catch (InvalidArgumentException iae) {
		// fall though
	}
	if (addr != null) {
		HashSet<CallData> calls = new HashSet<CallData>();
		Connection[] conns = addr.getConnections();
		if (conns != null) {
			int connSize = conns.length;
			for (int i = 0; i < connSize; i++) {
				Call call = conns[i].getCall();
				CallData cd = this.toCallData(call);
				if (cd != null) {
					calls.add(cd);
					try {
						this.register(call);
					} catch (Exception ex) {
						// we can't track these!
						System.out.println("Failure to track changes to: " + call + "; Reason: " + ex);
					}
				}
			}
		}
		return (CallData[])calls.toArray(new CallData[0]);
	}
	return null;
}
/**
 * Ask the raw TelephonyProvider to give a snapshot of all Calls at a Terminal.
 * <P>This will only be called on a TelephonyProvider that "trottle"s call events.
 * <P><B>Note:</B> This implies that the given Calls will have events delivered on it until such time
 * as a "TelephonyProvider::releaseCallId(CallId)".
 * Creation date: (2000-06-20 15:22:50)
 * @author: Richard Deadman
 * @return A set of call data.
 * @param name The Terminal's logical name
 */
public CallData[] getCallsOnTerminal(String name) {
	Terminal term = null;
	try {
		term = this.getJtapiProv().getTerminal(name);
	} catch (InvalidArgumentException iae) {
		// fall though
	}
	if (term != null) {
		HashSet<CallData> calls = new HashSet<CallData>();
		TerminalConnection[] tcs = term.getTerminalConnections();
		if (tcs != null) {
			int tcsSize = tcs.length;
			for (int i = 0; i < tcsSize; i++) {
				Call call = tcs[i].getConnection().getCall();
				CallData cd = this.toCallData(call);
				if (cd != null) {
					calls.add(cd);
					try {
						this.register(call);
					} catch (Exception ex) {
						// we can't track these!
						System.out.println("Failure to track changes to: " + call + "; Reason: " + ex);
					}
				}
			}
		}
		return (CallData[])calls.toArray(new CallData[0]);
	}
	return null;
}
/**
 * Interrogate JTAPI capabilities and return a property holder for them to the Generic Framework.
 */
public Properties getCapabilities() {
	Properties props = new Properties();
	Provider prov = this.getJtapiProv();

	// fill in the provider stuff
	CallCapabilities cCap = prov.getCallCapabilities();
	props.put(Capabilities.CREATE, new Boolean(cCap.canConnect()));

	ConnectionCapabilities conCap = prov.getConnectionCapabilities();
	props.put(Capabilities.RELEASE, new Boolean(conCap.canDisconnect()));

	TerminalConnectionCapabilities tcCap = prov.getTerminalConnectionCapabilities();
	props.put(Capabilities.ANSWER, new Boolean(tcCap.canAnswer()));

	if (tcCap instanceof CallControlTerminalConnectionCapabilities) {
		CallControlTerminalConnectionCapabilities cctcc = (CallControlTerminalConnectionCapabilities)tcCap;
		props.put(Capabilities.HOLD, new Boolean(cctcc.canHold()));
		props.put(Capabilities.JOIN, new Boolean(cctcc.canJoin()));
	}

	props.put(Capabilities.THROTTLE, Boolean.TRUE);

	return props;
}
/**
 * Package-protected accessor for the wrapped JTAPI Provider.
 * Creation date: (2000-06-01 14:40:49)
 * @author: Richard Deadman
 * @return A JTAPI Provider I am a raw Generic Telephony Provider adapter for.
 */
protected javax.telephony.Provider getJtapiProv() {
	return jtapiProv;
}
/**
 * Package accessor for the Generic Framework listener adapter that listens to JTAPI events and
 * sends Generic JTAPI Framework events.
 * Creation date: (2000-06-06 13:04:17)
 * @author: Richard Deadman
 * @return The InverterListener
 */
InverterListener getListener() {
	return listener;
}
/**
 * Forward the getPrivateData off to the appropriate object, or return null if the object does not support PrivateData.
 */
public Object getPrivateData(CallId call, String address, String terminal) {
	Object target = this.resolveTarget(call, address, terminal);

	if ((target != null) && (target instanceof PrivateData)) {
		return ((PrivateData)target).getPrivateData();
	} else
		return null;
}
/**
 * Internal accessor for the properties map
 * Creation date: (2000-02-22 14:20:52)
 * @author: Richard Deadman
 * @return The map that lists the provider properties
 */
private Properties getProvProps() {
	return provProps;
}
/**
 * Find the TerminalConnection.
 * @param call A call holding the TerminalConnection
 * @param terminal The address of the TerminalConnection
 * @param terminal The terminal of the TerminalConnection
 * @return The TerminalConnection, or null.
 */
private TerminalConnection getTc(CallId call, String address, String terminal) {
	Call c = this.getCallMap().jtapiCall(call);
	Connection con = null;
	TerminalConnection tc = null;
	
	Connection[] conns = c.getConnections();
	for (int i = 0; i < conns.length && con == null; i++) {
		if (conns[i].getAddress().getName().equals(address)) {
			con = conns[i];
			TerminalConnection[] tcs = con.getTerminalConnections();
			for (int j = 0; j < tcs.length && tc == null; i++) {
				if (tcs[j].getTerminal().getName().equals(terminal)) {
					tc = tcs[j];
				}
			}
		}
	}

	return tc;
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
	Terminal[] terms = this.getJtapiProv().getTerminals();
	TermData[] result = new TermData[terms.length];

	for (int i = 0; i < terms.length; i++) {
		Terminal t = terms[i];
		result[i] = new TermData(t.getName(), t instanceof MediaTerminal);
	}

	return result;
}
/**
 * getTerminals method comment.
 */
public TermData[] getTerminals(String address) throws InvalidArgumentException {
	Terminal[] terms = this.getJtapiProv().getAddress(address).getTerminals();
	TermData[] result = new TermData[terms.length];

	for (int i = 0; i < terms.length; i++) {
		Terminal t = terms[i];
		result[i] = new TermData(t.getName(), t instanceof MediaTerminal);
	}

	return result;
}
/**
 * hold method comment.
 */
public void hold(CallId call, String address, String terminal) throws MethodNotSupportedException, RawStateException, PrivilegeViolationException, ResourceUnavailableException {
	TerminalConnection tc = this.getTc(call, address, terminal);
	if (tc != null) {	// we found the connection
		if (tc instanceof CallControlTerminalConnection) {
			try {
				((CallControlTerminalConnection)tc).hold();
			} catch (InvalidStateException ise) {
				throw new RawStateException(call, address, terminal,
					ise.getObjectType(),
					ise.getState(),
					ise.getMessage());
			}
		} else {
			throw new MethodNotSupportedException("Not a CallControl Terminal Connection.  Can't hold");
		}
	} else {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "Could not find terminal connection");
	}
}
/**
 * Initialize the Inverter Provider with a Map that defines which real JTAPI implementation to load beneath me.
 * This consists of two properties:
 * <ul>
 *  <li><B>PeerClassName</B> That defines the PeerClass name passed into the JtapiPeerFactory
 *  <li><B>ProviderString</B> That defines the provider service string passed into the JtapiPeer to create the provider.
 * </ul>
 * If "replace" is a map property, then the passed in map replaces instead of augmenting the default
 * properties map.
 */
@SuppressWarnings("unchecked")
public void initialize(Map props) throws ProviderUnavailableException {
	Map m = null;
	Object value = null;
	
	// determine if we need to totally replace the current properties
	boolean replace = false;
	if (props != null) {
		value = props.get("replace");
		if (value instanceof String && value != null) {
			String override = (String)value;
			if (override.length() > 0 && Character.toLowerCase(override.charAt(0)) == 't')
				replace = true;
		}
	}
	if (replace) {
		m = props;
	} else {
		m = this.getProvProps();
		m.putAll(props);
	}

	// create an instance of the Jtapi Provider
	JtapiPeer peer = null;
	try {
		peer = JtapiPeerFactory.getJtapiPeer((String)m.get(InverterProvider.PEER_KEY));
	} catch (JtapiPeerUnavailableException jpue) {
		throw new ProviderUnavailableException(jpue.getMessage());
	}
	Provider prov = peer.getProvider((String)m.get(InverterProvider.PROV_KEY));
	this.setJtapiProv(prov);
	
	// set the release property
	String releaseMedia = (String)m.get(InverterProvider.MEDIA_FREE_RELEASE);
	if ((releaseMedia != null) && (releaseMedia.length() > 0) && (releaseMedia.toLowerCase().charAt(0) == 't'))
		this.setMediaFreeRelease(true);
		
	// free the map object
	this.setProvProps(null);

}
/**
 * isMediaTerminal method comment.
 */
public boolean isMediaTerminal(String terminal) {
	Terminal term = null;
	try {
		term = this.getJtapiProv().getTerminal(terminal);
	} catch (InvalidArgumentException iae) {
		return false;
	}

	if (term instanceof MediaTerminal)
		return true;
	else
		return false;
}
/**
 * join method comment.
 */
public CallId join(CallId call1, CallId call2, String address, String terminal) throws MethodNotSupportedException, RawStateException, PrivilegeViolationException, InvalidArgumentException, ResourceUnavailableException {
	IdMapper map = this.getCallMap();
	Call c1 = map.jtapiCall(call1);
	Call c2 = map.jtapiCall(call2);

	if (c1 instanceof CallControlCall) {
		try {
			this.setConferenceController((CallControlCall)c1, address, terminal);
			((CallControlCall)c1).conference(c2);
		} catch (InvalidStateException ise) {
			throw new RawStateException(call1, null, null,
				ise.getObjectType(),
				ise.getState(),
				ise.getMessage());
		}
		return call1;
	} else if (c2 instanceof CallControlCall) {
		try {
			this.setConferenceController((CallControlCall)c2, address, terminal);
			((CallControlCall)c2).conference(c1);
		} catch (InvalidStateException ise) {
			throw new RawStateException(call2, null, null,
				ise.getObjectType(),
				ise.getState(),
				ise.getMessage());
		}
		return call2;
	} else {
		throw new MethodNotSupportedException("Neither Call supports CallControl");
	}
}
/**
 * This method loads the Provider's initial values.
 * Creation date: (2000-02-10 10:11:41)
 * @author: Richard Deadman
 */
private Properties loadResources(String resName) {
	// We must be able to load the properties file
	Properties props = new Properties();
	try {
		props.load(this.getClass().getResourceAsStream("/" + InverterProvider.RESOURCE_NAME));
	} catch (java.io.IOException ioe) {
		// eat and hope that the initialize method sets my required properties
	} catch (NullPointerException npe) {
		// no resource file -- eat as well
	}

	// delay initialization until initialize() called -- allow property replacement

	// return
	return props;
}
/**
 * Ensure that this call is registered with our Listener so that we get all state-change events.
 * <P>Note: We require JTAPI providers under us to report accept Listeners and Observers since we use
 * both to track state.  As of JTAPI 1.2, not all packages supported Listeners, and so we need Observers
 * to track call control state.
 * Creation date: (2000-06-27 9:28:13)
 * @author: Richard Deadman
 * @param call The call to track the state of.
 * @exception javax.telephony.MethodNotSupportedException The JTAPI inverter provider does not support both Observers and Listeners.
 * @exception javax.telephony.ResourceUnavailableException The JTAPI inverter provider has run out of internal resources to track the object's state.
 */
private void register(Call call) throws MethodNotSupportedException, ResourceUnavailableException {
	InverterListener il = this.getListener();
	call.addCallListener(il);
	call.addObserver(il);
}
/**
 * release method comment.
 */
public void release(String address, CallId call) throws PrivilegeViolationException, MethodNotSupportedException, ResourceUnavailableException, RawStateException {
	Call c = this.getCallMap().jtapiCall(call);
	Connection con = null;
	
	Connection[] conns = c.getConnections();
	for (int i = 0; i < conns.length && con == null; i++) {
		if (conns[i].getAddress().getName().equals(address)) {
			con = conns[i];
		}
	}

	if (con != null) {
		try {
			con.disconnect();
		} catch (InvalidStateException ise) {
			throw new RawStateException(call, address, null,
				ise.getObjectType(),
				ise.getState(),
				ise.getMessage());
		}
	} else {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "Connection not found");
	}
}
/**
 * Release the Framework call id.
 */
public void releaseCallId(CallId id) {
	this.getCallMap().freeId(id);
}
/**
 * We should only get this prior to shutdown.
 */
public void removeListener(TelephonyListener ro) {
	this.getListener().setTListener(null);
	// tell InvertorListener to disconnect from all JTAPI objects.
	// this.getListener().disconnect();
}
/**
 * Tell the JTAPI provider to report events on Calls that visit this address.
 */
public void reportCallsOnAddress(String address, boolean flag)
throws InvalidArgumentException, ResourceUnavailableException {
	Address addr = this.getJtapiProv().getAddress(address);

	InverterListener list = this.getListener();

	if (flag)
		try {
			addr.addCallListener(list);
			addr.addCallObserver(list);
		} catch (MethodNotSupportedException mnse) {
			throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "Inverter Observer not supported");
		}
	else {
		addr.removeCallListener(list);
		addr.removeCallObserver(list);
	}
}
/**
 * reportCallsOnTerminal method comment.
 */
public void reportCallsOnTerminal(String terminal, boolean flag)
throws InvalidArgumentException, ResourceUnavailableException {
	Terminal term = this.getJtapiProv().getTerminal(terminal);

	InverterListener list = this.getListener();

	if (flag)
		try {
			term.addCallListener(list);
			term.addCallObserver(list);
		} catch (MethodNotSupportedException mnse) {
			throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "Inverter Observers not supported");
		}
	else {
		term.removeCallListener(list);
		term.removeCallObserver(list);
	}
}
/**
 * Create a Call Object and map it to a CallId.
 */
public CallId reserveCallId(String address) {
	InverterListener il = this.getListener();
	try {
		Call call = this.getJtapiProv().createCall();
			// ensure we monitor this call
		call.addCallListener(il);
		call.addObserver(il);
			// return its id
		return this.getCallMap().getId(call);
	} catch (Exception e) {
			// ResourceUnavailableException, InvalidStateException,
			// PrivilegeViolationException, MethodNotSupportedException
		return null;
	}
}
/**
 * Resolve the target information into a JTAPI object.
 * Creation date: (2000-08-09 12:04:32)
 * @author: Richard Deadman
 * @return The appropriate JTAPI object based on the parameters, or null if it could not be resolved.
 * @param call If null, used for Providers, Addresses and Terminals; otherwise Calls and Connections
 * @param addr If null, used for Providers, Calls and Terminals; otherwise Addresses, Connections and TerminalConnections
 * @param term If null, used for Providers, Calls, Addresses and Connections; otherwise Terminals and TerminalConnections, 
 */
private Object resolveTarget(CallId call, String addr, String term) {
	Provider prov = this.getJtapiProv();

	Call c = null;
	Address a = null;
	Terminal t = null;

		// first check if a provider is requested
	if ((call == null) && (addr == null) && (term == null)) {
		return prov;
	}
	
		// first check Calls, Connections and TerminalConnections
	if (call != null) {
		IdMapper map = this.getCallMap();
		c = map.jtapiCall(call);
		if (c == null) {
			// bad mapping
			return c;
		}
	}

	// Now look up Address, Connection and TerminalConnection info
	if (addr != null) {
		try {
			a = prov.getAddress(addr);
		} catch (InvalidArgumentException iae) {
			// bad mapping
			return null;
		}
	}

	// Now look up Terminal and TerminalConnection info
	if (term != null) {
		try {
			t = prov.getTerminal(term);
		} catch (InvalidArgumentException iae) {
			// bad mapping
			return null;
		}
	}

	// check if we want a Call, Connection or TerminalConnection
	if (call != null) {
		if (addr != null) {
			Connection[] conns = c.getConnections();
			int csize = conns.length;
			for (int i = 0; i < csize; i++) {
				if (conns[i].getAddress().equals(a)) {
					Connection conn = conns[i];
					if (term != null) { // want the TerminalConnection
						TerminalConnection[] tcs = conn.getTerminalConnections();
						int tcSize = tcs.length;
						for (int j = 0; j < tcSize; j++) {
							if (tcs[j].getTerminal().equals(t))
								return tcs[j];
						}
					} else { // just want the connection
						return conn;
					}
				}
			}
		} else {	// just want the call
			return c;
		}
	} else {	// want the Address or Terminal
		if (addr != null) {
			return a;
		} else if (term != null) {
			return t;
		}
	}

	// return null to indicate that the object could not be resolved.
	return null;
}
/**
 * Forward the sendPrivateData off to the appropriate object, or return null if the object does not support PrivateData.
 */
public Object sendPrivateData(CallId call, String address, String terminal, Object data) {
	Object target = this.resolveTarget(call, address, terminal);

	if ((target != null) && (target instanceof PrivateData)) {
		return ((PrivateData)target).sendPrivateData(data);
	} else
		return null;
}
/**
 * Try to find the terminal connection for the call and set it as the conference controller.
 * Creation date: (2000-10-04 13:10:40)
 * @param call javax.telephony.callcontrol.CallControlCall
 * @param address The TerminalConnection's Connection address name.
 * @param terminal The TerminalConnection's terminal name.
 */
private void setConferenceController(CallControlCall call, String address, String terminal) {
	if ((address == null) || (terminal == null))
		return;

	Connection[] conns = call.getConnections();
	int connSize = conns.length;
	for (int i = 0; i < connSize; i++) {
		if (conns[i].getAddress().getName().equals(address)) {
			TerminalConnection[] tcs = conns[i].getTerminalConnections();
			int tcSize = tcs.length;
			for (int j = 0; j < tcSize; j++) {
				if (tcs[i].getTerminal().getName().equals(terminal)) {
					try {
						call.setConferenceController(tcs[i]);
					} catch (Exception e) {
						// we tried...
					}
					return;
				}
			}
		}
	}
}
/**
 * Internal setter for the managed Jtapi provider I am an Adapter for.
 * Creation date: (2000-06-01 14:40:49)
 * @author: Richard Deadman
 * @param newJtapiProv A plugged-in JTAPI adapter
 */
private void setJtapiProv(Provider newJtapiProv) {
	this.jtapiProv = newJtapiProv;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-06-06 13:04:17)
 * @author: Richard Deadman
 * @param newListener net.sourceforge.gjtapi.raw.invert.InverterListener
 */
private void setListener(InverterListener newListener) {
	listener = newListener;
}
/**
 * Forward the setPrivateData off to the appropriate object, or eat the command if the object does not support PrivateData.
 */
public void setPrivateData(CallId call, String address, String terminal, Object data) {
	Object target = this.resolveTarget(call, address, terminal);

	if ((target != null) && (target instanceof PrivateData)) {
		((PrivateData)target).setPrivateData(data);
	}
}
/**
 * Internal setter
 * Creation date: (2000-02-22 14:20:52)
 * @author: Richard Deadman
 * @param newProvProps A new set of properties
 */
private void setProvProps(Properties newProvProps) {
	provProps = newProvProps;
}
/**
 * Tell the wrapped JTAPI provider to shut down.
 */
public void shutdown() {
	this.getJtapiProv().shutdown();
}
/**
 * reportCall method comment.
 */
public boolean stopReportingCall(CallId id) {
	Call call = this.getCallMap().jtapiCall(id);
	if (call == null)
		return false;

	InverterListener list = this.getListener();

	call.removeCallListener(list);
	call.removeObserver(list);

	return true;
}
/**
 * Convert a Jtapi Call object to a CallData snapshot.
 * Creation date: (2000-06-24 0:13:14)
 * @author: Richard Deadman
 * @return A snapshot of the call's state.
 * @param c A Jtapi Call.
 */
private CallData toCallData(Call c) {
	Connection[] conns = c.getConnections();
	ConnectionData[] cd = null;
	if (conns != null) {
		int connSize = conns.length;
		cd = new ConnectionData[connSize];
		for (int i = 0; i < connSize; i++) {
			Connection con = conns[i];
			TerminalConnection[] tcs = con.getTerminalConnections();
			TCData[] tcd = null;
			if (tcs != null) {
				int tcSize = tcs.length;
				tcd = new TCData[tcSize];
				for (int j = 0; j < tcSize; j++) {
					TerminalConnection tc = tcs[i];
					Terminal term = tc.getTerminal();
						// find the CallControlTerminalConnection state
					int cctcs = CallControlTerminalConnection.UNKNOWN;
					if (tc instanceof CallControlTerminalConnection) {
						cctcs = ((CallControlTerminalConnection)tc).getCallControlState();
					} else {
						switch (tc.getState()) {
							case TerminalConnection.ACTIVE: {
								cctcs = CallControlTerminalConnection.TALKING;
								break;
							}
							case TerminalConnection.DROPPED: {
								cctcs = CallControlTerminalConnection.DROPPED;
								break;
							}
							case TerminalConnection.IDLE: {
								cctcs = CallControlTerminalConnection.IDLE;
								break;
							}
							case TerminalConnection.PASSIVE: {
								cctcs = CallControlTerminalConnection.BRIDGED;
								break;
							}
							case TerminalConnection.RINGING: {
								cctcs = CallControlTerminalConnection.RINGING;
								break;
							}
						}
					}
					tcd[j] = new TCData(cctcs, new TermData(term.getName(),
											term instanceof MediaTerminal));
				}
			}
			String addrName = con.getAddress().getName();
			boolean local = true;
			// test if the Address is not local
			try {
				this.getJtapiProv().getAddress(addrName);
			} catch (InvalidArgumentException iae) {
				local = false;
			}
			cd[i] = new ConnectionData(con.getState(),
							addrName,
							local,
							tcd);
		}
	}
	return new CallData(this.getCallMap().getId(c), c.getState(), cd);
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "A Generic JTAPI TelephonyProvider adapter for a JTAPI provider named: " + this.getJtapiProv().toString();
}
/**
 * unHold method comment.
 */
public void unHold(CallId call, String address, String terminal) throws MethodNotSupportedException, RawStateException, PrivilegeViolationException, ResourceUnavailableException {
	TerminalConnection tc = this.getTc(call, address, terminal);
	if (tc != null) {	// we found the connection
		if (tc instanceof CallControlTerminalConnection) {
			try {
				((CallControlTerminalConnection)tc).unhold();
			} catch (InvalidStateException ise) {
				throw new RawStateException(call, address, terminal,
					ise.getObjectType(),
					ise.getState(),
					ise.getMessage());
			}
		} else {
			throw new MethodNotSupportedException("Not a CallControl Terminal Connection.  Can't hold");
		}
	} else {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "Could not find terminal connection");
	}
}
	/**
	 * Should we release the NewMedia MediaService on freeing the media
	 * terminal?
	 * @return boolean
	 */
	public boolean mediaFreeRelease() {
		return mediaFreeRelease;
	}

	/**
	 * Sets the mediaFreeRelease state used by the NewMediaProvider.
	 * @param mediaFreeRelease The mediaFreeRelease to set
	 */
	private void setMediaFreeRelease(boolean mediaFreeRelease) {
		this.mediaFreeRelease = mediaFreeRelease;
	}

}
