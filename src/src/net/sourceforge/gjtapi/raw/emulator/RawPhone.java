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
import net.sourceforge.gjtapi.TelephonyListener;
import net.sourceforge.gjtapi.CallId;
/**
 * This describes the interface to a simple test phone
 * Creation date: (2000-02-07 13:52:47)
 * @author: RichardDeadman
 */
public interface RawPhone {
	static final int IDLE = 0;
	static final int RINGING = 1;
	static final int ACTIVE = 2;
	static final int HOLD = 3;
	static final int DIALTONE = 4;
	static final int CONSULT = 5;
	static final int BRIDGED = 6;
/**
 * Add a Leg to the Phone.
 * Creation date: (2000-02-29 13:51:38)
 * @author: Richard Deadman
 * @return boolean
 * @param leg The leg to add to the phone
 */
boolean add(Leg leg);
/**
 * Tell's the phone to answer a certain call
 * Creation date: (2000-02-07 14:01:03)
 * @author: Richard Deadman
 * @return true if the call is answered.
 */
boolean answer(CallId call);
/**
 * If I am in CONSULT state, conference my active and held call together
 * Creation date: (2000-02-28 12:12:49)
 * @author: Richard Deadman
 */
boolean conference();
/**
 * If I am in IDLE or HOLD state, create a new call
 * Creation date: (2000-02-28 12:12:49)
 * @author: Richard Deadman
 */
boolean createCall();
/**
 * 
 * @param digits java.lang.String
 * @exception net.sourceforge.gjtapi.RawStateException The exception description.
 * @exception javax.telephony.InvalidPartyException The exception description.
 */
void dial(java.lang.String digits) throws net.sourceforge.gjtapi.RawStateException, javax.telephony.InvalidPartyException;
/**
 * Insert the method's description here.
 * Creation date: (2000-02-09 13:22:29)
 * @author: 
 * @return java.lang.String
 */
String getAddress();
/**
 * 
 * @return net.sourceforge.gjtapi.raw.emulator.RawCall[]
 */
net.sourceforge.gjtapi.raw.emulator.RawCall[] getCalls();
/**
 * Get the state of the phone.
 * Creation date: (2000-02-09 10:44:31)
 * @author: Richard Deadman
 * @return int
 */
int getState();
/**
 * Insert the method's description here.
 * Creation date: (2000-02-07 14:01:34)
 * @author: 
 * @return boolean
 */
boolean hold();
/**
 * Insert the method's description here.
 * Creation date: (2000-02-09 23:44:26)
 * @author: 
 * @param digits java.lang.String
 */
void receiveDTMF(String digits);
/**
 * 
 * @return boolean
 * @param leg net.sourceforge.gjtapi.raw.emulator.Leg
 */
boolean remove(Leg leg);
/**
 * Tell the phone to report back the first num received DTMF signals
 * Creation date: (2000-02-09 23:44:26)
 * @author: Richard Deadman
 * @param num The number of received digits to report
 */
String reportDTMF(int num);
/**
 * Notes whether to send detected DTMF signals as events
 * Creation date: (2000-05-10 13:57:17)
 * @author: Richard Deadman
 * @param flag if true, send the events, otherwise suppress them
 */
void sendDetectedDtmf(boolean flag);
/**
 * Tell the phone to send out the specified message
 * Creation date: (2000-02-09 23:44:26)
 * @author: Richard Deadman
 * @param msg The DTMF (or other) characters to broadcast.
 */
void sendDTMF(String msg);
/**
 * Insert the method's description here.
 * Creation date: (2000-08-06 23:35:57)
 * @author: Richard Deadman
 * @param status java.lang.String
 */
void setStatus(String status);
/**
 * Replace an old leg with a new one.  Used when a call is transferred or conferenced.
 * Creation date: (2000-02-28 15:07:34)
 * @author: Richard Deadman
 * @param oldLeg The existing leg to replace
 * @param newLeg The new leg
 */
void swap(Leg oldLeg, Leg newLeg);
/**
 * Replace an old leg with a new one.  Used when a call is transferred or conferenced.
 * Creation date: (2000-02-28 15:07:34)
 * @author: Richard Deadman
 * @param call The call to connect the phone to
 * @param oldLeg The existing leg to replace
 * @param sink The event sink for the new leg
 */
Leg swap(RawCall call, Leg oldLeg, TelephonyListener sink);
/**
 * Insert the method's description here.
 * Creation date: (2000-02-07 14:01:50)
 * @author: 
 * @return boolean
 */
boolean unHold();
}
