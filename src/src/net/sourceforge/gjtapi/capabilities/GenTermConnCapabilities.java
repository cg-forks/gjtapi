package net.sourceforge.gjtapi.capabilities;

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
import net.sourceforge.gjtapi.FreeTerminalConnection;
import javax.telephony.callcontrol.capabilities.*;
import javax.telephony.capabilities.*;
import net.sourceforge.gjtapi.media.*;
/**
 * Define static and dynamic TerminalConnection capabilities.
 * Creation date: (2000-03-15 9:25:13)
 * @author: Richard Deadman
 */
public class GenTermConnCapabilities extends BaseCap
	implements CallControlTerminalConnectionCapabilities,
		TerminalConnectionCapabilities,
		Cloneable {
	private boolean answer = false;
	private boolean hold = false;
	private boolean leave = false;
/**
 * canAnswer method comment.
 */
public boolean canAnswer() {
	return this.answer;
}
/**
 * canHold method comment.
 */
public boolean canHold() {
	return this.hold;
}
/**
 * canJoin method comment.
 */
public boolean canJoin() {
	return false;
}
/**
 * canLeave method comment.
 */
public boolean canLeave() {
	return this.leave;
}
/**
 * We can unhold if we can hold.
 */
public boolean canUnhold() {
	return this.hold;
}
/**
 * Return the set of dynamic capabilities for a terminal connection
 * Creation date: (2000-03-15 11:43:32)
 * @author: Richard Deadman
 * @return TerminalConnection dynamic capabilities.
 * @param tc The TerminalConnection to reflect the capabilities of.
 */
public GenTermConnCapabilities getDynamic(FreeTerminalConnection tc) {
	GenTermConnCapabilities caps = null;
	try {
		caps = (GenTermConnCapabilities)this.clone();
	} catch (CloneNotSupportedException cnse) {
		if (tc instanceof FreeMediaTerminalConnection) {
			caps = new GenMediaTermConnCapabilities();
		} else {
			caps = new GenTermConnCapabilities();
		}
		caps.answer = this.answer;
		caps.hold = this.hold;
		caps.leave = this.leave;
	}
	int state = tc.getState();
	switch (state) {
		case FreeTerminalConnection.IDLE:
		case FreeTerminalConnection.DROPPED:
		case FreeTerminalConnection.UNKNOWN: {
			caps.answer = false;
			// fall through to disallowing ringing actions
		}
		case FreeTerminalConnection.RINGING: {
			caps.hold = false;
			caps.leave = false;
			break;
		}
		case FreeTerminalConnection.ACTIVE:
		case FreeTerminalConnection.PASSIVE: {
			caps.answer = false;
			break;
		}
	}
	// now set the media availability
	if (caps instanceof GenMediaTermConnCapabilities)
		((GenMediaTermConnCapabilities)caps).setAvailable(((FreeMediaTerminalConnection)tc).getMediaAvailability() != 0);

	caps.setDynamic(true);
	return caps;
}
/**
 * Set the answering capability for terminal connections
 * Creation date: (2000-03-15 10:52:44)
 * @author: Richard Deadman
 * @param val The capability value - true is answer can be invoked.
 */
void setAnswerCapability(boolean val) {
	this.answer = val;
}
/**
 * Set the hold capability for terminal connections
 * Creation date: (2000-03-15 10:52:44)
 * @author: Richard Deadman
 * @param val The capability value - true is hold and unhold can be invoked.
 */
void setHoldCapability(boolean val) {
	this.hold = val;
}
/**
 * Set the leave capability for terminal connections
 * Creation date: (2000-03-15 10:52:44)
 * @author: Richard Deadman
 * @param val The capability value - true is leave can be invoked.
 */
void setLeaveCapability(boolean val) {
	this.leave = val;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "TerminalConnection and CallControlTerminalConnection " + (this.isDynamic() ? "dynamic" : "static") + " Capabilities";
}
}
