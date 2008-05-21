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
import javax.telephony.*;
import javax.telephony.media.*;
import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.capabilities.*;
import net.sourceforge.gjtapi.raw.*;
import java.util.*;
import java.io.*;
/**
 * This is a pluggable provider that multiplexes several other providers together.
 * <P>Note that this decides which subprovider to delegate calls off to using Call, Address and
 * Terminal to subprovider maps.  In the case where a sub-provider does not return a full set of Address
 * and Terminal information (throws ResourceUnavailableException for getAddresses() or allows dynamic
 * addresses by returning "dynamicAddresses = t" in its capabilities Properties object), we may not have
 * full delegation information.  We have two options:
 * <ol>
 *  <li>Broadcast all requests where the receiver isn't known
 *  <li>Catch all address and terminal mappings in Listener events.
 * </ol>
 * Currently we do both, with no tuning to try to determine if the extra cost of Listener interception
 * is worth it.  The three methods that provide broadcast are:
 * <ul>
 *  <li>reportCallsOnAddress(String address, boolean flag)
 *  <li>reportCallsOnTerminal(String terminal, boolean flag)
 *  <li>reserveCallId(String address)
 * </ul>
 * Creation date: (2000-02-22 10:53:56)
 * @author: Richard Deadman
 */
public class MuxProvider implements TelephonyProvider {
	private final static String RESOURCE_NAME = "Mux.props";
	private final static String PROVIDER_PREFIX = "Provider";
	private final static String CLASS_PREFIX = "Class_";
	private final static String PROPS_PREFIX = "Props_";

	private final static int UNFLUSHED = 0;
	private final static int FLUSHED = 1;
	private final static int TOOBIG = 2;

	private Map addToSub = new HashMap();
	private int addrFlag = UNFLUSHED;
	private int termFlag = UNFLUSHED;
	private HashSet termData = new HashSet();	// Set of TermData holders.
	private Map subToCaps = new HashMap();	// maps sub-providers to RawCapabilities sets
	/**
	 * Set of MuxCallIds that hold 1 or more CallHolders.  Each MuxCallId is a logical
	 * call possibly bridged across multiple low-level sub-providers.
	 **/
	private Set calls = new HashSet();
	private Map lowToLogicalMap = new HashMap();
	private Map termToSub = new HashMap();
/**
 * Add a MuxCallId and its backwards lookup holder to my call set.
 * Creation date: (2000-09-26 16:13:08)
 * @param ch net.sourceforge.gjtapi.raw.mux.CallHolder
 * @param mci net.sourceforge.gjtapi.raw.mux.MuxCallId
 */
private void addCall(CallHolder ch, MuxCallId mci) {
	this.getCalls().add(mci);
	this.getLowToLogicalMap().put(ch, mci);
}
/**
 * Forward to remote provider
 */
public void addListener(TelephonyListener rl) {
	Iterator it = this.getSubProviders().iterator();
	while (it.hasNext()) {
		TelephonyProvider rp = ((TelephonyProvider)it.next());
		rp.addListener(new MuxListener(rl, this, rp));
	}
}
/**
  * Allocate a media type resource to a sub-provider's terminal.
  * Creation date: (2000-03-09 10:52:06)
  * @param: terminal The terminal to be attached to media resources
  * @param type A flag telling the type of media to attach.  See RawProvider for static types.
  * @param params Control paramters for the resource.
  * @return: true if the media was allocated.
  */
public boolean allocateMedia(String terminal, int type, Dictionary params) {
	TelephonyProvider rp = this.getTerminalSub(terminal);
	if (((RawCapabilities)this.getSubToCaps().get(rp)).allocateMedia)
		return rp.allocateMedia(terminal, type, params);
	else
		return true;	// The rawProvider guaranteed that it does not need this call.
}
/**
 * Forward answerCall to remote provider
 */
public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
	  MethodNotSupportedException, RawStateException {
	CallHolder ch = ((MuxCallId)call).getLeg(address);
	if (ch != null) {
		ch.getTpi().answerCall(ch.getCall(), address, terminal);
	}
}
/**
 * attachMedia method comment.
 */
public boolean attachMedia(net.sourceforge.gjtapi.CallId call, java.lang.String address, boolean onFlag) {
	CallHolder ch = ((MuxCallId)call).getLeg(address);
	if (ch != null) {
		return ch.getTpi().attachMedia(ch.getCall(), address, onFlag);
	} else
		return false;
}
/**
 * beep method comment.
 */
public void beep(net.sourceforge.gjtapi.CallId call) {
	Iterator it = ((MuxCallId)call).getCallHolders();
	while (it.hasNext()) {
		CallHolder ch = (CallHolder)it.next();
		ch.getTpi().beep(ch.getCall());
	}
}
/**
 * Create a call from the given address and terminal to the remote address
 */
public CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException,
	  InvalidPartyException, InvalidArgumentException, RawStateException,
	  MethodNotSupportedException {
	MuxCallId logicalCall = (MuxCallId)id;
	CallHolder ch = logicalCall.getLeg(address);
	if (ch == null) {
		TelephonyProvider sub = this.getAddressSub(address);
		if (sub == null)
			throw new InvalidPartyException(InvalidPartyException.ORIGINATING_PARTY);
		else {
			ch = new CallHolder(sub.reserveCallId(address), sub);
			logicalCall.addCall(ch);
		}
	}
		
	return ch.getTpi().createCall(ch.getCall(), address, term, dest);
}
/**
 * find a logical CallId for the given low-level call.
 * Creation date: (2000-09-24 0:45:49)
 * @param subCall Low-level CallId
 * @param subTpi Low-level TelephonyProvider that holds the call, or null.
 */
MuxCallId findCall(CallId subCall, TelephonyProvider subTpi) {
	// Check for the logical CallId in the backward lookup table
	return this.findCall(new CallHolder(subCall, subTpi));
}
/**
 * find a logical CallId for the given low-level call.
 * Creation date: (2000-09-24 0:45:49)
 * @param ch The CallHolder that represents the sub-call
 */
MuxCallId findCall(CallHolder ch) {
	// Check for the logical CallId in the backward lookup table
	return (MuxCallId)this.getLowToLogicalMap().get(ch);
}
/**
  * Free a media type resource from a sub-provider's terminal.
  * Creation date: (2000-03-09 10:52:06)
  * @param: terminal The terminal to be freed from media resources
  * @param type A flag telling the type of media to free.  See RawProvider for static types.
  * @return: true if the media was freed.
  */
public boolean freeMedia(String terminal, int type) {
	TelephonyProvider rp = this.getTerminalSub(terminal);
	if (((RawCapabilities)this.getSubToCaps().get(rp)).allocateMedia)
		return rp.freeMedia(terminal, type);
	else
		return true;	// The rawProvider guaranteed that it does not need this call.
}
/**
 * Take the values pointed to by the existing and new and return the greatest common denominator in String
 * format.
 * Creation date: (2000-03-14 15:20:48)
 * @author: Richard Deadman
 * @param existing An existing value, a String of form "txxx" or "fxxx".
 * @param update A new String representation of a boolean value.
 */
private Object gcd(Object existing, Object update) {
	if ((existing instanceof String && ((String)existing).length() > 0 &&
			Character.toLowerCase(((String)existing).charAt(0)) == 't') ||
	    (existing instanceof Boolean && ((Boolean)existing).booleanValue()))
			// can stop now - already true
			return existing;

		// test if existing false and update exists -- replace with update
	if ((update instanceof String && ((String)update).length() > 0) || (update instanceof Boolean)) {
		return update;
	}

	// existing was false and update not valid
	return existing;
}
/**
 * Return the static set of addresses I manage, using lazy instantiation.
 * Creation date: (2000-02-22 15:32:00)
 * @author: Richard Deadman
 * @return The set of know addresses I manage
 */
public String[] getAddresses() throws ResourceUnavailableException {
	Map addMap = this.getAddToMap();

		// test if we need to flush the addresses in
	if (this.addrFlag == MuxProvider.UNFLUSHED) {
		synchronized (this) {
			if (this.addrFlag == MuxProvider.UNFLUSHED) {	// double check
					// ask each sub-provider
				Iterator it = this.getSubProviders().iterator();
				while (it.hasNext()) {
					TelephonyProvider sub = (TelephonyProvider)it.next();
					String[] subAddrs = null;
					try {
						subAddrs = sub.getAddresses();
					} catch (ResourceUnavailableException rue) {
						this.addrFlag = MuxProvider.TOOBIG;	// mark as not all available
						break;
					}
					int size = subAddrs.length;
					for (int i = 0; i < size; i++) {
						addMap.put(subAddrs[i], sub);
					}
				}
				if (this.addrFlag == MuxProvider.UNFLUSHED)
					this.addrFlag = FLUSHED;
			}
		}
	}
	// now test for too big
	if (this.addrFlag == MuxProvider.TOOBIG) {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN,
			"Some Sub-TelephonyProviders cannot return all addresses");
	}

		// must now be set to FLUSHED
	return (String[])addMap.keySet().toArray(new String[0]);
}
/**
 * Return from the remote provider a set of address names associated with a terminal.
 */
public String[] getAddresses(String terminal) throws InvalidArgumentException {
	TelephonyProvider sub = this.getTerminalSub(terminal);
	String[] addrs = null;
	if (sub != null) {
		addrs = sub.getAddresses(terminal);
	} else {	// broadcast
		boolean found = false;
		Iterator it = this.getSubProviders().iterator();
		while (it.hasNext() && !found) {
			try {
				sub = (TelephonyProvider)it.next();
				addrs = sub.getAddresses(terminal);
					// didn't throw exception -- success
				found = true;
				this.getTermToMap().put(terminal, sub);
			} catch (InvalidArgumentException iae) {
				// eat and move on to next provider
			}
		}
		if (!found)
			throw new InvalidArgumentException("No muxed subproviders know this Terminal: " + terminal);
	}

	// now map these to the sub-provider
	if (addrs != null) {
		Map addrMap = this.getAddToMap();
		int size = addrs.length;
		for (int i = 0; i < size; i++)
			addrMap.put(addrs[i], sub);
	}

	// now return the set
	return addrs;
}
/**
 * Get the sub-provider mapped to by a certain Address
 * Creation date: (2000-03-09 12:26:30)
 * @author: Richard Deadman
 * @return The subprovider that handles the address
 * @param address A address name to find a subprovider for
 */
private TelephonyProvider getAddressSub(String address) {
	return (TelephonyProvider)this.getAddToMap().get(address);
}
/**
 * getAddressType method comment.
 */
public int getAddressType(java.lang.String name) {
	TelephonyProvider sub = this.getAddressSub(name);
	return sub.getAddressType(name);
}
/**
 * Private accessor for the Address to Subprovider map
 * Creation date: (2000-02-22 13:55:20)
 * @author: Richard Deadman
 * @return The map that maps addresses to sub-providers.
 */
private Map getAddToMap() {
	return addToSub;
}
/**
 * Find the call snapshot for a given call
 */
public CallData getCall(CallId id) {
	Iterator it = ((MuxCallId)id).getCallHolders();
	Map connections = new HashMap();	// map of address to best ConnectionData holde -- local if found
	int state = Call.IDLE;			// best known state.

	// now collect all connections and not most active call
	while (it.hasNext()) {
		CallHolder ch = (CallHolder)it.next();
		TelephonyProvider sub = ch.getTpi();
		CallData call = sub.getCall(ch.getCall());

		// test if any branches are ACTIVE
		if (call.callState == Call.ACTIVE)
			state = call.callState;

		// now map these to the sub-provider
		if ((call != null) && (call.connections != null)) {
			// record all Addresses and Terminals
			int connSize = call.connections.length;
			for (int i = 0; i < connSize; i++) {
				ConnectionData cd = call.connections[i];
				if (this.mapConnection(cd, sub)) {
					// note the local connections, overriding any remote entry
					// remote connections will be reported by other sub-providers
					connections.put(cd.address, cd);
				} else {
					// only add to connections if no local yet found
					if (!connections.containsKey(cd.address))
						connections.put(cd.address, cd);
				}
			}
		}
	}

	// Merge the set of morphed sub-calls
	CallData mergedCall = null;
	int size = connections.size();
	if (size > 0) {
		mergedCall = new CallData(id, state, (ConnectionData[])connections.values().toArray(new ConnectionData[size]));
	}
	
	// now return the set
	return mergedCall;
}
/**
 * Internal accessor for the set of logical calls I'm handling.
 * Creation date: (2000-02-22 16:06:01)
 * @author: Richard Deadman
 * @return The set of logical calls that may be bridged across more than 1 sub-provider calls.
 */
private Set getCalls() {
	return calls;
}
/**
 * Get a set of CallData snapshots for all calls that are attached to an address.
 * This will follow logical links to call legs in other sub-providers.
 */
public CallData[] getCallsOnAddress(String number) {
	TelephonyProvider sub = this.getAddressSub(number);
	if (sub != null) {
		CallData[] cd = sub.getCallsOnAddress(number);
	
		// now map these calls to the sub-provider
		if (cd != null) {
			int cdSize = cd.length;
			for (int i = 0; i < cdSize; i++) {	// for each found call
				CallData call = cd[i];
					// lazily create the logical call
				MuxCallId callId = this.noteCall(call.id, sub);
					// trace the call to other providers and store back in our array
				cd[i] = this.merge(call, this.traceCalls(callId, call.connections, new HashSet()));
					// record the sub-parts
				this.mapCall(callId, call, sub);
			}
				// return the traced calls
			return cd;
		}
	}

	// We found no calls
	return null;
}
/**
 * Get a set of CallData snapshots for all calls that are attached to a Terminal.
 * This will follow logical links to call legs in other sub-providers.
 */
public CallData[] getCallsOnTerminal(String name) {
	TelephonyProvider sub = this.getTerminalSub(name);
	if (sub != null) {
		CallData[] cd = sub.getCallsOnTerminal(name);
	
		// now map these calls to the sub-provider
		if (cd != null) {
			int cdSize = cd.length;
			for (int i = 0; i < cdSize; i++) {	// for each found call
				CallData call = cd[i];
					// lazily create the logical call
				MuxCallId callId = this.noteCall(call.id, sub);
					// trace the call to other providers and store back in our array
				cd[i] = this.merge(call, this.traceCalls(callId, call.connections, new HashSet()));
					// record the sub-parts
				this.mapCall(callId, call, sub);
			}
				// return the traced calls
			return cd;
		}
	}

	// We found no calls
	return null;
}
/**
 * Create the lowest common denominator of capabilities and return it.
 */
public java.util.Properties getCapabilities() {
	Properties merged = new Properties();

	// load the set of capabilities
	Iterator subs = this.getSubToCaps().values().iterator();
	while (subs.hasNext()) {
		Properties subProps = ((Properties)subs.next());

		if (subProps != null) {
			// test each capability
			Iterator keys = subProps.keySet().iterator();
			while (keys.hasNext()) {
				Object key = keys.next();
				Object val = null;
				if (key.equals(Capabilities.THROTTLE) || key.equals(Capabilities.MEDIA) ||
					key.equals(Capabilities.ALLOCATE_MEDIA) || key.equals(Capabilities.DYNAMIC_ADDRESSES) ||
					((key instanceof String) && (((String)key).endsWith(Capabilities.PD))))
					val = this.gcd(merged.get(key), subProps.get(key));
				else
					val = this.lcd(merged.get(key), subProps.get(key));
				merged.put(key, val);
			}
		}
	}
	return merged;
}
/**
 * getDialledDigits method comment.
 */
public java.lang.String getDialledDigits(net.sourceforge.gjtapi.CallId id, java.lang.String address) {
	CallHolder ch = ((MuxCallId)id).getLeg(address);
	if (ch != null) {
		return ch.getTpi().getDialledDigits(ch.getCall(), address);
	} else
		return null;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-09-25 16:27:57)
 * @return java.util.Map
 */
private java.util.Map getLowToLogicalMap() {
	return lowToLogicalMap;
}
/**
 * Find the sub-provider to forward the getPrivateData call to, and forward it.
 */
public Object getPrivateData(CallId call, String address, String terminal) {
	TelephonyProvider tp = null;	// if single sub-provider identified
	CallId newCallId = null;
	Iterator it = null;				// if call bridges sub-providers, or broadcast required
		// check if call ids the provider
	if (call != null) {
		if  (address != null) {		// Connection of TerminalConnection
			CallHolder ch = this.getSub(call, address);
			tp = ch.getTpi();
			newCallId = ch.getCall();
		} else {					// Broadcast to all multiplexed Calls
			it = ((MuxCallId)call).getCallHolders();
			Set set = new HashSet();
			while (it.hasNext()) {
				CallHolder ch = (CallHolder)it.next();
				Object o = ch.getTpi().getPrivateData(ch.getCall(), address, terminal);
				if (o != null)
					set.add(o);
			}
			return set.toArray();
		}
	} else if (address != null) {	// Call is null - just Address defined
		// check if the address will now do it
		tp = this.getAddressSub(address);
	} else if (terminal != null) {	// Call is null - just Terminal defined
		// check if only the terminal will id it.
		tp = this.getTerminalSub(terminal);
	} else {						// all null - Provider broadcast
		// all providers are required
		Set set = new HashSet();
		it = this.getSubProviders().iterator();
		while (it.hasNext()) {
			Object o = ((TelephonyProvider)it.next()).getPrivateData(call, address, terminal);
			if (o != null)
				set.add(o);
		}
		return set.toArray();
	}

		// one provider found
	if (tp != null) {
		return tp.getPrivateData(newCallId, address, terminal);
	}

		// no providers found
	return null;
}
/**
 * Get the sub-provider for a logical call that is handling a particular address.
 * This allows a call being bridged across multiple sub-providers to find the correct sub-provider.
 * Creation date: (2000-03-09 12:26:30)
 * @author: Richard Deadman
 * @return The subprovider that handles the call's address
 * @param id A call id to find a subprovider for
 * @paran address An address that identifies a call-leg handled by one of the sub-providers.
 */
private CallHolder getSub(CallId id, String address) {
	return ((MuxCallId)id).getLeg(address);
}
/**
 * Internal accessor.
 * Creation date: (2000-02-22 15:37:42)
 * @author: Ricahrd Deadman
 * @return The set of RawProviders I delegate to.
 */
private Set getSubProviders() {
	return this.getSubToCaps().keySet();
}
/**
 * Internal accessor
 * Creation date: (2000-02-22 16:06:01)
 * @author: Richard Deadman
 * @return The Sub-provider to RawCapabilites map.
 */
private Map getSubToCaps() {
	return subToCaps;
}
/**
 * Return the static set of terminals I manage, using lazy instantiation.
 * Creation date: (2000-02-22 15:32:00)
 * @author: Richard Deadman
 * @return The set of know addresses I manage
 */
public TermData[] getTerminals() throws ResourceUnavailableException {
	Map termMap = this.getTermToMap();
	HashSet tdSet = this.termData;

		// test if we need to flush the addresses in
	if (this.termFlag == MuxProvider.UNFLUSHED) {
		synchronized (tdSet) {
			if (this.termFlag == MuxProvider.UNFLUSHED) {	// double check
					// ask each sub-provider
				Iterator it = this.getSubProviders().iterator();
				while (it.hasNext()) {
					TelephonyProvider sub = (TelephonyProvider)it.next();
					TermData[] subTerms = null;
					try {
						subTerms = sub.getTerminals();
					} catch (ResourceUnavailableException rue) {
						this.termFlag = MuxProvider.TOOBIG;	// mark as not all available
						tdSet.clear();			// might as well clear the TermData holder
						this.termData = null;
						break;
					}
					int size = subTerms.length;
					for (int i = 0; i < size; i++) {
						TermData td = subTerms[i];
						tdSet.add(td);
						termMap.put(td.terminal, sub);
					}
				}
				if (this.termFlag == MuxProvider.UNFLUSHED)
					this.termFlag = FLUSHED;
			}
		}
	}
	// now test for too big
	if (this.termFlag == MuxProvider.TOOBIG) {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN,
			"Some Sub-TelephonyProviders cannot return all addresses");
	}

		// must now be set to FLUSHED
	return (TermData[])tdSet.toArray(new TermData[0]);
}
/**
 * Return from the remote provider a set of terminal names for an address.
 */
public TermData[] getTerminals(String address) throws InvalidArgumentException {
	TelephonyProvider sub = this.getAddressSub(address);
	TermData[] terms = null;
	if (sub != null) {
		terms = sub.getTerminals(address);
	} else {	// broadcast
		boolean found = false;
		Iterator it = this.getSubProviders().iterator();
		while (it.hasNext() && !found) {
			try {
				sub = (TelephonyProvider)it.next();
				terms = sub.getTerminals(address);
					// didn't throw exception -- success
				found = true;
				this.getAddToMap().put(address, sub);
			} catch (InvalidArgumentException iae) {
				// eat and move on to next provider
			}
		}
		if (!found)
			throw new InvalidArgumentException("No muxed subproviders know this Address: " + address);
	}

	// now map these to the sub-provider
	if (terms != null) {
		Map termMap = this.getTermToMap();
		int size = terms.length;
		for (int i = 0; i < size; i++)
			termMap.put(terms[i].terminal, sub);
	}

	// now return the set
	return terms;
}
/**
 * Get the sub-provider mapped to by a certain Terminal
 * Creation date: (2000-03-09 12:26:30)
 * @author: Richard Deadman
 * @return The subprovider that handles the terminal
 * @param address A terminal name to find a subprovider for
 */
private TelephonyProvider getTerminalSub(String terminal) {
	return (TelephonyProvider)this.getTermToMap().get(terminal);
}
/**
 * Private accessor for terminal map.
 * Creation date: (2000-03-09 10:37:55)
 * @author: Richard Deadman
 * @return The map that maps terminal names to sub-providers.
 */
private java.util.Map getTermToMap() {
	return termToSub;
}
/**
 * Send a hold message for a terminal to a remote provider.
 */
public void hold(CallId call, String address, String term) throws RawStateException, MethodNotSupportedException,
		PrivilegeViolationException, ResourceUnavailableException {
	TelephonyProvider sub = this.getAddressSub(address);
	if (sub != null)
		sub.hold(call, address, term);
}
/**
 * Load my properties and use to look-up my set of sub-providers
 * These properties could be used locally or sent to the server for the creation of a user-session.
 */
public void initialize(Map props) throws ProviderUnavailableException {
	Map m = null;
	Object value = null;
	
	// See if I also need to load the properties file
	boolean replace = false;
	if (props != null) {
		value = props.get("replace");
		replace = net.sourceforge.gjtapi.capabilities.Capabilities.resolve(value);
	}
	if (replace)
		m = props;
	else {
		m = this.loadResources(MuxProvider.RESOURCE_NAME);
		if (props != null)
			m.putAll(props);
	}

	// Now load each sub-provider
	Map subCaps = this.getSubToCaps();
	
	Iterator it = m.keySet().iterator();
	while (it.hasNext()) {
		String key = (String)it.next();
		if (key.startsWith(MuxProvider.PROVIDER_PREFIX)) {
			String em = (String)m.get(key);
			String cn = (String)m.get(MuxProvider.CLASS_PREFIX+em);
			String propfile = (String)m.get(MuxProvider.PROPS_PREFIX+em);

			// load the sub-provider
			TelephonyProvider rp = null;
			try {
				rp = ProviderFactory.createProvider((CoreTpi)Class.forName(cn).newInstance());
			} catch (Exception ex) {
				throw new ProviderUnavailableException();
			}
			rp.initialize(this.loadResources(propfile));

			// add the provider's RawCapabilities
			subCaps.put(rp, rp.getCapabilities());
		}
	}
}
/**
 * Delegate the call on to the appropriate sub-provider, if it registered for these calls.
 */
public boolean isMediaTerminal(String terminal) {
	TelephonyProvider rp = this.getTerminalSub(terminal);
	if (((RawCapabilities)this.getSubToCaps().get(rp)).allMediaTerminals)
		return true;	// The rawProvider guaranteed that all terminals are media terminals.
	else
		return rp.isMediaTerminal(terminal);
}
/**
 * Determine if the given sub-provider throttles calls.
 */
/*private boolean isThrottled(TelephonyProvider rp) {
	Properties props = (Properties)this.getSubToCaps().get(rp);
	if (props != null) {
		return Capabilities.resolve(props.get(Capabilities.THROTTLE));
	}
	return true;	// Assume it does
}
*/
/**
 * Tell the remote provider to join two calls
 */
public CallId join(CallId call1, CallId call2, String address, String terminal) throws RawStateException, InvalidArgumentException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	MuxCallId mCall1 = (MuxCallId)call1;
	MuxCallId mCall2 = (MuxCallId)call2;

		// find the common callholders
	CallHolder ch1 = mCall1.getLeg(address);
	CallHolder ch2 = mCall2.getLeg(address);

		// check that we have the same sub-provider
	TelephonyProvider sub = ch1.getTpi();
	if (!sub.equals(ch2.getTpi())) {
		throw new InvalidArgumentException("Mux Join: No common TerminalConnection found");
	}

		// tell the sub-provider to join the parts
	sub.join(ch1.getCall(), ch2.getCall(), address, terminal);

		// update the address to CallHolder set in call1
	String[] adds = mCall2.getAddsForSubCall(ch2);
	int addSize = adds.length;
	for (int i = 0; i < addSize; i++) {
		mCall1.addLeg(adds[i], ch1);
	}

		// move the other calls from call23 to call1
	Iterator it = mCall2.getCallHolders();
	while (it.hasNext()) {
		CallHolder ch = (CallHolder)it.next();
		if (!ch.equals(ch2)) {
			// add to mCall1
			mCall1.addCall(ch);
			// update the address->CallHolder table in call 1
			adds = mCall2.getAddsForSubCall(ch);
			addSize = adds.length;
			for (int i = 0; i < addSize; i++) {
				mCall1.addLeg(adds[i], ch);
			}

			// update the provider
			this.getLowToLogicalMap().put(ch, mCall1);
		}
	}
	
		// clear the old mux call
	mCall2.free();

		// return the call
	return call1;
}
/**
 * Take the values pointed to by the existing and new and return the lowest common denominator in String
 * format.
 * Creation date: (2000-03-14 15:20:48)
 * @author: Richard Deadman
 * @param existing An existing value, a String of form "txxx" or "fxxx".
 * @param update A new String representation of a boolean value.
 */
private Object lcd(Object existing, Object update) {
	if ((existing instanceof String && ((String)existing).length() > 0 &&
			Character.toLowerCase(((String)existing).charAt(0)) != 't') ||
	    (existing instanceof Boolean && !((Boolean)existing).booleanValue()))
			// can stop now - already false
			return existing;

		// test if existing true and update exists -- replace with update
	if ((update instanceof String && ((String)update).length() > 0) || (update instanceof Boolean)) {
		return update;
	}

	// existing was true and update not valid
	return existing;
}
/**
 * This method loads the Provider's values from a property file.
 * Creation date: (2000-02-22 10:11:41)
 * @author: Richard Deadman
 */
private Properties loadResources(String resName) {
	// We must be able to load the properties file
	Properties props = new Properties();
	try {
		props.load(this.getClass().getResourceAsStream("/" + resName));
	} catch (IOException ioe) {
		// return empty Properties
	}

	return props;
}
/**
 * Ensure that I have a logical CallId already for the given low-level call.
 * This will look for the call as an existing call, or if it doesn't exist:
 * <ol>
 * <li>Check if any other calls connect to this call
 * <li>Create a new logical call and trace it through any sub-providers for un-reported legs.
 * </ol>
 * Creation date: (2000-09-24 0:45:49)
 * @param subCall Low-level CallId
 * @param subTpi Low-level TelephonyProvider that holds the call.
 */
MuxCallId locateCall(CallId subCall, TelephonyProvider subTpi) {
	// Check for the logical CallId in the backward lookup table
	MuxCallId mci = this.findCall(subCall, subTpi);
	// test if the Logical CallId does not yet exist
	if (mci == null) {
		// first check if any other existing calls can be traced to this call
		CallData cd = subTpi.getCall(subCall);
		String[] remoteAddresses = cd.getRemoteAddresses();
		int raSize = remoteAddresses.length;
		if (raSize > 0) {
			for (int i = 0; (i < raSize) && (mci == null); i++) {
				Iterator it = this.getCalls().iterator();
				while (it.hasNext()) {
					MuxCallId testCall = (MuxCallId)it.next();
					if (testCall.contains(new CallHolder(subCall, subTpi))) {
						mci = testCall;
						break;
					}
				}
			}
		}
		if (mci == null) {
			// otherwise create the call
			mci = this.noteCall(subCall, subTpi);
			// now trace this call through to link up other legs
			this.traceCalls(mci, cd.connections, new HashSet());
		}
	}
	return mci;
}
/**
 * Ensure that a physical sub-call is properly recorded.
 * Creation date: (2000-09-27 15:49:07)
 */
private void mapCall(MuxCallId call, CallData callData, TelephonyProvider sub) {
	// Create and add a new CallHolder if needed
	CallHolder tmpHolder = new CallHolder(callData.id, sub);
	if (!call.contains(tmpHolder)) {
		call.addCall(tmpHolder);
	}

	ConnectionData[] connData = callData.connections;
	int cdSize = connData.length;
	// update the address and terminal mapping
	for (int i = 0; i < cdSize; i++) {
		this.mapConnection(connData[i], sub);
	}
}
/**
 * Given a Connection, ensure it is mapped into a sub-provider
 * @return true if the connection is in the sub-provider's address space
 */
private boolean mapConnection(ConnectionData cd, TelephonyProvider sub) {
	// now map these to the sub-provider
	if (cd.isLocal) {
		Map termMap = this.getTermToMap();
		// record all Addresses and Terminals
		this.getAddToMap().put(cd.address, sub);
		// and the Connection's Terminals
		int tcSize = cd.terminalConnections.length;
		for (int j = 0; j < tcSize; j++) {
			TCData tcd = cd.terminalConnections[j];
			termMap.put(tcd.terminal.terminal, sub);
		}
	}
	return cd.isLocal;
}
/**
 * Merge two CallData collections into one that represents the merged call.
 * Creation date: (2000-10-02 14:51:36)
 * @return The merged CallData representing the virtual call
 * @param cd1 The first call
 * @param cd2 The bridged call
 */
private CallData merge(CallData cd1, CallData cd2) {
	// null value test
	if (cd2 == null) {
		return cd1;
	}
	if (cd1 == null) {
		return cd2;
	}
	
	Set conns = new HashSet();				// the set of connections
	Set localAddresses = new HashSet();		// set of local addresses so we can remove remote later
	int i;

	// Create a set of Connections
	ConnectionData[] cdSet = cd1.connections;
	ConnectionData cd;
	for (i = 0; i < cdSet.length; i++) {
		cd = cdSet[i];
		conns.add(cd);
		if (cd.isLocal)
			localAddresses.add(cd.address);
	}
	cdSet = cd2.connections;
	for (i = 0; i < cdSet.length; i++) {
		cd = cdSet[i];
		conns.add(cd);
		if (cd.isLocal)
			localAddresses.add(cd.address);
	}

	// prune remote connections that have a local twin
	Iterator it = conns.iterator();
	while (it.hasNext()) {
		cd = (ConnectionData)it.next();
		if (!cd.isLocal && (localAddresses.contains(cd.address))) {
			it.remove();
		}
	}

	// Now return the merged set of CallData
	return new CallData(cd1.id, cd1.callState, (ConnectionData[])conns.toArray(new ConnectionData[conns.size()]));
}
/**
 * Map an Address name from a sub-provider.
 * Creation date: (2000-08-09 0:38:22)
 * @author: Richard Deadman
 * @param id A name of a sub-provider's Address
 * @param sub The sub-provider that handles this address
 */
void noteAddress(String addressName, TelephonyProvider sub) {
	this.getAddToMap().put(addressName, sub);
}
/**
 * Ensure that I have a logical CallId already for the given low-level call.
 * This doesn't follow links since that may or may not require the return of CallData information.
 * Creation date: (2000-09-24 0:45:49)
 * @param subCall Low-level CallId
 * @param subTpi Low-level TelephonyProvider that holds the call.
 */
MuxCallId noteCall(CallId subCall, TelephonyProvider subTpi) {
	// Check for the logical CallId in the backward lookup table
	MuxCallId mci = this.findCall(subCall, subTpi);
	// test if the Logical CallId does not yet exist
	if (mci == null) {
		CallHolder ch = new CallHolder(subCall, subTpi);
		mci = new MuxCallId();
		mci.addCall(ch);
		this.addCall(ch, mci);
	}
	return mci;
}
/**
 * Map a Terminal name from a sub-provider.
 * Creation date: (2000-08-09 0:38:22)
 * @author: Richard Deadman
 * @param id A name of a sub-provider's Terminal
 * @param sub The sub-provider that handles the terminal
 */
void noteTerminal(String terminalName, TelephonyProvider sub) {
	this.getTermToMap().put(terminalName, sub);
}
/**
 * Delegate the call onto the appropriate sub-provider
 */
public void play(String terminal, String[] streamIds, int offset, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
	this.getTerminalSub(terminal).play(terminal, streamIds, offset, rtcs, optArgs);
}
/**
 * Delegate off to the appropriate sub-provider
 */
public void record(String terminal, String streamId, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
	this.getTerminalSub(terminal).record(terminal, streamId, rtcs, optArgs);
}
/**
 * Create a logical CallId to handle any sub-provider muxing and register it with
 * the local lookup table.
 * Creation date: (2000-09-25 11:45:34)
 * @return net.sourceforge.gjtapi.raw.mux.MuxCallId
 * @param id net.sourceforge.gjtapi.CallId
 * @param sub net.sourceforge.gjtapi.TelephonyProvider
 */
private MuxCallId registerCall(CallId id, TelephonyProvider sub) {
	MuxCallId cid = new MuxCallId();
	cid.addCall(id, sub);
	this.getCalls().add(cid);
	return cid;
}
/**
 * Tell the remote provider to release an address from a call.
 */
public void release(String address, CallId call) throws PrivilegeViolationException,
	ResourceUnavailableException, MethodNotSupportedException, RawStateException {
	CallHolder ch = this.getSub(call, address);
	if (ch != null) {
		ch.getTpi().release(address, ch.getCall());
	}
}
/**
 * Release any CallId's that I have reserved.
 */
public void releaseCallId(CallId id) {
	MuxCallId mcid = (MuxCallId)id;		// This better cast...
	Iterator chs = mcid.getCallHolders();
	while (chs.hasNext()) {
		CallHolder call = (CallHolder)chs.next();
		call.getTpi().releaseCallId(call.getCall());
	}
	this.removeCall(mcid);
}
/**
 * Remove a MuxCallId and its backwards lookup holder to my call set.
 * Creation date: (2000-09-26 16:13:08)
 * @param mci net.sourceforge.gjtapi.raw.mux.MuxCallId
 */
boolean removeCall(MuxCallId mci) {
	boolean removed = this.getCalls().remove(mci);
	if (removed) {	// logical call existed...
		Iterator it = this.getLowToLogicalMap().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			if (entry.getValue().equals(mci)) {
				it.remove();
			}
		}
	}
	return removed;
}
/**
 * Forward removeListener to remote provider.
 */
public void removeListener(TelephonyListener rl) {
	Iterator it = this.getSubProviders().iterator();
	while (it.hasNext()) {
		TelephonyProvider rp = ((TelephonyProvider)it.next());
		rp.removeListener(new MuxListener(rl, this, rp));
	}
}
/**
 * Delegate off to the appropriate subprovider
 */
public void reportCallsOnAddress(String address, boolean flag) throws ResourceUnavailableException, InvalidArgumentException {
	TelephonyProvider rp = this.getAddressSub(address);
	if (rp != null) {
		if (((RawCapabilities)this.getSubToCaps().get(rp)).throttle)
			rp.reportCallsOnAddress(address, flag);
	} else {
		// poll each raw provider - should never do this since all addresses should have been recorded.
		Iterator it = this.getSubProviders().iterator();
		while (it.hasNext()) {
			try {
				rp = (TelephonyProvider)it.next();
				rp.reportCallsOnAddress(address, flag);
					// no exception - store rp
				this.getAddToMap().put(address, rp);
			} catch (InvalidArgumentException iae) {
				// eat this one and try next
			}
		}
		// didn't find any to handle the address
		throw new InvalidArgumentException("No muxed providers handle address: " + address);
	}
}
/**
 * Delegate off to the appropriate subprovider
 */
public void reportCallsOnTerminal(String terminal, boolean flag) throws ResourceUnavailableException, InvalidArgumentException {
	TelephonyProvider rp = this.getTerminalSub(terminal);
	if (rp != null) {
		if (((RawCapabilities)this.getSubToCaps().get(rp)).throttle)
			rp.reportCallsOnTerminal(terminal, flag);
	} else {
		// poll each raw provider  - should never do this since all terminals should have been recorded.
		Iterator it = this.getSubProviders().iterator();
		while (it.hasNext()) {
			try {
				rp = (TelephonyProvider)it.next();
				rp.reportCallsOnAddress(terminal, flag);
					// no exception - store rp
				this.getTermToMap().put(terminal, rp);
			} catch (InvalidArgumentException iae) {
				// eat this one and try next
			}
		}
		// didn't find any to handle the address
		throw new InvalidArgumentException("No muxed providers handle terminal: " + terminal);
	}
}
/**
 * Reserve a call id on the remote server.
 */
public CallId reserveCallId(String address) throws InvalidArgumentException {
	TelephonyProvider rp = this.getAddressSub(address);
	CallId id = null;

	if (rp != null) {
		id = rp.reserveCallId(address);
	} else {	// broadcast - we shouldn't see an Address that we haven't already recorded
		boolean found = false;
		Iterator it = this.getSubProviders().iterator();
		while (it.hasNext() && !found) {
			try {
				rp = (TelephonyProvider)it.next();
				id = rp.reserveCallId(address);
				found = true;
			} catch (InvalidArgumentException iae) {
				// eat and move on to next provider
			}
		}
		if (!found)
			throw new InvalidArgumentException("No muxed subproviders know this Address: " + address);
	}

	if (id != null) {
		// map the call id in our local table
		return this.registerCall(id, rp);
	} else
		return null;
}
/**
 * Delegate off to the appropriate subprovider
 */
public RawSigDetectEvent retrieveSignals(String terminal, int num, Symbol[] patterns, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
	return this.getTerminalSub(terminal).retrieveSignals(terminal, num, patterns, rtcs, optArgs);
}
/**
 * Find the sub-provider to forward the sendPrivateData call to, and forward it.
 */
public Object sendPrivateData(CallId call, String address, String terminal, Object data) {
	TelephonyProvider tp = null;	// used for unicast messages
	CallId newCallId = null;
	Iterator it = null;				// used of broadcast messages
		// check if call ids the provider
	if (call != null) {
		if  (address != null) {		// Connection of TerminalConnection
			CallHolder ch = this.getSub(call, address);
			tp = ch.getTpi();
			newCallId = ch.getCall();
		} else {					// Broadcast to all multiplexed Calls
			it = ((MuxCallId)call).getCallHolders();
			Set set = new HashSet();
			while (it.hasNext()) {
				CallHolder ch = (CallHolder)it.next();
				Object o = ch.getTpi().sendPrivateData(ch.getCall(), address, terminal, data);
				if (o != null)
					set.add(o);
			}
			return set.toArray();
		}
	} else if (address != null) {
		// check if the address will now do it
		tp = this.getAddressSub(address);
	} else if (terminal != null) {
		// check if only the terminal will id it.
		tp = this.getTerminalSub(terminal);
	} else {
		// all providers are required
		Set set = new HashSet();
		it = this.getSubProviders().iterator();
		while (it.hasNext()) {
			Object o = ((TelephonyProvider)it.next()).sendPrivateData(null, null, null, data);
			if (o != null)
				set.add(o);
		}
		return set.toArray();
	}

		// one provider found
	if (tp != null) {
		return tp.sendPrivateData(newCallId, address, terminal, data);
	}

		// no providers found
	return null;
}
/**
 * Delegate off to the appropriate subprovider
 */
public void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
	this.getTerminalSub(terminal).sendSignals(terminal, syms, rtcs, optArgs);
}
/**
 * We assume that the providers are at both ends of the range
 */
public void setLoadControl(java.lang.String startAddr, java.lang.String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws javax.telephony.MethodNotSupportedException {
	if (startAddr != null) {
		this.getAddressSub(startAddr).setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatment);
	}
	if ((endAddr != null) && (!startAddr.equals(endAddr))) {
		this.getAddressSub(startAddr).setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatment);
	}
}
/**
 * Find the sub-provider to forward the setPrivateData call to, and forward it.
 */
public void setPrivateData(CallId call, String address, String terminal, Object data) {
	TelephonyProvider tp = null;	// used for unicast messages
	CallId newCallId = null;
	Iterator it = null;				// used for broadcast messages
		// check if call ids the provider
	if (call != null) {
		if  (address != null) {		// Connection of TerminalConnection
			CallHolder ch = this.getSub(call, address);
			tp = ch.getTpi();
			newCallId = ch.getCall();
		} else {					// Broadcast to all multiplexed Calls
			it = ((MuxCallId)call).getCallHolders();
			while (it.hasNext()) {
				CallHolder ch = (CallHolder)it.next();
				ch.getTpi().setPrivateData(ch.getCall(), address, terminal, data);
			}
		}
	} else if (address != null) {
		// check if the address will now do it
		tp = this.getAddressSub(address);
	} else if (terminal != null) {
		// check if only the terminal will id it.
		tp = this.getTerminalSub(terminal);
	} else {
		// all providers are required
		it = this.getSubProviders().iterator();
		while (it.hasNext()) {
			((TelephonyProvider)it.next()).setPrivateData(null, null, null, data);
		}
	}

		// one provider found
	if (tp != null) {
		tp.setPrivateData(newCallId, address, terminal, data);
	}
}
/**
 * Tell the remote provider to shutdown.  It may choose to ignore me.
 */
public void shutdown() {
	Iterator it = this.getSubProviders().iterator();
	while (it.hasNext()) {
		((TelephonyProvider)it.next()).shutdown();
	}
}
/**
 * Pass on the stop media request to the correct sub-provider
 */
public void stop(String terminal) {
	this.getTerminalSub(terminal).stop(terminal);
}
/**
 * Delegate off to the appropriate sub-provider
 */
public boolean stopReportingCall(CallId call) {
	Iterator it = ((MuxCallId)call).getCallHolders();
	boolean result = true;		// assume that they all will unless one declines
	while (it.hasNext()) {
		CallHolder ch = (CallHolder)it.next();
		result = result && ch.getTpi().stopReportingCall(ch.getCall());
	}
	return result;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	StringBuffer sb = new StringBuffer("Raw Multiplexor Provider managing: ");
	Iterator it = this.getSubProviders().iterator();
	while (it.hasNext()) {
		TelephonyProvider rp = (TelephonyProvider)it.next();
		sb.append(" ").append(rp.toString());
	}
	return sb.toString();
}
/**
 * This method assists in knitting call legs together that exist in two sub-providers
 * but that represent parts of one logical call.
 * Creation date: (2000-09-27 15:49:07)
 * @return net.sourceforge.gjtapi.CallData
 * @param muxCall The mux virtual call that holds all sub-call holders.
 * @param from The address set the call leg comes from.  Used to help distinguish multiple calls at the destination.
 * @param to The address where the call leg should be.
 * @param state the Connection state of the call leg.  Used to help distinguish leg that both come from the same source.
 * @param addressSet The set of currently found locally-resolved addresses.  Used to stop cyclical recursion.
 */
private CallData traceCall(MuxCallId muxCall, String[] from, String to, int state, HashSet addressSet) {
	CallData found = null;

	int fromSize = from.length;
	// find the sub-provider and ask it for all calls on the address
	TelephonyProvider prov = this.getAddressSub(to);
	if (prov != null) {
		CallData[] subCalls = prov.getCallsOnAddress(to);
		int size = subCalls.length;
			// look at all sub-calls
		for (int i = 0; i < size; i++) {
			CallData cd = subCalls[i];
			int connSize = cd.connections.length;
			for (int j = 0; j < connSize; j++) {
				ConnectionData conn = cd.connections[j];
				for (int k = 0; k < fromSize; k++) {
					if ((conn.address.equals(from[k])) && (conn.connState == state)) {
						if (found == null) {
							found = cd;
							break;	// don't check any more connections
						} else {
							// two possible calls -- return null to indicate failure
							return null;
						}
					}
				}
				if (found != null)
					break;		// break out of connections
			}
		}

		// now we need to check if we should shut off reporting for the other returned Calls
		boolean shutOff = false;
		for (int i = 0; (i < size) && (!shutOff); i++) {
			CallData cd = subCalls[i];
			CallId subCallId = cd.id;
			if ((!cd.equals(found)) && (this.findCall(subCallId, prov) == null)) {
				prov.stopReportingCall(subCallId);
				shutOff = true;
			}
		}
			// If we turned any call reporting off, we need to turn call reporting off for
			// future calls on this address
		if (shutOff) {
				// only catch non-Runtime exceptions -- we want others to blow things up
			Exception e = null;
			try {
				prov.reportCallsOnAddress(to, false);
				prov.getCall(found.id);				// to ensure this is still being reported
			} catch (InvalidArgumentException iae) {
				e = iae;
			} catch (ResourceUnavailableException rue) {
				e = rue;
			}
			if (e != null) {
				System.out.println("Internal logic error during multiplexor call tracing:");
				e.printStackTrace();
			}
		}

		if (found != null) {
			// Add the new local addresses to the addressSet
			ConnectionData[] connData = found.connections;
			int cdSize = connData.length;
			for (int i = 0; i < cdSize; i++) {
				if (connData[i].isLocal) {
					addressSet.add(connData[i].address);
				}
			}

			// Create and add a new CallHolder if needed
			if (muxCall.getLeg(to) == null) {
				muxCall.addCall(found.id, prov);
				// update the address and terminal mapping
				for (int i = 0; i < cdSize; i++) {
					this.mapConnection(connData[i], prov);
				}
			}

			// finally, recursively merge in any other bridged connections
			found = this.merge(found, this.traceCalls(muxCall,
								connData,
								addressSet));
		}
	}

	return found;
}
/**
 * This method assists in knitting call legs together that exist in two sub-providers by merging all remote connections in the logical mux call.
 * Only remote addresses not yet recorded in addressSet are followed.
 * but that represent parts of one logical call.
 * Creation date: (2000-09-27 15:49:07)
 * @return net.sourceforge.gjtapi.CallData
 * @param muxCall The mux virtual call that holds all sub-call holders.
 * @param addressSet The set of currently found locally-resolved addresses.  Used to stop cyclical recursion.
 */
private CallData traceCalls(MuxCallId muxCall, ConnectionData[] conns, HashSet addressSet) {
	CallData found = null;

	int cdSize = conns.length;
	// finally, recursively merge in any other bridged connections
	for (int i = 0; i < cdSize; i++) {
		if (!conns[i].isLocal) {
			ConnectionData conn = conns[i];
			// ensure we don't get in a recursive loop
			if (!addressSet.contains(conn.address))
				found = this.merge(found, this.traceCall(muxCall,
								found.getLocalAddresses(),
								conn.address,
								conn.connState,
								addressSet));
		}
	}

	return found;
}
/**
 * Pas on the triggerRTC method to the correct sub-provider.
 */
public void triggerRTC(String terminal, Symbol action) {
	this.getTerminalSub(terminal).triggerRTC(terminal, action);
}
/**
 * Tell the remote provider to unhold a terminal from a call
 */
public void unHold(CallId call, String address, String term) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	CallHolder ch = this.getSub(call, address);
	if (ch != null) {
		ch.getTpi().unHold(ch.getCall(), address, term);
	}
}
}
