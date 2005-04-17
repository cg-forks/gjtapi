/*
	Copyright (c) 2005 Serban Iordache 
	
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
package net.sourceforge.gjtapi.raw.tapi3;

import java.util.Map;

/**
 * This interface provides an abstraction level that binds the Java implementation with the native implementation (e.g. Tapi3Provider.dll).  
 * @author Serban Iordache
 */
public interface Tapi3Native {
    /**
     * Initialize the Tapi3 provider
     * @param props The name value properties map
     * @return Array of addresses or null on error
     */
    public String[] tapi3Init(Map props);
    
    /**
     * Shut down the TAPI session
     * @return Error code (0=success) 
     */
    public int tapi3Shutdown();
    
    /**
     * Answer an incoming call
     * @param callID The identifier for the call
     * @return Error code (0=success) 
     */
    public int tapi3AnswerCall(int callID); 
    
    /**
     * Disconnect a call
     * @param callID The identifier for the call
     * @return Error code (0=success) 
     */
    public int tapi3DisconnectCall(int callID); 
    
    /**
     * Release a call
     * @param callID The identifier for the call
     * @return Error code (0=success) 
     */
    public int tapi3ReleaseCall(int callID); 

    /**
     * Reserve a callId
     * @param address The address that the call will start on
     * @return The reserved callID or a negative error code
     */
    public int tapi3ReserveCallId(String address);

    /**
     * Create a call
     * @param callID The callId reserved for the call
     * @param address The address to make a call from
     * @param dest The destination address
     * @return The callID or a negative error code
     */
    public int tapi3CreateCall(int callID, String address, String dest); 

    /**
     * Put a call on hold 
     * @param callID The identifier for the call
     * @param address The address that defines the call to hold
     * @return Error code (0=success) 
     */
    public int tapi3Hold(int callID);
    
    /**
     * Join one call to another call
     * @param callID1 The identifier for one call
     * @param callID2 The identifier for another call
     * @return The new callID or a negative error code
     */
    public int tapi3Join(int callID1, int callID2);
    
    /**
     * Take a call off hold 
     * @param callID The identifier for the call that we want to take off hold
     * @return Error code (0=success) 
     */
    public int tapi3UnHold(int callID);

    /**
     * Play DTMF tones on a terminal
     * @param terminal The terminal to play the DTMF tones to
     * @param digits A set of digits to send
     * @return Error code (0=success)
     */
    public int tapi3SendSignals(String terminal, String digits);
    
    /**
     * Register a Tapi3 provider 
     * @param provider The Tapi3 provider
     */
    public void registerProvider(Tapi3Provider provider);
}
