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
import java.util.Vector;

import net.sourceforge.gjtapi.*;
import javax.telephony.Event;
/**
 * This represents the state of a Phone.
 * Creation date: (2000-02-28 12:41:07)
 * @author: Richard Deadman
 */
public class PhoneModel implements PhoneListener, RawPhone {
	private int state = IDLE;
	private Leg activeCall;
	private Leg heldCall;
	private String address;
	private PhoneManager manager;
	private PhoneListener listener;
		// collector for DTMF signals
	private StringBuffer digitBucket = new StringBuffer();
	private boolean sendDtmf = false;	// do I cause events for DTMF signals?
		// hold on to any Play or Record threads I may have
		// This may be used to allow playing to stop by interrupting the Thread
	private Thread playThread = null;
	private Thread recordThread = null;
/**
 * Create a Phone with a known address
 * Creation date: (2000-02-28 12:46:19)
 * @author: Richard Deadman
 * @param addr The address of the phone
 * @param list The view listening to my changes
 * @param mgr The call manager used to create new calls
 */
public PhoneModel(String addr, PhoneListener list, PhoneManager mgr) {
	super();
	
	this.setAddress(addr);
	this.setListener(list);
	this.setManager(mgr);
}
/**
 * Add a link between this phone and a call
 * Creation date: (2000-02-29 13:47:35)
 * @author: Richard Deadman
 * @return boolean
 * @param leg The leg to add to the phone
 */
public boolean add(Leg leg) {
	this.setActiveLeg(leg);
	if (leg.getState() == Leg.ALERTING)
		this.getListener().ringing();
	return true;
}
/**
 * Answer a ringing call
 */
public boolean answer() {
	return this.getActiveLeg().answer();
}
/**
 * Answer a ringing call
 */
public boolean answer(CallId call) {
	Leg l = this.getActiveLeg();
	if (l.getCall().equals(call)) {
		return l.answer();
	}
	return false;
}
/**
 * bridged method comment.
 */
public void bridged() {
	this.setState(BRIDGED);
	this.getListener().bridged();
}
/**
 * I must have a Held and active call to conference.
 */
public boolean conference() {
	Leg active = this.getActiveLeg();
	Leg held = this.getHeldLeg();

	if (active != null && held != null) {
		held.getCall().join(active.getCall());
	
		this.setActiveLeg(held);
		this.setHeldLeg(null);
		this.connected();
		return true;
	}
	return false;
}
/**
 * connected method comment.
 */
public void connected() {
	if (this.getHeldLeg() != null)
		this.bridged();
	else {
		this.setState(ACTIVE);
		this.getListener().connected();
	}
}
/**
 * consult method comment.
 */
public boolean consult() {
	this.setState(PhoneModel.CONSULT);
	this.getListener().consult();
	return true;
}
/**
 * createCall method comment.
 */
public boolean createCall() {
	PhoneManager pm = this.getManager();
	boolean res = this.add(new Leg(new RawCall(pm), this, pm.getListener(), Leg.IDLE));
	if (res) {
		int state = this.getState();
		if (state == PhoneModel.IDLE)
			this.setState(PhoneModel.DIALTONE);
		if (state == PhoneModel.HOLD)
			this.setState(PhoneModel.CONSULT);
	}
	return res;
}
/**
 * Cause the phone to dial a set of digits
 */
public void dial(String digits) throws javax.telephony.InvalidPartyException, RawStateException {
	int state = this.getState();
	Leg leg = this.getActiveLeg();
		// test if we are on hold with no idle leg
	if (state == PhoneModel.HOLD && leg == null) {
		this.getManager().createCall(this, digits);
		this.consult();
		return;
	}
		// test if we are dialing on a idle leg
	if (state == PhoneModel.DIALTONE && leg.getState() == Leg.IDLE) {
		// legs with dialtone or ringtone on it are connected ;jw
		leg.connected();
		leg.getCall().dial(this, digits);
		this.getListener().dialing();
		return;
	}
		// test if we are sending on a connected leg
	if ((state == PhoneModel.ACTIVE || state == PhoneModel.CONSULT || state == PhoneModel.BRIDGED) && leg.getState() == Leg.CONNECTED) {
		this.sendDTMF(digits);
		return;
	}

	System.err.println("Error in state machine: ");
	new Exception().printStackTrace(System.err);
	
}
/**
 * dialing method comment.
 */
public void dialing() {
	this.getListener().dialing();
}
/**
 * drop the active leg
 */
public boolean drop() {
	Leg leg = this.getActiveLeg();
	if (leg != null)
		return leg.drop();
		
	return false;
}
/**
 * See if I'm logically equal to a TestPhone.  This calls the generic equals method.
 * Creation date: (2000-03-01 9:43:21)
 * @author: Richard Deadman
 * @return true if the TestPhone has me as its model
 * @param tp The test phone.
 */
public boolean equals(TestPhone tp) {
	return tp.equals(this);
}
/**
 * getCall method comment.
 */
private Leg getActiveLeg() {
	return this.activeCall;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-28 12:45:43)
 * @author: 
 * @return java.lang.String
 */
public java.lang.String getAddress() {
	return address;
}
/**
 * Return an array of Calls associated with me.
 */
public RawCall[] getCalls() {
	Vector<RawCall> calls = new Vector<RawCall>();
	int size = 0;
	Leg l = null;
	if ((l = this.getActiveLeg()) != null) {
		calls.add(l.getCall());
		size++;
	}
	if ((l = this.getHeldLeg()) != null) {
		calls.add(l.getCall());
		size++;
	}
	return (RawCall[])calls.toArray(new RawCall[size]);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-31 14:50:56)
 * @author: 
 * @return java.lang.StringBuffer
 */
private java.lang.StringBuffer getDigitBucket() {
	return digitBucket;
}
/**
 * Held leg accessor
 * Creation date: (2000-02-28 12:43:28)
 * @author: Richard Deadman
 * @return The leg on hold
 */
private Leg getHeldLeg() {
	return heldCall;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-01 9:46:36)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.emulator.PhoneListener
 */
private PhoneListener getListener() {
	return listener;
}
/**
 * Get the Phone Manager
 * Creation date: (2000-02-29 13:50:27)
 * @author: Richard Deadman
 * @return The manager of all phones
 */
private PhoneManager getManager() {
	return manager;
}
/**
 * Get any Play Thread associated with this terminal.
 * Creation date: (2000-05-10 15:27:55)
 * @author: Richard Deadman
 * @return A Thread currently playing on the phone, or null if none.
 */
Thread getPlayThread() {
	return playThread;
}
/**
 * Get any Record Thread associated with this terminal.
 * Creation date: (2000-05-10 15:27:55)
 * @author: Richard Deadman
 * @return A Thread currently recording on the phone, or null if none.
 */
Thread getRecordThread() {
	return recordThread;
}
/**
 * Get the DTMF event sending flag
 * Creation date: (2000-02-29 13:50:27)
 * @author: Richard Deadman
 * @return true if I am to send on DTMF incoming signals as events.
 */
private boolean getSendDtmf() {
	return this.sendDtmf;
}
/**
 * Get the state of the phone.
 * Creation date: (2000-02-28 12:42:27)
 * @author: Richard Deadman
 * @return int
 */
public int getState() {
	return state;
}
/**
 * Hold the phone
 */
public boolean hold() {
	if (this.getState() == RawPhone.ACTIVE) {
		Leg l = this.getActiveLeg();
		this.setHeldLeg(l);
		this.setActiveLeg(null);
		this.setState(RawPhone.HOLD);
		this.onHold();
		return true;
	}
	return false;
}
/**
 * idle method comment.
 */
public void idle() {
	this.setState(PhoneModel.IDLE);
	this.getListener().idle();
}
/**
 * inCall method comment.
 */
public void inCall(Leg newLeg) {
	switch (newLeg.getState()) {
		case Leg.ALERTING: {
			this.setState(PhoneModel.RINGING);
			break;
		}
		case Leg.INPROGRESS:
		case Leg.CONNECTED: {
			this.setState(PhoneModel.ACTIVE);
			break;
		}
		case Leg.IDLE: {
			this.setState(PhoneModel.DIALTONE);
			break;
		}
		default: {
			System.err.println("Error in Call Leg state machine: adding leg in state " + newLeg.getState());
			return;
		}
	}
	this.getListener().inCall(newLeg);
}
/**
 * onHold method comment.
 */
public boolean onHold() {
	this.setState(PhoneModel.HOLD);
	// notify framework
	String endpoint = this.getAddress();
	this.getManager().getListener().terminalConnectionHeld(this.getHeldLeg().getCall(),
		endpoint,
		endpoint,
		Event.CAUSE_NORMAL);
	return this.getListener().onHold();
}
/**
 * Act on incoming DTMF signals
 */
public void receiveDTMF(String digits) {
	// store them for future retrieval
	StringBuffer sb = this.getDigitBucket();
	synchronized (sb) {
		sb.append(digits);
	}
	// act on any RTCs?

	// act on resource properties
	if (this.getSendDtmf()) {
		this.getManager().getListener().mediaSignalDetectorDetected(this.getAddress(),
			net.sourceforge.gjtapi.media.SymbolConvertor.convert(digits));
	}

	// notify views
	this.getListener().receiving(digits);
}
/**
 * receiving method comment.
 */
public void receiving(String digits) {
	this.getListener().receiving(digits);
}
/**
 * drop method comment.
 */
public boolean remove(Leg leg) {
	boolean flag = false;
	Leg active = this.getActiveLeg();
	Leg held = this.getHeldLeg();
	if (active != null && active.equals(leg)) {
		this.setActiveLeg(null);
		flag = true;
	}
	if (held != null && held.equals(leg)) {	// just in case it's on both
		this.setHeldLeg(null);
		flag = true;
	}
	
	if (flag) {
		// notify any listener's of the new state
		if (this.getActiveLeg() == null && this.getHeldLeg() == null) {
			this.idle();
		} else if (this.getActiveLeg() != null) {
			this.connected();
		} else	// held must still exist -- go into consult mode
			this.consult();
	}
	
	return true;
}
/**
 * reportDTMF method comment.
 */
public String reportDTMF(int num) {
	StringBuffer sb = this.getDigitBucket();
	String results;
	synchronized (sb) {
		int len = sb.length();
		num = num > len ? len : num;
		results = sb.substring(0, num);
		sb.delete(0, num);
	}
	return results;
}
/**
 * ringing method comment.
 */
public void ringing() {
	this.setState(PhoneModel.RINGING);
	this.getListener().ringing();
}
/**
 * Notes whether to send detected DTMF signals as events
 * Creation date: (2000-05-10 13:57:17)
 * @author: Richard Deadman
 * @param flag if true, send the events, otherwise suppress them
 */
public void sendDetectedDtmf(boolean flag) {
	this.sendDtmf = flag;
}
/**
 * Send the DTMF out to all legs of the call.
 */
public void sendDTMF(java.lang.String msg) {
	this.getActiveLeg().getCall().sendDTMF(msg);
	this.getListener().sending();
}

/**
 * sending method comment.
 */
public void sending() {
	this.getListener().sending();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-28 12:43:03)
 * @author: 
 * @param newActiveCall net.sourceforge.gjtapi.raw.emulator.RawCall
 */
private void setActiveLeg(Leg newActiveLeg) {
	activeCall = newActiveLeg;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-28 12:45:43)
 * @author: 
 * @param newAddress java.lang.String
 */
private void setAddress(java.lang.String newAddress) {
	address = newAddress;
}
/**
 * Set a call leg as on hold
 * Creation date: (2000-02-28 12:43:28)
 * @author: Richard Deadman
 * @param newHeldLeg The new leg that is on hold
 */
private void setHeldLeg(Leg newHeldLeg) {
	heldCall = newHeldLeg;
}
/**
 * Set the RawListener that receives my events.
 * Creation date: (2000-03-01 9:46:36)
 * @author: Richard Deadman
 * @param newListener A listener interface for Raw events.
 */
private void setListener(PhoneListener newListener) {
	listener = newListener;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-29 13:50:27)
 * @author: 
 * @param newManager net.sourceforge.gjtapi.raw.emulator.PhoneManager
 */
private void setManager(PhoneManager newManager) {
	manager = newManager;
}
/**
 * Set the Player thread associated with the phone.
 * Creation date: (2000-05-10 15:27:55)
 * @author: Richard Deadman
 * @param newPlayThread A Thread playing audio on the active call.
 */
void setPlayThread(java.lang.Thread newPlayThread) {
	playThread = newPlayThread;
}
/**
 * Set the Recorder thread associated with the phone.
 * Creation date: (2000-05-10 15:27:55)
 * @author: Richard Deadman
 * @param newPlayThread A Thread recording audio from the active call.
 */
void setRecordThread(Thread newRecordThread) {
	this.recordThread = newRecordThread;
}
/**
 * Change the state of the Terminal/Address
 * Creation date: (2000-02-28 12:42:27)
 * @author: Richard Deadman
 * @param newState int
 */
private void setState(int newState) {
	state = newState;
}
/**
 * setStatus method comment.
 */
public void setStatus(String status) {
	System.out.println(this.getAddress() + " received " + status);
}
/**
 * swap method comment.
 */
public void swap(Leg oldLeg, Leg newLeg) {
	if (activeCall != null && activeCall.equals(oldLeg))
		this.setActiveLeg(newLeg);
	else if (heldCall != null && heldCall.equals(oldLeg))
		this.setHeldLeg(newLeg);
}
/**
 * swap method comment.
 */
public Leg swap(RawCall newCall, Leg oldLeg, TelephonyListener sink) {
	Leg active = this.getActiveLeg();
	Leg held = this.getHeldLeg();
	Leg l = null;
	// test if we need to merge the active and held calls
	if (held != null && held.getCall().equals(newCall)) {
		l = held;
		this.setActiveLeg(l);
		this.setHeldLeg(null);
		this.setState(PhoneModel.ACTIVE);
	}
	// test if we need to merge the active and held calls
	if (active != null && active.getCall().equals(newCall)) {
		l = active;
		this.setHeldLeg(null);
		this.setState(PhoneModel.ACTIVE);
	}
	if (l == null)
		l = new Leg(newCall, oldLeg, sink);
	return l;
}
/**
 * unHold method comment.
 */
public boolean unHold() {
	if (this.getState() == RawPhone.HOLD) {
		Leg l = this.getHeldLeg();
		this.setActiveLeg(l);
		this.setHeldLeg(null);
		this.setState(RawPhone.ACTIVE);
		// notify framework
		String endpoint = this.getAddress();
		this.getManager().getListener().terminalConnectionTalking(l.getCall(),
			endpoint,
			endpoint,
			Event.CAUSE_NORMAL);
		return true;
	}
	return false;
}
}
