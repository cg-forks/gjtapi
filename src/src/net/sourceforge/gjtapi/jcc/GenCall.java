package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2002 Deadman Consulting (www.deadman.ca) 

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
import javax.csapi.cc.jcc.*;
import javax.jcat.JcatAddress;
import javax.jcat.JcatCall;
import javax.jcat.JcatConnection;
import javax.jcat.JcatTerminal;
import javax.jcat.JcatTerminalConnection;
import javax.jcat.JcatTerminalConnectionListener;
import javax.telephony.Connection;

import java.util.*;
/**
 * Wrapper for a Generic JTAPI Framework Call object to make it Jain Jcc compliant.
 *
 * <P>Note that the current implementation of release(int) is
 * not properly implemented due to an insufficient service provider SPI not providing sufficient information.
 * 
 * Creation date: (2000-10-10 12:42:59)
 * @author: Richard Deadman
 */
public class GenCall implements JccCall, JcatCall {

	/**
	 * Supervisor alarm.
	 * This is a thread that times when a supervisor ends on a call and executes a treatment.
	 **/
	public class Supervisor implements Runnable {
		private GenCall call;
		private JccCallListener listener;
		private double duration;
		private int treatment;
		public Supervisor(GenCall c, JccCallListener cl, double dur, int treat) {
			super();
			this.call = c;
			this.listener = cl;
			this.duration = dur;
			this.treatment = treat;
		}

		/**
		 * The thread is run when the call becomes active
		 **/
		public void run() {
			try {
				Thread.sleep((long)this.duration);
			} catch (InterruptedException ie) {
				// carry on
			}
			// Now do my treatment
			int treat = this.treatment;
			if ((treat & 4) > 0) {
				// send warning tone
				FreeCall fc = this.call.getFrameCall();
				((GenericProvider)fc.getProvider()).getRaw().beep(fc.getCallID());
			}
			if ((treat & 1) > 0) {
				try {
					call.release();
				} catch (Exception ex) {
					// ignore
				}
			}
			if ((treat & 2) > 0) {
				this.listener.callSuperviseEnd(new SuperviseEvent(this.call,
					JccCallEvent.CALL_SUPERVISE_END));
			}

			// now remove the listener
			this.call.removeCallListener(this.listener);
		}
	}

	public class SuperviseEvent implements JccCallEvent {
		private GenCall call;
		private int id;

		public SuperviseEvent(GenCall call, int eId) {
			super();

			this.call = call;
			this.id = eId;
		}

		public int getID() {
			return this.id;
		}

		public JccCall getCall() {
			return this.call;
		}

		public Object getSource() {
			return this.getCall();
		}

		public int getCause() {
				// must use JcpCallEvent constant to avoid Jcc 1.0b error
			return JccCallEvent.CAUSE_NORMAL;
		}
	}
	private Provider provider;
	private FreeCall frameCall;
	private Set<GenConnection> pendingConns = new HashSet<GenConnection>();
	private Set<Supervisor> waitingSupervisors = new HashSet<Supervisor>();
/**
 * GenCall constructor comment.
 */
public GenCall(Provider prov, FreeCall call) {
	super();

	this.setProvider(prov);
	this.setFrameCall(call);
}
/**
 * addCallListener method comment.
 */
public void addCallListener(JccCallListener listener) throws javax.csapi.cc.jcc.MethodNotSupportedException, javax.csapi.cc.jcc.ResourceUnavailableException {
	this.getFrameCall().addCallListener(new CallListenerAdapter((Provider)this.getProvider(), listener));
}/**
 * addConnectionListener method comment.
 */
public void addConnectionListener(JccConnectionListener cl, EventFilter fl) throws javax.csapi.cc.jcc.ResourceUnavailableException, javax.csapi.cc.jcc.MethodNotSupportedException {
	this.getFrameCall().addCallListener(new ConnListenerAdapter((Provider)this.getProvider(), cl, fl));
}
/**
 * Add a Connection to the list of those about to be routed.
 * Creation date: (2000-11-09 16:27:58)
 * @param conn com.uforce.jain.generic.GenConnection
 */
private void addPendingConn(GenConnection conn) {
	this.getPendingConns().add(conn);
}
/**
 * Here we return the single connection that is created from the originating address.
 */
public JccConnection createConnection(String targetAddress, String originatingAddress, String originalCalledAddress, String redirectingAddress) throws javax.csapi.cc.jcc.InvalidStateException, javax.csapi.cc.jcc.PrivilegeViolationException, javax.csapi.cc.jcc.MethodNotSupportedException, javax.csapi.cc.jcc.ResourceUnavailableException {
	Provider prov = this.getPrivateProvider();
	JccAddress target = null;
	GenAddress origAddr = null;
	JccAddress origCall = null;
	JccAddress redirect = null;
	try {
		target = (JccAddress)prov.getAddress(targetAddress);
		origAddr = (GenAddress)prov.getAddress(originatingAddress);
		if (originalCalledAddress != null)
			origCall = (JccAddress)prov.getAddress(originalCalledAddress);
		if (redirectingAddress != null)
			redirect = (JccAddress)prov.getAddress(redirectingAddress);
	} catch (InvalidPartyException iae) {
		throw new ResourceUnavailableException(
			ResourceUnavailableException.ORIGINATOR_UNAVAILABLE);
	}
	GenConnection conn = new GenConnection(prov, this, target, origAddr, origCall, redirect);
	this.addPendingConn(conn);
	return conn;
}

	/**
	 * 		Places a call from an originating address to a destination address string. 
		
		<p>The Call must be in the {@link JcpCall#IDLE} state (and therefore have no 
		existing associated JcpConnections and the Provider must be in the 
		{@link JcpProvider#IN_SERVICE} state. The successful effect of this method 
		is to place the call and create and return two JcpConnections associated with 
		this Call. 
		
		<h5>Method Arguments</h5>
		
		This method has two arguments. The first argument is the originating Address 
		for the Call. The second argument is a destination string whose value represents 
		the address to which the call is placed. This destination address must be valid and 
		complete. 
		
		<h5>Method Post-conditions</h5>
		
		This method returns successfully when the Provider can successfully initiate the 
		placing of the call. As a result, when the JccCall.connect() method returns, the JccCall 
		will be in the {@link JcpCall#ACTIVE} state and exactly two JccConnections will be 
		created and returned. The JccConnection associated with the originating endpoint is 
		the first JccConnection in the returned array.  This JccConnection will execute the 
		originating JccConnection's Final State Diagram (see 
		<a href="package-summary.html#TOConnections">table 3</a>). The JccConnection associated 
		with the destination endpoint is the second JccConnection in the returned array and 
		will execute the terminating JccConnection's Final State Diagram. These 
		two JccConnections must at least be in the {@link #IDLE} state. That is, if one of 
		the Connections progresses beyond the IDLE state while this method is completing, this
		Connection may be in a state other than the IDLE. This state must be reflected by an 
		event sent to the application. 
		
		<p><B>Pre-Conditions:</B> <OL>
		<LI>(this.getProvider()).getState() == JcpProvider.IN_SERVICE 
		<LI>this.getState() == JcpCall.IDLE 
		</OL>
		
		<B>Post-Conditions:</B> <OL>
		<LI>(this.getProvider()).getState() == JcpProvider.IN_SERVICE 
		<LI>this.getState() == JcpCall.ACTIVE 
		<LI>Let Connection c[] = this.getConnections() 
		<LI>c.length == 2 
		<LI>c[0].getState() == JcpConnection.IDLE (at least) 
		<LI>c[1].getState() == JcpConnection.IDLE (at least) 
		</OL>
		
		@param origaddr The originating Address for this call.
		@param dialedDigits The destination address string for this call.
		
		@return array of Connections
		
		@throws ResourceUnavailableException An internal resource necessary for placing 
		the call is unavailable.
		@throws PrivilegeViolationException The application does not have the proper 
		authority to place a call.
		@throws InvalidPartyException Either the originator or the destination does not 
		represent a valid party required to place a call.
		@throws InvalidStateException Some object required by this method is not in a valid 
		state as designated by the pre-conditions for this method.
		@throws MethodNotSupportedException The implementation does not support this method.
		@since 1.0a
	*/
	public JccConnection[] connect(JccAddress origaddr, String dialedDigits) throws 
	javax.csapi.cc.jcc.ResourceUnavailableException, javax.csapi.cc.jcc.PrivilegeViolationException, javax.csapi.cc.jcc.InvalidPartyException, 
	javax.csapi.cc.jcc.InvalidStateException, javax.csapi.cc.jcc.MethodNotSupportedException {
		
		Provider prov = this.getPrivateProvider();
		JccAddress target = null;
		try {
			target = (JccAddress)prov.getAddress(dialedDigits);
		} catch (javax.csapi.cc.jcc.InvalidPartyException iae) {
			throw new javax.csapi.cc.jcc.ResourceUnavailableException(
				javax.csapi.cc.jcc.ResourceUnavailableException.ORIGINATOR_UNAVAILABLE);
		}
		GenConnection conn1 = new GenConnection(prov, this, origaddr, (GenAddress)origaddr, null, null);
		GenConnection conn2 = new GenConnection(prov, this, target, (GenAddress)origaddr, null, null);
		this.addPendingConn(conn1);
		this.addPendingConn(conn2);
		
		// now complete the routing
		try {
			conn1.routeConnection(true);
		} catch (javax.csapi.cc.jcc.InvalidArgumentException iae) {	// is this a spec. bug?
			throw new javax.csapi.cc.jcc.InvalidStateException(conn1, javax.csapi.cc.jcc.InvalidStateException.CONNECTION_OBJECT, conn1.getState(), "trying to route connection");
		}
		try {
			conn2.routeConnection(true);
		} catch (javax.csapi.cc.jcc.InvalidArgumentException iae) {	// is this a spec. bug?
			throw new javax.csapi.cc.jcc.InvalidStateException(conn2, javax.csapi.cc.jcc.InvalidStateException.CONNECTION_OBJECT, conn2.getState(), "trying to route connection");
		}
		
		JccConnection jc[] = new JccConnection[2];
		jc[0] = conn1;
		jc[1] = conn2;
		
		return jc;
		
	}

/**
 * Compares two objects for equality. Returns a boolean that indicates
 * whether this object is equivalent to the specified object. This method
 * is used when an object is stored in a hashtable.
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals(Object obj) {
	if (obj instanceof GenCall) {
		return this.getFrameCall().equals(((GenCall)obj).getFrameCall());
	}
	return false;
}
/**
 * Return the set of routed or pending (not yet routed) connections.
 * This is synchronized to ensure that Connection.routeConnection() can transfer
 * a pending connection to a routed one without causing "getConnections()" errors.
 */
public synchronized JccConnection[] getConnections() {
	GenConnection[] conns = null;
	int routedSize = 0;
	javax.telephony.Connection[] frameConns = this.getFrameCall().getConnections();
	if (frameConns != null) {
		routedSize = frameConns.length;
		conns = new GenConnection[routedSize];
		for (int i = 0; i < routedSize; i++) {
			conns[i] = this.getPrivateProvider().findConnection((FreeConnection)frameConns[i]);
		}
	}

	// Now see if we have any pending connections to add (not yet routed to the fabric)
	Set<GenConnection> pending = this.getPendingConns();
	int pendSize = pending.size();
	if (pendSize > 0) {
		GenConnection[] routed = conns;
		conns = new GenConnection[routedSize + pendSize];
		int i = 0;
		for (; i < routedSize; i++) {
			conns[i] = routed[i];
		}
		Iterator<GenConnection> it = pending.iterator();
		while (it.hasNext()) {
			conns[i] = it.next();
			i++;
		}
	}
	return conns;
}
/**
 * Accessor for the Generic JTAPI Framework Call I wrap.
 * Creation date: (2000-10-10 12:45:02)
 * @return net.sourceforge.gjtapi.FreeCall
 */
net.sourceforge.gjtapi.FreeCall getFrameCall() {
	return frameCall;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-10 10:31:16)
 * @return java.util.Set
 */
private Set<GenConnection> getPendingConns() {
	return pendingConns;
}
/**
 * Internal accessor.
 * Creation date: (2000-10-30 11:34:12)
 * @return com.uforce.jain.generic.Provider
 */
private Provider getPrivateProvider() {
	return this.provider;
}
/**
 * getProvider method comment.
 */
public JccProvider getProvider() {
	return this.getPrivateProvider();
}
/**
 * Morph the JTAPI call state into a Jcc Call state
 */
public int getState() {
	switch (this.getFrameCall().getState()) {
		case javax.telephony.Call.IDLE: {
			return JccCall.IDLE;
		}
		case javax.telephony.Call.ACTIVE: {
			return JccCall.ACTIVE;
		}
		case javax.telephony.Call.INVALID: {
			return JccCall.INVALID;
		}
	}
	return JccCall.INVALID;
}
/**
 * Return the set of Supervisor Runnables that are invoked on a call when if goes active.
 * Creation date: (2000-11-10 14:37:52)
 * @return java.util.Set
 */
Set<Supervisor> getWaitingSupervisors() {
	return waitingSupervisors;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getFrameCall().hashCode();
}
/**
 * release method comment.
 */
public void release() throws javax.csapi.cc.jcc.InvalidStateException, javax.csapi.cc.jcc.PrivilegeViolationException, javax.csapi.cc.jcc.ResourceUnavailableException {
	
	try {
		this.getFrameCall().drop();
	} catch (javax.telephony.InvalidStateException ise) {
		throw new javax.csapi.cc.jcc.InvalidStateException(ise.getObject(),
								ise.getObjectType(),
								ise.getState(),
								ise.getMessage());
	} catch (javax.telephony.PrivilegeViolationException pve) {
		throw new javax.csapi.cc.jcc.PrivilegeViolationException(pve.getType(), pve.getMessage());
	} catch (javax.telephony.MethodNotSupportedException mnse) {
		throw new RuntimeException("Framework doesn't support drop but yet declares it does!");
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw new javax.csapi.cc.jcc.ResourceUnavailableException(rue.getType());
	}
}
    /**
        This method requests the release of the call object and associated connection
        objects. Thus this method is equivalent to using the {@link JccConnection#release(int)}
        method on each JccConnection which is part of the Call. Typically each JccConnection 
        associated with this call will move into the {@link JccConnection#DISCONNECTED} state. 
        The call will also be terminated in the network. If the application
        has registered as a listener then it receives the {@link JcpCallEvent#CALL_EVENT_TRANSMISSION_ENDED}
        event.  <p>
        Valid cause codes (prefixed by <code>CAUSE_</code>) for the integer that is 
        named causeCode are defined in {@link JcpEvent} and {@link JccCallEvent}.
        
        <P>Note: currently cuase codes are not properly reported in events due to a
        limitation in the CoreTpi. Causes are not sent to the underlying fabric.

       <p> <B>Pre-conditions:</B> <OL>
        <LI>(this.getProvider()).getState() == IN_SERVICE <br>
        <LI> this.getState() == ACTIVE
        </OL>
       <p> <B>Post-conditions:</B> <OL>
        <LI>(this.getProvider()).getState() == IN_SERVICE <br>
        <LI> this.getState() == INVALID
        <LI>CALL_EVENT_TRANSMISSION_ENDED event delivered to the 
        valid Calllisteners. 
        <LI>Appropriate ConnectionEvents are also delivered to the ConnectionListeners. 
        </OL>

       @param causeCode an integer that represents a cause code.  Valid values 
       are defined in {@link JcpEvent} and {@link JccCallEvent}, they are typically prefixed 
       by <code>CAUSE_</code>.

        @throws PrivilegeViolationException The application does not have 
        the authority or permission to disconnect the Call. For example, 
         an  Address associated with this Call may not be controllable 
        in the Provider's domain. 
        @throws ResourceUnavailableException An internal resource required 
        to drop a connection is not available. 
        @throws InvalidStateException Some object required for the 
        successful invocation of this method is not in the proper state as 
        given by this method's pre-conditions. 
		@throws InvalidArgumentException The given release cause code is invalid. 
		@since 1.0a
    */
    public void release(int causeCode) throws javax.csapi.cc.jcc.PrivilegeViolationException, 
    javax.csapi.cc.jcc.ResourceUnavailableException, javax.csapi.cc.jcc.InvalidStateException, javax.csapi.cc.jcc.InvalidArgumentException {
    	this.release();
    }
    
/**
 * removeCallListener method comment.
 */
public void removeCallListener(JccCallListener listener) {
	this.getFrameCall().removeCallListener(new CallListenerAdapter(this.getPrivateProvider(), listener));
}
/**
 * removeConnectionListener method comment.
 */
public void removeConnectionListener(JccConnectionListener cl) {
	this.getFrameCall().removeCallListener(new ConnListenerAdapter((Provider)this.getProvider(), cl, null));
}
/**
 * Remove a Connection to the list of those about to be routed.
 * Creation date: (2000-11-09 16:27:58)
 * @param conn com.uforce.jain.generic.GenConnection
 */
void removePendingConn(GenConnection conn) {
	this.getPendingConns().remove(conn);
}
/**
 * routeCall method comment.
 */
public JccConnection routeCall(String targetAddress, String originatingAddress, String originalDestinationAddress, String redirectingAddress) throws javax.csapi.cc.jcc.MethodNotSupportedException, javax.csapi.cc.jcc.ResourceUnavailableException, javax.csapi.cc.jcc.InvalidPartyException, javax.csapi.cc.jcc.InvalidArgumentException, javax.csapi.cc.jcc.InvalidStateException, javax.csapi.cc.jcc.PrivilegeViolationException {
	JccConnection conn = (JccConnection)this.createConnection(targetAddress, originatingAddress, originalDestinationAddress, redirectingAddress);

	conn.routeConnection(true);

	return conn;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-10 12:45:02)
 * @param newFrameCall net.sourceforge.gjtapi.FreeCall
 */
private void setFrameCall(net.sourceforge.gjtapi.FreeCall newFrameCall) {
	frameCall = newFrameCall;
}
/**
 * Set the Provider for the Jain Jcc Call.
 * Creation date: (2000-10-10 12:45:02)
 * @param prov The Provider that created this call.
 */
private void setProvider(Provider prov) {
	this.provider = prov;
}
/**
 * superviseCall method comment.
 */
public void superviseCall(JccCallListener cl, double time, int treatment, double bytes) throws javax.csapi.cc.jcc.MethodNotSupportedException {
	if (time == 0) {
		throw new javax.csapi.cc.jcc.MethodNotSupportedException("Volume based supervision not supported");
	}

	Supervisor supervisor = new Supervisor(this, cl, time, treatment);
	Set<Supervisor> sups = this.getWaitingSupervisors();
		// ensure we don't have a race condition with any installed SuperviseInstallers
	synchronized(sups) {
		int initialSize = sups.size();
		// add the new supervisor listeners
		cl.callSuperviseStart(new SuperviseEvent(this,
			JccCallEvent.CALL_SUPERVISE_START));
		sups.add(supervisor);

		// See if we can trigger the supervisor right away.
		if (this.getState() == JccCall.ACTIVE) {
			new Thread(supervisor).start();
		} else {
			if (initialSize == 0)
				try {
					this.addCallListener(new SuperviseInstaller());
				} catch (javax.csapi.cc.jcc.ResourceUnavailableException rue) {
					throw new javax.csapi.cc.jcc.MethodNotSupportedException("Could not listen for active event: " + rue);
				}
		}
	}
}
/**
 * Suppervise a call based on time
 */
public void superviseCall(JccCallListener cl, double time, int treatment) throws javax.csapi.cc.jcc.MethodNotSupportedException {
	this.superviseCall(cl, time, treatment, 0);
}

/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jcc Call for: " + this.getFrameCall().toString();
}
	/* (non-Javadoc)
	 * @see javax.jcat.JcatCall#addTerminalConnectionListener(javax.jcat.JcatTerminalConnectionListener)
	 */
	public void addTerminalConnectionListener(JcatTerminalConnectionListener termConnListener)
		throws MethodNotSupportedException, ResourceUnavailableException {
		this.getFrameCall().addCallListener(new TerminalConnectionListenerAdapter((Provider)this.getProvider(), termConnListener));

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatCall#blindTransfer(java.lang.String)
	 */
	public JcatConnection blindTransfer(String dialledDigits)
		throws
			InvalidArgumentException,
			InvalidStateException,
			InvalidPartyException,
			MethodNotSupportedException,
			PrivilegeViolationException,
			ResourceUnavailableException {
		try {
			Connection conn = this.getFrameCall().transfer(dialledDigits);
			return new GenConnection(this.getPrivateProvider(), conn);
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidArgumentException iae) {
			throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		} catch (javax.telephony.InvalidPartyException ipe) {
			throw new InvalidPartyException(ipe.getType(), ipe.getMessage());
		}
	}

	/**
	 * Conference two calls together.
	 * @see javax.jcat.JcatCall#conference(javax.jcat.JcatCall)
	 */
	public void conference(JcatCall otherCall)
		throws
			InvalidArgumentException,
			InvalidStateException,
			MethodNotSupportedException,
			PrivilegeViolationException,
			ResourceUnavailableException {
		try {
			this.getFrameCall().conference(((GenCall)otherCall).getFrameCall());
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidArgumentException iae) {
			throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		}
	}

	/**
	 * Connect a call from the local address/terminal pair
	 * to a remote set of digits.
	 * @see javax.jcat.JcatCall#connect(javax.jcat.JcatTerminal, javax.jcat.JcatAddress, java.lang.String)
	 */
	public JcatConnection[] connect(
		JcatTerminal term,
		JcatAddress addr,
		String dialedDigits)
		throws
			ResourceUnavailableException,
			PrivilegeViolationException,
			InvalidPartyException,
			InvalidStateException,
			MethodNotSupportedException {
		Provider prov = this.provider;
		JcatConnection[] jconns = null;
		try {
			Connection[] conns = this.getFrameCall().connect(((GenTerminal)term).getFrameTerm(),
									((GenAddress)addr).getFrameAddr(),
									dialedDigits);
			int len = conns.length;
			jconns = new JcatConnection[len];
			for (int i = 0; i < len; i++) {
				jconns[i] = prov.findConnection((FreeConnection)conns[i]);
			}
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidArgumentException iae) {
			//throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidPartyException ipe) {
			throw new InvalidPartyException(ipe.getType());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		}
		return jconns;
	}

	/**
	 * Consult with another address while puting the first call on hold.
	 * @see javax.jcat.JcatCall#consult(javax.jcat.JcatTerminalConnection, java.lang.String)
	 */
	public JcatConnection[] consult(
		JcatTerminalConnection termconn,
		String dialedDigits)
		throws
			InvalidArgumentException,
			InvalidPartyException,
			InvalidStateException,
			MethodNotSupportedException,
			PrivilegeViolationException,
			ResourceUnavailableException {
		Provider prov = this.provider;
		JcatConnection[] jconns = null;
		try {
			Connection[] conns = this.getFrameCall().consult(((GenTerminalConnection)termconn).getFrameTC(),
									dialedDigits);
			int len = conns.length;
			jconns = new JcatConnection[len];
			for (int i = 0; i < len; i++) {
				jconns[i] = prov.findConnection((FreeConnection)conns[i]);
			}
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidArgumentException iae) {
			throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidPartyException ipe) {
			throw new InvalidPartyException(ipe.getType());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		}
		return jconns;
	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatCall#consultTransfer(javax.jcat.JcatCall)
	 */
	public void consultTransfer(JcatCall otherCall)
		throws
			InvalidArgumentException,
			InvalidPartyException,
			InvalidStateException,
			MethodNotSupportedException,
			PrivilegeViolationException,
			ResourceUnavailableException {
		// we can only do this if we have a conference controller
		try {
			// First we conference the calls together, and then the transfer controller drops of the call
			this.getFrameCall().transfer(((GenCall)otherCall).getFrameCall());
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidArgumentException iae) {
			throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.InvalidPartyException ipe) {
			throw new InvalidPartyException(ipe.getType(), ipe.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		}

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatCall#getConferenceController()
	 */
	public JcatTerminalConnection getConferenceController() {
		return this.provider.findTerminalConnection((FreeTerminalConnection)this.getFrameCall().getConferenceController());
	}

	/** Is conferencing allowed on this call?
	 * @see javax.jcat.JcatCall#getConferenceEnable()
	 */
	public boolean getConferenceEnable() {
		return this.getFrameCall().getConferenceEnable();
	}

	/**
	 * Get the TerminalConnection set as the transfer controller.
	 * This is found by looking for the GJTAPI conference controller
	 * and then finding the wrapper from the provider.
	 * @see javax.jcat.JcatCall#getTransferController()
	 */
	public JcatTerminalConnection getTransferController() {
		return this.provider.findTerminalConnection((FreeTerminalConnection)this.getFrameCall().getTransferController());
	}

	/**
	 * Test if transfer is supported on the JTAPI terminal connection I wrap.
	 * @see javax.jcat.JcatCall#getTransferEnable()
	 */
	public boolean getTransferEnable() {
		return this.getFrameCall().getTransferEnable();
	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatCall#removeTerminalConnectionListener(javax.jcat.JcatTerminalConnectionListener)
	 */
	public void removeTerminalConnectionListener(JcatTerminalConnectionListener terminalConnectionListener) {
		this.getFrameCall().removeCallListener(new TerminalConnectionListenerAdapter((Provider)this.getProvider(), terminalConnectionListener));

	}

	/**
	 * Set the Conference controller by looking under the
	 * wrappers.
	 * @see javax.jcat.JcatCall#setConferenceController(javax.jcat.JcatTerminalConnection)
	 */
	public void setConferenceController(JcatTerminalConnection tc)
		throws
			InvalidArgumentException,
			InvalidStateException,
			MethodNotSupportedException,
			ResourceUnavailableException {
		try {
			this.getFrameCall().setTransferController(((GenTerminalConnection)tc).getFrameTC());
		} catch (javax.telephony.InvalidArgumentException iae) {
			throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		}
	}

	/**
	 * Set the ConferenceEnabled flag for the underlying system.
	 * @see javax.jcat.JcatCall#setConferenceEnable(boolean)
	 */
	public void setConferenceEnable(boolean enabled)
		throws
			InvalidArgumentException,
			InvalidStateException,
			MethodNotSupportedException,
			ResourceUnavailableException {
		try {
			this.getFrameCall().setConferenceEnable(enabled);
		} catch (javax.telephony.InvalidArgumentException iae) {
			throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.PrivilegeViolationException pve) {
			// type descrepency...
			throw new ResourceUnavailableException(pve.getType());
		}

	}

	/**
	 * Set the TransferController that the framework uses.
	 * @see javax.jcat.JcatCall#setTransferController(javax.jcat.JcatTerminalConnection)
	 */
	public void setTransferController(JcatTerminalConnection termconn)
		throws
			InvalidArgumentException,
			InvalidStateException,
			MethodNotSupportedException,
			ResourceUnavailableException {
		try {
			this.getFrameCall().setTransferController(((GenTerminalConnection)termconn).getFrameTC());
		} catch (javax.telephony.InvalidArgumentException iae) {
			throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		}

	}

	/**
	 * Set the flag for if transfer is enabled.
	 * @see javax.jcat.JcatCall#setTransferEnable(boolean)
	 */
	public void setTransferEnable(boolean enabled)
		throws
			InvalidArgumentException,
			InvalidStateException,
			MethodNotSupportedException,
			ResourceUnavailableException {
		try {
			this.getFrameCall().setTransferEnable(enabled);
		} catch (javax.telephony.InvalidArgumentException iae) {
			throw new InvalidArgumentException(iae.getMessage());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		} catch (javax.telephony.PrivilegeViolationException pve) {
			// type descrepency...
			throw new ResourceUnavailableException(pve.getType());
		}

	}

}
