package net.sourceforge.gjtapi.raw.invert;

/*
	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 

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
import javax.telephony.events.CallEv;
import net.sourceforge.gjtapi.*;
import javax.telephony.*;
import javax.telephony.callcontrol.*;
import javax.telephony.callcontrol.events.*;
import javax.telephony.media.*;
/**
 * This is a listener that is hooked to JTAPI objects and translates their events into
 * TelephonyListener events.
 * Creation date: (2000-06-06 12:15:10)
 * @author: Richard Deadman
 */
public class InverterListener implements AddressListener, CallControlCallObserver, MediaListener, TerminalConnectionListener, TerminalListener {
	private TelephonyListener tListener = null;
	private IdMapper callMap = null;
/**
 * Create an Invertor with the CallId listener.
 * Creation date: (2000-06-06 13:05:17)
 * @author: Richard Deadman
 * @param callMap A map of CallId to JTAPI Call objects
 */
public InverterListener(IdMapper callMap) {
	this.setCallMap(callMap);
}
/**
 * Swallow this.
 */
public void addressListenerEnded(AddressEvent event) {}
/**
 * callActive method comment.
 */
public void callActive(CallEvent event) {
	this.getTListener().callActive(this.getCallMap().getId(event.getCall()), event.getCause());
}
/**
 * This handles the events not currently listened for by the Listener architecture.
 * These include:
 * <ul>
 *  <li>CallCtlTermConnHeldEv
 *  <li>CallCtlTermConnTalkingEv
 * </ul>
 * All other events are eaten with the assumption that the corresponding listener event is also
 * thrown.
 */
public void callChangedEvent(javax.telephony.events.CallEv[] eventList) {
	TerminalConnection tc = null;
	for (int i = 0; i < eventList.length; i++) {
		CallEv ev = eventList[i];
		switch (ev.getID()) {
			case CallCtlTermConnHeldEv.ID: {
				tc = ((javax.telephony.events.TermConnEv)ev).getTerminalConnection();
				this.getTListener().terminalConnectionHeld(this.getCallMap().getId(ev.getCall()), 
					tc.getConnection().getAddress().getName(),
					tc.getTerminal().getName(),
					ev.getCause());
				break;
			}
			case CallCtlTermConnTalkingEv.ID: {
				tc = ((javax.telephony.events.TermConnEv)ev).getTerminalConnection();
				this.getTListener().terminalConnectionTalking(this.getCallMap().getId(ev.getCall()), 
					tc.getConnection().getAddress().getName(),
					tc.getTerminal().getName(),
					ev.getCause());
				break;
			}
		}
	}
}
/**
 * swallow this event.
 */
public void callEventTransmissionEnded(CallEvent event) {}
/**
 * callInvalid method comment.
 */
public void callInvalid(CallEvent event) {
	this.getTListener().callInvalid(this.getCallMap().getId(event.getCall()), event.getCause());
}
/**
 * connectionAlerting method comment.
 */
public void connectionAlerting(ConnectionEvent event) {
	this.getTListener().connectionAlerting(this.getCallMap().getId(event.getCall()), event.getConnection().getAddress().getName(), event.getCause());
}
/**
 * connectionConnected method comment.
 */
public void connectionConnected(ConnectionEvent event) {
	this.getTListener().connectionConnected(this.getCallMap().getId(event.getCall()), event.getConnection().getAddress().getName(), event.getCause());
}
/**
 * Swallow for now -- we don't yet accept created events but implie them.
 */
public void connectionCreated(ConnectionEvent event) {}
/**
 * connectionDisconnected method comment.
 */
public void connectionDisconnected(ConnectionEvent event) {
	this.getTListener().connectionDisconnected(this.getCallMap().getId(event.getCall()), event.getConnection().getAddress().getName(), event.getCause());
}
/**
 * connectionFailed method comment.
 */
public void connectionFailed(ConnectionEvent event) {
	this.getTListener().connectionFailed(this.getCallMap().getId(event.getCall()), event.getConnection().getAddress().getName(), event.getCause());
}
/**
 * connectionInProgress method comment.
 */
public void connectionInProgress(ConnectionEvent event) {
	this.getTListener().connectionInProgress(this.getCallMap().getId(event.getCall()), event.getConnection().getAddress().getName(), event.getCause());
}
/**
 * Swallow.  We don't handle these at present
 */
public void connectionUnknown(ConnectionEvent event) {}
/**
 * Return the shared CallId to Call mapper.
 * Creation date: (2000-06-06 13:08:42)
 * @author: Richard Deadman
 * @return The two-way Call to CallId mapper.
 */
private IdMapper getCallMap() {
	return callMap;
}
/**
 * Package accessor for the Generic JTAPI event listener.
 * Creation date: (2000-06-06 12:19:11)
 * @author: Richard Deadman
 * @return A TelephonyListener that receives Generic JTAPI Framework events
 */
net.sourceforge.gjtapi.TelephonyListener getTListener() {
	return tListener;
}
/**
 * Swallow.
 */
public void multiCallMetaMergeEnded(MetaEvent event) {}
/**
 * Swallow.
 */
public void multiCallMetaMergeStarted(MetaEvent event) {}
/**
 * Swallow.
 */
public void multiCallMetaTransferEnded(MetaEvent event) {}
/**
 * Swallow.
 */
public void multiCallMetaTransferStarted(MetaEvent event) {}
/**
 * Set the shared Call to CallId two-way map holder.
 * Creation date: (2000-06-06 13:08:42)
 * @author: Richard Deadman
 * @param newCallMap The two-way Call to CallId map holder.
 */
private void setCallMap(IdMapper newCallMap) {
	callMap = newCallMap;
}
/**
 * Set the TelephonyListener delegate for this adapter.
 * Creation date: (2000-06-06 12:19:11)
 * @author: Richard Deadman
 * @param newTListener The raw TelephonyListener to delegate adapted events to.
 */
void setTListener(net.sourceforge.gjtapi.TelephonyListener newTListener) {
	tListener = newTListener;
}
/**
 * Swallow.
 */
public void singleCallMetaProgressEnded(MetaEvent event) {}
/**
 * Swallow.
 */
public void singleCallMetaProgressStarted(MetaEvent event) {}
/**
 * Swallow.
 */
public void singleCallMetaSnapshotEnded(MetaEvent event) {}
/**
 * Swallow.
 */
public void singleCallMetaSnapshotStarted(MetaEvent event) {}
/**
 * Swallow -- instead we monitor Talking and Held.
 */
public void terminalConnectionActive(TerminalConnectionEvent event) {}
/**
 * terminalConnectionCreated method comment.
 */
public void terminalConnectionCreated(TerminalConnectionEvent event) {
	TerminalConnection tc = event.getTerminalConnection();
	this.getTListener().terminalConnectionCreated(this.getCallMap().getId(event.getCall()),
		tc.getConnection().getAddress().getName(),
		tc.getTerminal().getName(),
		event.getCause());
}
/**
 * terminalConnectionDropped method comment.
 */
public void terminalConnectionDropped(TerminalConnectionEvent event) {
	TerminalConnection tc = event.getTerminalConnection();
	this.getTListener().terminalConnectionDropped(this.getCallMap().getId(event.getCall()),
		tc.getConnection().getAddress().getName(),
		tc.getTerminal().getName(),
		event.getCause());
}
/**
 * Swallow
 */
public void terminalConnectionPassive(TerminalConnectionEvent event) {}
/**
 * terminalConnectionRinging method comment.
 */
public void terminalConnectionRinging(TerminalConnectionEvent event) {
	TerminalConnection tc = event.getTerminalConnection();
	this.getTListener().terminalConnectionRinging(this.getCallMap().getId(event.getCall()),
		tc.getConnection().getAddress().getName(),
		tc.getTerminal().getName(),
		event.getCause());
}
/**
 * Swallow.
 */
public void terminalConnectionUnknown(TerminalConnectionEvent event) {}
/**
 * terminalListenerEnded method comment.
 */
public void terminalListenerEnded(TerminalEvent event) {}
/**
 * Describe myself.
 * @return a string representation of the receiver
 */
public String toString() {
	return "An event listener that adapts registered JTAPI events to the Generic JTAPI event model.";
}
}
