/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import java.util.Set;

import javax.csapi.cc.jcc.EventFilter;
import javax.csapi.cc.jcc.InvalidArgumentException;
import javax.csapi.cc.jcc.JccProvider;
import javax.csapi.cc.jcc.ResourceUnavailableException;

/**
 * JcatProvider interface extends the JccProvider interface. This interface is expected to be used when advanced call control features are desired. JcatProvider has the same finite state machine as JccProvider.

A JcatProvider is associated with JcatCall, JcatConnection and JcatAddress which are extensions of corresponding JCC entities. In addition, a JcatProvider also has JcatTerminal objects and JcatTerminalConnection objects associated with it during a call. 
 */
public interface JcatProvider extends JccProvider {

	Set getCalls(JcatAddress address);
	
	Set getTerminals(java.lang.String nameRegex);
	
	EventFilter createEventFilterRegistration(java.lang.String terminalNameRegex,
													 int matchDisposition,
													 int nomatchDisposition)
											  throws ResourceUnavailableException,
													 InvalidArgumentException;
}
