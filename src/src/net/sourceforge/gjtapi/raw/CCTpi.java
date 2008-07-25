package net.sourceforge.gjtapi.raw;

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
import net.sourceforge.gjtapi.*;
/**
 * These are the methods required by TelephonyProviders that support call control for JTAPI
 * Creation date: (2000-10-04 13:46:43)
 * @author: Richard Deadman
 */
public interface CCTpi extends BasicJtapiTpi {
/**
  * Put a call identified by a terminal and connection on hold (CallControlTerminalConnection)
  *
  * @param call The call that we are holding on.
  * @param address The address for the terminal that defines the call leg to hold
  * @param terminal The terminal that we want to make on hold
  * @exception RawException Some low-level exception occured.
  **/
void hold(CallId call, String address, String terminal) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException;
/**
  * Join one call to another call (Connection).
  * If the address and terminal are not null, they should refer to a TerminalConnection on
  * call1.
  * For assisted transfer, this allows the joining of an on-hold call to an active call.
  * Unassisted transfer and single-step transfer can be accomplished in the JTAPI layer by
  * combining this call with a release() method call.
  *
  * @param call1 One call
  * @param call Another call
  * @param address An address that may be used for knitting the calls together, if necessary.
  * @param terminal A terminal that may be used for knitting the calls together.
  * @return The new call.  This may be the same as one of the original calls.
  *
  * @exception RawException Some low-level state exception occured.
  **/
CallId join(CallId call1, CallId call2, String address, String terminal) throws RawStateException, InvalidArgumentException,
	MethodNotSupportedException, PrivilegeViolationException, ResourceUnavailableException;
/**
  * Take a call off hold (CallControlTerminalConnection)
  *
  * @param term The terminal that we want to take off hold
  * @param call The call to reconnect to
  *
  * @exception RawStateException Some low-level state exception occured.
  **/
void unHold(CallId call, String address, String terminal) throws RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException;
}
