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
import java.util.Map;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;

import net.sourceforge.gjtapi.CallId;
import net.sourceforge.gjtapi.RawStateException;
import net.sourceforge.gjtapi.TelephonyListener;
/**
 * This is the basic set of functionality common to JTAPI and Jcc
 * Creation date: (2000-10-04 13:44:49)
 * @author: Richard Deadman
 */
public interface CoreTpi {
/**
  * Add an observer for RawEvents
  *
  * @param ro New event listener
  * @return void
  **/
void addListener(TelephonyListener ro);
	 /**
	  * Answer a call that has appeared at a particular terminal
	  *
	  * @param call The system identifier for the call
	  * @param address The address of the connection that is ringing.
	  * @param terminal the terminal to answer the call on.
	  * @exception RawStateException A holder for a low-level state problem.
	  **/
	 void answerCall(CallId call, String address, String terminal) throws PrivilegeViolationException, ResourceUnavailableException,
	  MethodNotSupportedException, RawStateException; 
	/**
	 * Make a call.  Not that this follows the JTAPI sematics of an idle call
	 * being created synchronusly (two connections).  Events from the raw provider
	 * will indicate state transitions.
	 *
	 * @param id The callId reserved for the call.
	 * @param address The logical address to make a call from
	 * @param term The physical address for the call, if applicable
	 * @param dest The destination address
	 * @return A call Id.  This may be used later to track call progress.
	 * @exception RawStateException One of the objects is not in the correct state.
	 **/
	 CallId createCall(CallId id, String address, String term, String dest) throws ResourceUnavailableException, PrivilegeViolationException,
	  InvalidPartyException, InvalidArgumentException, RawStateException,
	  MethodNotSupportedException; 
/**
 * Ask the raw provider to update the capabilities offered by the provider
 * This is expected to return a map of capability names to strings.  If the string starts with
 * 't' or 'T', the capability is turned on, otherwise it is turned off.  Alternatively Boolean values
 * can be used.  If the value is not found for a key, the default value is used.
 * <P>To use this feature, the RawProvider needs to copy the GenericCapabilities.props file and change any
 * properties that are supported differently.  The the RawProvider could load the properties file into
 * a Properties object and return it.  If the default value is supported, then the corresponding line
 * may be omitted from the file.
 * Creation date: (2000-03-14 14:48:36)
 * @author: Richard Deadman
 * @return A properties file with name to value pairs for the basic raw provider functions.
 */
java.util.Properties getCapabilities();
/**
 * This allows for any context-specific parameters to be set.
 * The map may include such pairs as "name"="xxx" or "password"="yyy".
 * The provider is not active until this has
 * been called.  The property map may be null.
 * 
 * Creation date: (2000-02-11 12:13:36)
 * @author: Richard Deadman
 * @param props The name value properties map
 */
@SuppressWarnings("unchecked")
void initialize(Map props)
    throws ProviderUnavailableException;
/**
 * Tell the provider that it may release a call id for future use.  This is necessary to ensure that
 * TelephonyProvider call ids are not reused until the Generic JTAPI Framework layer is notified of
 * the death of the call.
 * Creation date: (2000-02-17 22:25:48)
 * @author: Richard Deadman
 * @param id The CallId that may be freed.
 */
void releaseCallId(CallId id);
/**
  * Remove a listener for RawEvents
  *
  * @param ro Listener to remove
  * @return void
  **/
void removeListener(TelephonyListener ro);
/**
 * Tell the provider to reserve a call id for future use.  The provider does not have to hang onto it.
 * Creation date: (2000-02-16 14:48:48)
 * @author: Richard Deadman
 * @return The CallId created by the provider.
 * @param term The address that the call will start on.  Used by muxes to isolate the correct provider.
 * @exception InvalidArgumentException If the Address is not in the provider's domain.
 */
CallId reserveCallId(String address) throws InvalidArgumentException;
/**
 * Perform any cleanup after my holder has finished with me.
 * Creation date: (2000-02-11 13:07:46)
 * @author: Richard Deadman
 */
void shutdown();
}
