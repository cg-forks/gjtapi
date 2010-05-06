package net.sourceforge.gjtapi.raw.emulator;

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
import javax.telephony.media.*;
import net.sourceforge.gjtapi.media.*;
import net.sourceforge.gjtapi.raw.FullJtapiTpi;
import java.io.*;
import java.util.*;
import net.sourceforge.gjtapi.*;
import javax.telephony.*;
/**
 * This is a simple JTAPI emulation stub
 * Creation date: (2000-02-04 15:04:19)
 * @author: Richard Deadman
 */
public class EmProvider implements FullJtapiTpi {
	// property to note if I should replace properties loaded from resource file during initialization
	public final static String REPLACE = "replace";
	// property to note that I shoould operate headless
	public final static String DISPLAY = "display";
	
	private final static String RESOURCE_NAME = "Emulator.props";
	private final static String ADDRESS_PREFIX = "Address";
	private static Set<EmProvider> universe = new HashSet<EmProvider>();	// globally accessible set of emulators
	private TestManager mgr;
	private Properties provProps;
/**
 * Raw constructor used by the GenericJtapiPeer factory
 * Creation date: (2000-02-10 10:28:55)
 * @author: Richard Deadman
 */
public EmProvider() {
	super();

	// read provider details and load the resources, if available
	this.setProvProps(this.loadResources("/" + EmProvider.RESOURCE_NAME));
}
/**
 * Set the event listener.
 */
public void addListener(TelephonyListener rl) {
	this.getMgr().setListener(rl);
}
/**
 * We don't have any real resources to manage, but we do have to see if we send detected DTMF
 */
@SuppressWarnings("unchecked")
public boolean allocateMedia(java.lang.String terminal, int type, Dictionary resourceArgs) {
	if (resourceArgs == null)
		return true;	// we don't have any events to send
		
	boolean generate = false;
	// a bug in JTAPI 1.3.1 means that ESymbol cannot be instantiated since its parent interface is not in the jar file.
	try {
		// get the array of events symbols that indicate what needs to be sent
		Symbol[] evsToSend = (Symbol[])resourceArgs.get(SignalDetectorConstants.p_EnabledEvents);
		if (evsToSend == null)
			return true;
			
		// now see if the array contains the RetrieveSignals symbol
		Symbol rs = SignalDetectorConstants.ev_RetrieveSignals;
		for (int i = 0; i < evsToSend.length; i++)
		    if (rs.equals(evsToSend[i]))
		    	generate = true;
	} catch (NoClassDefFoundError cnfe) {
		// assume we should generate signals
		generate = true;
	}
	this.getPhone(terminal).sendDetectedDtmf(generate);
	return true;
}
/**
 * Tell a call to be answered on a terminal.
 * Here we check if the call is Ringing at the terminal
 */
public void answerCall(CallId call, String address, String terminal) throws RawStateException, ResourceUnavailableException {
	RawPhone phone = this.getPhone(terminal);
	if (phone != null) {
		if (!phone.answer(call))
			throw new RawStateException(call, terminal, terminal,
				javax.telephony.InvalidStateException.TERMINAL_CONNECTION_OBJECT,
				javax.telephony.TerminalConnection.UNKNOWN);
	} else
		throw new ResourceUnavailableException(ResourceUnavailableException.ORIGINATOR_UNAVAILABLE);
}
/**
 * Create a call from the given address and terminal to the remote destination address.
 */
public CallId createCall(CallId id, String address, String term, String dest) throws InvalidPartyException, RawStateException {
	RawPhone phone = this.getPhone(address);
		// Add the new call
	int phState = phone.getState();
	if (phState == RawPhone.IDLE || phState == RawPhone.HOLD) {
		new Leg((RawCall)id, phone, this.getMgr().getListener(), Leg.IDLE);
	} else {
		throw new RawStateException(id, null, null,
				javax.telephony.InvalidStateException.CALL_OBJECT,
				javax.telephony.TerminalConnection.UNKNOWN);
	}
	((TestPhone)phone).getModel().dial(dest);
	return id;
}
/**
 * Find the provider that supports the given address
 * Creation date: (2000-10-04 15:31:06)
 * @return net.sourceforge.gjtapi.raw.emulator.EmProvider
 * @param address java.lang.String
 */
static EmProvider findProvider(String address) {
		// iterating over universe caused a StackOverflowError -- VisualAge class library bug
	try {
	int provSize = universe.size();
	EmProvider[] provs = universe.toArray(new EmProvider[provSize]);

	for (int i = 0; i < provSize; i++) {
		try {
			provs[i].getTerminals(address);
			return provs[i];
		} catch (InvalidArgumentException iae) {
			// try next one
		}
	}
	} catch (StackOverflowError soe) {
		soe.printStackTrace(System.err);
	}
	return null;
}
/**
 * Null resource freer.
 */
public boolean freeMedia(java.lang.String terminal, int type) {
	return true;
}
/**
 * Return all the addresses managed by the provider
 */
public String[] getAddresses() {
	return this.getMgr().getPhones().keySet().toArray(new String[0]);
}
/**
 * Return the Address names associated with the Terminal name.
 */
public String[] getAddresses(String terminal) throws InvalidArgumentException {
	if (this.getMgr().getPhone(terminal) == null)
		throw new InvalidArgumentException("Unknown address/phone: " + terminal);
	String[] addresses = {terminal};
	return addresses;
}
/**
 * Return a data snapshot of the call referenced by the call id.
 */
public CallData getCall(CallId id) {
	if (id instanceof RawCall && id != null) {
		return ((RawCall)id).getCallData();
	}
	return null;
}
/**
 * Return an array of snapshot information for all calls associated with an address.
 */
public CallData[] getCallsOnAddress(String number) {
	RawPhone rp = this.getPhone(number);
	if (rp != null) {
		RawCall[] calls = rp.getCalls();
		int size = calls.length;
		CallData[] cd = new CallData[size];
		for (int i = 0; i < size; i++) {
			cd[i] = calls[i].getCallData();
		}
	}
	return null;
}
/**
 * Return an array of snapshot information for all calls associated with an terminal.
 */
public CallData[] getCallsOnTerminal(String name) {
	return this.getCallsOnAddress(name);
}
/**
 * We support all the Generic Capabilities, with the exception of throttling and terminal and terminalConnection PrivateData sending.
 * Otherwise we could return null.
 */
public Properties getCapabilities() {
	return this.getProvProps();
}
/**
 * Gets the Phone Manager for the system.
 * Creation date: (2000-02-10 10:07:53)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.raw.emulator.TestManager
 */
TestManager getMgr() {
	return mgr;
}
/**
 * Return the phone associated with an address, or null
 * Creation date: (2000-02-10 12:23:49)
 * @author: Richard Deadman
 * @return A RawPhone terminal representation
 * @param address An address for a phone (assuming 1:1 corespondence)
 */
RawPhone getPhone(String address) {
	return this.getMgr().getPhone(address);
}
/**
 * getPrivateData method comment.
 */
public java.lang.Object getPrivateData(net.sourceforge.gjtapi.CallId call, java.lang.String address, java.lang.String terminal) {
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
 * Get a list of all the terminal names I manage.
 */
public TermData[] getTerminals() {
	String[] addresses = this.getAddresses();

	int size = addresses.length;
	TermData[] td = new TermData[size];
	for (int i = 0; i < size; i++) {
		td[i] = new TermData(addresses[i], true);
	}

	return td;
}
/**
 * Get all the terminal names associated with the address
 */
public TermData[] getTerminals(String address) throws InvalidArgumentException {
	if (this.getMgr().getPhone(address) == null)
		throw new InvalidArgumentException("Unknown address/phone: " + address);
	TermData[] terms = {new TermData(address, true)};
	return terms;
}
/**
 * Hold the given terminal.  Here we map address to terminals 1:1
 */
public void hold(CallId call, String address, String term) throws RawStateException {
	try {
		this.getPhone(term).hold();
	} catch (NullPointerException npe) {
		// No phone matched
		throw new RawStateException(null, term, term,
				javax.telephony.InvalidStateException.TERMINAL_CONNECTION_OBJECT,
				javax.telephony.TerminalConnection.UNKNOWN);
	}
}
/**
 * Provide application specific initialization
 */
@SuppressWarnings("unchecked")
public void initialize(Map props) throws ProviderUnavailableException {
	Map m = null;
	Object value = null;
	
	// determine if we need to totally replace the current properties
	boolean replace = false;
	if (props != null) {
		value = props.get(REPLACE);
		replace = net.sourceforge.gjtapi.capabilities.Capabilities.resolve(value);
	}
	if (replace)
		m = props;
	else {
		m = this.getProvProps();
		if (props != null)
			m.putAll(props);
	}

	// now look for all addresses
	Vector<String> adds = new Vector<String>();
	Iterator it = m.keySet().iterator();
	while (it.hasNext()) {
		String key = (String)it.next();
		if (key.startsWith(EmProvider.ADDRESS_PREFIX)) {
			adds.add((String)m.get(key));
		}
	}

	// Now create an instance of the manager
	this.setMgr(new TestManager(adds.toArray(new String[adds.size()])));

	// determine if a view is required
	value = m.get(DISPLAY);
	if (value instanceof String) {
		String display = (String)value;
		if (display != null && display.length() > 0 && Character.toLowerCase(display.charAt(0)) == 't')
			this.getMgr().show();
	}

	// add myself to the global set of provider
	EmProvider.universe.add(this);

}
/**
 * All my terminals handle media.
 */
public boolean isMediaTerminal(java.lang.String terminal) {
	return true;
}
/**
 * Join two calls
 */
public CallId join(CallId call1, CallId call2, String address, String terminal) throws MethodNotSupportedException {
	RawCall from = (RawCall)call1;
	RawCall to = (RawCall)call2;

	from.join(to);

	return from;
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
		props.load(this.getClass().getResourceAsStream(resName));
	} catch (IOException ioe) {
		// eat and hope that the initialize method sets my required properties
	}

	// delay initialization until initialize() called -- allow property replacement

	// return
	return props;
}
/**
 * Since I don't have any real playing resources, I just pause and then report success.
 */
@SuppressWarnings("unchecked")
public void play(String terminal, String[] streamIds, int offset, javax.telephony.media.RTC[] rtcs, Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	TestPhone phone = (TestPhone)this.getPhone(terminal);
	PhoneModel pm = phone.getModel();
	try {
		// ask the terminal to pretend to play the file
		StringBuffer sb = new StringBuffer("Playing files:");
		for (int i = 0; i < streamIds.length; i++)
			sb.append(" ").append(streamIds[i]);
		phone.setStatus(sb.toString());
		pm.setPlayThread(Thread.currentThread());
		java.applet.AudioClip ac = null;
		for (int i = 0; i < streamIds.length; i++) {
			String media = streamIds[i];
			try {
				ac = java.applet.Applet.newAudioClip(new java.net.URL(media));
				ac.play();
			} catch (java.net.MalformedURLException mfe) {
				System.out.println("Bad URL: " + media);
				Thread.sleep(2000);
			}
		}
	} catch (InterruptedException ie) {
		// continue then
	}
	// clean up
	pm.setPlayThread(null);
}
/**
 * I can't really listen, so I just return some stuff
 * If an Http URL is give, I try putting to the url.
 *
 * Updates to the HTTP and URLConnection output code from Mario Dorion, May 10, 2000
 */
@SuppressWarnings("unchecked")
public void record(String terminal, String streamId, RTC[] rtcs, Dictionary optArgs)
	throws MediaResourceException {
		// pause for two seconds
	PhoneModel pm = ((TestPhone)this.getPhone(terminal)).getModel();
	pm.setRecordThread(Thread.currentThread());
	try {
		Thread.sleep(2000);
	} catch (InterruptedException ie) {
		// continue then
	}
	pm.setRecordThread(null);

	// try to construct a URL from the streamId
	try {
		java.net.URL url = new java.net.URL(streamId);
		java.io.OutputStream os = null;
		if (url.getProtocol().equals("file")) {
			os = new FileOutputStream(url.getFile());
		} else {
			java.net.URLConnection cnx = url.openConnection();
			cnx.setDoOutput(true);
			if (cnx instanceof java.net.HttpURLConnection)
				((java.net.HttpURLConnection)cnx).setRequestMethod("PUT");	// POST or PUT
			os = cnx.getOutputStream();
		}
		os.write("Hello World".getBytes());
		os.flush();
		os.close();
	} catch (java.net.MalformedURLException mue) {
		throw new MediaResourceException("Could not record to streamId: not URL");
	} catch (java.io.IOException ioe) {
		throw new MediaResourceException("Error recording to URL: " + streamId);
	}
}
/**
 * release a terminal from a call
 */
public void release(String address, CallId call) throws RawStateException {
	// For the emulator, phones and addresses have a 1:1 relationship
	RawPhone phone = this.getPhone(address);
	Leg leg = null;
	if (phone != null)
		leg = ((RawCall)call).getLeg(phone);
	
	if (leg != null)
		leg.drop();
	else
		throw new RawStateException(call, address, null,
				javax.telephony.InvalidStateException.CONNECTION_OBJECT,
				javax.telephony.TerminalConnection.UNKNOWN);
}
/**
 * Release a CallId.  Ignored by this provider.
 *
 * @param id The call id no longer used by the JTAPI layer.
 * @author: Richard Deadman
 */
public void releaseCallId(CallId id) {}
/**
 * Remove an event observer
 */
public void removeListener(TelephonyListener rl) {
	TestManager mgr = this.getMgr();
	if (rl.equals(mgr.getListener()))
		mgr.setListener(null);
}
/**
 * reportCallsOnAddress method comment.
 */
public void reportCallsOnAddress(java.lang.String address, boolean flag) {
}
/**
 * reportCallsOnTerminal method comment.
 */
public void reportCallsOnTerminal(java.lang.String terminal, boolean flag) {
}
/**
 * Reserve a callId for future use.
 */
public net.sourceforge.gjtapi.CallId reserveCallId(String address) throws InvalidArgumentException {
	// test if valid address
	if (this.getMgr().getPhone(address) == null)
		throw new InvalidArgumentException("Address " + address + " unknown by emulator");
	return new RawCall(this.getMgr());
}
/**
 * Retrieve up to num signals from the signal buffer associated with terminal, waiting until either
 * then number of signals is available, a patterns is met, a rtc is fired to stop retrieving, or a
 * timeout occurs.
 * <P> Note that currently this implementation does not wait for signals to arrive.
 *
 * @param terminal The terminal name to retrieve patterns from
 * @param num The maximum number of signals to return
 * @param patterns A set of signals that cause the call to return early.
 * @param rtcs A set of run-time controls to control the signal detector.
 * @param optArgs Optional signal detector arguments.
 * @return An event factory that holds the detected signals and any reasons.
 */
@SuppressWarnings("unchecked")
public RawSigDetectEvent retrieveSignals(String terminal, int num, Symbol[] patterns, RTC[] rtcs, Dictionary optArgs)
		throws MediaResourceException {
	//---------------------------------------------------------------------------
	// Bugs remaining - don't allow -1
	//                  dont check patterns or rtcs,
	//                  timouts - only obey p_Duration in the LOCAL dictionary
	//---------------------------------------------------------------------------
	String sigs = "";

 
	//---------------------------------------------------------------------------
	// Get the local p_Duration (if any).
	//---------------------------------------------------------------------------
	Object lTimeoutVal = null;

	if (optArgs != null) {
		Object lTimeoutKey = SignalDetectorConstants.p_Duration;
		lTimeoutVal = optArgs.get(lTimeoutKey);
	}

	boolean lTimed = (lTimeoutVal != null);
	int ltmout = (lTimed ? ((Integer) lTimeoutVal).intValue() : 0);
	int ltmdone = 0;

	while ((sigs.length() < num)
		&& ((!lTimed) || (ltmdone < ltmout))) {
		sigs += this.getMgr().getPhone(terminal).reportDTMF(num - sigs.length());
		ltmdone += 100;
		try {
			Thread.sleep(100);
		} catch (Exception e) {
		}
	}

	//---------------------------------------------------------------------------
	// Check for timeout
	//---------------------------------------------------------------------------
	if (sigs.length() < num) {
		return RawSigDetectEvent.timeout(
			terminal,
			SymbolConvertor.convert(sigs));
	}
	//	now turn the found characters into a GenericSignalDetectorEvent
	return RawSigDetectEvent.maxDetected(terminal, SymbolConvertor.convert(sigs));
 }					// we should wait until num is met or a pattern is met or a rtc tells us to stop...

/**
 * Send the passed data to a terminal, if one indicated.
 * This is just simple testing.
 */
public Object sendPrivateData(CallId call, String address, String terminal, Object data) {
	if (terminal != null) {
		RawPhone rp = this.getPhone(terminal);
		
		if (rp != null) {
			rp.setStatus(data.toString());

			// send back private data response
			this.getMgr().getListener().terminalPrivateData(terminal, "Ouch!", javax.telephony.events.Ev.CAUSE_NORMAL);
		}
	}
	return "Yea!";
}
/**
 * Send the DTMF signals on to the appropriate phone.
 */
@SuppressWarnings("unchecked")
public void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
	RawPhone rp = this.getMgr().getPhone(terminal);
	if (rp != null) {
		rp.sendDTMF(SymbolConvertor.convert(syms));
	}
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-10 10:07:53)
 * @author: 
 * @param newMgr net.sourceforge.gjtapi.raw.emulator.TestManager
 */
private void setMgr(TestManager newMgr) {
	mgr = newMgr;
}
/**
 * setPrivateData method comment.
 */
public void setPrivateData(net.sourceforge.gjtapi.CallId call, java.lang.String address, java.lang.String terminal, java.lang.Object data) {}
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
 * Clean up any resources
 */
public void shutdown() {
	// close my manager
	this.getMgr().close();
	
	// throw away my manager
	this.setMgr(null);

	// remove myself from the universe
	EmProvider.universe.remove(this);
}
/**
 * stop method comment.
 */
public void stop(java.lang.String terminal) {
	PhoneModel pm = ((TestPhone)this.getPhone(terminal)).getModel();
	Thread t = pm.getPlayThread();;
	if (t != null)
		t.interrupt();
	t = pm.getRecordThread();
	if (t != null)
		t.interrupt();
}
/**
 * Since I don't support call throttling, I should never be called.
 */
public boolean stopReportingCall(CallId call) {
	return true;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "A JTAPI Raw Provider for: " + this.getMgr();
}
/**
 * Only triggers we support are for shutting down any player or recorders
 */
public void triggerRTC(java.lang.String terminal, javax.telephony.media.Symbol action) {
	if (action.equals(PlayerConstants.rtca_Stop)) {
		Thread t = ((TestPhone)this.getPhone(terminal)).getModel().getPlayThread();
		if (t != null)
			t.interrupt();
	} else if (action.equals(RecorderConstants.rtca_Stop)) {
		Thread t = ((TestPhone)this.getPhone(terminal)).getModel().getRecordThread();
		if (t != null)
			t.interrupt();
	}
}
/**
 * unHold method comment.
 */
public void unHold(CallId call, String address, String term) throws RawStateException {
	try {
		this.getPhone(term).unHold();
	} catch (NullPointerException npe) {
		// No phone matched
		throw new RawStateException(call, term, term,
				javax.telephony.InvalidStateException.TERMINAL_CONNECTION_OBJECT,
				javax.telephony.TerminalConnection.UNKNOWN);
	}
}
}
