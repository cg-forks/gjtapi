/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import java.util.Set;

import javax.csapi.cc.jcc.InvalidPartyException;
import javax.csapi.cc.jcc.JccCallListener;
import javax.csapi.cc.jcc.MethodNotSupportedException;
import javax.csapi.cc.jcc.PrivilegeViolationException;
import javax.csapi.cc.jcc.ResourceUnavailableException;

/**
A JcatTerminal represents a physical endpoint connected to the softswitch domain. For example, telephone set, computer workstations and hand-held devices are modeled as JcatTerminal objects if they act as physical endpoints in a communications network. A JcatTerminal object has a string name that is unique for all JcatTerminal objects. The JcatTerminal does not attempt to interpret this string in any way. This name is first assigned when the JcatTerminal is created and does not change throughout the lifetime of the object. The method getName() returns the name of the JcatTerminal object. The name of the JcatTerminal may not have any real-world interpretation. Typically, JcatTerminals are chosen from a list of JcatTerminal objects obtained from an JcatAddress object.

JcatTerminal objects may be classified into two categories: local and remote. Local JcatTerminal objects are those terminals which are part of the JcatProvider's local domain. These JcatTerminal objects are created by the implementation of the JcatProvider object when it is first instantiated. Remote JcatTerminal objects are those outside of the JcatProvider's domain which the JcatProvider learns about during its lifetime through various happenings (e.g., an incoming call from a currently unknown address). Note that applications never explicitly create new JcatTerminal objects through the JCAT API.
Address and Terminal Objects
JcatAddress and JcatTerminal objects exist in a many-to-many relationship. A JcatAddress object may have zero or more JcatTerminal objects associated with it. For each JcatTerminal associated with a JcatAddress, that JcatTerminal must also reflect its association with the JcatAddress. Since the implementation creates JcatAddress (and JcatTerminal) objects, it is responsible for insuring the correctness of these relationships. The JcatTerminal objects associated with a JcatAddress is given by the JcatAddress.getTerminals() method. An association between a JcatAddress and a JcatTerminal object indicates that the JcatTerminal contains the JcatAddress object as one of its telephone number addresses. In many instances, a telephone set (represented by a JcatTerminal object) has only one directory number (represented by a JcatAddress object) associated with it. In more complex configurations, telephone sets may have several directory numbers associated with them. For example, some of the current PDAs may exhibit this configuration.
Terminals and Call Objects
JcatTerminal objects represent the physical endpoints of a call. With respect to a single JcatAddress endpoint on a JcatCall, multiple physical JcatTerminal endpoints may exist. JcatTerminal objects are related to JcatCall objects via the JcatTerminalConnection object. JcatTerminalConnection objects are associated with JcatCall indirectly via JcatConnection objects.

A JcatTerminal may be associated with a JcatCall only if one of its JcatAddress objects is associated with the JcatCall. The JcatTerminalConnection object has a state which describes the current relationship between the JcatConnection and the JcatTerminal. Each JcatTerminal object may be part of more than one call, and in each case, is represented by a separate JcatTerminalConnection object. The getTerminalConnections() method returns all JcatTerminalConnection objects currently associated with the Terminal.

A JcatTerminal object is associated with a JcatConnection until the JcatTerminalConnection moves into the JcatTerminalConnection.DROPPED state. At that time, the JcatTerminalConnection is no longer reported via the JcatTerminal.getTerminalConnections() method. Therefore, the JcatTerminal.getTerminalConnections() method never reports a JcatTerminalConnection in the JcatTerminalConnection.DROPPED state.
Terminal Listeners and Events
All changes in a JcatTerminal object are reported via the JcatTerminalListener interface. Applications instantiate an object which implements this interface and begins this delivery of events to this object using the addTerminalListener(JcatTerminalListener) method. Applications receive events on a listener until the listener is removed via the removeTerminalListener(JcatTerminalListener) method or until the JcatTerminal is no longer observable. In these instances, each JcatTerminalListener instance receives a JcatTerminalEvent.TERMINAL_EVENT_TRANSMISSION_ENDED as its final event. At this point, we envisage use of this feature to be used to report association and disassociation of JcatAddresses with a JcatTerminal.
Call Listeners
At times, applications may want to monitor a particular JcatTerminal for all JcatCalls which come to that JcatTerminal. For example, a desktop telephone application is only interested in calls associated with a particular agent terminal. To achieve this sort of JcatTerminal-based JcatCall monitoring applications may add JcatCallListeners to a JcatTerminal via the addCallListener(JccCallListener) method. When a JcatCallListener is added to a JcatTerminal, this listener instance is immediately added to all JcatCall objects at this JcatTerminal and is added to all calls which come to this JcatTerminal in the future. These listeners remain on the call as long as the JcatTerminal is associated with the call.
 */
public interface JcatTerminal {

	void addCallListener(JccCallListener listener)
						 throws MethodNotSupportedException,
								ResourceUnavailableException;

	void addTerminalListener(JcatTerminalListener listener);
	
	Set getAddresses();
	
	String getName();
	
	JcatProvider getProvider();
	
	Set getTerminalConnections();
	
	void removeCallListener(JccCallListener calllistener);
	
	void removeTerminalListener(JcatTerminalListener termlistener);
	
	JcatTerminalCapabilities getTerminalCapabilities();
	
	void registerAddress(JcatAddress addr)
						 throws InvalidPartyException,
								MethodNotSupportedException,
								PrivilegeViolationException;

	void deregisterAddress(JcatAddress addr)
						   throws InvalidPartyException,
								  MethodNotSupportedException,
								  PrivilegeViolationException;
}