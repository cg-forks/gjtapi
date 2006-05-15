package net.sourceforge.gjtapi.raw.emulator;

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
import net.sourceforge.gjtapi.*;
import javax.telephony.*;

/**
 * Somewhat akin to a Connection and TerminalConnection joined,
 * a leg represents a phone attached to a call.
 * Creation date: (2000-02-28 14:13:45)
 * @author: Richard Deadman
 */
public class Leg {
	public final static int IDLE = 0;
	public final static int INPROGRESS = 1;
	public final static int ALERTING = 2;
	public final static int CONNECTED = 3;
	public final static int FAILED = 4;
	public final static int DISCONNECTED = 5;
		
	private RawCall call;
	private RawPhone phone;
	private int state;
	private TelephonyListener sink = null;
/**
 * Replace a leg between a phone and a call.
 * Creation date: (2000-02-28 14:17:50)
 * @author: Richard Deadman
 * @param c The call
 * @param oldLeg The old leg to clone phone and state from
 * @param listener The observer for event transition callbacks
 */
public Leg(RawCall c, Leg oldLeg, TelephonyListener listener) {
	super();
	
	this.init(c, oldLeg.getPhone(), listener);

	phone.swap(oldLeg, this);

	// finally, after everything is set up, set ourt state, which notifies observers
	this.setState(oldLeg.getState());
}
/**
 * Create a leg between a phone and a call.
 * Creation date: (2000-02-28 14:17:50)
 * @author: Richard Deadman
 * @param c The call
 * @param p The phone connected by this leg to the call
 * @param listener The listener for event transition callbacks.
 * @param state The state of the new leg
 */
public Leg(RawCall c, RawPhone p, TelephonyListener listener, int state) {
	super();
	
	this.init(c, p, listener);

	phone.add(this);

	// finally, after everything is set up, set ourt state, which notifies observers
	this.setState(state);
}
/**
 * Tell the phone I'm associated with to start ringing.
 * Creation date: (2000-02-29 12:36:40)
 * @author: Richard Deadman
 */
public boolean alert() {
	if (this.getState() == IDLE) {
		this.setState(ALERTING);
		return true;
	}
	return false;
}
/**
 * Answer the leg
 * Creation date: (2000-02-28 15:51:23)
 * @author: Richard Deadman
 * @return boolean
 */
public boolean answer() {
	if (this.getState() == ALERTING) {
		this.setState(CONNECTED);
		// now tell all INPROGRESS legs on the call to move to CONNECTED
		this.getCall().connected();
		return true;
	}
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-01 11:05:48)
 * @author: 
 */
void connected() {
	// legs with dialtone or ringtone on it are connected ;jw
	if (this.getState() == INPROGRESS || this.getState() == IDLE)
		this.setState(CONNECTED);
}
/**
 * Drop this connection
 * Creation date: (2000-02-29 12:36:40)
 * @author: Richard Deadman
 */
public boolean drop() {
	this.setState(DISCONNECTED);
	this.getPhone().remove(this);
	this.getCall().removeLeg(this);
	return true;
}
/**
 * Test object equality
 * Creation date: (2000-02-28 15:02:17)
 * @author: Richard Deadman
 * @return boolean
 * @param other The object I am logically tested against
 */
public boolean equals(Object other) {
	if (other instanceof Leg) {
		Leg l = (Leg)other;
		return this.getCall().equals(l.getCall()) && this.getPhone().equals(l.getPhone());
	}
	return false;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-28 14:15:48)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.emulator.RawCall
 */
public RawCall getCall() {
	return call;
}
/**
 * Return the javax.telephony.Connection state
 * Creation date: (2000-06-23 12:24:17)
 * @author: Richard Deadman
 * @return int
 */
int getConnState() {
	switch (this.getState()) {
		case Leg.ALERTING: {
			return Connection.ALERTING;
		}
		case Leg.CONNECTED: {
			return Connection.CONNECTED;
		}
		case Leg.DISCONNECTED: {
			return Connection.DISCONNECTED;
		}
		case Leg.FAILED: {
			return Connection.FAILED;
		}
		case Leg.IDLE: {
			return Connection.IDLE;
		}
		case Leg.INPROGRESS: {
			return Connection.INPROGRESS;
		}
	}
	return Connection.UNKNOWN;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-28 14:16:42)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.emulator.RawPhone
 */
public RawPhone getPhone() {
	return phone;
}
/**
 * Internal accessor for the state transition event listener.
 * Creation date: (2000-02-28 16:09:10)
 * @author: Richard Deadman
 * @return The Framework handle for state transition notification.
 */
private TelephonyListener getSink() {
	return sink;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-28 14:17:14)
 * @author: 
 * @return int
 */
public int getState() {
	return state;
}
/**
 * Return a hashcode for sorting.
 * Creation date: (2000-02-28 14:59:44)
 * @author: Richard Deadman
 * @return int
 */
public int hashCode() {
	return this.getCall().hashCode() + this.getPhone().hashCode();
}
/**
 * Initialize the state
 * Creation date: (2000-02-28 14:17:14)
 * @author: Richard Deadman
 * @param newCall The call the leg is attached to.
 * @param newPhone The phone the leg it on.
 * @param sink The event receiver.
 */
private void init(RawCall newCall, RawPhone newPhone, TelephonyListener sink) {
	// 1. Set the phone
	if (newPhone instanceof TestPhone)
		phone = ((TestPhone)newPhone).getModel();
	else
		phone = newPhone;

	// now the sink
	this.setSink(sink);

	// now the call
	call = newCall;

	// finally add the leg to the call (phone and state must be set)
	call.addLeg(this);
}
/**
 * Tell the phone I'm associated with to start dialing.
 * Creation date: (2000-02-29 12:36:40)
 * @author: Richard Deadman
 */
public boolean inProgress() {
	if (this.getState() == IDLE) {
		this.setState(INPROGRESS);
		return true;
	}
	return false;
}
/**
 * Set the event listener for the object
 * Creation date: (2000-02-28 16:09:10)
 * @author: Richard Deadman
 * @param newSink The new state transition event listener.
 */
void setSink(TelephonyListener newSink) {
	sink = newSink;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-28 14:17:14)
 * @author: 
 * @param newState int
 */
private void setState(int newState) {
	state = newState;

	PhoneListener pl = (PhoneListener)this.getPhone();	// hack alert
	TelephonyListener sink = this.getSink();
	String endpoint = this.getPhone().getAddress();
	
	// determine event type
	switch (newState) {
		case IDLE: {
			pl.inCall(this);
			//sink.connectionCreated(this.getCall(), endpoint, Event.CAUSE_NORMAL);
			sink.terminalConnectionCreated(this.getCall(), endpoint, endpoint, Event.CAUSE_NORMAL);
			break;
		}
		case INPROGRESS: {
			sink.connectionInProgress(this.getCall(), endpoint, Event.CAUSE_NORMAL);
			break;
		}
		case ALERTING: {
			pl.ringing();
			sink.connectionAlerting(this.getCall(), endpoint, Event.CAUSE_NORMAL);
			sink.terminalConnectionRinging(this.getCall(), endpoint, endpoint, Event.CAUSE_NORMAL);
			break;
		}
		case CONNECTED: {
			pl.connected();
			sink.connectionConnected(this.getCall(), endpoint, Event.CAUSE_NORMAL);
			sink.terminalConnectionTalking(this.getCall(), endpoint, endpoint, Event.CAUSE_NORMAL);
			break;
		}
		case FAILED: {
			sink.connectionFailed(this.getCall(), endpoint, Event.CAUSE_NORMAL);
			break;
		}
		case DISCONNECTED: {
			pl.idle();
			sink.terminalConnectionDropped(this.getCall(), endpoint, endpoint, Event.CAUSE_NORMAL);
			sink.connectionDisconnected(this.getCall(), endpoint, Event.CAUSE_NORMAL);
			break;
		}
	}
}
}
