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
import javax.telephony.*;
/**
 * This tracks phones and calls and manages their basic connections.
 * Creation date: (2000-02-09 10:08:48)
 * @author: Richard Deadman
 */
import java.util.*;
import net.sourceforge.gjtapi.*;

public class PhoneManager {
	private Hashtable<String, TestPhone> phones = new Hashtable<String, TestPhone>();	// map addresses to phones 1:1
	private TelephonyListener listener;	// shared listener
/**
 * Create a Phone Manager for a set of addresses.
 * For each Address, a virtual phone is created.
 * Creation date: (2000-02-09 10:54:54)
 * @author: Richard Deadman
 * @param addresses An array of phone address to manage
 * @param listener A listener to report Raw events to.
 */
public PhoneManager(String[] addresses, TelephonyListener listener) {
	super();
	Hashtable<String, TestPhone> p = this.getPhones();
	for (int i = 0; i < addresses.length; i++) {
		p.put(addresses[i], new TestPhone(addresses[i], this));
	}

	this.setListener(listener);
}
/**
 * This allows a call to be made to another devices.  Should only be called internally.
 * Creation date: (2000-02-09 10:33:08)
 * @author: Richard Deadman
 * @return a new Call object
 * @param from The phone initiating the call
 * @param to The address being called.
 */
RawCall createCall(RawPhone from, String to) throws RawStateException, InvalidPartyException {
	RawCall call = null;
	RawPhone phone = this.getPhone(to);
	if (phone != null) {
		if (phone.getState() != RawPhone.IDLE) {
			int state = phone.getState();
			int jstate = TerminalConnection.UNKNOWN;
			switch (state) {
				case RawPhone.ACTIVE: {
					jstate = TerminalConnection.ACTIVE;
				}
				case RawPhone.DIALTONE: {
					jstate = TerminalConnection.ACTIVE;
				}
				case RawPhone.HOLD: {
					jstate = javax.telephony.callcontrol.CallControlTerminalConnection.HELD;
				}
				case RawPhone.RINGING: {
					jstate = TerminalConnection.RINGING;
				}
			}
			String name = phone.getAddress();
			throw new RawStateException(null, name, name,
						RawStateException.TERMINAL_CONNECTION_OBJECT,
						jstate);
		}
		// make the call
		call = new RawCall(this);
		new Leg(call, from, this.getListener(), Leg.INPROGRESS);
		new Leg(call, phone, this.getListener(), Leg.ALERTING);
	} else {
		throw new InvalidPartyException(javax.telephony.InvalidPartyException.DESTINATION_PARTY);
	}
	return call;
}
/**
 * Get the listener for the Raw events
 * Creation date: (2000-02-14 10:58:21)
 * @author: Richard Deadman
 * @return The event listener
 */
TelephonyListener getListener() {
	return this.listener;
}
/**
 * Get the phone associated with a given address.  This assumes a 1:1 relationship.
 * Creation date: (2000-02-10 10:58:06)
 * @author: Richard Deadman
 * @return The phone associated with the given address
 * @param address A string address for the phone
 */
RawPhone getPhone(String address) {
	return (RawPhone)this.getPhones().get(address);
}
/**
 * Returns a Hashtable that maps phone numbers to RawPhone instances
 * Creation date: (2000-02-09 10:31:08)
 * @author: Richard Deadman
 * @return a Hashtable
 */
Hashtable<String, TestPhone> getPhones() {
	return phones;
}
/**
 * Set the listener that has an interest in our Raw events
 * Creation date: (2000-02-14 10:58:21)
 * @author: Richard Deadman
 * @param newListener The event listener.
 */
void setListener(TelephonyListener newListener) {
	this.listener = newListener;
}
}
