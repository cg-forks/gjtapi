/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

import javax.csapi.cc.jcc.JccCallEvent;
import javax.csapi.cc.jcc.JccEvent;

/**
 * This is the base interface for all JcatConnection related events. 
 */
public interface JcatCallEvent extends JccCallEvent, JccEvent {

	// don't know real values yet...
	public static final int CAUSE_CONFERENCE = 1;
	
	public static final int CAUSE_TRANFER = 2;
}
