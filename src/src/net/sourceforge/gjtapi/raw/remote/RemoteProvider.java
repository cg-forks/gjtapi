package net.sourceforge.gjtapi.raw.remote;

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
import java.io.*;
import java.util.Dictionary;
import javax.telephony.media.*;
import javax.telephony.*;
import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.media.*;
import java.rmi.*;

/* Copyright UForce Inc. (for now) */
/**
 * This is a remote version of the Generic JTAPI Service Provider.
 *
 * @author: Richard Deadman
 **/

public interface RemoteProvider extends Remote {
/**
  * Add a listener for RawEvents
  *
  * @param rl New listener
  * @return void
  * @exception RemoteException A distribution (network) error has occured.
  *
  * @author: Richard Deadman
  **/
void addListener(RemoteListener rl) throws RemoteException;
/**
  * Allocate a media resource for a terminal
  *
  * @param terminal The terminal to attach a media resource to.
  * @param type A constant defining the type of resource to add.
  * @param params A dictionary of resource control parameters.
  * @return true if the resource is allocated
  **/
boolean allocateMedia(String terminal, int type, Dictionary params) throws RemoteException;
/**
  * Answer a call that has appeared at a particular terminal
  *
  * @param call The system identifier for the call
  * @param address The address the call is to be answered on.
  * @param terminal the terminal to answer the call on.
  * @exception RemoteException A distribution problem has occured.
  **/
void answerCall(SerializableCallId call, String address, String terminal) throws RemoteException, PrivilegeViolationException, ResourceUnavailableException,
	  MethodNotSupportedException, RawStateException;
/**
 * attachMedia method comment.
 */
public boolean attachMedia(SerializableCallId call, java.lang.String address, boolean onFlag) throws RemoteException;
/**
 * beep method comment.
 */
public void beep(SerializableCallId call) throws RemoteException;
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
 * @exception RemoteException A distribution (network) error has occured.
 *
 * @author: Richard Deadman
 **/
SerializableCallId createCall(SerializableCallId id, String address, String term, String dest) throws RemoteException, ResourceUnavailableException, PrivilegeViolationException,
	  InvalidPartyException, InvalidArgumentException, RawStateException,
	  MethodNotSupportedException;
/**
  * Free a media resource from a terminal
  *
  * @param terminal The terminal to release a media resource from.
  * @param type A constant defining the type of resource to release.
  * @return true if the resource is freed.
  **/
boolean freeMedia(String terminal, int type) throws RemoteException;
/**
 * Get a list of available addresses
 * Creation date: (2000-02-11 12:29:00)
 * @author: Richard Deadman
 * @return An array of address names
 * @exception RemoteException A distribution (network) error has occured.
 * @exception ResourceUnavailableException If the Address set is too big to return.
 */
String[] getAddresses() throws RemoteException, ResourceUnavailableException;
/**
 * Get all the addresses associated with an terminal.
 * Generally this is only called during dynamic Terminal exploration.
 * Creation date: (2000-06-05 12:30:54)
 * @author: Richard Deadman
 * @return An array of address names.
 * @param terminal The terminal we want addresses for.
 * @exception RemoteException A distribution (network) error has occured.
 * @exception InvalidArgumentException If the terminal is not known.
 */
String[] getAddresses(String terminal) throws RemoteException, InvalidArgumentException;
/**
 * getAddressType method comment.
 */
public int getAddressType(java.lang.String name) throws RemoteException;
/**
 * 
 * @return net.sourceforge.gjtapi.CallData
 * @param id net.sourceforge.gjtapi.CallId
 * @exception java.rmi.RemoteException The exception description.
 */
net.sourceforge.gjtapi.CallData getCall(net.sourceforge.gjtapi.CallId id) throws java.rmi.RemoteException;
/**
 * Ask the raw TelephonyProvider to give a snapshot of all Calls on an Address.
 * <P>This will only be called on a TelephonyProvider that "trottle"s call events.
 * <P><B>Note:</B> This implies that the given Call will have events delivered on it until such time
 * as a "TelephonyProvider::releaseCallId(CallId)".
 * Creation date: (2000-06-20 15:22:50)
 * @author: Richard Deadman
 * @return A set of call data.
 * @param number The Address's logical number
 */
CallData[] getCallsOnAddress(String number) throws RemoteException;
/**
 * Ask the raw TelephonyProvider to give a snapshot of all Calls at a Terminal.
 * <P>This will only be called on a TelephonyProvider that "trottle"s call events.
 * <P><B>Note:</B> This implies that the given Calls will have events delivered on it until such time
 * as a "TelephonyProvider::releaseCallId(CallId)".
 * Creation date: (2000-06-20 15:22:50)
 * @author: Richard Deadman
 * @return A set of call data.
 * @param name The Terminal's logical name
 */
CallData[] getCallsOnTerminal(String name) throws RemoteException;
/**
 * Ask the raw provider to update the capabilities offered by the provider
 * This is expected to return a map of capability names to strings.  If the string starts with
 * 't' or 'T', the capability is turned on, otherwise it is turned off.  If the value is not found for
 * a key, the default value is used.
 * <P>To use this feature, the RawProvider needs to copy the GenericCapabilities.props file and change any
 * properties that are supported differently.  The the RawProvider could load the properties file into
 * a Properties object and return it.  If the default value is supported, then the corresponding line
 * may be omitted from the file.  Note that the generic layer may choose to ignore certain
 * settings if they are not supported at that layer.
 * Creation date: (2000-03-14 14:48:36)
 * @author: Richard Deadman
 * @return java.util.Map
 */
java.util.Properties getCapabilities() throws RemoteException;
/**
 * getDialledDigits method comment.
 */
public java.lang.String getDialledDigits(SerializableCallId id, java.lang.String address) throws RemoteException;
/**
 * Get the PrivateData from a raw TelephonyProviderInterface Object.
 * This applies to the previous action on the Object.
 * <P>This method is mapped to any of six logical raw TPI provider objects based on the following
 * mapping, where the three columns represent the three method parameters:
 * <table BORDER >
 *<tr BGCOLOR="#C0C0C0">
 *<td></td>
 *
 *<th>Call</th>
 *
 *<th>Address</th>
 *
 *<th>Terminal</th>
 *</tr>
 *
 *<tr>
 *<td>Provider</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Call</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Address</td>
 *
 *<td></td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Terminal</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td>X</td>
 *</tr>
 *
 *<tr>
 *<td>Connection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>TerminalConnection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *</tr>
 *
 * <caption ALIGN=BOTTOM><i>"blank" indicates void</i></caption>
 * </table>
 * Creation date: (2000-08-05 22:25:45)
 * @return Any serializable object.  IF PrivateDataCapabilities.canGetPrivateData() returns false, this will return null.
 * @param call A CallId or void
 * @param address An Address name or void
 * @param terminal A Terminal name or void
 * @throws NotSerializableException If the returned object is not serializable.
 */
Serializable getPrivateData(CallId call, String address, String terminal) throws RemoteException, NotSerializableException;
/**
 * Get a list of available terminals.
 * This may be null if the Telephony (raw) Provider does not support Terminals.
 * If the Terminal set it too large, this will throw a ResourceUnavailableException
 * <P>Since we went to lazy connecting between Addresses and Terminals, this is called so
 * we don't have to follow all Address->Terminal associations to get the full set of Terminals.
 * Creation date: (2000-02-11 12:29:00)
 * @author: Richard Deadman
 * @return An array of terminal names, media type containers.
 * @exception ResourceUnavailableException if the set it too large to be returned dynamically.
 */
TermData[] getTerminals() throws ResourceUnavailableException, RemoteException;
/**
 * Get all the terminals associated with an address.
 * Creation date: (2000-02-11 12:30:54)
 * @author: Richard Deadman
 * @return An array of terminal name, media type containers.
 * @param address The address number we want terminal names for.
 * @throws InvalidArgumentException indicating that the address is unknown.
 * @exception RemoteException A distribution (network) error has occured.
 */
TermData[] getTerminals(String address) throws RemoteException, InvalidArgumentException;
/**
  * Put a call on hold (CallControlTerminalConnection)
  *
  * @param term The terminal that we want to make on hold
  * @param address The address on the terminal that holds the call leg to hold
  * @exception RemoteException A distribution (network) error has occured.
  *
  * @author: Richard Deadman
  **/
void hold(SerializableCallId call, String address, String term) throws RemoteException, RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException;
/**
 * This allows for any context-specific parameters to be set.
 * The provider is not active until this has
 * been called.  The props may be null.
 * 
 * Creation date: (2000-02-11 12:13:36)
 * @author: Richard Deadman
 * @param props A name-value properties map
 * @exception RemoteException A distribution (network) error has occured.
 */
void initialize(java.util.Map props) throws RemoteException, ProviderUnavailableException;
/**
 * Ask the RawProvider if the named terminal is media-capable.
 * Creation date: (2000-03-07 15:35:02)
 * @author: Richard Deadman
 * @return true if the terminal can be used with media control commands.
 * @param terminal The raw-provider specific unique name for the terminal
 */
boolean isMediaTerminal(String terminal) throws RemoteException;
/**
  * Join one call to another call (Connection)
  * For assisted transfer, this allows the joining of an on-hold call to an active call.
  * Unassisted transfer and single-step transfer can be accomplished in the JTAPI layer by
  * combining this call with a release() method call.
  *
  * @param call1 One call
  * @param call Another call
  * @param address The address of a TerminalConnection to be used as a ConferenceController
  * @param terminal The terminal of a TerminalConnection to be used as a ConferenceController
  * @return The new call.  This may be the same as one of the original calls.
  *
  * @exception RemoteException A distribution (network) error has occured.
  *
  * @author: Richard Deadman
  **/
CallId join(SerializableCallId call1, SerializableCallId call2, String address, String terminal) throws RemoteException, RawStateException, InvalidArgumentException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException;
	 /**
	  * Start playing a set of audio streams named by the streamIds (may be urls).
	  *
	  * @param terminal The terminal to play the audio on.
	  * @param streamIds The ids for the audi streams to play, usually URLs
	  * @param int offset The number of milliseconds into the audio to start
	  * @param rtcs A set of runtime control sets that tune the playing.
	  * @param optArgs A dictionary of control arguments.
	  *
	  * @exception MediaResourceException A wrapper for a PlayerEvent that describes what went wrong.
	  **/
	 void play(String terminal, String[] streamIds, int offset, RTCHolder[] rtcs, Dictionary optArgs) throws MediaResourceException, RemoteException; 
/**
  * Start recording an audio streams named by the streamId (may be urls).
  *
  * @param terminal The terminal to record the audio from.
  * @param streamId The id for the audio streams to create, usually a URL
  * @param rtcs A set of runtime control sets that tune the recording.
  * @param optArgs A dictionary of control arguments.
  *
  * @exception MediaResourceException A wrapper for a RecorderEvent that describes what went wrong.
  **/
void record(String terminal, String streamId, RTCHolder[] rtcs, Dictionary optArgs) throws MediaResourceException, RemoteException;
/**
  * Release a connection to a call (Connection)
  *
  * @param address The address that we want to release
  * @param call The call to disconnect from
  *
  * @exception RemoteException A distribution (network) error has occured.
  *
  * @author: Richard Deadman
  **/
void release(String address, SerializableCallId call) throws RemoteException, PrivilegeViolationException,
	ResourceUnavailableException, MethodNotSupportedException, RawStateException;
/**
 * Tell the provider that it may release a call id for future use.  This is necessary to ensure that
 * provider call ids are not released until the JTAPI layer is notified of the death of the call.
 * Creation date: (2000-02-17 22:25:48)
 * @author: Richard Deadman
 * @param id The CallId that may be freed.
 */
void releaseCallId(SerializableCallId id) throws RemoteException;
/**
  * Remove a listener for RawEvents
  *
  * @param rl Listener to remove
  * @return void
  * @exception RemoteException A distribution (network) error has occured.
  *
  * @author: Richard Deadman
  **/
void removeListener(RemoteListener rl) throws RemoteException;
/**
 * Tell the RawProvider that Calls which reach this Address should have Call, Connection and TerminalConnections
 * events reported on them.
 * Creation date: (2000-03-07 15:17:16)
 * @author: Richard Deadman
 * @param address The name of the address to change the reporting status on.
 * @param flag true if reporting is to be enabled, false to stop events relating to the call.
 * @exception InvalidArgumentException If the address is not known by the telephony provider.
 * @exception ResourceUnavailableException If the provider could not find the reources to report the calls.
 */
void reportCallsOnAddress(String address, boolean flag) throws RemoteException, InvalidArgumentException, ResourceUnavailableException;
/**
 * Tell the RawProvider that Calls which reach this Terminal should have Call, Connection and TerminalConnections
 * events reported on them.
 * Creation date: (2000-03-07 15:17:16)
 * @author: Richard Deadman
 * @return true if the terminal is known, false otherwise
 * @param address The name of the terminal to change the reporting status on.
 * @param flag true if reporting is to be enabled, false to stop events relating to the call.
 * @exception InvalidArgumentException If the terminal is not known by the telephony provider.
 * @exception ResourceUnavailableException If the reporting could not be started due to resource constraints.
 */
void reportCallsOnTerminal(String terminal, boolean flag) throws RemoteException, InvalidArgumentException, ResourceUnavailableException;
/**
  * Tell the provider to reserve a call id for future use.  The provider does not have to hang onto it.
  * Creation date: (2000-02-16 14:48:48)
  * @author: Richard Deadman
  * @return The CallId created by the provider.
  * @param address The address the call will start from.
  * @exception RemoteException A distribution (network) error has occured.
  * @exception InvalidArgumentException The remote provider does not know the Address.
  *
  * @author: Richard Deadman
  **/
SerializableCallId reserveCallId(String address) throws RemoteException, InvalidArgumentException;
/**
  * Receive DTMF tones from a terminal
  *
  * @param terminal The terminal the signal receiver is attached to.
  * @param num The number of signals to retrieve
  * @param syms A set of symbols patterns to return
  * @param rtcs A set of runtime control sets that tune the signalling.
  * @param optArgs A dictionary of control arguments.
  * @return A RawSigDetectEvent factory for creating an event that can be sent getSignalBuffer() to retrieve the signals.
  *
  * @exception MediaResourceException A wrapper for a SignalDetectorEvent that describes what went wrong.
  **/
RawSigDetectEvent retrieveSignals(String terminal, int num, SymbolHolder[] patterns, RTCHolder[] rtcs, Dictionary optArgs) throws MediaResourceException, RemoteException;
/**
 * Sends PrivateData for a raw TelephonyProviderInterface Object for it to act upon immediately.
 * <P>This method is mapped to any of six logical raw TPI provider objects based on the following
 * mapping, where the three columns represent the three method parameters:
 * <table BORDER >
 *<tr BGCOLOR="#C0C0C0">
 *<td></td>
 *
 *<th>Call</th>
 *
 *<th>Address</th>
 *
 *<th>Terminal</th>
 *</tr>
 *
 *<tr>
 *<td>Provider</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Call</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Address</td>
 *
 *<td></td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Terminal</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td>X</td>
 *</tr>
 *
 *<tr>
 *<td>Connection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>TerminalConnection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *</tr>
 *
 * <caption ALIGN=BOTTOM><i>"blank" indicates void</i></caption>
 * </table>
 * Creation date: (2000-08-05 22:25:45)
 * return Any serializable object.  If the object's Capabilities.canSendPrivateData() returns false, this returns null.
 * @param call A CallId or void
 * @param address An Address name or void
 * @param terminal A Terminal name or void
 * @param data Any serializable object.
 * @throws NotSerializableException If the returned object is not serializable.
 */
Serializable sendPrivateData(CallId call, String address, String terminal, Serializable data) throws RemoteException, NotSerializableException;
/**
  * Play DTMF tones on a terminal
  *
  * @param terminal The terminal to record the audio from.
  * @param syms A set of symbols to play
  * @param rtcs A set of runtime control sets that tune the signalling.
  * @param optArgs A dictionary of control arguments.
  *
  * @exception MediaResourceException A wrapper for a SignalGeneratorEvent that describes what went wrong.
  **/
void sendSignals(String terminal, SymbolHolder[] syms, RTCHolder[] rtcs, Dictionary optArgs) throws MediaResourceException, RemoteException;
/**
 * setLoadControl method comment.
 */
public void setLoadControl(java.lang.String startAddr, java.lang.String endAddr, double duration, double admissionRate, double interval, int[] treatment) throws javax.telephony.MethodNotSupportedException, RemoteException;
/**
 * Set the PrivateData for a raw TelephonyProviderInterface Object.
 * This applies to the next action on the Object.
 * <P>This method is mapped to any of six logical raw TPI provider objects based on the following
 * mapping, where the three columns represent the three method parameters:
 * <table BORDER >
 *<tr BGCOLOR="#C0C0C0">
 *<td></td>
 *
 *<th>Call</th>
 *
 *<th>Address</th>
 *
 *<th>Terminal</th>
 *</tr>
 *
 *<tr>
 *<td>Provider</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Call</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Address</td>
 *
 *<td></td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>Terminal</td>
 *
 *<td></td>
 *
 *<td></td>
 *
 *<td>X</td>
 *</tr>
 *
 *<tr>
 *<td>Connection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td></td>
 *</tr>
 *
 *<tr>
 *<td>TerminalConnection</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *
 *<td>X</td>
 *</tr>
 *
 * <caption ALIGN=BOTTOM><i>"blank" indicates void</i></caption>
 * </table>
 * Creation date: (2000-08-05 22:25:45)
 * @param call A CallId or void
 * @param address An Address name or void
 * @param terminal A Terminal name or void
 * @param data Any serializable object.
 */
void setPrivateData(CallId call, String address, String terminal, Serializable data) throws RemoteException;
/**
  * Perform any cleanup after my holder has finished with me.
  * Creation date: (2000-02-11 13:07:46)
  * @exception RemoteException A distribution (network) error has occured.
  *
  * @author: Richard Deadman
  **/
void shutdown() throws RemoteException;
/**
 * Stop any media resources attached to a terminal.
 * Creation date: (2000-03-09 16:08:12)
 * @author: Richard Deadman
 * @param terminal The terminal name.
 */
void stop(String terminal) throws RemoteException;
/**
 * Tell the RawProvider that a certain Call no longer needs to have Call, Connection and
 * TerminalConnections events reported.
 * Creation date: (2000-03-07 15:17:16)
 * @author: Richard Deadman
 * @return true if the call is known, false otherwise
 * @param call The handle on the call to turn state change reporting off for.
 */
boolean stopReportingCall(SerializableCallId call) throws RemoteException;
/**
 * Send Runtime control actions to media resources bound to a terminal.
 * Creation date: (2000-03-09 16:09:09)
 * @author: Richard Deadman
 * @param terminal The name of the terminal the media resources are bound to.
 * @param action The RTC action symbol to invoke on the media resources.
 */
void triggerRTC(String terminal, SymbolHolder action) throws RemoteException;
/**
  * Take a call off hold (CallControlTerminalConnection)
  *
  * @param term The terminal that we want to take off hold
  * @param call The call to reconnect to
  *
  * @exception RemoteException A distribution (network) error has occured.
  *
  * @author: Richard Deadman
  **/
void unHold(SerializableCallId call, String address, String term) throws RemoteException, RawStateException, MethodNotSupportedException,
	PrivilegeViolationException, ResourceUnavailableException;
}
