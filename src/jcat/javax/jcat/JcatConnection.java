/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import java.util.Set;

import javax.csapi.cc.jcc.InvalidArgumentException;
import javax.csapi.cc.jcc.InvalidPartyException;
import javax.csapi.cc.jcc.InvalidStateException;
import javax.csapi.cc.jcc.JccConnection;
import javax.csapi.cc.jcc.MethodNotSupportedException;
import javax.csapi.cc.jcc.PrivilegeViolationException;
import javax.csapi.cc.jcc.ResourceUnavailableException;

/**
 * The JcatConnection interface extends the JccConnection interface. While the JccConnection interface specified the relationship between the JccCall and JccAddress objects, JcatConnection is also associated additionally with a JcatTerminal (through the JcatTerminalConnection object). This is expected to enable advanced call control services which require multiple terminals per address or multiple addresses per terminal. These capabilities would be needed given the potential of devices today such as the PDA (considered as a terminal), which can handle voice calls as VOIP as well as using the mobile voice networks (each of which requires separate addresses).

Thus, each JcatConnection object is associated directly with a JcatCall, a JcatAddress and a JcatTerminalConnection object. The JcatConnection object has a finite state machine associated with it. A JcatConnection object maintains a state that reflects the relationship between a JcatCall and a JcatAddress as well as between the JcatCall and the JcatTerminal (through the JcatTerminalConnection object). The JcatConnection state is important to the application because it indicates a logical view to the application. The application can take specific actions on the JcatConnection object when it is in a specific state and specific events have been reported to the application.

In each state, the connection object can detect certain events, which can be reported to an application. As in the case of a JccConnection object, the same JcatConnection object may not be used in another call. The existence of a JcatConnection implies that its address is associated with its call in the manner described by the JcatConnection's state. In addition, this also implies that the JcatTerminalConnection is associated with the JcatCall object.

While a single FSM is specified for the JcatCall, the states traversed by a particular endpoint depends on the role of the endpoint --whether the endpoint originated the call or whether the endpoint is receiving a call. Based on this, we consider two types of connection objects, namely origintaing connection (O-Connection) and terminating connection (T-Connection) objects. An O-Connection object represents an association between a call object and an originating endpoint represented by a JcatAddress object. A T-Connection object represents an association between a call object and a terminating address object.
Connection object state transition diagram
The figure below illustrates the finite-state diagram for the JcatConnection object. The finite-states describe the allowable state transitions of an O-Connection or T-Connection object. The API must guarantee these state transitions.

Note that this state machine is a refinement of the JccConnection FSM. The CONNECTED state of JccConnection is divided into a CONNECTED state and a SUSPENDED state. Since the states defined in the JcatConnection interface provide more detail to the states defined in the JccConnection interface, each state in the JccConnection interface corresponds to a state defined in the JcatConnection interface. Conversely, each JcatConnection state corresponds to exactly one JccConnection state. This arrangement permits applications to view either the JccConnection state or the JcatConnection state and still see a consistent view. 
 */
public interface JcatConnection extends JccConnection {

	public static final int SUSPENDED = 1;
	
	Set getTerminalConnections();
	
	JcatConnection park(java.lang.String destinationAddress)
						throws InvalidPartyException,
							   InvalidStateException,
							   MethodNotSupportedException,
							   PrivilegeViolationException,
							   ResourceUnavailableException;

	void reconnect()
				   throws InvalidArgumentException,
						  InvalidStateException,
						  MethodNotSupportedException,
						  PrivilegeViolationException,
						  ResourceUnavailableException;

	void suspendConnection()
						   throws InvalidStateException,
								  MethodNotSupportedException,
								  ResourceUnavailableException;
}
