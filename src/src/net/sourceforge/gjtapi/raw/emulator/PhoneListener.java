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
 * This describes the non-command events that a phone may respond to.
 * So, when a phone is added to a call, the phone will be notified using this interface.
 * Creation date: (2000-02-09 12:22:50)
 * @author: Richard
 */
public interface PhoneListener {
/**
 * Insert the method's description here.
 * Creation date: (2000-03-01 13:24:58)
 * @author: 
 */
void bridged();
/**
 * Tell the phone that the active call is now active
 * Creation date: (2000-02-09 12:24:46)
 * @author: 

 */
void connected();
/**
 * If I am in HOLD state and have added a CONSULTation call
 * Creation date: (2000-02-28 12:12:49)
 * @author: Richard Deadman
 */
boolean consult();
/**
 * Tell that phone that it has started dialing
 * Creation date: (2000-03-01 11:26:34)
 * @author: Richard Deadman
 */
void dialing();
/**
 * Tell the phone that it is now idle
 * Creation date: (2000-02-09 12:24:46)
 * @author: Richard Deadman

 */
void idle();
/**
 * Tell a phone a leg has joined.
 * Creation date: (2000-02-09 12:24:46)
 * @author: Richard Deadman
 * @param newLeg The leg added to the phone
 */
void inCall(Leg newLeg);
/**
 * If I now going into the HOLD state
 * Creation date: (2000-02-28 12:12:49)
 * @author: Richard Deadman
 */
boolean onHold();
/**
 * Insert the method's description here.
 * Creation date: (2000-03-01 11:31:41)
 * @author: 
 * @param digits java.lang.String
 */
void receiving(String digits);
/**
 * Insert the method's description here.
 * Creation date: (2000-03-01 9:48:45)
 * @author: 
 */
void ringing();
/**
 * Tell that phone that it has started sending DTMF
 * Creation date: (2000-03-01 11:26:34)
 * @author: Richard Deadman
 */
void sending();
}
