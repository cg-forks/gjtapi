/*
 * Created on Sep 11, 2003
 *
 * 
 */
package javax.jcat;

import java.util.Set;

import javax.csapi.cc.jcc.EventFilter;
import javax.csapi.cc.jcc.InvalidArgumentException;
import javax.csapi.cc.jcc.InvalidPartyException;
import javax.csapi.cc.jcc.JccAddress;
import javax.csapi.cc.jcc.MethodNotSupportedException;
import javax.csapi.cc.jcc.PrivilegeViolationException;

/**
 * Like a JccAddress, a JcatAddress represents a user's
 * directory number. This directory number may be based
 * on an E.164 addressing scheme or some other addressing
 * scheme. For an IP-based network, the address might
 * represent an IP address or a specific string (e.g.,
 * "758-1000@telcordia.com"), as indicated by JccAddress.
 * getType(). At a minimum an address is unique within a
 * Call Agent's local domain.
 * 
 * <P>A JcatAddress object has a string name which
 * corresponds to the directory number. This name is
 * first assigned when the Address object is created
 * and does not change throughout the lifetime of the
 * object. The operation JccAddress.getName() returns
 * the name of the JcatAddress object.
 * <H2>JcatAddress and JcatTerminal Objects</H2>
 * A JcatAddress is also associated with a JcatTerminal.
 * JcatAddress and JcatTerminal objects exist in a
 * many-to-many relationship. A JcatAddress object may
 * have zero or more JcatTerminals associated with it.
 * Each JcatTerminal associated with a JcatAddress must
 * reflect its association with the JcatAddress. Since
 * the implementation creates JcatAddress (and
 * JcatTerminal) objects, it is responsible for
 * ensuring the correctness of these relationships. The
 * JcatTerminals associated with a JcatAddress are given
 * by the getTerminals() operation.
 * 
 * <P>An association between a JcatAddress and a
 * JcatTerminal indicates that the JcatTerminal is
 * addressable using that JcatAddress. In many instances,
 * a telephone set (represented by a JcatTerminal object)
 * has only one directory number (represented by a
 * JcatAddress object) associated with it. In more
 * complex configurations, telephone sets may have
 * several directory numbers associated with them.
 * Likewise, a directory number may appear on more than
 * one telephone set.
 * 
 * <H2>JcatAddress and JcatCall Objects</H2>
 * <P>JcatAddress objects represent the logical
 * endpoints of a call. A logical view of a call views
 * the call as originating from one JcatAddress endpoint
 * and terminating at another JcatAddress endpoint.
 * 
 * <P>JcatAddress objects are related to JcatCall objects
 * via the JcatConnection object; this is the same as in
 * case of the JccConnection objects. The JcatConnection
 * object has a state which describes the current
 * relationship between the JcatCall and the JcatAddress.
 * Each JcatAddress object may be part of more than one
 * call, and in each case, is represented by a separate
 * JcatConnection object. The getConnections() operation
 * returns all connection objects currently associated
 * with the address.
 * 
 * <P>A JcatAddress is associated with a JcatCall until
 * the JcatConnection moves into the
 * JccConnection.DISCONNECTED state. At that time, the
 * JcatConnection is no longer reported via the
 * getConnections() operation. This behavior is similar
 * to that for JccConnection objects.
 * 
 * <h2>Address Listeners and Events</H2>
 * <P>All changes in a JcatAddress object are reported
 * via the JcatAddressListener interface. Applications
 * instantiate an object which implements this interface
 * and begins this delivery of events to this object
 * using the addAddressListener(JcatAddressListener,
 * EventFilter) operation. Applications receive events
 * on a listener until the listener is removed via the
 * removeAddressListener(JcatAddressListener) operation
 * or until the JcatAddress is not longer observable.
 * In these instances, each JcatAddressListener instance
 * receives a
 * JcatAddressEvent.ADDRESS_EVENT_TRANSMISSION_ENDED as
 * its final event.
 */
public interface JcatAddress extends JccAddress {

	void addAddressListener(JcatAddressListener addrlistener, EventFilter eventFilter);
	
	void registerTerminal(JcatTerminal term)
						  throws InvalidPartyException,
								 MethodNotSupportedException,
								 PrivilegeViolationException;
	void deregisterTerminal(JcatTerminal term)
							throws InvalidPartyException,
								   MethodNotSupportedException,
								   PrivilegeViolationException;

	Set getTerminals();

	void removeAddressListener(JcatAddressListener addrlistener);

	Set getConnections();

	void setDisplayText(java.lang.String text,
							   boolean displayAllowed)
						throws InvalidArgumentException;

	String getDisplayText();

	boolean getDisplayAllowed();
	
}
