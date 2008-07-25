package net.sourceforge.gjtapi.raw;

/*
	Copyright (c) 2002 ilink Kommunikationssysteme GmbH (www.ilink.de), Deadman Consulting (www.deadman.ca) 

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
import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.RawStateException;

/**
 * This interface is an extension of the CCTpi that allows a low-level provider
 * to provide an atomic call to accomplish "consult" functionality.
 * This is sometimes needed since CSTA telephony systems cannot make a
 * consultation call by holding an existing TerminalConnection, making a new call and
 * joining them together.
 * <P>ServiceProviders that implement this interface will automatically have 
 * a new low-level capability, "spconsult" added to their set of capabilities.
 * This will allow the FreeCall.consult() method to determine if the low-level
 * consult method should be used, or an assembled method using hold, createCall and join.
 * <P>Only service providers that are going to provide an implementation to
 * this method should implement it. If for some reason the service provider does
 * implement this method but do not support atomic consult, the service provider
 * should be sure to return a mapping of Capabilities.CONSULT => "f".
 */
public interface ConsultTpi extends CCTpi {
	/**
	 * Consult a device from an active call. Not that this follows the JTAPI
	 * sematics of an idle call being created synchronusly (two connections).
	 * Events from the raw provider will indicate state transitions. 
	 * <P>Preconditions:
	 * <ul>
	 * <li>existingCall is in an ACTIVE state
	 * <li>consultCall is a CallId that has been reserved but not yet bound by
	 * the low-level service provider to a call.
	 * </ul>
	 * 
	 * @param existingCall The CallId of the active call.
	 * @param consultCall The CallId that has been allocated but is "IDLE".
	 * @param address The logical address to make a consultation from 
	 * @param term The physical address for the consultation, if applicable 
	 * @param dest The destination address 
	 * @exception RawStateException One of the objects is not in the correct state. 
	 **/ 
	void consult(CallId existingCall, CallId consultCall, String address, String term, String dest)
		throws ResourceUnavailableException, PrivilegeViolationException,
			InvalidPartyException, InvalidArgumentException, RawStateException,
			MethodNotSupportedException;
}

