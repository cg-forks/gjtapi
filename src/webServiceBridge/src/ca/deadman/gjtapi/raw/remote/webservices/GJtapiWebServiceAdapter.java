package ca.deadman.gjtapi.raw.remote.webservices;
/*
	Copyright (c) Richard Deadman, Deadman Consulting (www.deadman.ca)

	All rights reserved.

	This software is dual licenced under the GPL and a commercial license.
	If you wish to use under the GPL, the following license applies, otherwise
	please contact Deadman Consulting at sales@deadman.ca for commercial licensing.

    ---

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.events.*;
import javax.telephony.media.MediaResourceException;
import javax.telephony.media.RTC;
import javax.telephony.media.Symbol;

import ca.deadman.gjtapi.raw.remote.MovableEventIds;

import net.sourceforge.gjtapi.CallData;
import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.RawStateException;
import net.sourceforge.gjtapi.TelephonyListener;
import net.sourceforge.gjtapi.TelephonyProvider;
import net.sourceforge.gjtapi.TermData;
import net.sourceforge.gjtapi.media.SymbolConvertor;
import net.sourceforge.gjtapi.media.SymbolHolder;
import net.sourceforge.gjtapi.raw.CoreTpi;
import net.sourceforge.gjtapi.raw.ProviderFactory;
import net.sourceforge.gjtapi.raw.remote.CallMapper;
import net.sourceforge.gjtapi.raw.remote.SerializableCallId;

/**
 * This is one possible implementation of the remote web service that implements the WewbProviderIF
 * interface. This implementation hooks directly into a GJTAPI TPI. Other implementations may be written in
 * any language as long as they export the correct JAX-RPC web service.
 * @author rdeadman
 *
 */
public class GJtapiWebServiceAdapter implements GJtapiWebServiceIF {
	// The name of the property holding the TPI class name
	private final static String TPI = "ca.deadman.gjtapi.wsTpi";
	/**
	 * This holds a queue of Events to be picked up by a client and also notes when the last pickup occured.
	 * @author rdeadman
	 */
	class EventQueue {
		final static int POLL_WAIT_TIME = 5000;

		private Calendar cal = Calendar.getInstance();

		private List eventsList = new ArrayList();
		private long lastAccessTime;

		EventQueue() {
			lastAccessTime = cal.getTimeInMillis();
		}

		synchronized void addEvent(EventHolder eh) {
			this.eventsList.add(eh);

			// tell any waiting polls to continue
			this.notifyAll();
		}

		synchronized EventHolder[] getEvents() {
			this.lastAccessTime = cal.getTimeInMillis();

			// wait for events to come it -- interrupted by events
			try {
				this.wait(POLL_WAIT_TIME);
			} catch (InterruptedException ie) {
				// keep going
			}
			EventHolder[] evs = (EventHolder[])eventsList.toArray(new EventHolder[eventsList.size()]);
			eventsList.clear();
			return evs;
		}

		boolean shouldFree() {
			return (lastAccessTime + queueTTL) < cal.getTimeInMillis();
		}
	}

	/**
	 * A Telephony Listener that collects raw telephony events, turns them into EventHolders
	 * and assigns them to the Events Queues of the polling web service clients.
	 * @author rdeadman
	 */
	class RawEventHandler implements TelephonyListener {
		private Map queueMap;
		private CallMapper callMapper;

		/**
		 * Create an Event Handler with a handle to the map containing the set of EventQueues as
		 * values.
		 * @param qMap
		 */
		RawEventHandler(Map qMap, CallMapper mcm) {
			super();

			this.queueMap = qMap;
			this.callMapper = mcm;
		}
		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#addressPrivateData(java.lang.String, java.io.Serializable, int)
		 */
		public void addressPrivateData(
			String address,
			Serializable data,
			int cause) {
				// I can't handle private data...
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#callActive(net.sourceforge.gjtapi.CallId, int)
		 */
		public void callActive(CallId id, int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), CallActiveEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#callInvalid(net.sourceforge.gjtapi.CallId, int)
		 */
		public void callInvalid(CallId id, int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), CallInvalidEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#callOverloadCeased(java.lang.String)
		 */
		public void callOverloadCeased(String address) {
			EventHolder eh = new EventHolder();
			eh.evId = MovableEventIds.OVERLOAD_CEASED;
			eh.address = address;

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#callOverloadEncountered(java.lang.String)
		 */
		public void callOverloadEncountered(String address) {
			EventHolder eh = new EventHolder();
			eh.evId = MovableEventIds.OVERLOAD_ENCOUNTERED;
			eh.address = address;

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#callPrivateData(net.sourceforge.gjtapi.CallId, java.io.Serializable, int)
		 */
		public void callPrivateData(
			CallId call,
			Serializable data,
			int cause) {
				// we don't support private data
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionAddressAnalyse(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionAddressAnalyse(
			CallId id,
			String address,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, MovableEventIds.ADDRESS_ANALYZE, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionAddressCollect(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionAddressCollect(
			CallId id,
			String address,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, MovableEventIds.ADDRESS_COLLECT, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionAlerting(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionAlerting(CallId id, String address, int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, ConnAlertingEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionAuthorizeCallAttempt(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionAuthorizeCallAttempt(
			CallId id,
			String address,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, MovableEventIds.CONN_AUTH_CALL_ATTEMPT, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionCallDelivery(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionCallDelivery(
			CallId id,
			String address,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, MovableEventIds.CONN_CALL_DELIVERY, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionConnected(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionConnected(CallId id, String address, int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, ConnConnectedEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionDisconnected(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionDisconnected(
			CallId id,
			String address,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, ConnDisconnectedEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionFailed(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionFailed(CallId id, String address, int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, ConnFailedEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionInProgress(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionInProgress(
			CallId id,
			String address,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, ConnInProgressEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#connectionSuspended(net.sourceforge.gjtapi.CallId, java.lang.String, int)
		 */
		public void connectionSuspended(CallId id, String address, int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, MovableEventIds.CONN_SUSPENDED, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#mediaPlayPause(java.lang.String, int, int, javax.telephony.media.Symbol)
		 */
		public void mediaPlayPause(
			String terminal,
			int index,
			int offset,
			Symbol trigger) {
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#mediaPlayResume(java.lang.String, javax.telephony.media.Symbol)
		 */
		public void mediaPlayResume(String terminal, Symbol trigger) {
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#mediaRecorderPause(java.lang.String, int, javax.telephony.media.Symbol)
		 */
		public void mediaRecorderPause(
			String terminal,
			int duration,
			Symbol trigger) {
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#mediaRecorderResume(java.lang.String, javax.telephony.media.Symbol)
		 */
		public void mediaRecorderResume(String terminal, Symbol trigger) {
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#mediaSignalDetectorDetected(java.lang.String, javax.telephony.media.Symbol)
		 */
		public void mediaSignalDetectorDetected(
			String terminal,
			Symbol[] sigs) {
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#mediaSignalDetectorOverflow(java.lang.String, javax.telephony.media.Symbol)
		 */
		public void mediaSignalDetectorOverflow(
			String terminal,
			Symbol[] sigs) {
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#mediaSignalDetectorPatternMatched(java.lang.String, javax.telephony.media.Symbol, int)
		 */
		public void mediaSignalDetectorPatternMatched(
			String terminal,
			Symbol[] sigs,
			int index) {
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#providerPrivateData(java.io.Serializable, int)
		 */
		public void providerPrivateData(Serializable data, int cause) {
			// we don't suport private data
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#terminalConnectionCreated(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String, int)
		 */
		public void terminalConnectionCreated(
			CallId id,
			String address,
			String terminal,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, terminal, TermConnCreatedEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#terminalConnectionDropped(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String, int)
		 */
		public void terminalConnectionDropped(
			CallId id,
			String address,
			String terminal,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, terminal, TermConnDroppedEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#terminalConnectionHeld(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String, int)
		 */
		public void terminalConnectionHeld(
			CallId id,
			String address,
			String terminal,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, terminal, TermConnPassiveEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#terminalConnectionRinging(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String, int)
		 */
		public void terminalConnectionRinging(
			CallId id,
			String address,
			String terminal,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, terminal, TermConnRingingEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#terminalConnectionTalking(net.sourceforge.gjtapi.CallId, java.lang.String, java.lang.String, int)
		 */
		public void terminalConnectionTalking(
			CallId id,
			String address,
			String terminal,
			int cause) {
			EventHolder eh = new EventHolder(this.callMapper.swapId(id).hashCode(), address, terminal, TermConnActiveEv.ID, cause);

			this.store(eh);
		}

		/**
		 * @see net.sourceforge.gjtapi.TelephonyListener#terminalPrivateData(java.lang.String, java.io.Serializable, int)
		 */
		public void terminalPrivateData(
			String terminal,
			Serializable data,
			int cause) {
				// we don't support private data
		}

		/**
		 * Store the EventHolder in all the available EventQueues.
		 * @param eh The serializable representation of the event.
		 */
		private void store(EventHolder eh) {
			synchronized (this.queueMap) {
				Iterator it = this.queueMap.values().iterator();
				while (it.hasNext()) {
					EventQueue eq = (EventQueue)it.next();
					if (eq.shouldFree()) {
						it.remove();
					} else {
						eq.addEvent(eh);
					}
				}
			}
		}
	}
	/**
	 * timeut value in milliseconds after which a client is assumed dead and its queue will be released.
	 * Initially 5 minutes.
	 */
	private int queueTTL = 300000;
	private TelephonyProvider delegate;
	private CallMapper refMapper = new CallMapper();
	// The map of client ids to event queues.
	private Map eventQueueMap = new HashMap();

	private int nextId = 0;	// don't reuse -- we should never have that many clients between reboots.


	/**
	 * Constructor used by the WebProvider to do loop-back testing
	 */
	public GJtapiWebServiceAdapter(TelephonyProvider tp) {
		super();

		this.init(tp, new Properties());
	}

	private void init (TelephonyProvider tp, Properties props) {

		if (tp == null)
			throw new NullPointerException();
		else
			this.setDelegate(tp);

		// this only applies to the EmProvider
		//props.put(EmProvider.DISPLAY, "false");
		tp.initialize(props);

		// register my listener (wrapped in a event dispatch pool)
		tp.addListener(new RawEventHandler(this.eventQueueMap, this.refMapper));
	}

	/**
	 * Constructor used by the JAXRPCServlet and JAXWS servlet during web service loading.
	 */
	public GJtapiWebServiceAdapter(Properties properties) {
		super();

		// Find the TPI I use by looking up a property
		CoreTpi tpi = null;

		// first check if the properties set holds the TPI class name
		if (properties == null)
			properties = new Properties();
		String classname = properties.getProperty(TPI, "net.sourceforge.gjtapi.raw.emulator.EmProvider");

		// instantiate
		Object o = null;
		Throwable cause = null;
		try {
			o = Class.forName(classname).newInstance();
		} catch (ClassNotFoundException cnfe) {
			cause = cnfe;
			// fall through
		} catch (InstantiationException ie) {
			cause = ie;
			// fall through
		} catch (IllegalAccessException iae) {
			cause = iae;
			// fall through
		}

		if (cause != null) {
			throw new RuntimeException("Error creating adapter delegate", cause);
		}
		if (o instanceof CoreTpi) {
			tpi = (CoreTpi)o;
		} else {
			throw new RuntimeException("Could not instantiate GJTAPI Adapter TPI, not correct interface: " + classname);
		}
		this.init(ProviderFactory.createProvider(tpi), properties);
	}

	/**
	 * @see net.sourceforge.gjtapi.raw.remote.webservices.WebProviderIF#pollEvents(int)
	 */
	public EventHolder[] pollEvents(int id) throws RemoteException {
		EventQueue eq = (EventQueue)this.eventQueueMap.get(new Integer(id));
		if (eq == null)
			throw new RemoteException("Queue has been abandoned");
		else
			return eq.getEvents();
	}


	/**
	 * @see net.sourceforge.gjtapi.raw.remote.webservices.WebProviderIF#registerQueue()
	 */
	public int registerQueue() throws RemoteException {

		synchronized (eventQueueMap) {
			int id = this.nextId;
			nextId++;
			eventQueueMap.put(new Integer(id), new EventQueue());
			return id;
		}
	}


	/**
	 * @see net.sourceforge.gjtapi.raw.remote.webservices.WebProviderIF#removeQueue(int)
	 */
	public void removeQueue(int id) throws RemoteException {
		synchronized (this.eventQueueMap) {
			eventQueueMap.remove(new Integer(id));
		}
	}

/**
 * Forward the request on to the local RawProvider
 */
public boolean allocateMedia(String terminal, int type, HashMap params) {
	return this.getDelegate().allocateMedia(terminal, type, this.fromSerializable(params));
}
/**
 * Delegate off to remote provider
 */
public void answerCall(int call, String address, String terminal) throws MobileJavaxException, MobileStateException {

	try {
		this.getDelegate().answerCall(this.getProvCall(call), address, terminal);
	} catch (RawStateException rse) {
		throw new MobileStateException(new MobileState(rse.getCall().hashCode(), rse.getAddress(), rse.getTerminal(), rse.getObjectType(), rse.getState(), rse.getMessage()));
	} catch (PrivilegeViolationException pve) {
		throw new MobileJavaxException(pve);
	} catch (ResourceUnavailableException rue) {
		throw new MobileJavaxException(rue);
	} catch (MethodNotSupportedException mnse) {
		throw new MobileJavaxException(mnse);
	}
}
/**
 * attachMedia method comment.
 */
public boolean attachMedia(int call, java.lang.String address, boolean onFlag) throws java.rmi.RemoteException {

	return this.getDelegate().attachMedia(this.getProvCall(call), address, onFlag);
}
/**
 * beep method comment.
 */
public void beep(int call) throws java.rmi.RemoteException {

	this.getDelegate().beep(this.getProvCall(call));
}
/**
 * delegate on createCall
 */
public int createCall(int id, String address, String term, String dest) throws MobileJavaxException,
	  MobileStateException {
	TelephonyProvider rp = this.getDelegate();

	try {
		return this.getRefMapper().callToInt(rp.createCall(this.getProvCall(id), address, term, dest));
	} catch (RawStateException rse) {
		throw new MobileStateException(new MobileState(rse.getCall().hashCode(), rse.getAddress(), rse.getTerminal(), rse.getObjectType(), rse.getState(), rse.getMessage()));
	} catch (PrivilegeViolationException pve) {
		throw new MobileJavaxException(pve);
	} catch (ResourceUnavailableException rue) {
		throw new MobileJavaxException(rue);
	} catch (MethodNotSupportedException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (InvalidPartyException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (InvalidArgumentException mnse) {
		throw new MobileJavaxException(mnse);
	}
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
private Dictionary fromSerializable(HashMap dict) {
	if (dict == null)
		return null;
	Hashtable table = new Hashtable();
	Iterator keys = dict.keySet().iterator();
	while (keys.hasNext()) {
		Object k = keys.next();
		Symbol key;
		Object v = dict.get(k);
		Symbol value;
		if (k instanceof Integer)
			key = Symbol.getSymbol(((Integer)k).intValue());
		if (v instanceof Integer)
			value = Symbol.getSymbol(((Integer)v).intValue());

		table.put(k, v);
	}
	return table;
}
/**
 * Delegate remote getAddresses to real provider.
 */
public String[] getAddresses() throws RemoteException, MobileJavaxException {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null)
		try {
			return rp.getAddresses();
		} catch (ResourceUnavailableException ex) {
			throw new MobileJavaxException(ex);
		}
	else
		throw new RemoteException();
}
/**
 * Delegate remote getTerminals method to real provider.
 */
public String[] getAddressesForTerminal(String terminal) throws RemoteException, MobileJavaxException {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null)
		try {
			return rp.getAddresses(terminal);
		} catch (InvalidArgumentException ex) {
			throw new MobileJavaxException(ex);
		}
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
public MovableCallData getCall(int id) throws RemoteException  {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null)
		return this.toMovable(rp.getCall(this.getRefMapper().intToCall(id)));
	else
		throw new RemoteException();
}

/**
 * Create a JAX-RPC movable CallData record.
 * @param cd The original CallData record
 * @return CallData A cloned CallData record with a int.
 */
private MovableCallData toMovable(CallData cd) {
	return new MovableCallData(this.getRefMapper().callToInt(cd.id), cd.callState, cd.connections);
}
/**
 * getCallsOnAddress method comment.
 */
public MovableCallData[] getCallsOnAddress(java.lang.String number) throws java.rmi.RemoteException {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null)
		return this.toMovable(rp.getCallsOnAddress(number));
	else
		throw new RemoteException();
}
/**
 * Delegate on to real TelephonyProvider.
 */
public MovableCallData[] getCallsOnTerminal(String name) throws RemoteException  {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null)
		return this.toMovable(rp.getCallsOnTerminal(name));
	else
		throw new RemoteException();
}

/**
 * Turn an array of CallData elements into one containing CallData elements with Serializable IDs.
 * @param cds The original CallData array
 * @return CallData[] A cloned array with ints inside the Calldata records.
 */
private MovableCallData[] toMovable(CallData[] cds) {
	if (cds == null)
		return null;
	int len = cds.length;
	MovableCallData[] result = new MovableCallData[len];
	for (int i = 0; i < len; i++) {
		result[i] = this.toMovable(cds[i]);
	}
	return result;
}

/**
 * getCapabilities method comment.
 */
public java.util.Properties getCapabilities() throws java.rmi.RemoteException {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null) {
		System.out.println("Real Provider Properties: " + rp.getCapabilities());
		return rp.getCapabilities();
	}
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
 * getDialledDigits method comment.
 */
public java.lang.String getDialledDigits(int id, java.lang.String address) throws java.rmi.RemoteException {

	return this.getDelegate().getDialledDigits(this.getProvCall(id), address);
}
/**
 * Simple utility function for looking up a provider id
 * Creation date: (2000-02-18 0:16:44)
 * @author: Richard Deadman
 * @return The provider id
 * @param id The transmission int proxy reference
 */
private CallId getProvCall(int id) {
	return this.getRefMapper().intToCall(id);
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
public TermData[] getTerminals() throws RemoteException, MobileJavaxException  {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null)
		try {
			return rp.getTerminals();
		} catch (ResourceUnavailableException rue) {
			throw new MobileJavaxException(rue);
		}
	else
		throw new RemoteException();
}
/**
 * Delegate remote getTerminals method to real provider.
 */
public TermData[] getTerminalsForAddress(String address) throws RemoteException, MobileJavaxException {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null)
		try {
			return rp.getTerminals(address);
		} catch (InvalidArgumentException iae) {
			throw new MobileJavaxException(iae);
		}
	else
		throw new RemoteException();
}
/**
 * Delegate remote hold message to real provider.
 */
public void hold(int call, String address, String term) throws MobileStateException, MobileJavaxException {

	try {
		this.getDelegate().hold(this.getProvCall(call), address, term);
	} catch (RawStateException rse) {
		throw new MobileStateException(new MobileState(rse.getCall().hashCode(), rse.getAddress(), rse.getTerminal(), rse.getObjectType(), rse.getState(), rse.getMessage()));
	} catch (PrivilegeViolationException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (MethodNotSupportedException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (ResourceUnavailableException mnse) {
		throw new MobileJavaxException(mnse);
	}
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
public int join(int call1, int call2, String address, String terminal) throws MobileStateException, MobileJavaxException {

	try {
		return (int)this.getRefMapper().callToInt(this.getDelegate().join(this.getProvCall(call1),
				this.getProvCall(call2),
				address,
				terminal));
	} catch (RawStateException rse) {
		throw new MobileStateException(new MobileState(rse.getCall().hashCode(), rse.getAddress(), rse.getTerminal(), rse.getObjectType(), rse.getState(), rse.getMessage()));
	} catch (InvalidArgumentException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (PrivilegeViolationException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (MethodNotSupportedException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (ResourceUnavailableException mnse) {
		throw new MobileJavaxException(mnse);
	}
}
/**
 * Forward the request on to the local RawProvider
 */
public void play(String terminal, String[] streamIds, int offset, RTCPair[] pairs, HashMap optArgs) throws MobileResourceException {
	try {
		this.getDelegate().play(terminal, streamIds, offset, this.toRtcSet(pairs), this.fromSerializable(optArgs));
	} catch (MediaResourceException mrex) {
		throw new MobileResourceException(mrex);
	}
}

private RTC[] toRtcSet(RTCPair[] pairs) {
	if (pairs == null)
		return null;
	int len = pairs.length;
	RTC[] holders = new RTC[len];
	for (int i = 0; i < len; i++) {
		holders[i] = pairs[i].toRTC();
	}
	return holders;
}

/**
 * Forward the request on to the local RawProvider
 */
public void record(String terminal,
	String streamId,
	RTCPair[] holders,
	HashMap optArgs) throws MobileResourceException {
	try {
		this.getDelegate().record(terminal, streamId, this.toRtcSet(holders), this.fromSerializable(optArgs));
	} catch (MediaResourceException mrex) {
		throw new MobileResourceException(mrex);
	}
}
/**
 * Delegate remote release message to real provider.
 */
public void release(String address, int call) throws MobileJavaxException, MobileStateException {

	try {
		this.getDelegate().release(address, this.getProvCall(call));
	} catch (RawStateException rse) {
		throw new MobileStateException(new MobileState(rse.getCall().hashCode(), rse.getAddress(), rse.getTerminal(), rse.getObjectType(), rse.getState(), rse.getMessage()));
	} catch (PrivilegeViolationException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (ResourceUnavailableException mnse) {
		throw new MobileJavaxException(mnse);
	} catch (MethodNotSupportedException mnse) {
		throw new MobileJavaxException(mnse);
	}
}
/**
 * releaseCallId method comment.
 */
public void releaseCallId(int id) throws RemoteException {
	this.getDelegate().releaseCallId(this.getProvCall(id));

	// now release my handle to it
	CallMapper cm = this.getRefMapper();
	CallId localCallId = cm.intToCall(id);
	SerializableCallId serCallId = cm.swapId(localCallId);
	this.getRefMapper().freeId(serCallId);
}
/**
 * Forward the request on to the local RawProvider
 * <P>Since we don't track which Frameworks monitor a call, only allow turning this on.  Note
 * that multiple Generic Framework clients may have registered for reporting of this call.
 */
public void reportCallsOnAddress(String address, boolean flag)
throws MobileJavaxException {
	if (flag)
		try {
			this.getDelegate().reportCallsOnAddress(address, flag);
		} catch (InvalidArgumentException mnse) {
			throw new MobileJavaxException(mnse);
		} catch (ResourceUnavailableException mnse) {
			throw new MobileJavaxException(mnse);
		}
}
/**
 * Forward the request on to the local RawProvider
 * <P>Since we don't track which Frameworks monitor a call, only allow turning this on.  Note
 * that multiple Generic Framework clients may have registered for reporting of this call.
 */
public void reportCallsOnTerminal(String terminal, boolean flag)
throws MobileJavaxException {
	if (flag)
		try {
			this.getDelegate().reportCallsOnTerminal(terminal, flag);
		} catch (InvalidArgumentException mnse) {
			throw new MobileJavaxException(mnse);
		} catch (ResourceUnavailableException mnse) {
			throw new MobileJavaxException(mnse);
		}
}
/**
 * Delegate remote reserveCallId message to real provider.
 */
public int reserveCallId(String address) throws RemoteException, MobileJavaxException {
	TelephonyProvider rp = this.getDelegate();

	if (rp != null)
		try {
			return this.getRefMapper().callToInt(rp.reserveCallId(address));
		} catch (InvalidArgumentException iae) {
			throw new MobileJavaxException(iae);
		}
	else
		throw new RemoteException();
}
/**
 * Forward the request on to the local RawProvider
 */
public EventHolder retrieveSignals(String terminal,
	int num,
	int[] patHolders,
	RTCPair[] rtcHolders,
	HashMap optArgs) throws MobileResourceException {

	Symbol[] patterns = new Symbol[patHolders.length];
	for (int i = 0; i < patHolders.length; i++) {
		patterns[i] = Symbol.getSymbol(patHolders[i]);
	}

	RawSigDetectEvent rsde = null;
	try {
		rsde = this.getDelegate().retrieveSignals(terminal, num, patterns, this.toRtcSet(rtcHolders), this.fromSerializable(optArgs));
	} catch (MediaResourceException mrex) {
		throw new MobileResourceException(mrex);
	}
	EventHolder eh = new EventHolder();
	// fill in the raw sig detect event
	eh.terminal = rsde.getTerminal();
	eh.mediaQualifier = rsde.getQualifier().hashCode();
	eh.signals = SymbolConvertor.convert(SymbolHolder.decode(rsde.getSigs()));
	eh.mediaIndex = rsde.getPatternIndex();
	eh.mediaTrigger = rsde.getTrigger().hashCode();
	eh.mediaError = rsde.getErr().hashCode();
	return eh;
}
/**
 * Forward the request on to the local RawProvider
 */
public void sendSignals(String terminal,
	int[] symHolders,
	RTCPair[] rtcHolders,
	HashMap optArgs) throws MobileResourceException {

	Symbol[] syms = new Symbol[symHolders.length];
	for (int i = 0; i < symHolders.length; i++) {
		syms[i] = Symbol.getSymbol(symHolders[i]);
	}

	try {
		this.getDelegate().sendSignals(terminal,syms, this.toRtcSet(rtcHolders), this.fromSerializable(optArgs));
	} catch (MediaResourceException mrex) {
		throw new MobileResourceException(mrex);
	}
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
public void setLoadControl(java.lang.String startAddr, java.lang.String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws RemoteException, MobileJavaxException {
	try {
		this.getDelegate().setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatment);
	} catch (MethodNotSupportedException mnse) {
		throw new MobileJavaxException(mnse);
	}
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
public boolean stopReportingCall(int call)  {
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
public void triggerRTC(String terminal, int action) {
	this.getDelegate().triggerRTC(terminal, Symbol.getSymbol(action));
}
/**
 * Delegate remote unHold message to real provider
 */
public void unHold(int call, String address, String term) throws MobileStateException, MobileJavaxException {

	try {
		this.getDelegate().unHold(this.getProvCall(call), address, term);
	} catch (RawStateException rse) {
		throw new MobileStateException(new MobileState(rse.getCall().hashCode(), rse.getAddress(), rse.getTerminal(), rse.getObjectType(), rse.getState(), rse.getMessage()));
	} catch (MethodNotSupportedException ex) {
		throw new MobileJavaxException(ex);
	} catch (PrivilegeViolationException ex) {
		throw new MobileJavaxException(ex);
	} catch (ResourceUnavailableException ex) {
		throw new MobileJavaxException(ex);
	}
}

}
