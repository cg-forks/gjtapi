/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import javax.csapi.cc.jcc.InvalidArgumentException;
import javax.csapi.cc.jcc.InvalidPartyException;
import javax.csapi.cc.jcc.InvalidStateException;
import javax.csapi.cc.jcc.JccCall;
import javax.csapi.cc.jcc.MethodNotSupportedException;
import javax.csapi.cc.jcc.PrivilegeViolationException;
import javax.csapi.cc.jcc.ResourceUnavailableException;

/**
 * A JcatCall object extends the JccCall object . It
 * provides advanced features such as transfer,
 * conference etc. The JcatCall object has the same
 * finite state machine as the JccCall object. 
 */
public interface JcatCall extends JccCall {

	void addTerminalConnectionListener(JcatTerminalConnectionListener termconnlistener)
									   throws MethodNotSupportedException,
											  ResourceUnavailableException;

	JcatConnection blindTransfer(java.lang.String dialledDigits)
								 throws InvalidArgumentException,
										InvalidStateException,
										InvalidPartyException,
										MethodNotSupportedException,
										PrivilegeViolationException,
										ResourceUnavailableException;

	void conference(JcatCall othercall)
					throws InvalidArgumentException,
						   InvalidStateException,
						   MethodNotSupportedException,
						   PrivilegeViolationException,
						   ResourceUnavailableException;

	JcatConnection[] connect(JcatTerminal term,
									JcatAddress addr,
									java.lang.String dialedDigits)
							 throws ResourceUnavailableException,
									PrivilegeViolationException,
									InvalidPartyException,
									InvalidStateException,
									MethodNotSupportedException;

	JcatConnection[] consult(JcatTerminalConnection termconn,
									java.lang.String dialedDigits)
							 throws InvalidArgumentException,
									InvalidPartyException,
									InvalidStateException,
									MethodNotSupportedException,
									PrivilegeViolationException,
									ResourceUnavailableException;

	void consultTransfer(JcatCall otherCall)
						 throws InvalidArgumentException,
								InvalidPartyException,
								InvalidStateException,
								MethodNotSupportedException,
								PrivilegeViolationException,
								ResourceUnavailableException;

	JcatTerminalConnection getConferenceController();

	boolean getConferenceEnable();
	
	JcatTerminalConnection getTransferController();
	
	boolean getTransferEnable();
	
	void removeTerminalConnectionListener(JcatTerminalConnectionListener terminalConnectionListener);
	
	void setConferenceController(JcatTerminalConnection tc)
								 throws InvalidArgumentException,
										InvalidStateException,
										MethodNotSupportedException,
										ResourceUnavailableException;

	void setConferenceEnable(boolean enabled)
							 throws InvalidArgumentException,
									InvalidStateException,
									MethodNotSupportedException,
									ResourceUnavailableException;

	void setTransferController(JcatTerminalConnection termconn)
							   throws InvalidArgumentException,
									  InvalidStateException,
									  MethodNotSupportedException,
									  ResourceUnavailableException;

	void setTransferEnable(boolean enable)
						   throws InvalidArgumentException,
								  InvalidStateException,
								  MethodNotSupportedException,
								  ResourceUnavailableException;
}
