package net.sourceforge.gjtapi.media;

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
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import javax.telephony.Address;
import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.Provider;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlCall;
import javax.telephony.callcontrol.CallControlTerminalConnection;
import javax.telephony.media.AlreadyBoundException;
import javax.telephony.media.BindCancelledException;
import javax.telephony.media.ConfigSpec;
import javax.telephony.media.MediaBindException;
import javax.telephony.media.MediaCallException;
import javax.telephony.media.MediaConfigException;
import javax.telephony.media.MediaEvent;
import javax.telephony.media.MediaListener;
import javax.telephony.media.MediaProvider;
import javax.telephony.media.MediaResourceException;
import javax.telephony.media.MediaService;
import javax.telephony.media.MediaServiceListener;
import javax.telephony.media.MediaTerminal;
import javax.telephony.media.NoServiceReadyException;
import javax.telephony.media.NotBoundException;
import javax.telephony.media.Player;
import javax.telephony.media.PlayerEvent;
import javax.telephony.media.PlayerListener;
import javax.telephony.media.RTC;
import javax.telephony.media.Recorder;
import javax.telephony.media.RecorderEvent;
import javax.telephony.media.RecorderListener;
import javax.telephony.media.ResourceNotSupportedException;
import javax.telephony.media.SignalDetector;
import javax.telephony.media.SignalDetectorEvent;
import javax.telephony.media.SignalDetectorListener;
import javax.telephony.media.SignalGenerator;
import javax.telephony.media.SignalGeneratorEvent;
import javax.telephony.media.Symbol;

import net.sourceforge.gjtapi.GenericProvider;

/**
 * Implements MediaService and the basic Resource interfaces.
 * Resource methods are delegated to the bound MediaGroup.
 * <p>
 * This class understands about binding and configuration,
 * and knows how to delegate resource methods to whatever
 * object or objects are bound to this one. And conversely
 * understand how to register Listeners for each resource,
 * and dispatch events from any of the resources 
 * to all of the ResourceListeners.
 * <p>
 * The Resource methods in BasicMediaService are documented
 * in the associated Resource interface definition.
 */
public class GenericMediaService implements MediaService, MediaServiceListener, Player, PlayerListener, Recorder, RecorderListener, SignalDetector, SignalDetectorListener, SignalGenerator, MediaServiceHolder {
	/** The Provider and media service manager to work with */
	private GenericProvider provider = null;
	private MediaMgr mgr = null;
	/** Vector of MediaListeners (MediaServiceListener, 
			ResourceListener (PlayerListener, RecorderListener, SignalDetectorListener)) */
	protected static Vector<MediaListener> theListeners = new Vector<MediaListener>();
	private GenericMediaGroup mediaGroup = null;
/**
 * Create an unbound MediaService,
 * using the installation default MediaProvider.
 */
public GenericMediaService() throws JtapiPeerUnavailableException, ProviderUnavailableException {
	super();
	Provider prov = JtapiPeerFactory.getJtapiPeer(null).getProvider(null);
	if (prov instanceof MediaProvider)
		this.initMediaService((MediaProvider) prov);
	else
		throw new ProviderUnavailableException("Default Provider not a MediaProvider");
}
	/**
	 * Create an unbound MediaService, using services identified
	 * by the two String arguments.
	 * 
	 * @param peerName the name of a Class that implements JtapiPeer.
	 * @param providerString a "login" string for that JtapiPeer.
	 *
	 * @throws ClassNotFoundException if JtapiPeer class is not found
	 * @throws IllegalAccessException if JtapiPeer class is not accessible
	 * @throws InstantiationException if JtapiPeer class does not instantiate
	 * @throws ProviderUnavailableException if Provider is not available
	 * @throws ClassCastException if Provider instance is not a MediaProvider
	 */
	public GenericMediaService(String peerName, String providerString) 
	throws ClassNotFoundException, 
		InstantiationException, // hope to remove in Jtapi-2.0
		IllegalAccessException,	// hope to remove in Jtapi-2.0
		ProviderUnavailableException {
	    initMediaService(findMediaProvider(peerName, providerString));
	}
	/*
	 * Create an unbound MediaService, using services associated
	 * with the given MediaProvider object.
	 * The MediaProvider object may be a JTAPI Provider,
	 * or may be some other object obtained by other vendor-specific means.
	 *
	 * @param provider a MediaProvider instance
	 */
	public GenericMediaService(MediaProvider provider) {
	initMediaService(provider);
	}
	/** 
	 * Add a MediaListener to this MediaService.
	 * <p>
	 * Events generated by this MediaService are dispatched to the
	 * given listener if it implements the appropriate Listener interface.
	 * @@param listener an object that implements 
	 * one or more of the ResourceListener interfaces and/or 
	 * the MediaServiceListener interface.
	 */
	public void addMediaListener(MediaListener listener) {
	 theListeners.add(listener);
	}
	/*
	 * bind to an new outbound call.
	 * @exception MediaBindException one of AlreadyBoundException, BindCancelledException.
	 * @exception MediaConfigException if MediaGroup can not be configured as requested.
	 * @exception MediaCallException encapsulates any call processing exceptions
	 * generated while trying to establish the new Call and Connections.
	 */
	synchronized
	public void bindAndConnect(ConfigSpec configSpec, 
				     String origAddr, 
				     String dialDigits)
	throws MediaBindException, MediaConfigException, MediaCallException {
	    if(this.getMediaGroup() != null) throw new AlreadyBoundException();

	    	// find a media terminal
	    GenericProvider gp = this.getProvider();
	    Address addr = null;
	    try {
		    addr = gp.getAddress(origAddr);
	    } catch (InvalidArgumentException iae) {
		    throw new MediaCallException("Originating Address unknown");
	    }
			    
		Terminal[] terms = addr.getTerminals();
	    MediaTerminal term = null;
	    for (int i = 0; i < terms.length && term == null; i++) {
		    if (terms[i] instanceof MediaTerminal) {
		    	term = (MediaTerminal)terms[i];
		    	break;
		    }
	    }
	    if (term == null) {		// no media terminals
		    throw new MediaCallException("No media terminal for address " + origAddr);
	    }

	    	// bind to the terminal
	    this.bindToTerminal(configSpec, term);

	    	// make the call and connect it
	    try {
		    gp.createCall().connect(term, addr, dialDigits);
	    } catch (Exception e) {
	    	// release the MediaService, since the call failed
	    	this.releaseAndFree();

		    throw new MediaCallException("Error making call: " + e.toString());
	    }
	}
	/* 
	 * Bind this MediaService to a MediaGroup connected to an existing Call.
	 * This will typically create a new Connection to the Call.
	 * <p>
	 * @param configSpec declares configuration requirments for the MediaGroup.
	 * @param call a JTAPI Call object
	 *
	 * @exception MediaBindException one of AlreadyBoundException, BindCancelledException.
	 * @exception MediaConfigException if MediaGroup can not be configured as requested.
	 * @exception MediaCallException encapsulates any call processing exceptions
	 * generated while trying to establish the new Connections.
	 * @return the Connection between the bound Terminal and the given Call.
	 */
	synchronized
	public Connection bindToCall(ConfigSpec configSpec,
			   Call call)
	throws MediaBindException, MediaConfigException, MediaCallException {
	if (this.getMediaGroup() != null)
		throw new AlreadyBoundException();
	if (call == null)
		throw new MediaCallException("Null Call");
	MediaTerminal mt = null;
	CallControlTerminalConnection tc = null;	// for later, if we need it in adding in a new media terminal
	// first check if any of the current terminals support media
	Connection[] conns = call.getConnections();
	Connection mediaConn = null;
	for (int i = 0; i < conns.length; i++) {
		TerminalConnection[] tcs = conns[i].getTerminalConnections();
		if (tcs != null) {
			for (int j = 0; j < tcs.length; j++) {
				if (tc == null && tcs[j] instanceof CallControlTerminalConnection)
					tc = (CallControlTerminalConnection)tcs[j];
				if (tcs[j].getTerminal() instanceof MediaTerminal) {
					mt = (MediaTerminal)tcs[j].getTerminal();
					mediaConn = conns[i];
					break;
				}
			}
		}
	}
	if (mt == null) { // create a new media terminal connection to the call
		if (!(call instanceof CallControlCall))
			throw new MediaCallException("Call does not support conferencing in new Media Terminals");
		if (tc == null)
			throw new MediaCallException("No TerminalConnection to make conference from");
		CallControlCall ccc = (CallControlCall)call;

			// ask the provider for the first known available media terminal
		mt = this.getProvider().getAvailableMediaTerminal();

		// check if any MediaTerminals found
		if (mt == null)
			throw new MediaCallException("No available media terminals known");

		Connection[] consultCons = null;
		try {
			// connect to the active call by conferencing the media terminal in
			consultCons = ccc.consult(tc, mt.getAddresses()[0].getName());

			// answer the other end
			boolean answered = false;
			TerminalConnection[] newTcs = mt.getTerminalConnections();
			for (int i = 0; i < newTcs.length; i++) {
				if (newTcs[i].getState() == TerminalConnection.RINGING) {
					((CallControlTerminalConnection)newTcs[i]).answer();
					// mediaConn = newTcs[i].getConnection(); -- but the conference will break this
					answered = true;
					break;
				}
			}
			if (!answered) {
				for (int i = 0; i < consultCons.length; i++)
					consultCons[i].disconnect();
				throw new MediaCallException("Could not answer consultation call");
			}

			// now join things up
			ccc.conference(consultCons[0].getCall());
		} catch (Exception e) {
			if (e instanceof MediaCallException)
				throw (MediaCallException)e;
			else
				throw new MediaCallException("Error making consultation call");
		}
	}

	// now assign the media service to the connected media terminal
	this.bindToTerminal(configSpec, mt);

	// return the connection between the call and the terminal
	if (mediaConn == null) {
		conns = call.getConnections();
		int sz = conns.length;
		for (int i = 0; i < sz; i++) {
			TerminalConnection[] tcs = conns[i].getTerminalConnections();
			int tcSize = tcs.length;
			for (int j = 0; j < tcSize; j++) {
				if (tcs[j].getTerminal().equals(mt)) {
					mediaConn = conns[i];
					break;
				}
			}
		}
	}
	return mediaConn;
}
/* 
 * Bind this MediaService with a particular MediaGroup.
 * <p>This may be performed by another object in the same package.
 *
 * @param group The MediaGroup to hook into this MediaService
 *
 * @exception AlreadyBoundException
 */
synchronized void bindToGroup(GenericMediaGroup group) throws AlreadyBoundException {
	if (this.isBound())
		throw new AlreadyBoundException();
	this.setMediaGroup(group);
	this.getMgr().bind(group.getTerminal().getName(), this);
	
	// notify any thread that is waiting for this service to be bound
	synchronized(this) {
		this.notify();
	}
}
	/*
	 * This MediaService is ready for a MediaGroup.
	 * Waits for a Call to be delivered to this service name.
	 * <p>
	 * On return, this MediaService is bound to a MediaGroup,
	 * and that MediaGroup is configured to configSpec.
	 * <p>
	 * if cancelBindRequest() terminates this method,
	 * this method unblocks, throwing a BindCancelledException.
	 * <p>
	 * @param configSpec configure MediaGroup to configspec before binding.
	 * @param serviceName name under which this MediaService is to be registered.
	 * @exception MediaBindException one of AlreadyBoundException, BindCancelledException.
	 */
	// Does not throw MediaConfigException: that exception goes to releaseToService().
	
	/* The logical equivalent of S.100:RequestGroup() */
	synchronized
	public void bindToServiceName(ConfigSpec configSpec,
				  String serviceName)
	throws MediaBindException {
	    if(this.getMediaGroup() != null)
	    	throw new AlreadyBoundException();
	    // place in MediaMgr
	    this.getMgr().register(serviceName, this);

	    // wait for service to be invoked
	    synchronized (this) {
		    try {
			    this.wait();
		    } catch (InterruptedException ie) {
			    throw new BindCancelledException("Bind Thread Interrupted");
		    }
		    // test if the bind was cancelled
		    if (this.getMediaGroup() == null)
		    	throw new BindCancelledException("Cancelled");
	    }
	}
/* 
 * Bind this MediaService to a particular Terminal.
 * <p>
 *
 * @param configSpec declares configuration requirments for the MediaGroup.
 * @param terminal a JTAPI Terminal object.
 *
 * @exception MediaBindException one of AlreadyBoundException, BindCancelledException.
 * @exception MediaConfigException if MediaGroup can not be configured as requested.
 */
public void bindToTerminal(ConfigSpec configSpec, Terminal terminal) throws MediaBindException, MediaConfigException {
	if (this.isBound())
		throw new AlreadyBoundException();
	this.bindToGroup(new GenericMediaGroup(configSpec, terminal, this));
}
/* 
 * Bind this MediaService to a particular Terminal.
 * <p>
 * @param configSpec declares configuration requirments for the MediaGroup
 * @param terminalName a String that names a media capable Terminal
 * @throws MediaBindException one of AlreadyBoundException, BindCancelledException.
 * @throws MediaConfigException if MediaGroup can not be configured as requested
 */
public void bindToTerminalName(ConfigSpec configSpec, String terminalName) throws MediaBindException, MediaConfigException {
	Terminal t = null;
	try {
		t = this.getProvider().getTerminal(terminalName);
	} catch (InvalidArgumentException iae) {
		throw new ResourceNotSupportedException("No known terminal: " + terminalName);
	}
	this.bindToTerminal(configSpec, t);
}  
	/*
	 * Revoke previous bindToXXXX() request on this MediaService.
	 * <br>
	 * This is called from a thread that is NOT blocked in a bind() method, 
	 * and will unblock the bindToXXXX() request.
	 * <p>
	 * This method is "one-way" and Listener-safe. The app/Listener will
	 * receive either a bind completion event or a BindCancelledException as the
	 * result of the outstanding bindToXXXX(). 
	 */
	public void cancelBindRequest() {
		synchronized (this) {
			this.notifyAll();
		}
	}
/**
 * Checks if a virtual MediaGroup is bound, and returns it.
 * If no MediaGroup is bound, throw the RuntimeException NotBoundException.
 * <p>
 * Applications that subclass BasicMediaService should generally
 * <b>not</b> use this directly.
 *
 * @exception NotBoundException if not currently bound to a MediaGroup
 * @return Myself so that the player methods can be forwarded.
 */
private GenericMediaGroup checkGroup() throws NotBoundException {
	GenericMediaGroup mg = this.getMediaGroup();
	if (mg == null)
		throw new NotBoundException();
	return mg;
}
	/*
	 * Configure current MediaGroup according to ConfigSpec.
	 * <p>
	 * Post-Condition: (foreach R in configSpec {this.isInstanceOf(R)})
	 * and the Resource is allocated and connected in the MediaGroup.
	 * <p>
	 * <b>Note:</b>
	 * On some implementations this method stops all media operations.
	 *
	 * @param configSpec the requested configuration 
	 * @exception NotBoundException if not currently bound to a MediaGroup
	 * @exception MediaConfigException if resources can not be configured as requested
	 */
	public void configure(ConfigSpec configSpec) 
	throws MediaConfigException {
	    this.checkGroup().allocate(configSpec.getResourceSpecs());
	}
/*
 * Extract a subset from a dictionary based on a set of Symbol keys.
 *
 * If the key set is null, create a snapshot
 */
@SuppressWarnings("unchecked")
private Dictionary extract(Dictionary dict, Symbol[] keys) {
	// take snapshot if keys null
	if (keys == null) {
		Dictionary snapshot = new Hashtable(dict.size());
		Enumeration e = dict.keys();
		while (e.hasMoreElements()) {
			Object key = e.nextElement();
			snapshot.put(key, dict.get(key));
		}
		return snapshot;
	}

	// find the matching keys
	Dictionary subSet = new Hashtable();
	int size = keys.length;
	for (int i = 0; i < size; i++) {
		if (keys[i] != null) {
			Object o = dict.get(keys[i]);
			if (o != null)
				subSet.put(keys[i], o);
		}
	}
	return subSet;
}
/**
 * Called when the object is no longer held onto
 * Creation date: (2000-05-24 09:44:08)
 * @author: Richard Deadman
 */
@Override
public void finalize() {
	// release and free the MediaGroup but don't disconnect the connection.
	try {
		this.releaseAndFree();
	} catch (NotBoundException nbe) {
		// then we don't need to release
	}
}
	/* This implementation assumes that the Jtapi Provider
	 * is a MediaProvider, possibly an unfounded assumption.
	 * However, it serves to convince the compiler that the
	 * indicated exceptions may be thrown.
	 * <P>Note that a a null peer name is okay.
	 */ 
	MediaProvider findMediaProvider(String peerName, String providerString) 
	throws ClassNotFoundException, 
		InstantiationException,
		IllegalAccessException,
		ProviderUnavailableException {
	return ((MediaProvider)
		((JtapiPeer)Class.forName(peerName).newInstance())
		.getProvider(providerString));
	}
/* @see SignalDetector#flushBuffer() */
public SignalDetectorEvent flushBuffer() throws MediaResourceException {
	return this.checkGroup().flushBuffer();
}
/*
 * Get current configuration of the MediaGroup.
 * <p>
 * Note this does not generally return a copy of the ConfigSpec 
 * used to create/configure  this MediaGroup; the returned ConfigSpec
 * describes the full and actual configuration of resources.
 *
 * @return a ConfigSpec describing the current configuration.
 * @exception NotBoundException if not currently bound to a MediaGroup
 * <p>
 */
public ConfigSpec getConfiguration() throws NotBoundException {
	return this.checkGroup().getConfigSpec();
}
/**
 * Accessor for an Iterator over bound MediaListeners.
 * Creation date: (2000-05-03 15:22:54)
 * @author: Richard Deadman
 * @return An iterator over MediaListeners attached to me.
 */
public Iterator<MediaListener> getListeners() {
	return GenericMediaService.theListeners.iterator();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-13 15:04:17)
 * @author: 
 * @return net.sourceforge.gjtapi.media.GenericMediaGroup
 */
private GenericMediaGroup getMediaGroup() {
	return mediaGroup;
}
/**
 * Cast myself
 */
public MediaService getMediaService() {
	return this;
}
/**
 * Internal media manager accessor
 * Creation date: (2000-03-24 15:52:33)
 * @author: Richard Deadman
 * @return The media manager that tracks media services against terminals and waiting service names.
 */
private MediaMgr getMgr() {
	return mgr;
}
	/*
	 * get a collection of Parameters from the Group/Resources.
	 *
	 * @param keys is a Dictionary indicating the paramters of interest.
	 * The values in this Dictionary are placed with the values of the
	 * indicated parameter. If a requested parameter is not supported 
	 * by the MediaService implementation, that key is removed from the Dictionary.
	 * @return Dictionary of values bound to the given keys
	 */
	@SuppressWarnings("unchecked")
	public Dictionary getParameters(Symbol[] keys) { 
		return this.extract(this.checkGroup().getParameters(), keys);
	}
/**
 * Internal provider accessor
 * Creation date: (2000-03-24 15:52:33)
 * @author: Richard Deadman
 * @return The system provider that manages terminals and raw providers
 */
private net.sourceforge.gjtapi.GenericProvider getProvider() {
	return provider;
}
	/* 
	 * Get the Jtapi Terminal associated with the MediaService.
	 * The Terminal may be used to access the associated JTAPI objects.
	 * <p>
	 * If this MediaService is not associated with a 
	 * JTAPI call control Provider, this method may return null.
	 */
	public Terminal getTerminal() {
	return checkGroup().getTerminal();
	}
/*
 * Return the installation specific String that identifies
 * the Terminal to which this MediaService is bound.
 *
 * @return a String that uniquely identifies the bound Terminal.
 * @throws NotBoundException if not currently bound to a MediaGroup
 * @see #getTerminal
 */
public String getTerminalName() {
	Terminal t = this.getTerminal();
	if (t == null)
		return null;
	else
		return t.getName();
}
/*
 * Creates and returns a new Dictionary object that contains a snapshot of
 * the MediaGroup Dictionary.
 * <p>
 * @return A Dictionary of application-shared information.
 * @exception NotBoundException if not currently bound to a MediaGroup
 */
@SuppressWarnings("unchecked")
public Dictionary getUserDictionary() throws NotBoundException {
	this.checkGroup();
	return this.getMediaGroup().getDictionary();
}
	/**
	 * Creates and returns a new Dictionary that contains 
	 * the UserDictionary values corresponding to a given set of keys.
	 * <p>
	 * If the keys argument is <code>null</code> then a snapshot of 
	 * the entire UserDictionary is returned.
	 * <p>
	 * For interoperability with other languages, 
	 * the keys in the Dictionary are restricted to type Symbol.
	 * The result of using keys of other types is undefined,
	 * but throwing a ClassCastException is considered compliant.
	 * <p>
	 * @param keys an array of key Symbols
	 * @return a Dictionary of application-shared information.
	 *
	 * @throws NotBoundException if not currently bound to a MediaGroup
	 */
	@SuppressWarnings("unchecked")
	public Dictionary getUserValues(Symbol[] keys) throws NotBoundException {
		return this.extract(this.checkGroup().getDictionary(), keys);
	}
	private void initMediaService(MediaProvider provider) {
	GenericProvider prov = this.provider = (GenericProvider)provider;
	this.mgr = prov.getMediaMgr();
	// mark this group as unbound, no bind in progress:
	// also tests _provider for null, pings _provider.
	this.setMediaGroup(null);
	}
	/**
	 * @return true iff this MediaService is bound to a MediaGroup.
	 */
	public boolean isBound() {
	return (this.mediaGroup != null);
	}
	public void onDisconnected(MediaEvent event) {
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof MediaServiceListener) {
		try {
		    ((MediaServiceListener)listener).onDisconnected(event);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see SignalDetectorListener#onOverflow(SignalDetectorEvent) */ 
	public void onOverflow(SignalDetectorEvent signaldetectorevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof SignalDetectorListener) {
		try {
		    ((SignalDetectorListener)listener).onOverflow(signaldetectorevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see SignalDetectorListener#onPatternMatched(SignalDetectorEvent) */ 
	public void onPatternMatched(SignalDetectorEvent signaldetectorevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof SignalDetectorListener) {
		try {
		    ((SignalDetectorListener)listener).onPatternMatched(signaldetectorevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see PlayerListener#onPause(PlayerEvent) */ 
	public void onPause(PlayerEvent playerevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof PlayerListener) {
		try {
		    ((PlayerListener)listener).onPause(playerevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see RecorderListener#onPause(RecorderEvent) */ 
	public void onPause(RecorderEvent recorderevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof RecorderListener) {
		try {
		    ((RecorderListener)listener).onPause(recorderevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see PlayerListener#onResume(PlayerEvent) */ 
	public void onResume(PlayerEvent playerevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof PlayerListener) {
		try {
		    ((PlayerListener)listener).onResume(playerevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see RecorderListener#onResume(RecorderEvent) */ 
	public void onResume(RecorderEvent recorderevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof RecorderListener) {
		try {
		    ((RecorderListener)listener).onResume(recorderevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see SignalDetectorListener#onSignalDetected(SignalDetectorEvent) */ 
	public void onSignalDetected(SignalDetectorEvent signaldetectorevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof SignalDetectorListener) {
		try {
		    ((SignalDetectorListener)listener).onSignalDetected(signaldetectorevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see PlayerListener#onSpeedChange(PlayerEvent) */ 
	public void onSpeedChange(PlayerEvent playerevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof PlayerListener) {
		try {
		    ((PlayerListener)listener).onSpeedChange(playerevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/* @see PlayerListener#onVolumeChange(PlayerEvent) */ 
	public void onVolumeChange(PlayerEvent playerevent0) 
	{
	MediaListener listener; 
	Iterator<MediaListener> iter = theListeners.iterator();
	while(iter.hasNext()) {
	    listener = iter.next();
	    if(listener instanceof PlayerListener) {
		try {
		    ((PlayerListener)listener).onVolumeChange(playerevent0);
		} catch (Exception ex) {}
	    }
	}
	}
	/**
	 * Play a set of dtmf signals on the bound media group.
	 **/


	/* @see Player#play(String, int, RTC[], Dictionary) */ 
	@SuppressWarnings("unchecked")
	public PlayerEvent play(String[] streamId, int offset, RTC[] rtcs, Dictionary optArgs)
	throws MediaResourceException 
	{
	return ((Player)checkGroup()).
	play(streamId, offset, rtcs, optArgs);
	}
	/**
	 * Play a set of dtmf signals on the bound media group.
	 **/


	/* @see Player#play(String, int, RTC[], Dictionary) */ 
	@SuppressWarnings("unchecked")
	public PlayerEvent play(String streamId, int offset, RTC[] rtcs, Dictionary optArgs)
	throws MediaResourceException 
	{
	    final Player player = checkGroup();
	    return player.play(streamId, offset, rtcs, optArgs);
	}
	/* @see Recorder#record(String, RTC[], Dictionary) */ 
	@SuppressWarnings("unchecked")
	public RecorderEvent record(String string0, RTC[] rtc1, Dictionary dictionary2)
	throws MediaResourceException 
	{
	return ((Recorder)checkGroup()).
	record(string0, rtc1, dictionary2);
	}
/*
 * Release the MediaGroup with an indication that the 
 * Connection (and perhaps the Call) should be,
 * or perhaps already has been, dropped.
 * <p>
 * In general, this method will provoke framework specific processing
 * to handle the end of the call. 
 * <p>
 * @exception NotBoundException if not currently bound to a MediaGroup
 * <p>
 */
public void release() {
	// should we disconnect?
	boolean disconnect = this.getProvider().disconnectOnMediaRelease();
	Connection conn = null;
	
	if (disconnect) {
		// store the Connection
		TerminalConnection[] tcs = this.getTerminal().getTerminalConnections();
		if ((tcs != null) && (tcs.length >0))
			conn = tcs[0].getConnection();
	}
	
	this.releaseAndFree();
	
	// now, disconnect the call if the Provider thinks we should
	if (conn != null) {
		try {
			conn.disconnect();
		} catch (ResourceUnavailableException rue) {
			// fail silently -- we tried our best
		} catch (MethodNotSupportedException mnse) {
			// fail silently -- we tried our best
		} catch (InvalidStateException ise) {
			// fail silently -- we tried our best
		} catch (PrivilegeViolationException pve) {
			// fail silently -- we tried our best
		}
	}
}

/*
 * Private release. This is used both by the MediaService.release() and the
 * finalize method. This means that finalize doesn't cause a line to be dropped.
 */
private void releaseAndFree() {
	this.releaseGroup().free();
}

/*
 * Release the MediaGroup from a MediaService.
 * Note that the MediaGroup is still associated with a call.
 *
 * @exception NotBoundException if not currently bound to a MediaGroup
 */
synchronized private GenericMediaGroup releaseGroup() {
	GenericMediaGroup mg = this.checkGroup();
	this.getMgr().release(this.getTerminalName());
	this.setMediaGroup(null);
	mg.freeService();
	return mg;
}
	/*
	 * Release the bound terminal to the next waiting media service for further processing.
	 * <p>
	 * @param disposition A string that identifies the next MediaService. 
	 * @param timeout int milliseconds to wait for new service to become ready.
	 *
	 * @exception NotBoundException if not currently bound to a MediaGroup
	 * @exception MediaBindException one of NoServiceAssignedException or NoServiceReadyException
	 * @exception NoServiceAssignedException if disposition is not recognised 
	 * or is not mapped to any serviceName.
	 * @exception NoServiceReadyException if disposition is mapped to a serviceName,
	 * but none of the MediaServices registered to serviceName 
	 * are ready and do not become ready within <i>timeout</i> millisecs.
	 * @exception MediaConfigException if the MediaGroup could not be configured
	 * for the recipient service.
	 */
	synchronized
	public void releaseToService(String disposition, int timeout)
	throws MediaBindException, MediaConfigException {
		GenericMediaGroup mg = this.releaseGroup();

		// try to pass MediaGroup to next bound service
		MediaMgr mgr = this.getMgr();
		MediaService ms = mgr.unRegister(disposition);
		if (ms == null) {
			ms = mgr.waitForMediaService(mg, disposition, timeout);
			if (ms == null)
				throw new NoServiceReadyException("No MediaService registered for " + disposition);
		}
		((GenericMediaService)ms).bindToGroup(mg);
	}
	/** remove this MediaListener. */
	public void removeMediaListener(MediaListener listener) {
	 theListeners.remove(listener);
	}
	/* @see SignalDetector#retrieveSignals(int, Symbol[], RTC[], Dictionary) */ 
	@SuppressWarnings("unchecked")
	public SignalDetectorEvent retrieveSignals(int int0, Symbol[] symbol1, RTC[] rtc2, Dictionary dictionary3)
	throws MediaResourceException 
	{
	return ((SignalDetector)checkGroup()).
	retrieveSignals(int0, symbol1, rtc2, dictionary3);
	}
	/* @see SignalGenerator#sendSignals(Symbol[], RTC[], Dictionary) */ 
	@SuppressWarnings("unchecked")
	public SignalGeneratorEvent sendSignals(Symbol[] symbol0, RTC[] rtc1, Dictionary dictionary2)
	throws MediaResourceException 
	{
	return ((SignalGenerator)checkGroup()).
	sendSignals(symbol0, rtc1, dictionary2);
	}
	/* @see SignalGenerator#sendSignals(String, RTC[], Dictionary) */ 
	@SuppressWarnings("unchecked")
	public SignalGeneratorEvent sendSignals(String string0, RTC[] rtc1, Dictionary dictionary2)
	throws MediaResourceException 
	{
	return ((SignalGenerator)checkGroup()).
	sendSignals(string0, rtc1, dictionary2);
	}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-13 15:04:17)
 * @author: 
 * @param newMediaGroup net.sourceforge.gjtapi.media.GenericMediaGroup
 */
private void setMediaGroup(GenericMediaGroup newMediaGroup) {
	mediaGroup = newMediaGroup;
}
/*
 * Set the value of various parameters to the given values.
 */
@SuppressWarnings("unchecked")
public void setParameters(Dictionary params) throws NotBoundException {
	this.checkGroup().setParameters(params);
}
/*
 * Set the entire UserDictionary to a new collection of key-value pairs.
 * <p>
 * Note: 
 * setUserDictionary(null) will clear all key-value pairs from the Dictionary.
 * <p>
 * @param newDict A Dictionary whose contents is copied into the MediaGroup.
 * @exception NotBoundException if not currently bound to a MediaGroup
 */
@SuppressWarnings("unchecked")
public void setUserDictionary(Dictionary newDict) throws NotBoundException {
	this.checkGroup().setDictionary(newDict);
}
/**
 * Set the values of several UserDictionary keys.
 *
 * The values supplied in the given <code>dict</code>
 * are merged with the current UserDictionary.
 *
 * @param dict a Dictionary whose contents is merged into the UserDictionary.
 * @throws NotBoundException if not currently bound to a MediaGroup
 */
@SuppressWarnings("unchecked")
public void setUserValues(Dictionary newDict) throws NotBoundException {
	this.checkGroup();
	Dictionary dict = this.getMediaGroup().getDictionary();
	Enumeration keySet = newDict.keys();
	while (keySet.hasMoreElements()) {
		Object key = keySet.nextElement();
		dict.put(key, newDict.get(key));
	}
}
/*
 * Stop all media/resource operations currently in progress.
 * Unblocks all synchronous resource methods (all AsyncResourceEvent complete).
 * <p>
 * This is a non-blocking, one-way invocation.
 *
 * @exception NotBoundException if not currently bound to a MediaGroup
 */
public void stop() throws NotBoundException {
	String term = this.getTerminalName();
	this.getProvider().getRaw().stop(term);
}
/*
 * Trigger a RTC action.
 * <p>
 * This method allows the application to synthesize the triggering
 * of RTC actions. The RTC Condition will be rtcc_Application.
 * <p>
 * This is a non-blocking, one-way invocation.
 *
 * @param a Symbol for a recognized RTC action: rtca_<i>Action</i>
 * @exception NotBoundException if not currently bound to a MediaGroup
 */
public void triggerRTC(Symbol rtca) throws NotBoundException {
	String term = this.getTerminalName();
	this.getProvider().getRaw().triggerRTC(term, rtca);
}
}
