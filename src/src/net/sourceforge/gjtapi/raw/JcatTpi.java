/*
	Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca) 

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
package net.sourceforge.gjtapi.raw;

import javax.telephony.InvalidPartyException;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.ResourceUnavailableException;

import net.sourceforge.gjtapi.CallId;

/**
 * This is an extension to the JccTPI to provide support for the
 * Jcat capabilities:
 * <ul>
 *   <li>Displaying text (i.e. test messaging)
 *   <li>Register/Deregistering Addresses with Terminals
 *   <li>Suspending and Reconnecting Connections on a call.
 * </ul>
 * A service provider only needs to implement this method if it intends
 * to support the Jcat facilities that use this interface.
 * @author rdeadman
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public interface JcatTpi extends JccTpi {
	
	//----------- Display Text at an address -----------
	/**
	 * Sets some text on the display of the device(s)
	 * currently associated with this address.
	 * @param address
	 * @param text
	 */
	void setText(String address, String text);
	
	/**
	 * Gets the text currently on the display of the device
	 * currently connected to the address.
	 * <P>Unclear what to do if more than one device on that
	 * Address has text.
	 * @param address
	 * @return
	 */
	String getText(String address);
	/**
	 * Can I display text on this address?
	 * @param address
	 * @return
	 */
	boolean canDisplayText(String address);

	// --------- registering addresses and terminals ------

	/**
	 * Register the address with the terminal
	 * @param address
	 * @param terminal
	 * @throws InvalidPartyException
	 * @throws MethodNotSupportedException
	 */	
	void register(String address, String terminal) throws InvalidPartyException, MethodNotSupportedException;
	/**
	 * Unregister the address with the terminal
	 * @param address
	 * @param terminal
	 * @throws InvalidPartyException
	 * @throws MethodNotSupportedException
	 */	
	void unregister(String address, String terminal) throws InvalidPartyException, MethodNotSupportedException;

	//--------- Suspend and Reconnect Connections ----------
	/**
	 * Suspend a Connection from the call
	 * @param call
	 * @param address that specifies a Connection to the call
	 * @throws InvalidStateException
	 * @throws MethodNotSupportedException
	 * @throws ResourceUnavailableException
	 */	
	void suspend(CallId call, String address) throws InvalidStateException, MethodNotSupportedException, ResourceUnavailableException;
	/**
	 * Reconnect a suspended connection back to its call.
	 * @param call
	 * @param address The address that specifies which Connection to reconnect.
	 * @throws InvalidStateException
	 * @throws MethodNotSupportedException
	 * @throws ResourceUnavailableException
	 */
	void reconnect(CallId call, String address) throws InvalidStateException, MethodNotSupportedException, ResourceUnavailableException;
}
