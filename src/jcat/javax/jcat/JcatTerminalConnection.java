/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import javax.csapi.cc.jcc.InvalidStateException;
import javax.csapi.cc.jcc.MethodNotSupportedException;
import javax.csapi.cc.jcc.PrivilegeViolationException;
import javax.csapi.cc.jcc.ResourceUnavailableException;

/**
A JcatTerminalConnection object maintains a state that reflects the relationship between a JcatTerminal and a JcatConnection. A JcatTerminalConnection object state is distinct from the JcatConnection object states. The JcatConnection object states describe the relationship between an entire JcatAddress endpoint and a JcatCall, whereas the JcatTerminalConnection state describes the relationship between one of the JcatTerminal objects at the endpoint JcatAddress on the JcatCall with respect to its JcatConnection. Different JcatTerminals on a JcatCall, which are associated with the same, JcatConnection may be in different states. Furthermore, the state of the JcatTerminalConnection has a dependency and specific relationship to the state of its JcatConnection.
 * <P>...
 */
public interface JcatTerminalConnection {

	// don't know values yet
	public static final int IDLE = 1;
	public static final int RINGING = 2;
	public static final int DROPPED = 4;
	public static final int BRIDGED = 8;
	public static final int TALKING = 16;
	public static final int INUSE = 32;
	public static final int HELD = 64;
	
	void answer();
	
	JcatConnection getConnection();
	
	int getState();
	
	JcatTerminal getTerminal();
	
	void hold()
			  throws InvalidStateException,
					 MethodNotSupportedException,
					 PrivilegeViolationException,
					 ResourceUnavailableException;

	void join()
			  throws InvalidStateException,
					 MethodNotSupportedException,
					 PrivilegeViolationException,
					 ResourceUnavailableException;

	void leave()
			   throws InvalidStateException,
					  MethodNotSupportedException,
					  PrivilegeViolationException,
					  ResourceUnavailableException;

	void unhold()
				throws InvalidStateException,
					   MethodNotSupportedException,
					   PrivilegeViolationException,
					   ResourceUnavailableException;
}
