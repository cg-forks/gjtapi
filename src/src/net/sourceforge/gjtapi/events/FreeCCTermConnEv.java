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
/**
 * An Observer-style Callcontrol TermConnEv base class.
 * "dispatch()" is not overridden since listeners are not yet supported in callcontrol
 * Creation date: (2000-02-15 14:51:24)
 * @author: Richard Deadman
 */
public abstract class FreeCCTermConnEv extends FreeTerminalConnectionEvent implements javax.telephony.callcontrol.events.CallCtlTermConnEv {
/**
 * Protected FreeTermConnActiveEv constructor comment.
 * @param cause Cause identifier (see javax.telephony.Event)
 * @param metaCode The Observer-style MetaCode ohigher-level description
 * @param isNewMetaEvent Is this a MetaEvent?
 * @param tc The terminal connection the event applies to
 */
public FreeCCTermConnEv(int cause, int metaCode, boolean isNewMetaEvent, net.sourceforge.gjtapi.FreeTerminalConnection tc) {
	super(cause, metaCode, isNewMetaEvent, tc);
}
/**
 * Return the CallCtlEv callcontrol cause flag.  The only one we might want to throw is
 * CallCtlEv.CAUSE_TRANSFER, but the RawProvider is not aware of pseudo-transfers.
 */
public int getCallControlCause() {
	return javax.telephony.events.Ev.CAUSE_NORMAL;
}
  /**
   * Returns the called Address associated with this Call. The called
   * Address is defined as the Address to which the call has been originally
   * placed.
   * <p>
   * If the called address is unknown or not yet known, this method returns
   * null.
   * <p>
   * @return The called Address.
   */
public javax.telephony.Address getCalledAddress() {
	return null;
}
  /**
   * Returns the calling Address associated with this call. The calling
   * Address is defined as the Address which placed the telephone call.
   * <p>
   * If the calling address is unknown or not yet known, this method returns
   * null.
   * <p>
   * @return The calling Address.
   */
public javax.telephony.Address getCallingAddress() {
	return null;
}
  /**
   * Returns the calling Terminal associated with this Call. The calling
   * Terminal is defined as the Terminal which placed the telephone call.
   * <p>
   * If the calling Terminal is unknown or not yet know, this method returns
   * null.
   * <p>
   * @return The calling Terminal.
   */
public javax.telephony.Terminal getCallingTerminal() {
	return null;
}
  /**
   * Returns the last redirected Address associated with this Call.
   * The last redirected Address is the Address at which the current telephone
   * call was placed immediately before the current Address. This is common
   * if a Call is forwarded to several Addresses before being answered.
   * <p>
   * If the the last redirected address is unknown or not yet known, this
   * method returns null.
   * <p>
   * @return The last redirected Address for this telephone Call.
   */
public javax.telephony.Address getLastRedirectedAddress() {
	return null;
}
}
