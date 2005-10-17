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
import net.sourceforge.gjtapi.FreeCall;
import javax.telephony.*;
import javax.telephony.callcontrol.capabilities.*;
/**
 * This is a capabilities holder that manages both core and call control call capabilities.
 * This may be extended to other call capabilities packages in the future.
 * Creation date: (2000-03-14 13:16:05)
 * @author: Richard Deadman
 */
public class GenCallCapabilities extends BaseCap implements CallControlCallCapabilities, Cloneable {

	private boolean conference = false;
	private boolean connect = false;
	private boolean consultTCandDest = false;
	private boolean transferCall = false;
	private boolean transferDestination = false;
	
/**
 * canAddParty method comment.
 */
public boolean canAddParty() {
	return this.conference;
}
/**
 * canConference method comment.
 */
public boolean canConference() {
	return this.conference;
}
/**
 * canConnect method comment.
 */
public boolean canConnect() {
	return this.connect;
}
/**
 * forward to canConsult(TerminalConnection, String)
 */
public boolean canConsult() {
	return this.canConsult(null, null);
}
/**
 * canConsult method comment.
 */
public boolean canConsult(javax.telephony.TerminalConnection tc) {
	return false;
}
/**
 * canConsult method comment.
 */
public boolean canConsult(javax.telephony.TerminalConnection tc, java.lang.String destination) {
	return this.consultTCandDest;
}
/**
 * canDrop method comment.
 */
public boolean canDrop() {
	return true;
}
/**
 * canOffHook method comment.
 */
public boolean canOffHook() {
	return false;
}
/**
 * canSetConferenceController method comment.
 */
public boolean canSetConferenceController() {
	return this.conference;
}
/**
 * canSetConferenceEnable method comment.
 */
public boolean canSetConferenceEnable() {
	return this.conference;
}
/**
 * canSetTransferController method comment.
 */
public boolean canSetTransferController() {
	return this.transferCall;
}
/**
 * canSetTransferEnable method comment.
 */
public boolean canSetTransferEnable() {
	return this.transferCall;
}
/**
 * Forward to the support method for canTransfer(Call c).
 * @deprecated
 *
 */
public boolean canTransfer() {
	return this.canTransferCall();
}
/**
 * canTransfer method comment.
 */
public boolean canTransfer(java.lang.String destination) {
	return this.transferDestination;
}
/**
 * Forward to the supporting method for both me and canTransfer()
 */
public boolean canTransfer(javax.telephony.Call call) {
	return this.canTransferCall();
}
/**
 * Determine if a call can be transfered
 *
 */
private boolean canTransferCall() {
	return this.transferCall;
}
/**
 * Return the set of dynamic capabilities for a call.
 * Returns the dynamic capabilities for the instance of the Call object. Dynamic capabilities tell the application which actions are
	 possible at the time this method is invoked based upon the implementations knowledge of its ability to successfully perform the
	 action. This determination may be based upon argument passed to this method, the current state of the call model, or some
	 implementation-specific knowledge. These indications do not guarantee that a particular method can be successfully invoked,
	 however. 

	 <P>The dynamic call capabilities are based upon a Terminal/Address pair as well as the instance of the Call object. These parameters
	 are used to determine whether certain call actions are possible at the present. For example, the
	 CallCapabilities.canConnect() method will indicate whether a telephone call can be placed using the Terminal/Address pair
	 as the originating endpoint.

	 <P>Currently call capabilities are not constrained by Terminal/Address pairs.
 * Creation date: (2000-03-15 11:43:32)
 * @author: Richard Deadman
 * @return Call dynamic capabilities.
 * @param call The Call to reflect the capabilities of.
 */
public GenCallCapabilities getDynamic(FreeCall call, Terminal term, Address addr) {
	GenCallCapabilities caps = null;
	try {
		caps = (GenCallCapabilities)this.clone();
	} catch (CloneNotSupportedException cnse) {
		throw new RuntimeException("Cloneable error");
	}
	int state = call.getState();
	if (state == FreeCall.IDLE || state == FreeCall.INVALID) {
		caps.conference = caps.connect = false;
		caps.consultTCandDest = caps.transferCall = caps.transferDestination = false;
	}

	// modify based on originating terminal and address pair
	//if (term != null && addr != null) {
		
	//}
	
	caps.setDynamic(true);
	return caps;
}
/**
 * isObservable method comment.
 */
public boolean isObservable() {
	return true;
}
/**
 * Set the conference capability for calls
 * Creation date: (2000-03-15 10:52:44)
 * @author: Richard Deadman
 * @param val The capability value - true is conference can be invoked.
 */
void setConferenceCapability(boolean val) {
	this.conference = val;
}
/**
 * Change the connection capability for the call
 * Creation date: (2000-03-15 10:48:58)
 * @author: Richard Deadman
 * @param val The call connection capability
 */
void setConnectCapability(boolean val) {
	this.connect = val;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Call and CallControlCall " + (this.isDynamic() ? "dynamic" : "static") + " Capabilities";
}
/**
 * Update the transfer and conference flags depending on the call and terminalconnection capabilities.
 * Creation date: (2000-03-15 11:08:25)
 * @author: Richard Deadman
 * @param caps Capabilities for the terminal connections
 */
void updateCapabilities(GenTermConnCapabilities caps) {

	this.consultTCandDest = caps.canHold() && this.canConnect();

	this.transferCall = this.canConference() && caps.canLeave();

	this.transferDestination = this.consultTCandDest && this.transferCall;
}
}
