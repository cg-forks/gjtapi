package net.sourceforge.gjtapi.events;

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
import java.util.*;
import javax.telephony.callcontrol.*;
import javax.telephony.callcontrol.events.*;
import javax.telephony.events.*;
/**
 * This is the CallControl Call (and therefore TerminalConnection) event superclass.
 * <P>When a Call or TerminalConnection event is displatched, its CallControl twin should also
 * be dispatched if the Observer implements CallControlCallObserver.
 * Creation date: (2000-08-03 11:19:22)
 * @author: Richard Deadman
 */
public abstract class CCCallEv extends FreeCallEvent implements CallCtlCallEv {
	private int ccCause = CallCtlEv.CAUSE_UNKNOWN;
/**
 * Create an instance of a CallControl Call event.
 * @param cause An event field defined in CallCtlEv
 * @param metaCode The metacode id
 * @param isNewMetaEvent Is this the start of a new metacode
 * @param c A generic framework Call
 */
public CCCallEv(int cause, int metaCode, boolean isNewMetaEvent, net.sourceforge.gjtapi.FreeCall c) {
	super(ccEvToBasicEv(cause), metaCode, isNewMetaEvent, c);

	this.setCcCause(cause);
}
/**
 * Create a CallControl Call event.
 * @param cause From CallCtlEv.
 * @param c A generic framework FreeCall
 */
public CCCallEv(int cause, net.sourceforge.gjtapi.FreeCall c) {
	super(ccEvToBasicEv(cause), c);

	this.setCcCause(cause);
}
/**
 * Given a basic event cause, translate to CallCtlEv style cause
 */
public static int basicEvToCcEv(int eventCause){
  int ret = CallCtlEv.CAUSE_UNKNOWN;
  switch (eventCause) {
	  	// simple mapping
	case Ev.CAUSE_CALL_CANCELLED:
	  ret = CallCtlEv.CAUSE_CALL_CANCELLED;
	  break;
	case Ev.CAUSE_DEST_NOT_OBTAINABLE:
	  ret= CallCtlEv.CAUSE_DEST_NOT_OBTAINABLE;
	  break;
	case Ev.CAUSE_INCOMPATIBLE_DESTINATION:
	  ret = CallCtlEv.CAUSE_INCOMPATIBLE_DESTINATION;
	  break;
	case Ev.CAUSE_LOCKOUT:
	  ret = CallCtlEv.CAUSE_LOCKOUT;
	  break;
	case Ev.CAUSE_NETWORK_CONGESTION:
	  ret = CallCtlEv.CAUSE_NETWORK_CONGESTION;
	  break;
	case Ev.CAUSE_NETWORK_NOT_OBTAINABLE:
	  ret = CallCtlEv.CAUSE_NETWORK_NOT_OBTAINABLE;
	  break;
	case Ev.CAUSE_NEW_CALL:
	  ret = CallCtlEv.CAUSE_NEW_CALL;
	  break;
	case Ev.CAUSE_NORMAL:
	  ret = CallCtlEv.CAUSE_NORMAL;
	  break;
	case Ev.CAUSE_RESOURCES_NOT_AVAILABLE:
	  ret= CallCtlEv.CAUSE_RESOURCES_NOT_AVAILABLE;
	  break;
	case Ev.CAUSE_SNAPSHOT:
	  ret= CallCtlEv.CAUSE_SNAPSHOT;
	  break;
	default:
	  break;
  }
  return ret;
}
/**
 * Given a CallCtl event cause, translate to Ev style cause
 */
public static int ccEvToBasicEv(int ccEventCause){
  int ret = Ev.CAUSE_UNKNOWN;
  switch (ccEventCause) {
	  	// simple mapping
	case CallCtlEv.CAUSE_CALL_CANCELLED:
	  ret = Ev.CAUSE_CALL_CANCELLED;
	  break;
	case CallCtlEv.CAUSE_DEST_NOT_OBTAINABLE:
	  ret= Ev.CAUSE_DEST_NOT_OBTAINABLE;
	  break;
	case CallCtlEv.CAUSE_INCOMPATIBLE_DESTINATION:
	  ret = Ev.CAUSE_INCOMPATIBLE_DESTINATION;
	  break;
	case CallCtlEv.CAUSE_LOCKOUT:
	  ret = Ev.CAUSE_LOCKOUT;
	  break;
	case CallCtlEv.CAUSE_NETWORK_CONGESTION:
	  ret = Ev.CAUSE_NETWORK_CONGESTION;
	  break;
	case CallCtlEv.CAUSE_NETWORK_NOT_OBTAINABLE:
	  ret = Ev.CAUSE_NETWORK_NOT_OBTAINABLE;
	  break;
	case CallCtlEv.CAUSE_NEW_CALL:
	  ret = Ev.CAUSE_NEW_CALL;
	  break;
	case CallCtlEv.CAUSE_NORMAL:
	  ret = Ev.CAUSE_NORMAL;
	  break;
	case CallCtlEv.CAUSE_RESOURCES_NOT_AVAILABLE:
	  ret= Ev.CAUSE_RESOURCES_NOT_AVAILABLE;
	  break;
	case CallCtlEv.CAUSE_SNAPSHOT:
	  ret= Ev.CAUSE_SNAPSHOT;
	  break;
	  	// translations
	case CallCtlEv.CAUSE_BUSY:
	case CallCtlEv.CAUSE_CALL_NOT_ANSWERED:
	  ret= Ev.CAUSE_DEST_NOT_OBTAINABLE;
	  break;
	  	// the rest are unknown
	/*case CallCtlEv.CAUSE_ALTERNATE:
	case CallCtlEv.CAUSE_CALL_BACK:
	case CallCtlEv.CAUSE_CALL_PICKUP:
	case CallCtlEv.CAUSE_CONFERENCE:
	case CallCtlEv.CAUSE_DO_NOT_DISTURB:
	case CallCtlEv.CAUSE_PARK:
	case CallCtlEv.CAUSE_REDIRECTED:
	case CallCtlEv.CAUSE_REORDER_TONE:
	case CallCtlEv.CAUSE_TRANSFER:
	case CallCtlEv.CAUSE_TRUNKS_BUSY:
	case CallCtlEv.CAUSE_UNHOLD:
	case CallCtlEv.CAUSE_UNKNOWN:
	*/
	default:
	  break;
  }
  return ret;
}
/**
 * Get the CallControl cause id.
 * @return The CallControl cause id as defined in CallCtlEv
 */
public int getCallControlCause() {
	return this.ccCause;
}
/**
 * Delegate this off to my call.
 */
public javax.telephony.Address getCalledAddress() {
	return ((CallControlCall)this.getCall()).getCalledAddress();
}
/**
 * Delegate this off to my call.
 */
public javax.telephony.Address getCallingAddress() {
	return ((CallControlCall)this.getCall()).getCallingAddress();
}
/**
 * Delegate this off to my call.
 */
public javax.telephony.Terminal getCallingTerminal() {
	return ((CallControlCall)this.getCall()).getCallingTerminal();
}
/**
 * Delegate this off to my call.
 */
public javax.telephony.Address getLastRedirectedAddress() {
	return ((CallControlCall)this.getCall()).getLastRedirectedAddress();
}
/**
 * Internal setter for the CallControl cause id.
 * Creation date: (2000-08-03 12:37:10)
 * @param newCcCause A value from CallCtlEv that identifies the Call Control event cause.
 */
private void setCcCause(int newCcCause) {
	this.ccCause = newCcCause;
}
/**
 * Take an array or Call or TerminalConnection events and translate them to CallControl events.
 * Creation date: (2000-08-03 13:33:51)
 * @return An array of CallCtlCallEv[]
 * @param evs The basic CallEv[]
 */
public static CCCallEv[] toCcEvents(FreeCallEvent[] evs) {
	if (evs == null || evs.length == 0)
		return null;
	Set set = new HashSet();
	int size = evs.length;
	for (int i = 0; i < size; i++) {
		FreeCallEvent ev = evs[i];
		switch (ev.getID()) {
			case TermConnActiveEv.ID: {
				// see if we have a held or talking TerminalConnection
				FreeTermConnActiveEv activeEv = (FreeTermConnActiveEv)ev;
				CCTermConnEv tcev = null;
				if (activeEv.isTalking())
					tcev = new CCTermConnTalkingEv(CCCallEv.basicEvToCcEv(ev.getCause()),
						(FreeTerminalConnection)((FreeTerminalConnectionEvent)ev).getTerminalConnection());
				else
					tcev = new CCTermConnHeldEv(CCCallEv.basicEvToCcEv(ev.getCause()),
						(FreeTerminalConnection)((FreeTerminalConnectionEvent)ev).getTerminalConnection());
				set.add(tcev);
				break;
			}
			case TermConnDroppedEv.ID: {
				set.add(new CCTermConnDroppedEv(CCCallEv.basicEvToCcEv(ev.getCause()),
					(FreeTerminalConnection)((FreeTerminalConnectionEvent)ev).getTerminalConnection()));
				break;
			}
			case TermConnRingingEv.ID: {
				set.add(new CCTermConnRingingEv(CCCallEv.basicEvToCcEv(ev.getCause()),
					(FreeTerminalConnection)((FreeTerminalConnectionEvent)ev).getTerminalConnection()));
				break;
			}
			case TermConnUnknownEv.ID: {
				set.add(new CCTermConnUnknownEv(CCCallEv.basicEvToCcEv(ev.getCause()),
					(FreeTerminalConnection)((FreeTerminalConnectionEvent)ev).getTerminalConnection()));
				break;
			}
		}
	}
	return (CCCallEv[])set.toArray(new CCCallEv[0]);
}
}
