/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import java.util.EventListener;

import javax.csapi.cc.jcc.JccCallListener;
import javax.csapi.cc.jcc.JccConnectionListener;

/**
 * This interface is an extension of the JccConnectionListener interface and reports state changes both of the JccCall and its JcatConnections.

The methods provided on this interface are on account of the fact that the states defined in the JcatConnection interface provide more detail to the states defined in the JccConnection interface. As a result each state in the JccConnection interface corresponds to a state defined in the JcatConnection interface. Conversely, each JcatConnection state corresponds to exactly one JccConnection state. This arrangement permits applications to view either the JccConnection state or the JcatConnection state and still see a consistent view.

Additionally, note that the JccConnectionEvent.CONNECTION_CONNECTED state now also allows a transition to JcatConnectionEvent.CONNECTION_SUSPENDED state and back. 
 */
public interface JcatConnectionListener
	extends EventListener, JccCallListener, JccConnectionListener {

	/**
	 * Indicates that the JccConnection has just been
	 * placed in the JcatConnection.SUSPENDED state
	 * @param connectionevent event resulting from state change.
	 */
	void connectionSuspended(JcatConnectionEvent connectionevent);

}
