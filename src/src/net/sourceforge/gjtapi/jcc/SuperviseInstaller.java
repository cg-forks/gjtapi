package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2002 Deadman Consulting (www.deadman.ca) 

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
import java.util.Set;
import java.util.Iterator;
import javax.csapi.cc.jcc.*;

import net.sourceforge.gjtapi.jcc.GenCall.Supervisor;
/**
 * This is a simple call listener that listens for idle -> active transitions on a call
 * and installs any supervisors pending for the call.
 * Creation date: (2000-11-10 14:40:49)
 * @author: Richard Deadman
 */
class SuperviseInstaller implements JccCallListener {
/**
 * SuperviseInstaller constructor comment.
 */
public SuperviseInstaller() {
	super();
}
/**
 * Pop off any waiting supervisors for the call and start them.
 */
public void callActive(JccCallEvent event) {
	Set<Supervisor> sups = ((GenCall)event.getCall()).getWaitingSupervisors();
	synchronized (sups) {
		Iterator<Supervisor> it = sups.iterator();
		while (it.hasNext()) {
			new Thread((Runnable)it.next()).start();
			it.remove();
		}
	}
}
/**
 * callcreated method comment.
 */
public void callCreated(JccCallEvent event) {}
/**
 * callEventTransmissionEnded method comment.
 */
public void callEventTransmissionEnded(JccCallEvent event) {}
/**
 * callInvalid method comment.
 */
public void callInvalid(JccCallEvent event) {}

    /**
        Indicates that the supervision of the call has started.

        @param callevent JccCallevent.
    */
    public void callSuperviseStart( JccCallEvent callevent ) {}

    /**
        Indicates that the supervision of the call has ended.

        @param callevent JccCallevent.
    */
    public void callSuperviseEnd( JccCallEvent callevent ) {}

/**
 * Describe myself.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Monitor for a call that will install any waiting supervisors once the call goes active";
}
}
