package net.sourceforge.gjtapi.jcc;

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
import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.jcc.filter.*;
import javax.csapi.cc.jcc.*;
import javax.telephony.*;
import java.util.*;
import java.lang.ref.WeakReference;
/**
 * Provider of JAIN Call Control services as a layer on the Generic JTAPI Framework.
 * Creation date: (2000-10-10 12:33:24)
 * @author: Richard Deadman
 */
public class Provider implements JccProvider {

	/**
	 * Support class for tracking Generic JTAPI object associations back to
	 * the Jcc wrappers.
	 * <P>We have to make this a double-weak map so that the values (which hold handles
	 * to the keys) don't keep the keys from being garbage-collected.
	 **/
	private class DoubleWeakMap extends WeakHashMap {
		public Object put(Object key, Object value) {
			WeakReference wValue = (value instanceof WeakReference) ?
				(WeakReference)value :
				new WeakReference(value);
			Object prev = super.put(key, wValue);
			if (prev instanceof WeakReference)
				return ((WeakReference)prev).get();
			else
				return prev;
		}

		public Object get(Object key) {
			Object value = super.get(key);
			if (value instanceof WeakReference)
				return ((WeakReference)value).get();
			else
				return value;
		}
	}
	private GenericProvider genProv = null;
	private Map callListeners = new HashMap();	// JcpCallListener -> CallListenerAdapter
	private Map loadListeners = new HashMap();	//  CallLoadControlListener -> CallLoadControlEventFilter
	private DoubleWeakMap addrMap = new DoubleWeakMap();
	private DoubleWeakMap callMap = new DoubleWeakMap();
	private DoubleWeakMap connMap = new DoubleWeakMap();
/**
 * Provider constructor comment.
 */
public Provider(GenericProvider prov) {
	super();

	this.setGenProv(prov);
	
	// tell the GJTAPI provider about me
	prov.hookupJainCallback(this);
}
/**
 * addCallListener method comment.
 */
/*public void addCallListener(JccCallListener cl, EventFilter filter)
	//throws jain.application.services.jcp.MethodNotSupportedException, jain.application.services.jcp.ResourceUnavailableException
	{
	Map listMap = this.getCallListeners();
	
		// first see if we already have the listener registered.
	CallListenerAdapter cla = (CallListenerAdapter)listMap.get(cl);
	if (cla == null) {
		if (cl instanceof JccConnectionListener)
			cla = new ConnListenerAdapter(this,
					(JccConnectionListener)cl,
					filter);
		else
			cla = new CallListenerAdapter(this, cl, filter);

			// now add the new adapter
		listMap.put(cl, cla);
	} else {
		cla.setFilter(filter);
	}
}*/
/**
 * addCallListener method comment.
 */
public void addCallListener(JccCallListener cl) throws javax.csapi.cc.jcc.MethodNotSupportedException, javax.csapi.cc.jcc.ResourceUnavailableException {
	Map listMap = this.getCallListeners();
	
		// first see if we already have the listener registered.
	CallListenerAdapter cla = (CallListenerAdapter)listMap.get(cl);
	if (cla == null) {
		if (cl instanceof JccConnectionListener)
			cla = new ConnListenerAdapter(this,
					(JccConnectionListener)cl,
					null);
		else
			cla = new CallListenerAdapter(this, cl);

			// now add the new adapter to my list of JccCallListeners to add to later calls
		listMap.put(cl, cla);
		
		// ask any current calls to add the adapter
		this.addToAllCalls(cla);
	}
}

/**
 * Add a CallListenerAdapter to all existing calls on the GJTAPI domain, causing them to send
 * snapshot messages.
 * @param CallListener listener The CallListener to register against all calls.
 * @author Richard Deadman */
private void addToAllCalls(CallListener cl) {
	try {
		Call[] gCalls = this.getGenProv().getCalls();
		if (gCalls != null)
			for (int i = 0; i < gCalls.length; i++)
			    try {
					gCalls[i].addCallListener(cl);
			    } catch (javax.telephony.MethodNotSupportedException mnse) {
			    	// can't register with existing calls -- fail silently
			    }
	} catch (javax.telephony.ResourceUnavailableException rue) {
		// can't find existing calls -- fail silently
	}

}
/**
 * addCallLoadControlListener method comment.
 */
/*public void addCallLoadControlListener(CallLoadControlListener clcl, EventFilter filter) throws javax.csapi.cc.jcc.MethodNotSupportedException, javax.csapi.cc.jcc.ResourceUnavailableException {
	this.getLoadListeners().put(clcl, filter);
}*/
/**
 * Add a CallLoadControlListener with no filter
 */
public void addCallLoadControlListener(CallLoadControlListener clcl) throws javax.csapi.cc.jcc.MethodNotSupportedException, javax.csapi.cc.jcc.ResourceUnavailableException {
	this.getLoadListeners().put(clcl, null);
}
/**
 * addConnectionListener method comment.
 */
public void addConnectionListener(JccConnectionListener cl, EventFilter filter) {
	Map listMap = this.getCallListeners();
	
		// first see if we already have the listener registered.
	CallListenerAdapter cla = (CallListenerAdapter)listMap.get(cl);
	if (cla == null) {
		cla = new ConnListenerAdapter(this,
					(JccConnectionListener)cl,
					filter);
			// now add the new adapter
		listMap.put(cl, cla);
		
			// and add it to all existing calls
		this.addToAllCalls(cla);

	} else {
		if (cla instanceof ConnListenerAdapter)
			((ConnListenerAdapter)cla).setFilter(filter);
	}
}

/**
 * addProviderListener method comment.
 */
public void addProviderListener(JccProviderListener pl) throws javax.csapi.cc.jcc.MethodNotSupportedException, javax.csapi.cc.jcc.ResourceUnavailableException {
	try {
		this.getGenProv().addProviderListener(new ProviderListenerAdapter(
			this,
			pl));
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw new javax.csapi.cc.jcc.ResourceUnavailableException(rue.getType());
	} catch (javax.telephony.MethodNotSupportedException mnse) {
		throw new javax.csapi.cc.jcc.MethodNotSupportedException(mnse.getMessage());
	}
}
/**
 * Dispatch CallLoad cease events to all listeners
 * Creation date: (2000-11-14 16:07:15)
 * @param addr javax.telephony.Address
 */
public void callOverloadCeased(FreeAddress addr) {
	GenAddress ga = this.findAddress(addr);
	if (ga != null) {
		Iterator it = this.getLoadListeners().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			CallLoadControlListener listener = (CallLoadControlListener)entry.getKey();
			EventFilter filter = (EventFilter)entry.getValue();
			CallLoadControlEvent event = new CallOverloadEvent(this.findAddress(addr),
						CallLoadControlEvent.PROVIDER_CALL_OVERLOAD_CEASED);
			if ((filter == null) || (filter.getEventDisposition(event) != filter.EVENT_DISCARD))
				listener.providerCallOverloadCeased(event);
		}
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-14 16:07:15)
 * @param addr javax.telephony.Address
 */
public void callOverloadEncountered(FreeAddress addr) {
	GenAddress ga = this.findAddress(addr);
	if (ga != null) {
		Iterator it = this.getLoadListeners().entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry)it.next();
			CallLoadControlListener listener = (CallLoadControlListener)entry.getKey();
			EventFilter filter = (EventFilter)entry.getValue();
			CallLoadControlEvent event = new CallOverloadEvent(this.findAddress(addr),
						CallLoadControlEvent.PROVIDER_CALL_OVERLOAD_ENCOUNTERED);
			if ((filter == null) || (filter.getEventDisposition(event) != filter.EVENT_DISCARD))
				listener.providerCallOverloadEncountered(event);
		}
	}
}
/**
 * createCall method comment.
 */
public JccCall createCall() throws javax.csapi.cc.jcc.InvalidStateException, javax.csapi.cc.jcc.PrivilegeViolationException, javax.csapi.cc.jcc.MethodNotSupportedException, javax.csapi.cc.jcc.ResourceUnavailableException {
	GenCall call = null;
	try {
		call = this.findCall((FreeCall)this.getGenProv().createCall());
	} catch (javax.telephony.InvalidStateException ise) {
		throw new javax.csapi.cc.jcc.InvalidStateException(ise.getObject(),
								ise.getObjectType(),
								ise.getState(),
								ise.getMessage());
	} catch (javax.telephony.PrivilegeViolationException pve) {
		throw new javax.csapi.cc.jcc.PrivilegeViolationException(pve.getType(), pve.getMessage());
	} catch (javax.telephony.MethodNotSupportedException mnse) {
		throw new javax.csapi.cc.jcc.MethodNotSupportedException(mnse.getMessage());
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw new javax.csapi.cc.jcc.ResourceUnavailableException(rue.getType());
	}

		// now add any listeners
	Map callListeners = this.getCallListeners();
	int size = callListeners.size();
	if (size > 0) {
		FreeCall fCall = call.getFrameCall();
		Iterator it = callListeners.values().iterator();
		while (it.hasNext()) {
			CallListenerAdapter cl = (CallListenerAdapter)it.next();
			cl.callCreated(new CreatedCallEvent(call));
			fCall.addCallListener(cl);
		}
	}

		// return the call
	return call;
}
/**
 * createEventFilterAddressRange method comment.
 */
public EventFilter createEventFilterAddressRange(String lowAddress, String highAddress, int matchDisposition, int nomatchDisposition) throws javax.csapi.cc.jcc.ResourceUnavailableException {
	try {
		return new net.sourceforge.gjtapi.jcc.filter.AddressRangeFilter(this.getAddress(lowAddress), this.getAddress(highAddress), matchDisposition, nomatchDisposition);
	} catch (javax.csapi.cc.jcc.InvalidPartyException iae) {
		throw new javax.csapi.cc.jcc.ResourceUnavailableException(javax.csapi.cc.jcc.ResourceUnavailableException.ORIGINATOR_UNAVAILABLE);
	}
}
/**
 * Create a filter that uses a regular expression to look for matches.
 */
public EventFilter createEventFilterAddressRegEx(java.lang.String addressRE, int matchDisposition, int nomatchDisposition) {
	return new net.sourceforge.gjtapi.jcc.filter.AddressREFilter(addressRE, matchDisposition, nomatchDisposition);
}
/**
 * createEventFilterAnd method comment.
 */
public EventFilter createEventFilterAnd(EventFilter[] filters, int nomatchDisposition) {
	return new net.sourceforge.gjtapi.jcc.filter.AndFilter(filters, nomatchDisposition);
}
/**
 * createEventFilterAddressRange method comment.
 */
public EventFilter createEventFilterDestAddressRange(String lowAddress, String highAddress, int matchDisposition, int nomatchDisposition) throws javax.csapi.cc.jcc.ResourceUnavailableException {
	try {
		return new net.sourceforge.gjtapi.jcc.filter.DestAddressRangeFilter(this.getAddress(lowAddress), this.getAddress(highAddress), matchDisposition, nomatchDisposition);
	} catch (javax.csapi.cc.jcc.InvalidPartyException iae) {
		throw new javax.csapi.cc.jcc.ResourceUnavailableException(javax.csapi.cc.jcc.ResourceUnavailableException.ORIGINATOR_UNAVAILABLE);
	}
}
/**
 * Create a filter that uses a regular expression to look for matches.
 */
public EventFilter createEventFilterDestAddressRegEx(java.lang.String addressRE, int matchDisposition, int nomatchDisposition) {
	return new net.sourceforge.gjtapi.jcc.filter.DestAddressREFilter(addressRE, matchDisposition, nomatchDisposition);
}
/**
 * createEventFilterEventSet method comment.
 */
public EventFilter createEventFilterEventSet(int[] blockEvents, int[] notifyEvents) {
	return new net.sourceforge.gjtapi.jcc.filter.EventSetFilter(blockEvents, notifyEvents);
}
/**
 * createEventFilterOr method comment.
 */
public EventFilter createEventFilterOr(EventFilter[] filters, int nomatchDisposition) {
	return new net.sourceforge.gjtapi.jcc.filter.OrFilter(filters, nomatchDisposition);
}
/**
 * createEventFilterAddressRange method comment.
 */
public EventFilter createEventFilterOrigAddressRange(String lowAddress, String highAddress, int matchDisposition, int nomatchDisposition) throws javax.csapi.cc.jcc.ResourceUnavailableException {
	try {
		return new net.sourceforge.gjtapi.jcc.filter.OrigAddressRangeFilter(this.getAddress(lowAddress), this.getAddress(highAddress), matchDisposition, nomatchDisposition);
	} catch (javax.csapi.cc.jcc.InvalidPartyException iae) {
		throw new javax.csapi.cc.jcc.ResourceUnavailableException(javax.csapi.cc.jcc.ResourceUnavailableException.ORIGINATOR_UNAVAILABLE);
	}
}
/**
 * Create a filter that uses a regular expression to look for matches.
 */
public EventFilter createEventFilterOrigAddressRegEx(java.lang.String addressRE, int matchDisposition, int nomatchDisposition) {
	return new net.sourceforge.gjtapi.jcc.filter.OrigAddressREFilter(addressRE, matchDisposition, nomatchDisposition);
}

    /**
    This method returns a standard EventFilter which is implemented by the JCC platform.
    For all events that require filtering by this {@link EventFilter}, apply the following:
    <ul>
    <li>If the cause code is matched, the filter returns the value matchDisposition. 
    <li>If the cause code is not matched, then return nomatchDisposition.
    </ul>
    
    @param causeCode an integer that represents a cause code.  Valid cause codes (prefixed by 
    <code>CAUSE_</code>) are defined in {@link JcpEvent} and {@link JccCallEvent}.
    @param matchDisposition indicates the disposition of a JCC related event occurring on a
    JcpAddress which forms part of the range specified. This should be one of the legal
    dispositions namely, {@link EventFilter#EVENT_BLOCK}, {@link EventFilter#EVENT_DISCARD} or {@link EventFilter#EVENT_NOTIFY}. 
    @param nomatchDisposition indicates the disposition of a JCC related event occurring on a
    JcpAddress which DOES not form part of the range specified. This should be one of the legal
    dispositions namely, {@link EventFilter#EVENT_BLOCK}, {@link EventFilter#EVENT_DISCARD} or {@link EventFilter#EVENT_NOTIFY}. 
    @return EventFilter standard EventFilter provided by the JCC platform to enable 
    filtering of events based on the application's requirements.    
    @throws ResourceUnavailableException An internal resource for completing this call is unavailable. 
    @throws InvalidArgumentException One or more of the provided argument is not valid
    
    @since 1.0a
    */
    public EventFilter createEventFilterCauseCode(int causeCode, int matchDisposition, int nomatchDisposition) {
    	return new CauseCodeFilter(causeCode, matchDisposition, nomatchDisposition);
    }

/**
 * Find the JccAddress object that wraps the given JTAPI Address object.
 * Creation date: (2000-10-31 15:17:00)
 * @return net.sourceforge.gjtapi.jcc.GenCall
 * @param jtapiAddr javax.telephony.Address
 */
GenAddress findAddress(FreeAddress jtapiAddr) {
	DoubleWeakMap addrMap = this.getAddrMap();
	synchronized(addrMap) {
		GenAddress addr = (GenAddress)addrMap.get(jtapiAddr);
		if (addr == null) {
			addr = new GenAddress(this, jtapiAddr);
			addrMap.put(jtapiAddr, addr);
		}
		return addr;
	}
}
/**
 * Find the JccCall object that wraps the given JTAPI call object.
 * Creation date: (2000-10-31 15:17:00)
 * @return net.sourceforge.gjtapi.jcc.GenCall
 * @param jtapiCall javax.telephony.Call
 */
GenCall findCall(FreeCall jtapiCall) {
	DoubleWeakMap callMap = this.getCallMap();
	synchronized(callMap) {
		GenCall call = (GenCall)callMap.get(jtapiCall);
		if (call == null) {
			call = new GenCall(this, jtapiCall);
			callMap.put(jtapiCall, call);
		}
		return call;
	}
}
/**
 * Find the JccConnection object that wraps the given JTAPI Connection object.
 * Creation date: (2000-10-31 15:17:00)
 * @return net.sourceforge.gjtapi.jcc.GenCall
 * @param jtapiconn javax.telephony.Connection
 */
GenConnection findConnection(FreeConnection jtapiConn) {
	DoubleWeakMap connMap = this.getConnMap();
	synchronized(connMap) {
		GenConnection conn = (GenConnection)connMap.get(jtapiConn);
		if (conn == null) {
			conn = new GenConnection(this, jtapiConn);
			connMap.put(jtapiConn, conn);
		}
		return conn;
	}
}
/**
 * getAddress method comment.
 */
public JccAddress getAddress(String number) throws javax.csapi.cc.jcc.InvalidPartyException {
	try {
		return this.findAddress((FreeAddress)this.getGenProv().getAddress(number));
	} catch (javax.telephony.InvalidArgumentException iae) {
		throw new javax.csapi.cc.jcc.InvalidPartyException(
			javax.csapi.cc.jcc.InvalidPartyException.UNKNOWN_PARTY,
			iae.getMessage());
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-31 15:15:21)
 * @return net.sourceforge.gjtapi.jcc.Provider.DoubleWeakMap
 */
private net.sourceforge.gjtapi.jcc.Provider.DoubleWeakMap getAddrMap() {
	return addrMap;
}
/**
 * Return the map of listeners to adapters for calls.
 * Creation date: (2000-11-09 16:15:38)
 * @return java.util.Map
 */
private Map getCallListeners() {
	return callListeners;
}
/**
 * Private accessor for the map between the JccCall and the GJTAPI Call object.
 * Creation date: (2000-10-31 15:15:21)
 * @return net.sourceforge.gjtapi.jcc.Provider.DoubleWeakMap
 */
private net.sourceforge.gjtapi.jcc.Provider.DoubleWeakMap getCallMap() {
	return callMap;
}
/**
 * Private accessor for the map between the JccConnectionl and the GJTAPI Connectionl object.
 * Creation date: (2000-10-31 15:15:21)
 * @return net.sourceforge.gjtapi.jcc.Provider.DoubleWeakMap
 */
private net.sourceforge.gjtapi.jcc.Provider.DoubleWeakMap getConnMap() {
	return connMap;
}
/**
 * Accessor for the Generic JTAPI Provider I wrap.
 * Creation date: (2000-10-10 12:36:45)
 * @return net.sourceforge.gjtapi.GenericProvider
 */
net.sourceforge.gjtapi.GenericProvider getGenProv() {
	return genProv;
}
/**
 * Get the mapping from CallLoadControlListeners to their filters.
 * Creation date: (2000-11-10 12:18:07)
 * @return java.util.Set
 */
private Map getLoadListeners() {
	return loadListeners;
}
/**
 * getName method comment.
 */
public String getName() {
	return this.getGenProv().getName();
}
/**
 * getState method comment.
 */
public int getState() {
	int jtapiState = this.getGenProv().getState();
	switch (jtapiState) {
		case Provider.IN_SERVICE: {
			return JccProvider.IN_SERVICE;
		} 
		case Provider.OUT_OF_SERVICE: {
			return JccProvider.OUT_OF_SERVICE;
		} 
		case Provider.SHUTDOWN: {
			return JccProvider.SHUTDOWN;
		} 
		default: {
			return JccProvider.OUT_OF_SERVICE;
		}
	}
}
/**
 * removeCallListener method comment.
 */
public void removeCallListener(JccCallListener cl) {
		// see if it is registered
	Map clMap = this.getCallListeners();
	CallListenerAdapter cla = (CallListenerAdapter)clMap.remove(cl);
	if (cla == null)
		return;
	else {
			// tell all Jtapi calls to remove the listener adapter
		Iterator jtapiCallIt = this.getCallMap().keySet().iterator();
		while (jtapiCallIt.hasNext()) {
			((Call)jtapiCallIt.next()).removeCallListener(cla);
		}
	}
}
/**
 * removeCallLoadControlListener method comment.
 */
public void removeCallLoadControlListener(CallLoadControlListener clcl) {
	this.getLoadListeners().remove(clcl);
}
/**
 * Remove the JCC ConnectionListener.
 */
public void removeConnectionListener(JccConnectionListener cl) {
	this.removeCallListener(cl);
}
/**
 * removeProviderListener method comment.
 */
public void removeProviderListener(JccProviderListener listener) {
	this.getGenProv().removeProviderListener(new ProviderListenerAdapter(
		this,
		listener));
}
/**
 * setCallLoadControl method comment.
 */
public void setCallLoadControl(JccAddress[] a1, double dur, double[] mech, int[] treat) throws javax.csapi.cc.jcc.MethodNotSupportedException {
	String low = null;
	String high = null;
	double adRate = 0;
	double interval = 0;

	if (a1.length > 0)
		low = a1[0].getName();
	if (a1.length > 1)
		high = a1[1].getName();

	if (mech.length > 0)
		adRate = mech[0];
	if (mech.length > 1)
		interval = mech[1];

	try {
		this.getGenProv().getRaw().setLoadControl(
			low,
			high,
			dur,
			adRate,
			interval,
			treat);
	} catch (javax.telephony.MethodNotSupportedException mnse) {
		throw new javax.csapi.cc.jcc.MethodNotSupportedException(mnse.getMessage());
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-10 12:36:45)
 * @param newGenProv net.sourceforge.gjtapi.GenericProvider
 */
private void setGenProv(net.sourceforge.gjtapi.GenericProvider newGenProv) {
	genProv = newGenProv;
}
/**
 * shutdown method comment.
 */
public void shutdown() {
	this.getGenProv().shutdown();
}

	/**
	This method returns a standard EventFilter which is implemented by the JCC platform.
	For all events that require filtering by this {@link EventFilter}, apply the following:
	<ul>
	<li>If the mid call event type and value are matched and the connection's state (e.g. returned by 
	{@link JccConnection#getJccState()}) of the connection is {@link JccConnection#CONNECTED}, the filter 
	returns the value matchDisposition. 
	<li>If the mid call event type and value are not matched or the connection's state is not 
	{@link JccConnection#CONNECTED}, then return nomatchDisposition.
	</ul>
	
	@param midCallType an integer that represents the mid call type.  Valid values are defined, i.e. {@link MidCallData#SERVICE_CODE_DIGITS SERVICE_CODE_DIGITS},
	{@link MidCallData#SERVICE_CODE_FACILITY SERVICE_CODE_FACILITY}, {@link MidCallData#SERVICE_CODE_HOOKFLASH SERVICE_CODE_HOOKFLASH}, 
	{@link MidCallData#SERVICE_CODE_RECALL SERVICE_CODE_RECALL}, {@link MidCallData#SERVICE_CODE_U2U SERVICE_CODE_U2U}, and
	{@link MidCallData#SERVICE_CODE_UNDEFINED SERVICE_CODE_UNDEFINED}.
	@param midCallValue a string or regular expression that constrains the mid call value (for the purpose of this specification, the platform 
    will use the Perl5 regular expressions).  
	@param matchDisposition indicates the disposition of a {@link JccConnectionEvent#CONNECTION_MID_CALL}, {@link JccConnection#getMidCallData() getMidCallData()}
	gets access to the {@link MidCallData} object. The disposition should be one of the legal
	dispositions namely, {@link EventFilter#EVENT_BLOCK}, {@link EventFilter#EVENT_DISCARD} or {@link EventFilter#EVENT_NOTIFY}. 
	@param nomatchDisposition indicates the disposition of a {@link JccConnectionEvent#CONNECTION_MID_CALL}. This should be one of the legal
	dispositions namely, {@link EventFilter#EVENT_BLOCK}, {@link EventFilter#EVENT_DISCARD} or {@link EventFilter#EVENT_NOTIFY}. 
	@return EventFilter standard EventFilter provided by the JCC platform to enable 
	filtering of events based on the application's requirements.    
	@throws ResourceUnavailableException An internal resource for completing this request is unavailable. 
	@throws InvalidArgumentException One or more of the provided argument is not valid
	
	@since 1.0b
	*/
	public EventFilter createEventFilterMidCallEvent(int midCallType, String midCallValue, int matchDisposition, int nomatchDisposition) throws
	javax.csapi.cc.jcc.ResourceUnavailableException, javax.csapi.cc.jcc.InvalidArgumentException {
		//return new MidCallEventFilter(midCallType, midCallValue, matchDisposition, nomatchDisposition);
		throw new javax.csapi.cc.jcc.ResourceUnavailableException(javax.csapi.cc.jcc.ResourceUnavailableException.UNKNOWN);
	}

    /**
    This method returns a standard EventFilter which is implemented by the JCC platform.
    For all events that require filtering by this {@link EventFilter}, apply the following:
    <ul>
    <li>If the minimum address length is matched and the connection's state (e.g. returned by {@link JccConnection#getJccState()}) of the connection is {@link JccConnection#ADDRESS_ANALYZE}, the filter returns the value matchDisposition. 
    <li>If the minimum address length is not matched or the connection's state is not {@link JccConnection#ADDRESS_ANALYZE}, then return nomatchDisposition.
    </ul>
    
    Note that applications may need to remove this filter (through 
    {@link JccCall#removeConnectionListener(JccConnectionListener)} or 
    {@link JccProvider#removeConnectionListener(JccConnectionListener)}) if they are notified once.  Otherwise the 
    filter may be satisfied each time a set of digits is added to the received address and keep firing.  
    If this is not desirable, the application needs to remove the listener as indicated above.
    
    @param minimumAddressLength an integer that represents a minimum address length.  
    @param matchDisposition indicates the disposition of a {@link JccConnectionEvent#CONNECTION_ADDRESS_ANALYZE} where
    the length of the address matches or is greater than the given minimum length. This should be one of the legal
    dispositions namely, {@link EventFilter#EVENT_BLOCK}, {@link EventFilter#EVENT_DISCARD} or {@link EventFilter#EVENT_NOTIFY}. 
    @param nomatchDisposition indicates the disposition of a {@link JccConnectionEvent#CONNECTION_ADDRESS_ANALYZE} where
    the length of the address is less than the given minimum length. This should be one of the legal
    dispositions namely, {@link EventFilter#EVENT_BLOCK}, {@link EventFilter#EVENT_DISCARD} or {@link EventFilter#EVENT_NOTIFY}. 
    @return EventFilter standard EventFilter provided by the JCC platform to enable 
    filtering of events based on the application's requirements.    
    @throws ResourceUnavailableException An internal resource for completing this request is unavailable. 
    @throws InvalidArgumentException One or more of the provided argument is not valid
    
    @since 1.0b
    */
    public EventFilter createEventFilterMinimunCollectedAddressLength(int minimumAddressLength, int matchDisposition, int nomatchDisposition) throws
    javax.csapi.cc.jcc.ResourceUnavailableException, javax.csapi.cc.jcc.InvalidArgumentException {
    	return new MinimumCollectedAddressLengthFilter(minimumAddressLength, matchDisposition, nomatchDisposition);
    }

/**
 * Describe myself.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jain Call Control Provider for the Generic JTAPI Framework.";
}
/**
 * Register a GJTAPI Call with Jcc Listeners that have been registered using either
 * Provider.addCallListener() or Provider.addConnectionListener().
 * <P>This allows listeners to be registered with Jcc when they finally are visible to the Jcc Provider.
 * @author rdeadman
 *
 */
	public void registerCallListeners(FreeCall gjtapiCall) {
		// now add the JccCallListeners that I have queued up, by adding their adapters to the real call.
		Iterator it = this.getCallListeners().values().iterator();
		while (it.hasNext()) {
			gjtapiCall.addCallListener((CallListener)it.next());
		}
	}
}
