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
import net.sourceforge.gjtapi.media.*;
import javax.telephony.media.*;
import net.sourceforge.gjtapi.*;
import jain.application.services.jcc.*;
import jain.application.services.jcp.*;
import javax.telephony.*;
/**
 * Jain Jcc Connection adapter for a Generic JTAPI Connection
 * Creation date: (2000-10-10 13:48:56)
 * @author: Richard Deadman
 */
public class GenConnection implements JccConnection {
	private Provider prov = null;
	private Connection frameConn;
	/**
	 * Call
	 * This is the lazily instantiated handle to the JccCall I am connected to.
	 **/
	private JccCall call = null;
	/**
	 * Address
	 * This is the JcpAddress that I am associated with.  This is used to figure out where
	 * to route the call later.
	 **/
	private JcpAddress address = null;
	/**
	 * Blocking flag
	 * <P>This indicates whether the Connection is currently blocked from further processing.
	 * This is generally set by a EventFilter returning EVENT_BLOCK for an event.
	 * Once blocked, actions on the Connection cannot take place until a "continueProcessing()"
	 * message is sent.  Currently we do not support blocking time-outs.
	 **/
	private boolean blocked = false;
	/**
	 * LastAddr
	 * <P>This is the last redirected address for this part of the call before it was redirected.
	 * It's not clear why this isn't a Call property, but we assume that when the documentation
	 * states "the last redirected JcpAddress associated with the JcpCall" it really means the
	 * series of redirected call legs.
	 **/
	private JccAddress lastAddr = null;
	/**
	 * OriginalAddress
	 * <P>This is the original address for this part of the call before it was transferred.
	 * It's not clear why this isn't a Call property, but we assume that when the documentation
	 * states "the original JcpAddress associated with the JcpCall" it really means the series
	 * of routed call legs.
	 **/
	private JccAddress origAddr = null;
	/**
	 * CallingAddr
	 * This is the source address for the connection, if one exists.
	 **/
	private GenAddress callingAddr = null;
	/**
	 * routeAddreses
	 * This is a  route addresses that is appended to the Connection address
	 * when trying to route a call.
	 **/
	private String routeAddress = null;
	
	private int state = 0;
/**
 * This is created by a Call.createConnection, and leaves the Connection ready to be routed.
 * Creation date: (2000-11-09 15:11:29)
 * @param targetAddress java.lang.String
 * @param originatingAddress java.lang.String
 * @param redirectingAddress java.lang.String
 */
public GenConnection(Provider prov, JccCall call, JccAddress addr, GenAddress originatingAddress, JccAddress originalCalledAddress, JccAddress redirectingAddress) {
	super();

	this.setProv(prov);
	this.setCall(call);
	this.setAddress(addr);
	this.setCallingAddr(originatingAddress);
	this.setOrigAddr(originalCalledAddress);
	this.setLastAddr(redirectingAddress);
}
/**
 * GenConnection constructor comment.
 */
public GenConnection(Provider prov, Connection conn) {
	super();

	this.setProv(prov);
	this.setFrameConn(conn);
}
/**
 * answer method comment.
 */
public void answer() {
	// unblock
	this.continueProcessing();
	
	FreeTerminalConnection[] tcs = (FreeTerminalConnection[])this.getFrameConn().getTerminalConnections();
	if (tcs.length > 0)
		try {
			tcs[0].answer();
		} catch (Exception e) {
			throw new RuntimeException("Could not answer do to " + e);
		}
}
/**
 * attachMedia method comment.
 */
public void attachMedia() {
	// unblock
	this.continueProcessing();
	
	this.attachMedia(true);
}
/**
 * attachMedia method comment.
 */
private void attachMedia(boolean flag) {
	this.getProv().getGenProv().getRaw().attachMedia(((GenCall)this.getCall()).getFrameCall().getCallID(),
		this.getAddress().getName(),
		flag);
}
/**
 * Enable call processing to continue on this connection.
 */
public void continueProcessing() {
	this.setBlocked(false);
}
/**
 * detachMedia method comment.
 */
public void detachMedia() {
	// unblock
	this.continueProcessing();
	
	this.attachMedia(false);
}
/**
 * Am I sematically equal to the other object.
 * Creation date: (2000-10-10 13:51:57)
 * @return boolean
 * @param other java.lang.Object
 */
public boolean equals(Object other) {
	if (other instanceof GenConnection) {
		GenConnection gc = (GenConnection)other;
		return ((this.getCall().equals(gc.getCall())) &&
			(this.getAddress().equals(gc.getAddress())));
	}
	return false;
}
/**
 * getAddress method comment.
 */
public synchronized jain.application.services.jcp.JcpAddress getAddress() {
	if (this.address == null) {
		this.address = this.getProv().findAddress((FreeAddress)this.getFrameConn().getAddress());
	}
	return this.address;
}
/**
 * getCall method comment.
 */
public synchronized jain.application.services.jcp.JcpCall getCall() {
	if (this.call == null) {
		this.call = this.getProv().findCall((FreeCall)this.getFrameConn().getCall());
	}
	return this.call;
}
/**
 * Internal accessor.
 * Creation date: (2000-11-09 15:23:18)
 * @return java.lang.String
 */
private GenAddress getCallingAddr() {
	return callingAddr;
}
/**
 * getDestinationAddress method comment.
 */
public String getDestinationAddress() {
	int state = this.getJccState();
	if ((state == ADDRESS_COLLECT) ||
		(state == ADDRESS_ANALYZE) ||
		(state == CALL_DELIVERY)) {
			if (this.getRouteAddress() != null)
				return this.getRouteAddress();
			else
				return this.getAddress().getName();
		}
	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-10 13:54:27)
 * @return javax.telephony.Connection
 */
private javax.telephony.Connection getFrameConn() {
	return frameConn;
}
/**
 * getJccState method comment.
 */
public int getJccState() {
	int jtapiState = this.getFrameConn().getState();
	switch (jtapiState) {
		case Connection.ALERTING: {
			return JccConnection.ALERTING;
		} 
		case Connection.CONNECTED: {
			if (this.state == JccConnection.SUSPENDED)
				return this.state;
			return JccConnection.CONNECTED;
		} 
		case Connection.DISCONNECTED: {
			return JccConnection.DISCONNECTED;
		} 
		case Connection.FAILED: {
			return JccConnection.FAILED;
		} 
		case Connection.IDLE: {
			return JccConnection.IDLE;
		} 
		case Connection.INPROGRESS: {
			int jccState = this.state;
			if ((jccState == JccConnection.ADDRESS_ANALYZE) ||
				(jccState == JccConnection.ADDRESS_COLLECT) ||
				(jccState == JccConnection.AUTHORIZE_CALL_ATTEMPT) ||
				(jccState == JccConnection.CALL_DELIVERY))
				return jccState;
			return JccConnection.INPROGRESS;
		} 
		case Connection.UNKNOWN: {
			return JccConnection.UNKNOWN;
		} 
		default: {
			return JccConnection.UNKNOWN;
		}
	}
}
/**
 * Internal accessor for the address that I will be routed to.
 * Creation date: (2000-11-09 15:23:18)
 * @return java.lang.String
 */
private JcpAddress getJcpAddress() {
	return address;
}
/**
 * getLastAddr method comment.
 */
public String getLastAddress() {
	return this.lastAddr.getName();
}
/**
 * getMoreDialledDigits method comment.
 */
public java.lang.String getMoreDialedDigits() {
	return this.getProv().getGenProv().getRaw().getDialledDigits(((GenCall)this.getCall()).getFrameCall().getCallID(),
		this.getAddress().getName());
}
/**
 * getOriginalAddress method comment.
 */
public String getOriginalAddress() {
	return this.origAddr.getName();
}
/**
 * getOriginalAddress method comment.
 */
public jain.application.services.jcc.JccAddress getOriginatingAddress() {
	return this.getCallingAddr();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-09 15:32:29)
 * @return jain.application.services.jcc.JccCall
 */
private jain.application.services.jcc.JccCall getPrivateCall() {
	return call;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 12:55:56)
 * @return com.uforce.jain.generic.Provider
 */
private Provider getProv() {
	return prov;
}
/**
 * Internal accessor for the route address.
 * Creation date: (2001-01-24 10:39:17)
 * @return java.lang.String
 */
private java.lang.String getRouteAddress() {
	return routeAddress;
}
/**
 * getState method comment.
 */
public int getState() {
	int jtapiState = this.getFrameConn().getState();
	switch (jtapiState) {
		case Connection.ALERTING: {
			return JcpConnection.ALERTING;
		} 
		case Connection.CONNECTED: {
			return JcpConnection.CONNECTED;
		} 
		case Connection.DISCONNECTED: {
			return JcpConnection.DISCONNECTED;
		} 
		case Connection.FAILED: {
			return JcpConnection.FAILED;
		} 
		case Connection.IDLE: {
			return JcpConnection.IDLE;
		} 
		case Connection.INPROGRESS: {
			return JcpConnection.INPROGRESS;
		} 
		case Connection.UNKNOWN: {
			return JcpConnection.UNKNOWN;
		} 
		default: {
			return JccConnection.UNKNOWN;
		}
	}
}
/**
 * Combination of Provider, call and address hashcodes
 * Creation date: (2000-10-10 13:50:25)
 * @return int
 */
public int hashCode() {
	return this.getCall().hashCode() + this.getAddress().hashCode();
}
/**
 * Am I blocked from futher processing?
 */
public boolean isBlocked() {
	return this.blocked;
}
/**
 * release method comment.
 */
public void release() throws jain.application.services.jcp.InvalidStateException, jain.application.services.jcp.PrivilegeViolationException, jain.application.services.jcp.ResourceUnavailableException {
	// unblock
	this.continueProcessing();
	
	try {
		this.getFrameConn().disconnect();
	} catch (javax.telephony.InvalidStateException ise) {
		throw new jain.application.services.jcp.InvalidStateException(this.getFrameConn(),
			jain.application.services.jcp.InvalidStateException.CONNECTION_OBJECT,
			this.getFrameConn().getState(),
			ise.getMessage());
	} catch (javax.telephony.PrivilegeViolationException pve) {
		throw new jain.application.services.jcp.PrivilegeViolationException(
			jain.application.services.jcp.PrivilegeViolationException.ORIGINATOR_VIOLATION,
			pve.getMessage());
	} catch (javax.telephony.ResourceUnavailableException rue) {
		int exType = rue.getType();
		int newType;
		switch (exType) {
			case javax.telephony.ResourceUnavailableException.NO_DIALTONE: {
				newType = jain.application.services.jcp.ResourceUnavailableException.NO_DIALTONE;
				break;
			} 
			case javax.telephony.ResourceUnavailableException.OBSERVER_LIMIT_EXCEEDED: {
				newType = jain.application.services.jcp.ResourceUnavailableException.NO_DIALTONE;
				break;
			} 
			case javax.telephony.ResourceUnavailableException.ORIGINATOR_UNAVAILABLE: {
				newType = jain.application.services.jcp.ResourceUnavailableException.NO_DIALTONE;
				break;
			} 
			case javax.telephony.ResourceUnavailableException.OUTSTANDING_METHOD_EXCEEDED: {
				newType = jain.application.services.jcp.ResourceUnavailableException.NO_DIALTONE;
				break;
			} 
			case javax.telephony.ResourceUnavailableException.TRUNK_LIMIT_EXCEEDED: {
				newType = jain.application.services.jcp.ResourceUnavailableException.NO_DIALTONE;
				break;
			} 
			case javax.telephony.ResourceUnavailableException.UNSPECIFIED_LIMIT_EXCEEDED: {
				newType = jain.application.services.jcp.ResourceUnavailableException.NO_DIALTONE;
				break;
			} 
			case javax.telephony.ResourceUnavailableException.USER_RESPONSE: {
				newType = jain.application.services.jcp.ResourceUnavailableException.NO_DIALTONE;
				break;
			} 
			default: {
				newType = jain.application.services.jcp.ResourceUnavailableException.UNKNOWN;
			}
		}
		throw new jain.application.services.jcp.ResourceUnavailableException(newType);
	} catch (javax.telephony.MethodNotSupportedException mnse) {
		throw new RuntimeException("Release failed due to " + mnse);
	}
}
/**
 * routeConnection method comment.
 */
public void routeConnection(boolean attachMedia)
	throws 
		jain.application.services.jcp.MethodNotSupportedException, 
		jain.application.services.jcp.ResourceUnavailableException, 
		jain.application.services.jcp.InvalidPartyException, 
		jain.application.services.jcp.InvalidArgumentException, 
		jain.application.services.jcp.InvalidStateException, 
		jain.application.services.jcp.PrivilegeViolationException {
	// unblock
	this.continueProcessing();

	// Get the calling address and called address name
	GenAddress caller = this.getCallingAddr();
	Address jtapiCaller = null;
	if (caller != null)
		jtapiCaller = caller.getFrameAddr();
	String calledAddr = this.getAddress().getName();

	// Get the call that this is associated with
	Connection[] conns = null;
	GenCall call = (GenCall) this.getCall();
	FreeCall fCall = call.getFrameCall();
	Terminal firstTerm = jtapiCaller.getTerminals()[0];

	// See if routing is requested
	String route = this.getRouteAddress();
	if (route == null)
		route = calledAddr;
	boolean partyFailed = false;
	try {
		// synchronize on the GenCall so that we ensure these methods and "getConnections()"
		// do not run at the same time.
		synchronized (call) {
			conns = (Connection[]) fCall.connect(firstTerm, jtapiCaller, route);
			call.removePendingConn(this);
		}
	} catch (javax.telephony.InvalidStateException ise) {
		throw new jain.application.services.jcp.InvalidStateException(
			fCall, 
			ise.getObjectType(), 
			ise.getState(), 
			ise.getMessage()); 
	} catch (javax.telephony.InvalidPartyException ise) {
		partyFailed = true;
	} catch (javax.telephony.PrivilegeViolationException pve) {
		int type = pve.getType();
		int newType;
		switch (type) {
			case javax.telephony.PrivilegeViolationException.DESTINATION_VIOLATION :
				{
					newType = 
						jain.application.services.jcp.PrivilegeViolationException.DESTINATION_VIOLATION; 
				}
			case javax.telephony.PrivilegeViolationException.ORIGINATOR_VIOLATION :
				{
					newType = 
						jain.application.services.jcp.PrivilegeViolationException.ORIGINATOR_VIOLATION; 
				}
			default :
				{
					newType = 
						jain.application.services.jcp.PrivilegeViolationException.UNKNOWN_VIOLATION; 
				}
		}
		throw new jain.application.services.jcp.PrivilegeViolationException(
			newType, 
			pve.getMessage()); 
	} catch (javax.telephony.ResourceUnavailableException rue) {
		int type = rue.getType();
		int newType;
		switch (type) {
			case javax.telephony.ResourceUnavailableException.NO_DIALTONE :
				{
					newType = 
						jain.application.services.jcp.ResourceUnavailableException.NO_DIALTONE; 
				}
			case javax.telephony.ResourceUnavailableException.OBSERVER_LIMIT_EXCEEDED :
				{
					newType = 
						jain
							.application
							.services
							.jcp
							.ResourceUnavailableException
							.OBSERVER_LIMIT_EXCEEDED; 
				}
			case javax.telephony.ResourceUnavailableException.ORIGINATOR_UNAVAILABLE :
				{
					newType = 
						jain
							.application
							.services
							.jcp
							.ResourceUnavailableException
							.ORIGINATOR_UNAVAILABLE; 
				}
			case javax.telephony.ResourceUnavailableException.OUTSTANDING_METHOD_EXCEEDED :
				{
					newType = 
						jain
							.application
							.services
							.jcp
							.ResourceUnavailableException
							.OUTSTANDING_METHOD_EXCEEDED; 
				}
			case javax.telephony.ResourceUnavailableException.TRUNK_LIMIT_EXCEEDED :
				{
					newType = 
						jain.application.services.jcp.ResourceUnavailableException.TRUNK_LIMIT_EXCEEDED; 
				}
			case javax.telephony.ResourceUnavailableException.UNSPECIFIED_LIMIT_EXCEEDED :
				{
					newType = 
						jain
							.application
							.services
							.jcp
							.ResourceUnavailableException
							.UNSPECIFIED_LIMIT_EXCEEDED; 
				}
			case javax.telephony.ResourceUnavailableException.USER_RESPONSE :
				{
					newType = 
						jain.application.services.jcp.ResourceUnavailableException.USER_RESPONSE; 
				}
			default :
				{
					newType = jain.application.services.jcp.ResourceUnavailableException.UNKNOWN;
				}
		}
		throw new jain.application.services.jcp.ResourceUnavailableException(newType);
	} catch (javax.telephony.InvalidArgumentException iae) {
		throw new jain.application.services.jcp.InvalidArgumentException(
			iae.getMessage()); 
	} catch (javax.telephony.MethodNotSupportedException mnse) {
		throw new jain.application.services.jcp.MethodNotSupportedException(
			mnse.getMessage()); 
	}

	// now see which connection is our new connection
	if (!partyFailed)
		for (int j = 0; j < conns.length; j++) {
			if (conns[j].getAddress().getName().equals(calledAddr))
				this.setFrameConn(conns[j]);
		}

	// see if we failed to route the call
	if (partyFailed == true)
		throw new jain.application.services.jcp.InvalidPartyException(
			jain.application.services.jcp.InvalidPartyException.DESTINATION_PARTY, 
			"All selected routes failed: " + route); 

	// now attach the media if necessary
	if (attachMedia)
		this.attachMedia();
}
/**
 * selectRoute method comment.
 */
public void selectRoute(java.lang.String address) throws jain.application.services.jcp.MethodNotSupportedException {
	this.setRouteAddress(address);
}
/**
 * Set the JcpAddress that I am or will be routed to.
 * Creation date: (2000-11-09 15:23:18)
 * @param newAddress java.lang.String
 */
private void setAddress(JcpAddress newAddress) {
	address = newAddress;
}
/**
 * Turn blocking on or off.
 * Creation date: (2000-11-01 11:27:23)
 * @param newBlocked boolean
 */
synchronized void setBlocked(boolean newBlocked) {
	blocked = newBlocked;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-09 15:32:29)
 * @param newCall jain.application.services.jcc.JccCall
 */
private void setCall(jain.application.services.jcc.JccCall newCall) {
	call = newCall;
}
/**
 * Internal setter for the Address that started this call, if one is known.
 * Creation date: (2000-11-09 15:23:18)
 * @param newCallingAddr java.lang.String
 */
private void setCallingAddr(GenAddress newCallingAddr) {
	callingAddr = newCallingAddr;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-10 13:54:27)
 * @param newFrameConn javax.telephony.Connection
 */
private void setFrameConn(javax.telephony.Connection newFrameConn) {
	frameConn = newFrameConn;
}
/**
 * Set the override state for the object.
 * Creation date: (2000-11-15 14:32:16)
 * @return int
 * @param newState int
 */
int setJccState(int newState) {
	return this.state = newState;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-09 15:20:26)
 * @param newLastAddr jain.application.services.jcp.JcpAddress
 */
void setLastAddr(jain.application.services.jcc.JccAddress newLastAddr) {
	lastAddr = newLastAddr;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-09 15:20:26)
 * @param newOrigAddr jain.application.services.jcp.JcpAddress
 */
void setOrigAddr(jain.application.services.jcc.JccAddress newOrigAddr) {
	origAddr = newOrigAddr;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 12:55:56)
 * @param newProv com.uforce.jain.generic.Provider
 */
private void setProv(Provider newProv) {
	prov = newProv;
}
/**
 * Internal setter
 * Creation date: (2001-01-24 10:39:17)
 * @param newRouteAddress java.lang.String
 */
private void setRouteAddress(java.lang.String newRouteAddress) {
	routeAddress = newRouteAddress;
}
/**
 * Description
 * @return a string representation of the receiver
 */
public String toString() {
	StringBuffer buf = new StringBuffer(this.isBlocked() ? "B" : "Unb");
	return buf.append("locked Jain Connection adapter for: ")
		.append(this.getFrameConn().toString())
		.toString();
}
}