/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import javax.csapi.cc.jcc.JccCallEvent;
import javax.csapi.cc.jcc.JccConnectionEvent;
import javax.csapi.cc.jcc.JccEvent;

/**
 * This is the base interface for all JcatConnection related events. 
 */
public interface JcatConnectionEvent
	extends JcatCallEvent, JccCallEvent, JccConnectionEvent, JccEvent {

	/**
	 * This event indicates that the state of the
	 * JcatConnection object has changed to
	 * JcatConnection.SUSPENDED.
	 */
	public static final int CONNECTION_SUSPENDED = 8;

		
}
