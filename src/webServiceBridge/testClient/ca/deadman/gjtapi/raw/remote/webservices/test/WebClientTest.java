package ca.deadman.gjtapi.raw.remote.webservices.test;

/*
	Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca)

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

import javax.telephony.media.*;
import javax.xml.namespace.QName;
import javax.xml.rpc.Service;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.Stub;


import net.sourceforge.gjtapi.capabilities.Capabilities;
import net.sourceforge.gjtapi.raw.emulator.EmProvider;
import net.sourceforge.gjtapi.raw.remote.SerializableCallId;

import javax.telephony.*;

import ca.deadman.gjtapi.raw.remote.webservices.DataTranslator;
import ca.deadman.gjtapi.raw.remote.webservices.EventHolder;
//import ca.deadman.gjtapi.raw.remote.webservices.GJtapiWebServiceAdapter;
import ca.deadman.gjtapi.raw.remote.webservices.GJtapiWebServiceIF;
import ca.deadman.gjtapi.raw.remote.webservices.MobileJavaxException;
import ca.deadman.gjtapi.raw.remote.webservices.MobileJavaxStateHelper;
import ca.deadman.gjtapi.raw.remote.webservices.MobileResourceException;
import ca.deadman.gjtapi.raw.remote.webservices.MobileState;
import ca.deadman.gjtapi.raw.remote.webservices.MobileStateException;
import ca.deadman.gjtapi.raw.remote.webservices.MovableCallData;
import ca.deadman.gjtapi.raw.remote.webservices.GjtapiService_Impl;
import ca.deadman.gjtapi.raw.remote.webservices.RTCPair;

import java.net.URL;
import java.rmi.RemoteException;
import net.sourceforge.gjtapi.*;
import java.util.*;
/**
 * Test framework for the Web service
 *
 * This is a pluggable provider that provides access to a remote provider through a JAX-RPC
 * web-services pipe
 * Creation date: (2000-02-10 10:53:56)
 * @author: Richard Deadman
 */
public class WebClientTest {

	/*
	 * The name of the key in the initialization properties from which to find the
	 * Remote RPC URL to hook up to the Stubs.
	 */
	public final static String REMOTE_SERVER = "server";
	public final static String LOOPBACK = "loopback";

	// keys for the other web service lookup parts
	public final static String WSDL_NAME = "wsdlName";
	public final static String SERVICE_NAME = "serviceName";
	public final static String PORT_NAME = "portName";

	// The location of the properties file to set my values
	private final static String RESOURCE_NAME = "WebProvider.props";

	/*
	 * The stub that I send remote messages to.
	 */
	private GJtapiWebServiceIF remote;
	/*
	 * A polling object that gets remote events and sends them back to my TelephonyListener.
	 */
	private EventPoller poller = null;

	/**
	 * The set of properties that define my behaviour
	 */
	private Properties providerProps = new Properties();

	private DataTranslator dt = new DataTranslator();


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
		private GJtapiWebServiceIF service = null;
		// Remote event queue id
		private int queueId;

		// Flag to tell me to stop polling
		private boolean poll = true;

		/**
		 * Create the Event Poller with information about the GJTAPI listener and the remote service.
		 * @param tl
		 * @param remote
		 */
		EventPoller(TelephonyListener tl, GJtapiWebServiceIF remote) throws RemoteException {
			this.listener = tl;
			this.service = remote;

			// now ask for a Queue id
//			this.queueId = remote.registerQueue();

		}

		/**
		 * Start polling for events until I am told to stop.
		 * @see java.lang.Runnable#run()
		 */
		public void run() {
			EventHolder[] evs = null;	// event set
			int failureCount = 0;	// number or remote failures before I give up.
			int msBackoff = 50;	// how long should I wait after a failure
			RemoteException lastRe = null;

			while (poll && (failureCount < 10)) {
				// Get the events
//				try {
//					evs = service.pollEvents(this.queueId);
					failureCount = 0;
					lastRe = null;
/*				} catch (RemoteException re) {
					// incremente failure count
					failureCount++;

					// note the last exception
					lastRe = re;

					// wait
					try {
						Thread.sleep(msBackoff * failureCount);
					} catch (InterruptedException ie) {
						// keep going
					}

					// loop around again
					evs = null;
				}
*/
				// Send each event to the GJTAPI listener
				if (evs != null) {
					int evLength = evs.length;
					for (int i = 0; i < evLength; i++) {
						EventHolder eh = evs[i];
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
//			try {
//				this.service.removeQueue(queueId);
//			} catch (RemoteException re) {
				// we tried our best
//			}
		}

		/**
		 * Stop the poller
		 */
		void stopPolling() {
			this.poll = false;
		}
	}

	/**
	 * Test the web service
	 * @param args
	 */
	public static void main(String[] args) {
		WebClientTest tester = new WebClientTest();
		PrintStream sout = System.out;

		// hook up
		sout.println("Hooking up to service...");
		Map props = new HashMap();
		props.put(WebClientTest.REMOTE_SERVER, "localhost:8080");
		tester.initialize(props);

		// now getting the set of Addresses
		try {
			String[] addrs = tester.getAddresses();
			sout.println("Set of Addresses: " + addrs.length);
		} catch (ResourceUnavailableException rue) {
			sout.println("Exception during getAddresses(): " + rue.getLocalizedMessage());
		}

		// now getting the Addresses for a Terminal
		try {
			sout.println("Addresses for terminal 21: " + tester.getAddresses("21"));
		} catch (InvalidArgumentException iae) {
			sout.println("Exception during getAddresses(String): " + iae.getLocalizedMessage());
		}

		// now get the Address type
		sout.println("Address type for address 21: " + tester.getAddressType("21"));

		// beep the terminal
		sout.println("Beeping...");
		try {
			tester.beep(null);
		} catch (NullPointerException npe) {
			sout.println("Properly caught NPE");
		}

		// register my event queue
		int queueId = -1;
		try {
			queueId = tester.remote.registerQueue();
			sout.println("Registering queue: " + queueId);
		} catch (RemoteException re) {
			sout.println("Exception during getAddressType(): " + re.getLocalizedMessage());
		}

		// now poll for events
		if (queueId > -1) {
			try {
				EventHolder[] ehs = tester.remote.pollEvents(queueId);
				sout.println("Polling results: " + ehs);
			} catch (Exception ex) {
				ex.printStackTrace(sout);
			}
		}

		// get my capabilities
		sout.println("Capabilities: " + tester.getCapabilities());

		// get the TermData
		sout.println("Fetching terminal data...");
		try {
			TermData[] terms = tester.getTerminals();
			if (terms.length > 0) {
				sout.println("Terminal 0: " + terms[0]);
			}
		} catch (Exception ex) {
			ex.printStackTrace(sout);
		}

		// test the shutdown method
		sout.println("Testing shutdown");
		tester.shutdown();

		// end
		sout.println("All's well that ends well");
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
public boolean allocateMedia(String terminal, int type, Dictionary params) {
/*	try {
		return this.getRemote().allocateMedia(terminal, type, this.toParamHashMap(params));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
*/return false;}
/**
 * Forward answerCall to remote provider
 */
public void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
	  MethodNotSupportedException, RawStateException {
/*	try {
		this.getRemote().answerCall(call.hashCode(), address, terminal);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileStateException mse) {
		MobileState ms = mse.getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException mse) {
		Exception ex = mse.getState().getException();
		if (ex instanceof PrivilegeViolationException)
			throw (PrivilegeViolationException)ex;
		if (ex instanceof MethodNotSupportedException)
			throw (MethodNotSupportedException)ex;
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
	}
*/}
/**
 * attachMedia method comment.
 */
public boolean attachMedia(net.sourceforge.gjtapi.CallId call, java.lang.String address, boolean onFlag) {
/*	try {
		return this.getRemote().attachMedia(call.hashCode(), address, onFlag);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
*/return false;}
/**
 * beep method comment.
 */
public void beep(net.sourceforge.gjtapi.CallId call) {
	try {
		this.getRemote().beep(call.hashCode());
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
/*	try {
		return new SerializableCallId(this.getRemote().createCall(id.hashCode(), address, term, dest));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileStateException mse) {
		MobileState ms = mse.getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException mse) {
		Exception ex = mse.getState().getException();
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
*/return null;}
/**
 * Delegate to remote stub
 */
public boolean freeMedia(java.lang.String terminal, int type) {
/*	try {
		return this.getRemote().freeMedia(terminal, type);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
*/return false;}
/**
 * Get a set or addresses for a remote provider.
 */
public java.lang.String[] getAddresses() throws ResourceUnavailableException {
	try {
		return this.getRemote().getAddresses();
	} catch (RemoteException re) {
re.detail.printStackTrace();
		throw new PlatformException(re.getMessage());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
		return this.getRemote().getAddressesForTerminal(term);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
public net.sourceforge.gjtapi.CallData getCall(CallId id) {
	try {
		MovableCallData mcd = this.getRemote().getCall(id.hashCode());
		return new net.sourceforge.gjtapi.CallData(id, mcd.getCallState(), this.dt.translateConnectionData(mcd.getConnections()));
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
		MovableCallData[] mcds = this.getRemote().getCallsOnAddress(number);
		return this.toNormalCD(mcds);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}

private CallData[] toNormalCD(MovableCallData[] mcds) {
	if (mcds == null)
		return null;
	int len = mcds.length;
	CallData[] cds = new CallData[len];
	for (int i = 0; i < len; i++) {
		MovableCallData mcd = (MovableCallData)mcds[i];
		cds[i] = new CallData(new SerializableCallId(mcd.getId()), mcd.getCallState(), this.dt.translateConnectionData(mcd.getConnections()));
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
public CallData[] getCallsOnTerminal(String term) {
	try {
		MovableCallData[] mcds = this.getRemote().getCallsOnTerminal(term);
		return this.toNormalCD(mcds);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * getCapabilities method comment.
 */
public java.util.Properties getCapabilities() {
	try {
		Properties props = this.getRemote().getCapabilities();

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

	} catch (RemoteException re) {
		re.detail.printStackTrace();
		throw new PlatformException(re.getMessage());
	}
}
/**
 * getDialledDigits method comment.
 */
public String getDialledDigits(CallId id, java.lang.String address) {
	try {
		return this.getRemote().getDialledDigits(id.hashCode(), address);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Call getPrivateData on remote interface.
 * Since we can't retrieve arbitraty data, we turned this capability off, so this should never be called.
 */
public Object getPrivateData(CallId call, String address, String terminal) {
	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-17 14:24:48)
 * @author:
 * @return net.sourceforge.gjtapi.raw.remote.RemoteProvider
 */
private GJtapiWebServiceIF getRemote() {
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
		return this.dt.translateTermData(this.getRemote().getTerminals());
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
		if (ex instanceof ResourceUnavailableException)
			throw (ResourceUnavailableException)ex;
		// something's wrong
		throw new RuntimeException("Wrong remote type found", ex);
	}
}
/**
 * Return a set of terminal names from the remote provider.
 */
public TermData[] getTerminals(String address) throws InvalidArgumentException {
	try {
		return this.dt.translateTermData(this.getRemote().getTerminalsForAddress(address));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileStateException mse) {
		MobileState ms = mse.getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
	Properties provProps = getProviderProps();
	try {
		provProps.load(this.getClass().getResourceAsStream("/" + RESOURCE_NAME));
	} catch (IOException ioe) {
		// eat and hope that the initialize method sets my required properties
	} catch (NullPointerException npe) {
	}
	provProps.putAll(props);

	// Try to find this server
	String remoteHost = (String)provProps.get(WebClientTest.REMOTE_SERVER);
	String loopback = (String)provProps.get(WebClientTest.LOOPBACK);

	try {
		if (remoteHost != null) {
            this.setRemote(this.createProxy(remoteHost));
		} else if (loopback != null) {
			// internal test implementation
			EmProvider prov = new EmProvider();
			prov.initialize(null);
			//this.setRemote(new GJtapiWebServiceAdapter(ProviderFactory.createProvider(prov)));
		} else {
			// use hard-coded value
			Stub stub = (Stub) (new GjtapiService_Impl().getGJtapiWebServiceIFPort());
			stub._setProperty
			  (javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY,
			   "http://localhost:8080/Gjtapi-Ws/gjtapi-ws");
			this.setRemote((GJtapiWebServiceIF)stub);
		}
	} catch (Exception ex) {
		throw new ProviderUnavailableException(ex.getMessage());
	}
}

/**
 * Create the implementation-specific proxy * @return Stub */
    private GJtapiWebServiceIF createProxy(String remoteHost ) {
    	Properties props = this.getProviderProps();

		String UrlString = "http://" + remoteHost + props.getProperty(WSDL_NAME, "/gjtapi/ws/Gjtapi?WSDL");
		String nameSpaceUri = "urn:Foo";
		String serviceName = props.getProperty(SERVICE_NAME, "GjtapiService");
		String portName = props.getProperty(PORT_NAME, "GJtapiWebServiceIFPort");

		try {
			URL helloWsdlUrl = new URL(UrlString);

			ServiceFactory serviceFactory = ServiceFactory.newInstance();

			Service gjtapiService = serviceFactory.createService(helloWsdlUrl,
													new QName(nameSpaceUri, serviceName));

			GJtapiWebServiceIF ws = (GJtapiWebServiceIF) gjtapiService.getPort(
                       new QName(nameSpaceUri, portName),
                       GJtapiWebServiceIF.class);
   System.out.println("Returning service: " + ws);
            return ws;
		} catch (Exception ex) {
			ex.printStackTrace();
			return null;
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
		return new SerializableCallId(this.getRemote().join(call1.hashCode(),
				call2.hashCode(),
				address,
				terminal));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileStateException mse) {
		MobileState ms = mse.getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
	Dictionary optArgs) throws MediaResourceException {
	RTCPair[] rtcPairs = new RTCPair[rtcs.length];
	for (int i = 0; i < rtcs.length; i++) {
		RTC rtc = rtcs[i];
		RTCPair pair = rtcPairs[i] = new RTCPair();
		pair.setAction(rtc.getAction().hashCode());
		pair.setTrigger(rtc.getTrigger().hashCode());
	}
	try {
		this.getRemote().play(terminal, streamIds, offset, rtcPairs, this.toParamHashMap(optArgs));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileResourceException mrex) {
		throw this.dt.morph(mrex);
	}
}
/**
 * Put RTCs into serializable holder and send to remote stub
 */
public void record(String terminal,
	String streamId,
	RTC[] rtcs,
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTCPair[] rtcPairs = new RTCPair[rtcs.length];
	for (int i = 0; i < rtcs.length; i++) {
		RTC rtc = rtcs[i];
		RTCPair pair = rtcPairs[i] = new RTCPair();
		pair.setAction(rtc.getAction().hashCode());
		pair.setTrigger(rtc.getTrigger().hashCode());
	}
	try {
		this.getRemote().record(terminal, streamId, rtcPairs, this.toParamHashMap(optArgs));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileResourceException mrex) {
		throw this.dt.morph(mrex);
	}
}
/**
 * Tell the remote provider to release an address from a call.
 */
public void release(String address, CallId call) throws PrivilegeViolationException,
	ResourceUnavailableException, MethodNotSupportedException, RawStateException {
	try {
		this.getRemote().release(address, call.hashCode());
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileStateException mse) {
		MobileState ms = mse.getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
	try {
		this.getRemote().releaseCallId(id.hashCode());
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
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
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTCPair[] rtcPairs = new RTCPair[rtcs.length];
	for (int i = 0; i < rtcs.length; i++) {
		RTC rtc = rtcs[i];
		RTCPair pair = rtcPairs[i] = new RTCPair();
		pair.setAction(rtc.getAction().hashCode());
		pair.setTrigger(rtc.getTrigger().hashCode());
	}
	int[] patHolders = new int[patterns.length];
	for (int i = 0; i < patterns.length; i++) {
		patHolders[i] = patterns[i].hashCode();
	}
	try {
		return this.dt.toRawSigDetectEvent(this.getRemote().retrieveSignals(terminal, num, patHolders, rtcPairs, this.toParamHashMap(optArgs)));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileResourceException mrex) {
		throw this.dt.morph(mrex);
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
	Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	RTCPair[] rtcPairs = new RTCPair[rtcs.length];
	for (int i = 0; i < rtcs.length; i++) {
		RTC rtc = rtcs[i];
		RTCPair pair = rtcPairs[i] = new RTCPair();
		pair.setAction(rtc.getAction().hashCode());
		pair.setTrigger(rtc.getTrigger().hashCode());
	}
	int[] symHolders = new int[syms.length];
	for (int i = 0; i < syms.length; i++) {
		symHolders[i] = syms[i].hashCode();
	}
	try {
		this.getRemote().sendSignals(terminal, symHolders, rtcPairs, this.toParamHashMap(optArgs));
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileResourceException mrex) {
		throw this.dt.morph(mrex);
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
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
 * Set the remote stub.
 * Creation date: (2000-02-17 14:24:48)
 * @author:
 * @param newRemote net.sourceforge.gjtapi.raw.remote.RemoteProvider
 */
private void setRemote(GJtapiWebServiceIF newRemote) {
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
		return this.getRemote().stopReportingCall(call.hashCode());
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	}
}
/**
 * Replace each Symbol key or value with an Integer so that it can be send across a wire.
 * Creation date: (2000-03-13 9:34:39)
 * @author: Richard Deadman
 * @return A Hashmap of movable symbol data
 * @param dict A dictionary of parameters and values that control the usage of the resource.  This should move to a Map later.
 */
private HashMap toParamHashMap(Dictionary dict) {
	if (dict == null)
		return null;
	HashMap map = new HashMap();
	Enumeration keys = dict.keys();
	while (keys.hasMoreElements()) {
		Object k = keys.nextElement();
		Object v = dict.get(k);
		if (k instanceof Symbol)
			k = new Integer(((Symbol)k).hashCode());
		if (v instanceof Symbol)
			v = new Integer(((Symbol)v).hashCode());
		map.put(k, v);
	}
	return map;
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
		this.getRemote().triggerRTC(terminal, action.hashCode());
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
		this.getRemote().unHold(call.hashCode(), address, term);
	} catch (RemoteException re) {
		throw new PlatformException(re.getMessage());
	} catch (MobileStateException mse) {
		MobileState ms = mse.getMobileState();
		throw new RawStateException(new SerializableCallId(ms.getCall()), ms.getAddress(), ms.getTerminal(), ms.getType(), ms.getState(), ms.getInfo());
	} catch (MobileJavaxException mse) {
		Exception ex = new MobileJavaxStateHelper().getException(mse.getState());
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
private Properties getProviderProps() {
	return providerProps;
}
}
