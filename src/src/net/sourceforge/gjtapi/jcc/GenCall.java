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
import jain.application.services.jcp.*;
import javax.telephony.Address;
import net.sourceforge.gjtapi.*;
import jain.application.services.jcc.*;
import java.util.*;
/**
 * Wrapper for a Generic JTAPI Framework Call object to make it Jain Jcc compliant.
 * Creation date: (2000-10-10 12:42:59)
 * @author: Richard Deadman
 */
public class GenCall implements JccCall {

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

		public JcpCall getCall() {
			return this.call;
		}

		public Object getSource() {
			return this.getCall();
		}

		public int getCause() {
			return this.CAUSE_NORMAL;
		}
	}
	private Provider provider;
	private FreeCall frameCall;
	private Set pendingConns = new HashSet();
	private Set waitingSupervisors = new HashSet();
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
public void addCallListener(jain.application.services.jcc.JccCallListener cl, EventFilter fl) throws jain.application.services.jcp.MethodNotSupportedException, jain.application.services.jcp.ResourceUnavailableException {
	this.getFrameCall().addCallListener(new CallListenerAdapter((Provider)this.getProvider(), cl, fl));
}
/**
 * addCallListener method comment.
 */
public void addCallListener(jain.application.services.jcp.JcpCallListener listener) throws jain.application.services.jcp.MethodNotSupportedException, jain.application.services.jcp.ResourceUnavailableException {
	this.getFrameCall().addCallListener(new CallListenerAdapter((Provider)this.getProvider(), listener, null));
}
/**
 * addConnectionListener method comment.
 */
public void addConnectionListener(jain.application.services.jcc.JccConnectionListener cl, EventFilter fl) throws jain.application.services.jcp.ResourceUnavailableException, jain.application.services.jcp.MethodNotSupportedException {
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
public jain.application.services.jcc.JccConnection createConnection(String targetAddress, String originatingAddress, String originalCalledAddress, String redirectingAddress) throws jain.application.services.jcp.InvalidStateException, jain.application.services.jcp.PrivilegeViolationException, jain.application.services.jcp.MethodNotSupportedException, jain.application.services.jcp.ResourceUnavailableException {
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
public synchronized jain.application.services.jcp.JcpConnection[] getConnections() {
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
	Set pending = this.getPendingConns();
	int pendSize = pending.size();
	if (pendSize > 0) {
		GenConnection[] routed = conns;
		conns = new GenConnection[routedSize + pendSize];
		int i = 0;
		for (; i < routedSize; i++) {
			conns[i] = routed[i];
		}
		Iterator it = pending.iterator();
		while (it.hasNext()) {
			conns[i] = (GenConnection)it.next();
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
private java.util.Set getPendingConns() {
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
public jain.application.services.jcp.JcpProvider getProvider() {
	return this.getPrivateProvider();
}
/**
 * Morph the JTAPI call state into a Jcc Call state
 */
public int getState() {
	switch (this.getFrameCall().getState()) {
		case javax.telephony.Call.IDLE: {
			return this.IDLE;
		}
		case javax.telephony.Call.ACTIVE: {
			return this.ACTIVE;
		}
		case javax.telephony.Call.INVALID: {
			return this.INVALID;
		}
	}
	return this.INVALID;
}
/**
 * Return the set of Supervisor Runnables that are invoked on a call when if goes active.
 * Creation date: (2000-11-10 14:37:52)
 * @return java.util.Set
 */
java.util.Set getWaitingSupervisors() {
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
public void release() throws jain.application.services.jcp.InvalidStateException, jain.application.services.jcp.PrivilegeViolationException, jain.application.services.jcp.ResourceUnavailableException {
	
	try {
		this.getFrameCall().drop();
	} catch (javax.telephony.InvalidStateException ise) {
		throw new jain.application.services.jcp.InvalidStateException(ise.getObject(),
								ise.getObjectType(),
								ise.getState(),
								ise.getMessage());
	} catch (javax.telephony.PrivilegeViolationException pve) {
		throw new jain.application.services.jcp.PrivilegeViolationException(pve.getType(), pve.getMessage());
	} catch (javax.telephony.MethodNotSupportedException mnse) {
		throw new RuntimeException("Framework doesn't support drop but yet declares it does!");
	} catch (javax.telephony.ResourceUnavailableException rue) {
		throw new jain.application.services.jcp.ResourceUnavailableException(rue.getType());
	}
}
/**
 * removeCallListener method comment.
 */
public void removeCallListener(jain.application.services.jcp.JcpCallListener listener) {
	this.getFrameCall().removeCallListener(new CallListenerAdapter(this.getPrivateProvider(), listener, null));
}
/**
 * removeConnectionListener method comment.
 */
public void removeConnectionListener(jain.application.services.jcc.JccConnectionListener cl) {
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
public jain.application.services.jcc.JccConnection routeCall(String targetAddress, String originatingAddress, String originalDestinationAddress, String redirectingAddress) throws jain.application.services.jcp.MethodNotSupportedException, jain.application.services.jcp.ResourceUnavailableException, jain.application.services.jcp.InvalidPartyException, jain.application.services.jcp.InvalidArgumentException, jain.application.services.jcp.InvalidStateException, jain.application.services.jcp.PrivilegeViolationException {
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
public void superviseCall(JccCallListener cl, double time, int treatment, double bytes) throws jain.application.services.jcp.MethodNotSupportedException {
	if (time == 0) {
		throw new MethodNotSupportedException("Volume based supervision not supported");
	}

	Supervisor supervisor = new Supervisor(this, cl, time, treatment);
	Set sups = this.getWaitingSupervisors();
		// ensure we don't have a race condition with any installed SuperviseInstallers
	synchronized(sups) {
		int initialSize = sups.size();
		// add the new supervisor listeners
		cl.callSuperviseStart(new SuperviseEvent(this,
			JccCallEvent.CALL_SUPERVISE_START));
		sups.add(supervisor);

		// See if we can trigger the supervisor right away.
		if (this.getState() == JcpCall.ACTIVE) {
			new Thread(supervisor).start();
		} else {
			if (initialSize == 0)
				try {
					this.addCallListener(new SuperviseInstaller());
				} catch (ResourceUnavailableException rue) {
					throw new MethodNotSupportedException("Could not listen for active event: " + rue);
				}
		}
	}
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jcc Call for: " + this.getFrameCall().toString();
}
}
