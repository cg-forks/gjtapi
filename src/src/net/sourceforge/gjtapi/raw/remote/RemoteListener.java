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
import java.io.Serializable;
import net.sourceforge.gjtapi.media.*;
import java.rmi.*;
/* Copyright UForce Inc. 2000 */
/**
 * A Remote listener for events from a RemoteProvider
 *
 * @author: Richard Deadman
 **/

public interface RemoteListener extends Remote {
/**
 * Return Terminal data to the application
 * @author Richard Deadman
 * @param address The name of the address to return private data for.
 * @param data The data to return.  Serializable to allow remote proxying.
 * @param The ProvPrivateEv cause field.
 **/
	void addressPrivateData(String address, Serializable data, int cause) throws RemoteException;
/**
 * A call now exists in a real sense.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The call identifier
 * @param cause The Event cause code
 */
void callActive(SerializableCallId id, int cause) throws RemoteException;
/**
 * Basically a call has ended.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The call identifier
 * @param cause The Event cause code
 */
void callInvalid(SerializableCallId id, int cause) throws RemoteException;
/**
 * callOverloadCeased method comment.
 */
public void callOverloadCeased(java.lang.String address) throws RemoteException;
/**
 * callOverloadEncountered method comment.
 */
public void callOverloadEncountered(java.lang.String address) throws RemoteException;
/**
 * Return Terminal data to the application
 * @author Richard Deadman
 * @param call The CallId of the call to return private data for.
 * @param data The data to return.  Serializable to allow remote proxying.
 * @param The ProvPrivateEv cause field.
 **/
	void callPrivateData(SerializableCallId call, Serializable data, int cause) throws RemoteException;
/**
 * connectionAddressAnalyse method comment.
 */
public void connectionAddressAnalyse(SerializableCallId id, java.lang.String address, int cause) throws RemoteException;
/**
 * connectionAddressCollect method comment.
 */
public void connectionAddressCollect(SerializableCallId id, java.lang.String address, int cause) throws RemoteException;
/**
 * A connection between a call and an address is ringing.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The call of the connection
 * @param address The address the connection has
 * @param cause The Event cause code
 */
void connectionAlerting(SerializableCallId id, String address, int cause) throws RemoteException;
/**
 * connectionAuthorizeCallAttempt method comment.
 */
public void connectionAuthorizeCallAttempt(SerializableCallId id, java.lang.String address, int cause) throws RemoteException;
/**
 * connectionCallDelivery method comment.
 */
public void connectionCallDelivery(SerializableCallId id, java.lang.String address, int cause) throws RemoteException;
/**
 * A connection between a call and an address is connected.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The call of the connection
 * @param address The address the connection has
 * @param cause The Event cause code
 */
void connectionConnected(SerializableCallId id, String address, int cause) throws RemoteException;
/**
 * A connection between a call and an address is disconnected.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The call of the connection
 * @param address The address the connection has
 * @param cause The Event cause code
 */
void connectionDisconnected(SerializableCallId id, String address, int cause) throws RemoteException;
/**
 * A connection between a call and an address has failed.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The call of the connection
 * @param address The address the connection has
 * @param cause The Event cause code
 */
void connectionFailed(SerializableCallId id, String address, int cause) throws RemoteException;
/**
 * A connection between a call and an address is being processed.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The call of the connection
 * @param address The address the connection has
 * @param cause The Event cause code
 */
void connectionInProgress(SerializableCallId id, String address, int cause) throws RemoteException;
/**
 * connectionSuspended method comment.
 */
public void connectionSuspended(SerializableCallId id, java.lang.String address, int cause) throws RemoteException;
/**
 * A MediaService's player has paused due to an RTC
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param terminal The terminal that the media service is connected to.
 * @param index The media stream that was paused
 * @param offset How far into the media stream we were
 * @param trigger The symbol representing the RTC that triggered the pause
 */
void mediaPlayPause(String terminal, int index, int offset, SymbolHolder trigger) throws RemoteException;
/**
 * A MediaService's player has resumed due to an RTC
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param terminal The terminal that the media service is connected to.
 * @param trigger The symbol representing the RTC that triggered the pause
 */
void mediaPlayResume(String terminal, SymbolHolder trigger) throws RemoteException;
/**
 * A MediaService's player has paused due to an RTC
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param terminal The terminal that the media service is connected to.
 * @param duration The length of the recording, in milliseconds
 * @param trigger The symbol representing the RTC that triggered the pause
 */
void mediaRecorderPause(String terminal, int duration, SymbolHolder trigger) throws RemoteException;
/**
 * A MediaService's recorder has resumed due to an RTC
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param terminal The terminal that the media service is connected to.
 * @param trigger The symbol representing the RTC that triggered the pause
 */
void mediaRecorderResume(String terminal, SymbolHolder trigger) throws RemoteException;
/**
 * A MediaService's signal detector has detected a signal and p_enabledEvents includes ev_SignalDetected
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param terminal The terminal that the media service is connected to.
 * @param sigs The detected signals
 */
void mediaSignalDetectorDetected(String terminal, SymbolHolder[] sigs) throws RemoteException;
/**
 * A MediaService's signal detector has overflowed and p_enableEvents contains ev_Overflow
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param terminal The terminal that the media service is connected to.
 * @param sigs The signals detected up to the overflow
 */
void mediaSignalDetectorOverflow(String terminal, SymbolHolder[] sigs) throws RemoteException;
/**
 * A MediaService's signal detector has matched a pattern and p_enableEvents contains ev_Pattern[i]
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param terminal The terminal that the media service is connected to.
 * @param sigs The signals detected up to the overflow
 * @param index The index into the ev_pattern array
 */
void mediaSignalDetectorPatternMatched(String terminal, SymbolHolder[] sigs, int index) throws RemoteException;
/**
 * Return Provider data to the application
 * @author Richard Deadman
 * @param data The data to return.  Serializable to allow remote proxying.
 * @param The ProvPrivateEv cause field.
 **/
	void providerPrivateData(Serializable data, int cause) throws RemoteException;
/**
 * A terminal connection between a call and an address's terminal has been created.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The Call of the Terminal Connection
 * @param address The address of the Terminal Connection
 * @param terminal The physical endpoint of the Terminal Connection
 * @param cause The Event cause code
 */
void terminalConnectionCreated(SerializableCallId id, String address, String terminal, int cause) throws RemoteException;
/**
 * A terminal connection between a call and an address's terminal has been dropped.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The Call of the Terminal Connection
 * @param address The address of the Terminal Connection
 * @param terminal The physical endpoint of the Terminal Connection
 * @param cause The Event cause code
 */
void terminalConnectionDropped(SerializableCallId id, String address, String terminal, int cause) throws RemoteException;
/**
 * A terminal connection between a call and an address's terminal has become active and is held.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The Call of the Terminal Connection
 * @param address The address of the Terminal Connection
 * @param terminal The physical endpoint of the Terminal Connection
 * @param cause The Event cause code
 */
void terminalConnectionHeld(SerializableCallId id, String address, String terminal, int cause) throws RemoteException;
/**
 * A terminal connection between a call and an address's terminal is ringing.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The Call of the Terminal Connection
 * @param address The address of the Terminal Connection
 * @param terminal The physical endpoint of the Terminal Connection
 * @param cause The Event cause code
 */
void terminalConnectionRinging(SerializableCallId id, String address, String terminal, int cause) throws RemoteException;
/**
 * A terminal connection between a call and an address's terminal has become active and is talking.
 * Creation date: (2000-04-15 0:38:57)
 * @author: Richard Deadman
 * @param id The Call of the Terminal Connection
 * @param address The address of the Terminal Connection
 * @param terminal The physical endpoint of the Terminal Connection
 * @param cause The Event cause code
 */
void terminalConnectionTalking(SerializableCallId id, String address, String terminal, int cause) throws RemoteException;
/**
 * Return Terminal data to the application
 * @author Richard Deadman
 * @param terminal The name of the terminal to return private data for.
 * @param data The data to return.  Serializable to allow remote proxying.
 * @param The ProvPrivateEv cause field.
 **/
	void terminalPrivateData(String terminal, Serializable data, int cause) throws RemoteException;
}
