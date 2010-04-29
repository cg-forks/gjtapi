package net.sourceforge.gjtapi;

// NAME
//      $RCSfile$
// DESCRIPTION
//      [given below in javadoc format]
// DELTA
//      $Revision$
// CREATED
//      $Date$
// COPYRIGHT
//      Westhawk Ltd
// TO DO
//
// part of Free Jtapi.
/*
 * Copyright (C) 1999 by Westhawk Ltd (www.westhawk.co.uk)
 *
 * Permission to use, copy, modify, and distribute this software
 * for any purpose and without fee is hereby granted, provided
 * that the above copyright notices appear in all copies and that
 * both the copyright notice and this permission notice appear in
 * supporting documentation.
 * This software is provided "as is" without express or implied
 * warranty.
 */

import javax.telephony.*;
import javax.telephony.events.*;
import javax.telephony.capabilities.AddressCapabilities;
import net.sourceforge.gjtapi.events.*;
import net.sourceforge.gjtapi.capabilities.GenAddressCapabilities;
import java.lang.ref.WeakReference;
import java.util.*;
import javax.telephony.privatedata.PrivateData;

public class FreeAddress implements Address, PrivateData {

  private String name;
  private Provider provider;
  private boolean local;		// Is this an Address in the Provider's domain?
  
  private Vector<CallObserver> callObservers = new Vector<CallObserver>();
  private ObservableHelper observers = new ObservableHelper() {
	  Object[] mkObserverArray(int i) { 
	      return new AddressObserver[i];
	  }

	  void notifyObserver(Object o, Ev [] e) {
	      final AddressObserver obs = (AddressObserver) o;
	      obs.addressChangedEvent((AddrEv[])e);
	  }
  };

  private Set<TermData> terminals = null;		// holds the TermData of the terminals
  private Vector<ConnectionHolder> connections = new Vector<ConnectionHolder>(2);	// holds weak references to the Connection

  private transient Vector<CallListener> callListeners
      = new Vector<CallListener>();
  private transient Vector<AddressListener> addressListeners
      = new Vector<AddressListener>();
  private boolean reporting = false;

  /**
   * A weak reference to a Connection that can re-instantiate itself as necessary
   **/
  class ConnectionHolder {
	  private final CallId callId;
	  private final String address;
	  final WeakReference<FreeConnection> connRef;

	  ConnectionHolder(CallId id, String addr) {
		  callId = id;
		  address = addr;
		  connRef = null;
	  }

	  ConnectionHolder(FreeConnection connection) {
		  callId = ((FreeCall)connection.getCall()).getCallID();
		  address = connection.getAddress().getName();
		  connRef = new WeakReference<FreeConnection>(connection);
	  }

	  CallId getCallId() {
		  return callId;
	  }

	  private String getAddress() {
		  return address;
	  }

	  /**
	   * Return the current referrent or null
	   **/
	  private FreeConnection getReferent() {
		  WeakReference<FreeConnection> wr = connRef;
		  FreeConnection conn = null;
		  if (wr != null) {
			  conn = (FreeConnection)wr.get();
		  }
		  return conn;
	  }
	  public int hashCode() {
		  return this.getCallId().hashCode() + this.getAddress().hashCode();
	  }

	  public boolean equals(Object o) {
		  if (o instanceof ConnectionHolder) {
			  ConnectionHolder ch = (ConnectionHolder)o;
			  if (ch.getCallId() != null &&
					  ch.getCallId().equals(this.getCallId()) &&
					  ch.getAddress().equals(this.getAddress()))
			  	  return true;
		  }
		  return false;
	  }
	  
	  FreeConnection getConnection() {
		  FreeConnection conn = getReferent();
		  if (conn == null) {
			  synchronized (this) {
				  // double check
				  conn = getReferent();
				  if (conn == null) {
					  conn = ((GenericProvider)getProvider()).getCallMgr().getFaultedConnection(this.getCallId(), this.getAddress());
				  }
			  }
		  }
		  return conn;
	  }
  }
  /**
   * Create an Address object.
   * This should only be called by the DomainMgr.
   * @param num The Address's unique number, often a phone number
   * @param prov The Provider that hold the Address's domain.
   * @param local Is the Address local to the Provider's domain?
   **/
FreeAddress(String num, Provider prov, boolean local){
	super();
	this.setProvider(prov);
	this.setName(num);
	this.setLocal(local);
}    
  @SuppressWarnings("unchecked")
public synchronized void addAddressListener(AddressListener l) {
	if (l == null)
		return;

	Vector<AddressListener> v = addressListeners == null ? new Vector<AddressListener>(2) : (Vector<AddressListener>) addressListeners.clone();
	// protect the Address from garbage collection
	this.protect();

	// add the listener
	if (!v.contains(l)) {
	  v.addElement(l);
	  addressListeners = v;
	}
  }            
  public void addCallListener(CallListener l)
	throws ResourceUnavailableException,MethodNotSupportedException {
        synchronized (callListeners) {
            /* add to our list */
            if (!callListeners.contains(l)) {
                callListeners.addElement(l);
              this.startEvents();
            }
            /* and add to any existing calls */
            Call [] cs = getCalls();
            if (cs != null){
              for (int i = 0; i<cs.length;i++){
                    ((FreeCall)cs[i]).addCallListener(l, this);
              }
            }
        }
  }              
  public void addCallObserver(CallObserver observer) throws javax.telephony.ResourceUnavailableException, javax.telephony.MethodNotSupportedException {
	callObservers.add(observer);
	this.startEvents();
	
	/* and add to any existing calls */
	Call [] cs = getCalls();
	if (cs != null){
	  for (int i = 0; i<cs.length;i++){
		((FreeCall)cs[i]).addCallObserver(observer, this);
	  }
	}
  }              
  void addConnection(FreeConnection con) {
	Call call = con.getCall();
	CallListener[] cl = getCallListeners();
	if (cl != null){
            final FreeCall freeCall = (FreeCall) call;
	    for (CallListener current : cl) {
	        freeCall.addCallListener(current, this);
	    }
	}
	CallObserver[] cobs = getCallObservers();
	if (cobs != null) {
            final FreeCall freeCall = (FreeCall) call;
            for (CallObserver current : cobs) {
                freeCall.addCallObserver(current, this);
            }
	}
	final ConnectionHolder holder = new ConnectionHolder(con);
	connections.addElement(holder);
  }

/**
 * Add an old-style Observer to the Address object.
 * This will report Observation ended events and little else.
 * <P>Fixed casting: Loius Gibson, June 9, 2000
 *
 * @param observer The AddressObserver that will receive the update callbacks.
 * 
 **/
public void addObserver(AddressObserver observer) throws javax.telephony.ResourceUnavailableException, javax.telephony.MethodNotSupportedException {
	// check if we need protection
	if (observer != null) {
		this.protect();
		observers.addObserver(observer);
	}
  }            
  /**
   * this is an atypical fire method - in that it only fires on a single
   * listener, not all of them.
   */
  protected void fireAddressListenerEnded(AddressListener l ) {
	AddressEvent e = new FreeAddrObsEndedEv(Event.CAUSE_NORMAL,	// cause
			Ev.META_UNKNOWN,	// what is the meta-event
			false,				// is metaevent
			this);				// FreeAddress
	l.addressListenerEnded(e);
  }                
  public AddressCapabilities getAddressCapabilities(Terminal terminal) throws javax.telephony.InvalidArgumentException, javax.telephony.PlatformException {
	return getCapabilities();
  }        
  public AddressListener[] getAddressListeners() {
	  Vector<AddressListener> listeners = this.addressListeners;
	  if((listeners == null) || (listeners.size() == 0)) {
		  return null;
	  }
	  return listeners.toArray(new AddressListener[listeners.size()]);
  }        

  public CallListener[] getCallListeners() {
	  Vector<CallListener> listeners = this.callListeners;
	  if((listeners == null) || (listeners.size() == 0)) {
		  return null;
	  }
	  
      synchronized (listeners) {
    	  return listeners.toArray(new CallListener[listeners.size()]);
      }
  }

  public CallObserver[] getCallObservers() {
	  Vector<CallObserver> listeners = this.callObservers;
	  if((listeners == null) || (listeners.size() == 0)) {
		  return null;
	  }
	  
      synchronized (listeners) {
    	  return listeners.toArray(new CallObserver[listeners.size()]);
      }
  }

  /**
   * utility routine to get all calls associated with our connections
   */
  Call [] getCalls(){
	Call [] ret = null;
	Connection [] cons = this.getConnections();
	if (cons != null){
	  Vector<Call> v = new Vector<Call>(cons.length);
	  for (int i=0; i< cons.length; i++){
		Call ca = cons[i].getCall();
		if (ca != null){
		  v.addElement(ca);
		}
	  }
	  ret = new Call[v.size()];
	  v.copyInto(ret);
	}
	return ret;
  }        
 /**
  * Returns the dynamic capabilities for this instance of the Address object. Dynamic capabilities tell the application which actions are
  * possible at the time this method is invoked based upon the implementations knowledge of its ability to successfully perform the
  * action. This determination may be based upon argument passed to this method, the current state of the call model, or some
  * implementation-specific knowledge. These indications do not guarantee that a particular method will succeed when invoked,
  * however. 

  **/
  public AddressCapabilities getCapabilities() {
	return ((GenAddressCapabilities)this.getProvider().getAddressCapabilities()).getDynamic(this);
  }          
  /**
   * Transform the collection of weak ConnectionHolders into an array of Connection objects.
   **/
public Connection[] getConnections() {
	Connection[] ret = null;

	// check if we need to ask the raw TelephonyProvider to flush in my calls
	CallMgr cm = ((GenericProvider)this.getProvider()).getCallMgr();
	if (cm.isDynamic()) {
		cm.loadCalls(this);
	}

	// now dereference all my Connections
	synchronized (connections) {
            ret = new Connection[connections.size()];
            // check if the array is empty
            if (ret.length == 0) {
                return null;
            }
            Iterator<ConnectionHolder> it = connections.iterator();
            int i = 0;
            while (it.hasNext()) {
                ret[i] = ((ConnectionHolder) it.next()).getConnection();
                i++;
            }
	}
	return ret;
}
/**
 * Find or create a connection associated with a call
 * Creation date: (2000-02-15 13:38:51)
 * @author: Richard Deadman
 * @return A found or new Connection
 * @param call The call the connection should be hooked to
 */
FreeConnection getLazyConnection(FreeCall call) {
	// look for existing one
	Vector<ConnectionHolder> v = this.connections;
	ConnectionHolder testHolder = new ConnectionHolder(call.getCallID(), this.getName());
	if (v.contains(testHolder)) {
		// it's in there... find it
		Iterator<ConnectionHolder> it = v.iterator();
		while (it.hasNext()) {
			ConnectionHolder ch = (ConnectionHolder)it.next();
			if (ch.equals(testHolder))
				return ch.getConnection();
		}
	}
	// No connection found -- create a new one
	return new FreeConnection(call, this);
}
  public String getName() {
	return name;
  }        
  public AddressObserver[] getObservers() {
	return (AddressObserver[]) observers.getObjects();
  }        
/**
 * Get any PrivateData associated with my low-level object.
 */
public Object getPrivateData() {
	return ((GenericProvider)this.getProvider()).getRaw().getPrivateData(null, this.getName(), null);
}
  public Provider getProvider() {
	return provider;
  }        
/**
 * Return the set of Terminals associated with the Address.  IF these haven't been fetched yet,
 * ask the raw provider for them.
 **/
public Terminal[] getTerminals() {
	// check if we need to retrieve the Terminal list
	if (this.terminals == null) {
		synchronized (this) {
			// now double check
			if (this.terminals == null) {
				this.terminals = new HashSet<TermData>(1);
				try {
					TermData[] terms = ((GenericProvider) this.getProvider()).getRaw().getTerminals(this.getName());
					if (terms != null) {
						this.setTerminalData(terms);
					}
				} catch (InvalidArgumentException iae) {
					// no terminals -- but leave instantiated so we don't check again.
				}
			}
		}
	}
		// transform collection of Terminal names to array of terminals
	synchronized (terminals) {
		Terminal[] ret = null;
		if (this.terminals.isEmpty())
			return null;

		ret = new Terminal[terminals.size()];
		Iterator<TermData> it = terminals.iterator();
		int i = 0;
		while (it.hasNext()) {
			TermData termData = (TermData)it.next();
			ret[i] = ((GenericProvider) this.getProvider()).getDomainMgr().getLazyTerminal(termData.terminal, termData.isMedia);
			i++;
		}

		return ret;
	}
}
/**
 * Is this a local Address?
 * Creation date: (2000-06-22 9:44:13)
 * @author: Richard Deadman
 * @return true if the Address is in the Provider's domain.
 */
boolean isLocal() {
	return local;
}
  /**
   * Determine if I have any current Observers or Listeners.
   **/
private boolean isObserved() {
	// test if we should protect the Address
	if (((this.addressListeners == null) || (this.addressListeners.size() == 0)) &&
		((this.observers == null) || (this.observers.size() == 0))) {
		return false;
	} else {
		return true;
	}
}      
  /**
   * protect this address from garbage collection if dynamic Address memory is supported
   **/
private void protect() {
	// test if we are about to add the first observer
	if (!isObserved()) {
		((GenericProvider)this.getProvider()).getDomainMgr().protect(this);
	}
}      
   /**
	* Remove an AddressListener from the Address.
	* If this is the last listener or observer, allow the Address to be garbage collected if the domain
	* support its.
	**/
@SuppressWarnings("unchecked")
public synchronized void removeAddressListener(AddressListener l) {
	if (addressListeners != null && addressListeners.contains(l)) {
		Vector<AddressListener> v = (Vector<AddressListener>) addressListeners.clone();
		v.removeElement(l);
		addressListeners = v;
		fireAddressListenerEnded(l);

		// test if we can free for potential garbage collection
		this.unProtect();
	}
}
  @SuppressWarnings("unchecked")
public synchronized void removeCallListener(CallListener l) {
	if (callListeners.contains(l)) {
		Vector<CallListener> v = (Vector<CallListener>) callListeners.clone();
	  v.removeElement(l);
	  callListeners = v;
	  this.stopEvents();
	}
  }            
  public void removeCallObserver(CallObserver observer) {
	callObservers.remove(observer);
	this.stopEvents();
  }            
  boolean removeConnection(FreeConnection c) {
	return connections.removeElement(new ConnectionHolder(c));
  }          
  /**
   * Remove an old-style observer from the Address.
   * If this is the last listener or observer on the address, free if for garbage collection if soft
   * domain management is used.
   **/
public void removeObserver(AddressObserver observer) {
	if (observers.removeObserver(observer)) {
		sendAddrObservationEndedEv(observer);
		// check if we no longer need protection
		this.unProtect();
	}
}
/**
 * Forward the event off to all observers and listeners
 * Creation date: (2000-02-14 15:06:59)
 * @author: Richard Deadman
 * @param ev The event to forward
 */
public void send(FreeAddressEvent ev) {
	// send to observers
	this.sendToObservers(ev);

	// send to listeners
	Iterator<AddressListener> it = this.addressListeners.iterator();
	while (it.hasNext()) {
		AddressListener al = (AddressListener)it.next();
		if (ev.getID() == AddressEvent.ADDRESS_EVENT_TRANSMISSION_ENDED)
			al.addressListenerEnded(ev);
	}
}
  /**
   * this is an atypical send method - in that it only fires on a single
   * listener, not all of them.
   */
  protected void sendAddrObservationEndedEv(AddressObserver ao){
	AddrObservationEndedEv e[] = new AddrObservationEndedEv[1];
	e[0] = new FreeAddrObsEndedEv(Ev.CAUSE_NORMAL,	// cause
			Ev.META_UNKNOWN,	// what is the meta-event
			false,				// is metaevent
			this);				// FreeAddress
	ao.addressChangedEvent(e);
  }              
/**
 * Send PrivateData to my low-level object for processing.
 */
public java.lang.Object sendPrivateData(java.lang.Object data) {
	return ((GenericProvider)this.getProvider()).getRaw().sendPrivateData(null, this.getName(), null, data);
}
/**
 * Forward the event off to all observers.
 * Creation date: (2000-08-09 15:06:59)
 * @author: Richard Deadman
 * @param ev The event to forward
 */
void sendToObservers(FreeAddressEvent ev) {
	// send to observers
	FreeAddressEvent[] evs = {ev};
	this.observers.sendEvents(evs);
}
/**
 * Note if the Address is in the provider's domain
 * Creation date: (2000-06-22 9:44:13)
 * @author: Richard Deadman
 * @param newLocal true if a local Address, otherwise it represents an external participant in the call.
 */
private void setLocal(boolean newLocal) {
	this.local = newLocal;
}
  void setName(String newName) {
	name = newName;
  }        
/**
 * Set PrivateData to be used in the next low-level command.
 */
public void setPrivateData(java.lang.Object data) {
	((GenericProvider)this.getProvider()).getRaw().setPrivateData(null, this.getName(), null, data);
}
  void setProvider(javax.telephony.Provider newProvider) {
	provider = newProvider;
  }        
/**
 * Note the set of Terminal names associated with the Address.
 * Creation date: (2000-06-21 11:32:48)
 * @author: Richard Deadman
 * @param termNames An array or weak pointers (names) of associated Terminals.
 */
void setTerminalData(TermData[] termData) {
	Set<TermData> terms = this.terminals;
	if (terms == null) {
		synchronized (this) {
			// now double check
			if ((terms = this.terminals) == null) {
				terms = this.terminals = new HashSet<TermData>();
			}
		}
	}
	int size = termData.length;
	for (int i = 0; i < size; i++) {
		terms.add(termData[i]);
	}
}
/**
 * Called after Call Observers or Listeners are attached to the Address.
 * This will determine the current throttle state of the raw provider and update it
 * if necessary.
 * Creation date: (2000-05-04 15:23:56)
 * @author: Richard Deadman
 */
private synchronized void startEvents() throws ResourceUnavailableException {
	GenericProvider prov = null;
	if (!this.reporting) {
			if ((prov = (GenericProvider)this.getProvider()).getRawCapabilities().throttle) {
				try {
					prov.getRaw().reportCallsOnAddress(this.getName(), true);
				} catch (InvalidArgumentException iae) {
					// logic error!
					throw new RuntimeException("Error setting address reporting");
				}
			}
			this.reporting = true;
	}
}
/**
 * Called after Call Observers or Listeners are detatched from the Address.
 * This will determine the current throttle state of the raw provider and update it
 * if necessary.
 * Creation date: (2000-05-04 15:23:56)
 * @author: Richard Deadman
 */
private synchronized void stopEvents() {
	GenericProvider prov = null;
	if (this.reporting &&
		this.callListeners.size() == 0 &&
		this.callObservers.size() == 0) {
			if ((prov = (GenericProvider)this.getProvider()).getRawCapabilities().throttle) {
				try {
					prov.getRaw().reportCallsOnAddress(this.getName(), false);
				} catch (InvalidArgumentException iae) {
					// logic error!
					throw new RuntimeException("Error clearing address reporting");
				} catch (ResourceUnavailableException rue) {
					// eat it
				}
;
			}
			this.reporting = false;
	}
}
  /**
   * Unprotect this address from garbage collection if dynamic Address memory is supported
   **/
private void unProtect() {
	// test if we just removed the last observer
	if (!isObserved()) {
		((GenericProvider)this.getProvider()).getDomainMgr().unProtect(this);
	}
}      

	/**
	 * Describe myself
	 */
	public String toString() {
		return "Address: " + this.getName();
	}
}
