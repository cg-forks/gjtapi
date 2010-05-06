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
/**
 * This models a simple call as a collection of phones.  The call has only two states: RINGING and CONNECTED.
 * Creation date: (2000-02-09 10:08:15)
 * @author: Richard Deadman
 */
import java.util.*;
import net.sourceforge.gjtapi.*;
import javax.telephony.*;
import javax.telephony.callcontrol.CallControlCall;

public class RawCall implements CallId {
	public final static int IDLE = 0;
	public final static int CONNECTED = 1;
	public final static int DEAD = 2;
	
	private HashSet<Leg> legs = new HashSet<Leg>();
	private PhoneManager manager = null;
	private int state;
/**
 * Create a new IDLE call that is linked back to its PhoneManager
 * Creation date: (2000-02-09 12:20:25)
 * @author: Richard
 * @param pm net.sourceforge.gjtapi.raw.emulator.PhoneManager
 */
public RawCall(PhoneManager pm) {
	super();
	this.setState(RawCall.IDLE);
	this.setManager(pm);
}
/**
 * Add a leg to the list of legs in the call
 * Creation date: (2000-02-09 10:21:24)
 * @author: Richard Deadman
 * @param leg The new call leg
 */
public void addLeg(Leg leg) {
	this.getLegs().add(leg);
	if (this.getState() == IDLE)
		this.setState(CONNECTED);
		
	RawPhone phone = leg.getPhone();
	if (phone instanceof PhoneListener)
		((PhoneListener)phone).inCall(leg);
}
/**
 * Note that a call leg has connected.  Move any legs in INPROGRESS to CONNECTED
 * Creation date: (2000-03-01 11:00:31)
 * @author: 
 */
public void connected() {
	Set<Leg> legs = this.getLegs();
	Iterator<Leg> it = legs.iterator();
	while(it.hasNext()) {
		Leg l = (Leg)it.next();
		if (l.getState() == Leg.INPROGRESS)
			l.connected();
	}
}
/**
 * Add a new leg to a call or change a ringing call to active.
 * Creation date: (2000-02-09 12:56:37)
 * @author: 
 * @param phone net.sourceforge.gjtapi.raw.emulator.RawPhone
 */
public void dial(RawPhone phone) throws RawStateException {
	if (phone.getState() != RawPhone.IDLE) {
		String name = phone.getAddress();
		throw new RawStateException(this, name, name,
			RawStateException.TERMINAL_CONNECTION_OBJECT,
			TerminalConnection.ACTIVE);
	}
	new Leg(this, phone, this.getManager().getListener(), Leg.ALERTING);
	this.setState(RawCall.CONNECTED);
}
/**
 * Add a new leg to a call or change a ringing call to active.
 * Creation date: (2000-02-09 12:56:37)
 * @author: Richard Deadman
 * @param digits The address of the remote phone
 */
public void dial(RawPhone from, String digits) throws javax.telephony.InvalidPartyException, RawStateException {
	RawPhone ph = this.getManager().getPhone(digits);
	if (ph == null) {
		// look for remote phones
		EmProvider remoteProv = EmProvider.findProvider(digits);
		if (remoteProv == null)
			throw new javax.telephony.InvalidPartyException(javax.telephony.InvalidPartyException.DESTINATION_PARTY);
		// create a remote call for this other leg
		remoteProv.getMgr().createCall(from, digits);
		ph = remoteProv.getPhone(digits);
	}
	this.dial(ph);
}
/**
 * Return a CallData snapshot holder for myself.
 * Creation date: (2000-06-23 12:46:55)
 * @author: Richard Deadman
 * @return A dat holder that holds a snapshot of myself.
 */
CallData getCallData() {
	HashSet<Leg> legs = this.getLegs();
	int size = legs.size();
	ConnectionData[] conns = new ConnectionData[size];
	int i = 0;
	Iterator<Leg> it = legs.iterator();
	while (it.hasNext()) {
		Leg l = (Leg)it.next();
		TCData[] tcd = new TCData[1];
		String name = l.getPhone().getAddress();
			// should probably infer the TC state...
		tcd[0] = new TCData(javax.telephony.callcontrol.CallControlTerminalConnection.ACTIVE,
						new TermData(name, true));
		conns[i] = new ConnectionData(l.getConnState(), name,
						true, tcd);
	}

	return new CallData(this, this.getCallState(), conns);
}
/**
 * Return the javax.telephony.callcontrol.CallControlCall state
 * Creation date: (2000-06-23 12:24:17)
 * @author: Richard Deadman
 * @return int
 */
private int getCallState() {
	switch (this.getState()) {
		case RawCall.IDLE: {
			return CallControlCall.IDLE;
		}
		case RawCall.CONNECTED: {
			return CallControlCall.ACTIVE;
		}
		case RawCall.DEAD: {
			return CallControlCall.INVALID;
		}
	}
	return CallControlCall.INVALID;
}
/**
 * Get the leg associated with a given phone, or null
 * Creation date: (2000-03-01 12:11:12)
 * @author: Richard Deadman
 * @return The leg attached to the phone
 * @param ph A phone object
 */
Leg getLeg(RawPhone ph) {
	if (ph instanceof TestPhone)
		ph = ((TestPhone)ph).getModel();
		
	Iterator<Leg> it = this.getLegs().iterator();
	while (it.hasNext()) {
		Leg l = (Leg)it.next();
		if (l.getPhone().equals(ph)) {
			return l;
		}
	}
	return null;
}
/**
 * Return the set of Call legs
 * Creation date: (2000-02-09 10:20:37)
 * @author: Richard Deadman
 * @return HashSet
 */
private HashSet<Leg> getLegs() {
	return legs;
}
/**
 * Internal accessor for the PhoneManager.
 * Creation date: (2000-02-09 12:19:56)
 * @author: Richard Deadman 
 * @return The controller of all phones.
 */
private PhoneManager getManager() {
	return manager;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-09 12:29:03)
 * @author: 
 * @return int
 */
public int getState() {
	return state;
}
/**
 * Combine two calls together.
 * Creation date: (2000-02-28 12:20:39)
 * @author: Richard Deadman
 * @param other net.sourceforge.gjtapi.raw.emulator.RawCall
 */
public void join(RawCall other) {
	// Now tell each other Phone it is in a new call
	Iterator<Leg> it = other.getLegs().iterator();
	while (it.hasNext()) {
		Leg leg = (Leg)it.next();
		leg.getPhone().swap(this, leg, this.getManager().getListener());
	}
}
/**
 * Remove and notify all remainging legs
 * Creation date: (2000-02-09 15:17:05)
 * @author: Richard Deadman
 */
public void removeAll() {
	Iterator<Leg> it = this.getLegs().iterator();
	while (it.hasNext()) {
		Leg l = (Leg)it.next();
		RawPhone ph = l.getPhone();
		if (ph instanceof PhoneListener)
			((PhoneListener)ph).idle();
	}
	this.setState(DEAD);
}
/**
 * Remove a leg if it exists in this call.
 * Creation date: (2000-02-09 10:22:08)
 * @author: Richard Deadman
 * @param leg The leg to remove
 */
public void removeLeg(Leg leg) {
	// first remove the phone
	Set<Leg> ls = this.getLegs();
	ls.remove(leg);	// assume equals works for Legs

	int size = ls.size();
	
		// note when no legs left
	if (size == 0) {
		// tell the manager I'm outa here -- it will notify any trailing legs
		this.setState(DEAD);
		return;		
	}
	
	// If there is only one phone left in the call, destroy the last leg and call recursively
	if (ls.size() < 2) {
		Leg last = (Leg)ls.iterator().next();
		last.drop();
	}
}
/**
 * Broadcast DTMF to all phones
 * Creation date: (2000-02-09 23:40:36)
 * @author: Richard Deadman
 * @param digits The digits to be recieved.
 */
public void sendDTMF(String digits) {
	Iterator<Leg> it = this.getLegs().iterator();
	while (it.hasNext()){
		RawPhone phone = ((Leg)it.next()).getPhone();
		phone.receiveDTMF(digits);
	}
}

/**
 * Insert the method's description here.
 * Creation date: (2000-02-09 12:19:56)
 * @author: 
 * @param newManager net.sourceforge.gjtapi.raw.emulator.PhoneManager
 */
private void setManager(PhoneManager newManager) {
	manager = newManager;
}
/**
 * Change the Call State.  Only Coonected and Dead are currently well handled.
 * Creation date: (2000-02-09 12:29:03)
 * @author: Richard Deadman
 * @param newState int
 */
private void setState(int newState) {
	state = newState;

	switch (newState) {
		case RawCall.CONNECTED: {
			this.getManager().getListener().callActive(this, Event.CAUSE_NORMAL);
			break;
		}
		case RawCall.DEAD: {
			this.getManager().getListener().callInvalid(this, Event.CAUSE_NORMAL);
			break;
		}
	}
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	StringBuffer sb = new StringBuffer("A Call with phones: ");
	Iterator<Leg> it = this.getLegs().iterator();
	while (it.hasNext()) {
		sb.append(((Leg)it.next()).getPhone().getAddress());
		sb.append(" ");
	}
	return sb.toString();
}
}
