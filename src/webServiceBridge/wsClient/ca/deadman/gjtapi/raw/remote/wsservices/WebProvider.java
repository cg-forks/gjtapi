package ca.deadman.gjtapi.raw.remote.wsservices;

/*
	Copyright (c) 2008 Richard Deadman, Deadman Consulting (www.deadman.ca)

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
import java.io.*;

import javax.telephony.events.CallActiveEv;
import javax.telephony.events.CallInvalidEv;
import javax.telephony.events.ConnAlertingEv;
import javax.telephony.events.ConnConnectedEv;
import javax.telephony.events.ConnCreatedEv;
import javax.telephony.events.ConnDisconnectedEv;
import javax.telephony.events.ConnInProgressEv;
import javax.telephony.events.TermConnActiveEv;
import javax.telephony.events.TermConnCreatedEv;
import javax.telephony.events.TermConnDroppedEv;
import javax.telephony.events.TermConnPassiveEv;
import javax.telephony.events.TermConnRingingEv;
import javax.telephony.events.TermConnUnknownEv;
import javax.telephony.media.*;
import javax.telephony.media.events.MediaTermConnDtmfEv;
import javax.telephony.media.events.MediaTermConnStateEv;

import net.sourceforge.gjtapi.capabilities.Capabilities;
import net.sourceforge.gjtapi.media.*;
import net.sourceforge.gjtapi.raw.remote.SerializableCallId;

import javax.telephony.*;
import javax.xml.namespace.QName;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import net.sourceforge.gjtapi.*;
import java.util.*;

import ca.deadman.gjtapi.raw.remote.webservices.*;
import ca.deadman.gjtapi.raw.remote.MovableEventIds;

/**
 * This is a pluggable provider that provides access to a remote provider through a JAX-WS
 * web-services pipe
 * Creation date: (2008-09-07 10:53:56)
 * @author: Richard Deadman
 */
public class WebProvider implements TelephonyProvider {

	/*
	 * The name of the key in the initialization properties from which to find the
	 * Remote WS URL to hook up to the Stubs.
	 */
	// keys for the other web service lookup parts
	public final static String URL = "url";
	public final static String QNAME_URI = "qNameUri";
	public final static String QNAME_LOCAL = "qNameLocal";

	// The location of the properties file to set my values
	private final static String RESOURCE_NAME = "JaxWsProvider.props";

	private final String defaultUrl = "http://localhost:80/Gjtapi-ws";
	private final String defaultQNameUri = "http://webservices.remote.raw.gjtapi.deadman.ca/";
	private final String defaultQNameLocal = "GjtapiJaxWsService";

	/*
	 * The stub that I send remote messages to.
	 */
	private GjtapiJaxWs remote;
	/*
	 * A polling object that gets remote events and sends them back to my TelephonyListener.
	 */
	private EventPoller poller = null;

	/**
	 * The set of properties that define my behaviour
	 */
	private java.util.Properties providerProps = new java.util.Properties();

	/**
	 * Simple conversion utility
	 */
	private DataTranslator dt = new DataTranslator();

	/**
	 * This is a debug string that defines how we hooked up to the remote end
	 */
	private String hookupInfo = null;

	/**
	 * A polling thread that asks the web service for events and then forwards the event information
	 * on to the TelephonyListener. Events are received as an array of Event objects (more in the
	 * Observer) tradition, and so must be converted into the new Listener model.
	 * <P>A polling mechanism is needed since JAX-RPC does not support remote call-back objects
	 * or the passing of remote object handles.
	 * @author rdeadman
	 *
	 */
	class EventPoller implements Runnable {
		// The GJTAPI event listener
		private TelephonyListener listener = null;
		// The web service to ask for events from
		private GjtapiJaxWs service = null;
		// Remote event queue id
		private int queueId;

		// Flag to tell me to stop polling
		private boolean poll = true;

		/**
		 * Create the Event Poller with information about the GJTAPI listener and the remote service.
		 * @param tl
		 * @param remote
		 */
		EventPoller(TelephonyListener tl, GjtapiJaxWs remote) throws RemoteException {
			this.listener = tl;
			this.service = remote;

			// now ask for a Queue id
			this.queueId = remote.registerQueue();

		}

		/**
		 * Start polling for events until I am told to stop.
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			List<EventHolder> evs = null;	// event set
			int failureCount = 0;	// number or remote failures before I give up.
			int msBackoff = 50;	// how long should I wait after a failure
			RemoteException lastRe = null;

			while (poll && (failureCount < 10)) {
				// Get the events
				evs = service.pollEvents(this.queueId);
				failureCount = 0;
				lastRe = null;

				// Send each event to the GJTAPI listener
				if (evs != null) {
					int evLength = evs.size();
					for (int i = 0; i < evLength; i++) {
						EventHolder eh = evs.get(i);
						switch (eh.getEvId()) {
//							case Event.sdf: {
//								listener.addressPrivateData();
//							}
							case CallActiveEv.ID: {
								listener.callActive(new SerializableCallId(eh.getCallId()), eh.getCause());
								break;
							}
							case CallInvalidEv.ID: {
								listener.callInvalid(new SerializableCallId(eh.getCallId()), eh.getCause());
								break;
							}
							case ConnAlertingEv.ID: {
								listener.connectionAlerting(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case ConnConnectedEv.ID: {
								listener.connectionConnected(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case ConnCreatedEv.ID: {
								// I don't think this is ever called
								break;
							}
							case ConnInProgressEv.ID: {
								listener.connectionInProgress(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case ConnDisconnectedEv.ID: {
								listener.connectionDisconnected(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case TermConnActiveEv.ID: {
								listener.terminalConnectionTalking(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getTerminal(), eh.getCause());
								break;
							}
							case TermConnCreatedEv.ID: {
								listener.terminalConnectionCreated(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getTerminal(), eh.getCause());
								break;
							}
							case TermConnDroppedEv.ID: {
								listener.terminalConnectionDropped(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getTerminal(), eh.getCause());
								break;
							}
							case TermConnPassiveEv.ID: {
								listener.terminalConnectionHeld(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getTerminal(), eh.getCause());
								break;
							}
							case TermConnRingingEv.ID: {
								listener.terminalConnectionRinging(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getTerminal(), eh.getCause());
								break;
							}
							case TermConnUnknownEv.ID: {
								// no event
								break;
							}
							case MediaTermConnDtmfEv.ID: {
								switch (eh.getMediaEv()) {
									case MovableEventIds.MEDIA_DTMF_DETECT: {
										listener.mediaSignalDetectorDetected(eh.getTerminal(), SymbolConvertor.convert(eh.getSignals()));
										break;
									}
									case MovableEventIds.MEDIA_DTMF_OVERFLOW: {
										listener.mediaSignalDetectorOverflow(eh.getTerminal(), SymbolConvertor.convert(eh.getSignals()));
										break;
									}
									case MovableEventIds.MEDIA_DTMF_PATTERNMATCH: {
										listener.mediaSignalDetectorPatternMatched(eh.getTerminal(), SymbolConvertor.convert(eh.getSignals()), eh.getMediaIndex());
										break;
									}
								}
								break;
							}
							case MediaTermConnStateEv.ID: {
								switch (eh.getMediaEv()) {
									case MovableEventIds.MEDIA_PLAY_PAUSE: {
										listener.mediaPlayPause(eh.getTerminal(), eh.getMediaIndex(), eh.getMediaOffset(),Symbol.getSymbol(eh.getMediaTrigger()));
										break;
									}
									case MovableEventIds.MEDIA_PLAY_RESUME: {
										listener.mediaPlayResume(eh.getTerminal(), Symbol.getSymbol(eh.getMediaTrigger()));
										break;
									}
									case MovableEventIds.MEDIA_RECORD_PAUSE: {
										listener.mediaRecorderPause(eh.getTerminal(), eh.getMediaDuration(), Symbol.getSymbol(eh.getMediaTrigger()));
										break;
									}
									case MovableEventIds.MEDIA_RECORD_RESUME: {
										listener.mediaRecorderResume(eh.getTerminal(), Symbol.getSymbol(eh.getMediaTrigger()));
										break;
									}
								}
								break;
							}
							case MovableEventIds.ADDRESS_ANALYZE: {
								listener.connectionAddressAnalyse(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case MovableEventIds.ADDRESS_COLLECT: {
								listener.connectionAddressCollect(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case MovableEventIds.CONN_AUTH_CALL_ATTEMPT: {
								listener.connectionAuthorizeCallAttempt(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case MovableEventIds.CONN_CALL_DELIVERY: {
								listener.connectionCallDelivery(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case MovableEventIds.CONN_SUSPENDED: {
								listener.connectionSuspended(new SerializableCallId(eh.getCallId()), eh.getAddress(), eh.getCause());
								break;
							}
							case MovableEventIds.OVERLOAD_ENCOUNTERED: {
								listener.callOverloadEncountered(eh.getAddress());
								break;
							}
							case MovableEventIds.OVERLOAD_CEASED: {
								listener.callOverloadCeased(eh.getAddress());
								break;
							}
							default: {
								System.out.println("Unknown remote event: " + eh.getEvId());
							}
						}
					}
				}
			}
			if (poll) {
				// we stopped because of a remote failure
				System.out.println("Event Poller failed too often. Last Exception:");
				lastRe.printStackTrace(System.out);
			}

				// unregister out queueId
			this.service.removeQueue(queueId);
		}

		/**
		 * Stop the poller
		 */
		void stopPolling() {
			this.poll = false;
		}
	}
/**
 * Forward to remote provider
 */
public void addListener(TelephonyListener rl) {
	try {
		this.poller = new EventPoller(rl, remote);
		new Thread(this.poller).start();
	} catch (RemoteException re) {	// couldn't register a queue
		System.out.println("Failed to hook up for events");
	}
}
/**
 * Forward to a remote stub
 */
public boolean allocateMedia(String terminal, int type, java.util.Dictionary params) {
	return this.getRemote().allocateMedia(terminal, type, this.toParamMap(params));
}
/**
 * Forward answerCall to remote provider
 */
public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
	  MethodNotSupportedException, RawStateException {
	try {
		this.getRemote().answerCall(call.hashCode(), address, terminal);
	} catch (MobileStateException_Exception mse) {
		MobileState ms = mse.getFaultInfo().getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException_Exception msee) {
		MobileJavaxException mse = msee.getFaultInfo();
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
		if (ex instanceof PrivilegeViolationException)
			throw (PrivilegeViolationException)ex;
		if (ex instanceof MethodNotSupportedException)
			throw (MethodNotSupportedException)ex;
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
	}
}
/**
 * attachMedia method comment.
 */
public boolean attachMedia(net.sourceforge.gjtapi.CallId call, java.lang.String address, boolean onFlag) {
	return this.getRemote().attachMedia(call.hashCode(), address, onFlag);
}
/**
 * beep method comment.
 */
public void beep(net.sourceforge.gjtapi.CallId call) {
	this.getRemote().beep(call.hashCode());
}
/**
 * Create a call from the given address and terminal to the remote address
 */
public CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException,
	  InvalidPartyException, InvalidArgumentException, RawStateException,
	  MethodNotSupportedException {
	try {
		return new SerializableCallId(this.getRemote().createCall(id.hashCode(), address, term, dest));
	} catch (MobileStateException_Exception mse) {
		MobileState ms = mse.getFaultInfo().getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException_Exception msee) {
		MobileJavaxException mse = msee.getFaultInfo();
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
		if (ex instanceof PrivilegeViolationException)
			throw (PrivilegeViolationException)ex;
		if (ex instanceof MethodNotSupportedException)
			throw (MethodNotSupportedException)ex;
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		if (ex instanceof InvalidPartyException)
			throw (InvalidPartyException)ex;
		if (ex instanceof InvalidArgumentException)
			throw (InvalidArgumentException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
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
		return this.getRemote().getAddresses().toArray(new String[0]);
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Return a set of address names from the remote provider.
 */
public String[] getAddresses(String term) throws InvalidArgumentException {
	try {
		return this.getRemote().getAddressesForTerminal(term).toArray(new String[0]);
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof InvalidArgumentException)
			throw (InvalidArgumentException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
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
public CallData getCall(CallId id) {
	MovableCallData mcd = this.getRemote().getCall(id.hashCode());
	return new net.sourceforge.gjtapi.CallData(id, mcd.getCallState(), this.dt.translateConnectionData(mcd.getConnections()));
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
	List<MovableCallData> mcds = this.getRemote().getCallsOnAddress(number);
	return this.toNormalCD(mcds);
}

private net.sourceforge.gjtapi.CallData[] toNormalCD(List<MovableCallData> mcds) {
	if (mcds == null)
		return null;
	int len = mcds.size();
	CallData[] cds = new CallData[len];
	int i = 0;
	for (MovableCallData mcd : mcds) {
		cds[i] = new net.sourceforge.gjtapi.CallData(new SerializableCallId(mcd.getId()), mcd.getCallState(), this.dt.translateConnectionData(mcd.getConnections()));
		i++;
	}
	return cds;
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
	List<MovableCallData> mcds = this.getRemote().getCallsOnTerminal(term);
	return this.toNormalCD(mcds);
}
/**
 * getCapabilities method comment.
 */
public java.util.Properties getCapabilities() {
	//try {
		java.util.Properties props = new java.util.Properties(); //this.getRemote().getCapabilities();

		// turn off privateData support, since we can't serialize PrivateData over JAX-RPC
		props.put(Capabilities.PROV + Capabilities.GET, "f");
		props.put(Capabilities.PROV + Capabilities.SEND, "f");
		props.put(Capabilities.PROV + Capabilities.SET, "f");

		props.put(Capabilities.CALL + Capabilities.GET, "f");
		props.put(Capabilities.CALL + Capabilities.SEND, "f");
		props.put(Capabilities.CALL + Capabilities.SET, "f");

		props.put(Capabilities.ADDR + Capabilities.GET, "f");
		props.put(Capabilities.ADDR + Capabilities.SEND, "f");
		props.put(Capabilities.ADDR + Capabilities.SET, "f");

		props.put(Capabilities.TERM + Capabilities.GET, "f");
		props.put(Capabilities.TERM + Capabilities.SEND, "f");
		props.put(Capabilities.TERM + Capabilities.SET, "f");

		props.put(Capabilities.CONN + Capabilities.GET, "f");
		props.put(Capabilities.CONN + Capabilities.SEND, "f");
		props.put(Capabilities.CONN + Capabilities.SET, "f");

		props.put(Capabilities.TERM_CONN + Capabilities.GET, "f");
		props.put(Capabilities.TERM_CONN + Capabilities.SEND, "f");
		props.put(Capabilities.TERM_CONN + Capabilities.SET, "f");

		return props;

	/*} catch (RemoteException re) {
		re.detail.printStackTrace();
		throw new PlatformException(re.getMessage());
	}*/
}
/**
 * getDialledDigits method comment.
 */
public String getDialledDigits(CallId id, java.lang.String address) {
	return this.getRemote().getDialledDigits(id.hashCode(), address);
}
/**
 * Call getPrivateData on remote interface.
 * Since we can't retrieve arbitraty data, we turned this capability off, so this should never be called.
 */
public Object getPrivateData(CallId call, String address, String terminal) {
	return null;
}
/**
 * Internal accessor for the remote stub handle.
 * Creation date: (2008-09-07 14:24:48)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.remote.RemoteProvider
 */
private GjtapiJaxWs getRemote() {
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
		return this.dt.translateTermData(this.getRemote().getTerminals());
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Return a set of terminal names from the remote provider.
 */
public net.sourceforge.gjtapi.TermData[] getTerminals(String address) throws InvalidArgumentException {
	try {
		return this.dt.translateTermData(this.getRemote().getTerminalsForAddress(address));
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof InvalidArgumentException)
			throw (InvalidArgumentException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Send a hold message for a terminal to a remote provider.
 */
public void hold(CallId call, String term, String address) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	try {
		this.getRemote().hold(call.hashCode(), term, address);
	} catch (MobileStateException_Exception mse) {
		MobileState ms = mse.getFaultInfo().getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException_Exception msee) {
		Exception ex = new MobileJavaxStateHelper().getException(msee.getFaultInfo().getState());
		if (ex instanceof PrivilegeViolationException)
			throw (PrivilegeViolationException)ex;
		if (ex instanceof MethodNotSupportedException)
			throw (MethodNotSupportedException)ex;
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Initialize my connection to the remote provider.
 * These properties could be used locally or sent to the server for the creation of a user-session.
 * For now, we don't support sending the properties.
 */
public void initialize(Map props) throws ProviderUnavailableException {
	// load any properties file
//	java.util.Properties provProps = getProviderProps();
//	try {
//		provProps.load(this.getClass().getResourceAsStream("/" + RESOURCE_NAME));
//	} catch (IOException ioe) {
//		// eat and hope that the initialize method sets my required properties
//	} catch (NullPointerException npe) {
//	}
//	provProps.putAll(props);

	// Try to find this server
	java.net.URL url;
	String urlString = (String)props.get(WebProvider.URL);
	if(urlString == null) {
		urlString = this.defaultUrl;
	}
	System.out.println("Looking up URL (" + WebProvider.URL + "): " + urlString);
	try {
		url = new java.net.URL(urlString);
	} catch (MalformedURLException e) {
		// can't proceed
		e.printStackTrace();
		throw new RuntimeException(e);
	}
	
	// get the qName
	String qNameUri = (String)props.get(WebProvider.QNAME_URI);
	String qNameLocalPart = (String)props.get(WebProvider.QNAME_LOCAL);
	if(qNameUri == null) {
		qNameUri = this.defaultQNameUri;
	}
	if(qNameLocalPart == null) {
		qNameLocalPart = this.defaultQNameLocal;
	}
	QName qName = new QName(qNameUri, qNameLocalPart);

	try {
		this.hookupInfo = "remoteHost:" + url;
		GjtapiJaxWsService service = new GjtapiJaxWsService(url, qName);
		GjtapiJaxWs port = service.getGjtapiJaxWsPort();
		this.setRemote(port);
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
		return new SerializableCallId(this.getRemote().join(call1.hashCode(),
				call2.hashCode(),
				address,
				terminal));
	} catch (MobileStateException_Exception mse) {
		MobileState ms = mse.getFaultInfo().getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof PrivilegeViolationException)
			throw (PrivilegeViolationException)ex;
		if (ex instanceof MethodNotSupportedException)
			throw (MethodNotSupportedException)ex;
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		if (ex instanceof InvalidArgumentException)
			throw (InvalidArgumentException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Put RTCs into serializable holder and send to remote stub
 */
public void play(String terminal,
	String[] streamIds,
	int offset,
	RTC[] rtcs,
	java.util.Dictionary optArgs) throws MediaResourceException {
	List<RtcPair> rtcPairs = new ArrayList<RtcPair>(rtcs.length);
	for (int i = 0; i < rtcs.length; i++) {
		RTC rtc = rtcs[i];
		RtcPair pair = new RtcPair();
		pair.setAction(rtc.getAction().hashCode());
		pair.setTrigger(rtc.getTrigger().hashCode());
		rtcPairs.add(pair);
	}
	try {
		this.getRemote().play(terminal, Arrays.asList(streamIds), offset, rtcPairs, this.toParamMap(optArgs));
	} catch (MobileResourceException_Exception mrex) {
		throw this.dt.morph(mrex.getFaultInfo());
	}
}
/**
 * Put RTCs into serializable holder and send to remote stub
 */
public void record(String terminal,
	String streamId,
	RTC[] rtcs,
	java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	List<RtcPair> rtcPairs = new ArrayList<RtcPair>(rtcs.length);
	for (int i = 0; i < rtcs.length; i++) {
		RTC rtc = rtcs[i];
		RtcPair pair = new RtcPair();
		pair.setAction(rtc.getAction().hashCode());
		pair.setTrigger(rtc.getTrigger().hashCode());
		rtcPairs.add(pair);
	}
	try {
		this.getRemote().record(terminal, streamId, rtcPairs, this.toParamMap(optArgs));
	} catch (MobileResourceException_Exception mrex) {
		throw this.dt.morph(mrex.getFaultInfo());
	}
}
/**
 * Tell the remote provider to release an address from a call.
 */
public void release(String address, CallId call) throws PrivilegeViolationException,
	ResourceUnavailableException, MethodNotSupportedException, RawStateException {
	try {
		this.getRemote().release(address, call.hashCode());
	} catch (MobileStateException_Exception mse) {
		MobileState ms = mse.getFaultInfo().getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof PrivilegeViolationException)
			throw (PrivilegeViolationException)ex;
		if (ex instanceof MethodNotSupportedException)
			throw (MethodNotSupportedException)ex;
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Release any CallId's that I have reserved.
 */
public void releaseCallId(CallId id) {
		this.getRemote().releaseCallId(id.hashCode());
}
/**
 * Forward removeListener to remote provider.
 */
public void removeListener(TelephonyListener rl) {
	// tell the poller to stop
	this.poller.stopPolling();

	// now toss the poller away
	this.poller = null;
}
/**
 * Forward to remote stub
 */
public void reportCallsOnAddress(String address, boolean flag) throws InvalidArgumentException, ResourceUnavailableException {
	try {
		this.getRemote().reportCallsOnAddress(address, flag);
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		if (ex instanceof InvalidArgumentException)
			throw (InvalidArgumentException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Forward to remote stub
 */
public void reportCallsOnTerminal(String terminal, boolean flag) throws InvalidArgumentException, ResourceUnavailableException {
	try {
		this.getRemote().reportCallsOnTerminal(terminal, flag);
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		if (ex instanceof InvalidArgumentException)
			throw (InvalidArgumentException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Reserve a call id on the remote server.
 */
public CallId reserveCallId(String address) throws InvalidArgumentException {
	try {
		return new SerializableCallId(this.getRemote().reserveCallId(address));
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof InvalidArgumentException)
			throw (InvalidArgumentException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Put RTCs into serializable holder and send to remote stub
 */
public RawSigDetectEvent retrieveSignals(String terminal,
	int num,
	Symbol[] patterns,
	RTC[] rtcs,
	java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	List<RtcPair> rtcPairs = new ArrayList<RtcPair>(rtcs.length);
	for (int i = 0; i < rtcs.length; i++) {
		RTC rtc = rtcs[i];
		RtcPair pair = new RtcPair();
		pair.setAction(rtc.getAction().hashCode());
		pair.setTrigger(rtc.getTrigger().hashCode());
		rtcPairs.add(pair);
	}
	List<Integer> patHolders = new ArrayList<Integer>(patterns.length);
	for (int i = 0; i < patterns.length; i++) {
		patHolders.add(patterns[i].hashCode());
	}
	try {
		return this.dt.toRawSigDetectEvent(this.getRemote().retrieveSignals(terminal, num, patHolders, rtcPairs, this.toParamMap(optArgs)));
	} catch (MobileResourceException_Exception mrex) {
		throw this.dt.morph(mrex.getFaultInfo());
	}
}
/**
 * Trigger sendPrivateData against the remote interface.
 * Since we can't send arbitraty data, we turned this capability off, so this should never be called.
 */
public Object sendPrivateData(CallId call, String address, String terminal, Object data) {
	return null;
}
/**
 * Put RTCs and Symbols into serializable holder and send to remote stub
 */
public void sendSignals(String terminal,
	Symbol[] syms,
	RTC[] rtcs,
	java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	List<RtcPair> rtcPairs = new ArrayList<RtcPair>(rtcs.length);
	for (int i = 0; i < rtcs.length; i++) {
		RTC rtc = rtcs[i];
		RtcPair pair = new RtcPair();
		pair.setAction(rtc.getAction().hashCode());
		pair.setTrigger (rtc.getTrigger().hashCode());
		rtcPairs.add(pair);
	}
	List<Integer> symHolders = new ArrayList<Integer>(syms.length);
	for (int i = 0; i < syms.length; i++) {
		symHolders.add(syms[i].hashCode());
	}
	try {
		this.getRemote().sendSignals(terminal, symHolders, rtcPairs, this.toParamMap(optArgs));
	} catch (MobileResourceException_Exception mrex) {
		throw this.dt.morph(mrex.getFaultInfo());
	}
}
/**
 * setLoadControl method comment.
 */
public void setLoadControl(java.lang.String startAddr, java.lang.String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws javax.telephony.MethodNotSupportedException {
	List<Integer> treatmentList = new ArrayList<Integer>(treatment.length);
	for (int i = 0; i < treatment.length; i++) {
		treatmentList.add(new Integer(treatment[i]));
	}
	try {
		this.getRemote().setLoadControl(startAddr, endAddr, duration, admissionRate, interval, treatmentList);
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof MethodNotSupportedException)
			throw (MethodNotSupportedException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Send setPrivateData through remote proxy.
 * Since we can't send arbitraty data, we turned this capability off, so this should never be called.
 */
public void setPrivateData(CallId call, String address, String terminal, Object data) {
}
/**
 * Insert the method's description here.
 * Creation date: (2008-09-07 14:24:48)
 * @author: Richard Deadman
 * @param newRemote 
 */
private void setRemote(GjtapiJaxWs newRemote) {
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
		return this.getRemote().stopReportingCall(call.hashCode());
}
/**
 * Replace each Symbol key or value with an Integer so that it can be send across a wire.
 * Creation date: (2000-03-13 9:34:39)
 * @author: Richard Deadman
 * @return A Hashmap of movable symbol data
 * @param dict A dictionary of parameters and values that control the usage of the resource.  This should move to a Map later.
 */
private ArrayList<KeyValue> toParamMap(java.util.Dictionary dict) {
	if (dict == null)
		return null;
	ArrayList<KeyValue> map = new ArrayList<KeyValue>(dict.size());
	Enumeration keys = dict.keys();
	while (keys.hasMoreElements()) {
		Object k = keys.nextElement();
		Object v = dict.get(k);
		if (k instanceof Symbol)
			k = String.valueOf(((Symbol)k).hashCode());
		if (v instanceof Symbol)
			v = String.valueOf(((Symbol)v).hashCode());
		KeyValue kv = new KeyValue();
		kv.setKey(k.toString());
		kv.setValue(v.toString());
		map.add(kv);
	}
	return map;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Client proxy for a remote provider (" + hookupInfo + ")";
}
/**
 * Trigger a media runtime control (RTC) action on a remote terminal.
 */
public void triggerRTC(String terminal, javax.telephony.media.Symbol action) {
	this.getRemote().triggerRTC(terminal, action.hashCode());
}
/**
 * Tell the remote provider to unhold a terminal from a call
 */
public void unHold(CallId call, String address, String term) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException {
	try {
		this.getRemote().unHold(call.hashCode(), address, term);
	} catch (MobileStateException_Exception mse) {
		MobileState ms = mse.getFaultInfo().getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException_Exception mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getFaultInfo().getState());
		if (ex instanceof PrivilegeViolationException)
			throw (PrivilegeViolationException)ex;
		if (ex instanceof MethodNotSupportedException)
			throw (MethodNotSupportedException)ex;
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}

/**
 * Get the Properties set that defines how I should be initialized
 * @return Properties
 */
private java.util.Properties getProviderProps() {
	return providerProps;
}
}
