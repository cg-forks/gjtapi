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
import java.util.*;
import java.io.*;
import javax.telephony.callcontrol.capabilities.*;
import javax.telephony.capabilities.*;
/**
 * This is a holder for all the capabilities of the system.
 * Creation date: (2000-03-14 13:29:07)
 * @author: Richard Deadman
 */
public class Capabilities {
	private final static String RESOURCE_NAME = "/GenericCapabilities.props";
	public final static String ANSWER = "answer";
	public final static String CREATE = "create";
	public final static String HOLD = "hold";
	public final static String JOIN = "join";
	public final static String RELEASE = "release";

	// RawCapabilities
	public final static String THROTTLE = "throttle";
	public final static String MEDIA = "media";
	public final static String ALL_MEDIA_TERMINALS = "allMediaTerminals";
	public final static String ALLOCATE_MEDIA = "allocateMedia";

	public final static String DYNAMIC_ADDRESSES = "dynamicAddresses";

	// Private Data headers
	public final static String PROV = "prov";
	public final static String ADDR = "addr";
	public final static String TERM = "term";
	public final static String CALL = "call";
	public final static String CONN = "conn";
	public final static String TERM_CONN = "tc";
	public final static String PD = "PrivateData";
	public final static String GET = "Get" + PD;
	public final static String SEND = "Send" + PD;
	public final static String SET = "Set" + PD;
	
	private GenProviderCapabilities providerCap = null;
	private GenAddressCapabilities addressCap = null;
	private GenCallCapabilities callCap = null;
	private GenConnectionCapabilities connectionCap = null;
	private GenTermConnCapabilities termConnCap = null;
	private GenTerminalCapabilities terminalCap = null;
	private RawCapabilities rawCap = null;
/**
 * Create and initialize my capabilities
 * Creation date: (2000-03-14 14:16:14)
 * @author: Richard Deadman
 */
public Capabilities() {
	super();
	
	this.providerCap = new GenProviderCapabilities();
	this.addressCap = new GenAddressCapabilities();
	this.callCap = new GenCallCapabilities();
	this.connectionCap = new GenConnectionCapabilities();
	this.termConnCap = new GenTermConnCapabilities();
	this.terminalCap = new GenTerminalCapabilities();
	this.rawCap = new RawCapabilities();
	
	// initialize
	this.loadCapabilities(this.RESOURCE_NAME);
}
/**
 * Return my static set of Address Capabilities
 * Creation date: (2000-03-14 14:15:02)
 * @author: Richard Deadman
 * @return The static instance of the address capabilities.
 */
public GenAddressCapabilities getAddressCapabilities() {
	return this.addressCap;
}
/**
 * Return my static set of Call Capabilities
 * Creation date: (2000-03-14 14:15:02)
 * @author: Richard Deadman
 * @return The static instance of the call capabilities.
 */
public GenCallCapabilities getCallCapabilities() {
	return this.callCap;
}
/**
 * Return my static set of Connection Capabilities
 * Creation date: (2000-03-14 14:15:02)
 * @author: Richard Deadman
 * @return The static instance of the connection capabilities.
 */
public GenConnectionCapabilities getConnectionCapabilities() {
	return this.connectionCap;
}
/**
 * Return my static set of Provider Capabilities
 * Creation date: (2000-03-14 14:15:02)
 * @author: Richard Deadman
 * @return The static instance of the provider capabilities.
 */
public GenProviderCapabilities getProviderCapabilities() {
	return this.providerCap;
}
/**
 * Return my set of RawProvider Capabilities
 * Creation date: (2000-03-14 14:15:02)
 * @author: Richard Deadman
 * @return The capabilities that define which RawProvider calls are actually necessary.
 */
public RawCapabilities getRawCapabilities() {
	return this.rawCap;
}
/**
 * Return my static set of Terminal Capabilities
 * Creation date: (2000-03-14 14:15:02)
 * @author: Richard Deadman
 * @return The static instance of the terminal capabilities.
 */
public GenTerminalCapabilities getTerminalCapabilities() {
	return this.terminalCap;
}
/**
 * Return my static set of TerminalConnection Capabilities
 * Creation date: (2000-03-14 14:15:02)
 * @author: Richard Deadman
 * @return The static instance of the terminal-connection capabilities.
 */
public GenTermConnCapabilities getTerminalConnectionCapabilities() {
	return this.termConnCap;
}
/**
 * This method sets the Provider's capabilities values from a named Property file.
 * Creation date: (2000-03-14 10:11:41)
 * @author: Richard Deadman
 */
public void loadCapabilities(String resName) {
	// If this fails, the Capabilities object will be left null, which will signal getProvider() to throw
	// an exception.
	Properties props = new Properties();
	try {
		props.load(this.getClass().getResourceAsStream(resName));
		this.setCapabilities(props);
	} catch (IOException ioe) {
		// don't set capabilities then...
		return;
	}
}
/**
 * Help method to resolve capability values.
 * Creation date: (2000-06-26 8:17:34)
 * @author: Richard Deadman
 * @return true if the value is a Boolean True or a String starting in 'T' or 't'.
 * @param val java.lang.Object
 */
public static boolean resolve(Object val) {
	if (((val instanceof String) || (val instanceof Boolean)) && (val != null))
		return (val instanceof Boolean) ?
					((Boolean)val).booleanValue() :
					Character.toLowerCase(((String)val).charAt(0)) == 't';
	return false;
}
/**
 * Set or update the properties for the provider
 * Creation date: (2000-03-14 14:12:09)
 * @author: Richard Deadman
 * @param props A properties object that maps names to true/false values
 */
public void setCapabilities(Properties props) {
	if (props != null) {
		// handle them all here
		Iterator it = props.keySet().iterator();
		while (it.hasNext()) {
			Object okey = it.next();
			if (okey instanceof String) {
				String key = (String)okey;
				Object oval = props.get(key);
				boolean confTransChanged = false;
				if (oval instanceof Boolean || oval instanceof String) {
					boolean val = this.resolve(oval);
					if (key.equals(ANSWER)) {
						this.getTerminalConnectionCapabilities().setAnswerCapability(val);
					} else if (key.equals(CREATE)) {
						this.getCallCapabilities().setConnectCapability(val);
						confTransChanged = true;
					} else if (key.equals(JOIN)) {
						this.getCallCapabilities().setConferenceCapability(val);
						confTransChanged = true;
					} else if (key.equals(HOLD)) {
						this.getTerminalConnectionCapabilities().setHoldCapability(val);
						confTransChanged = true;
					} else if (key.equals(RELEASE)) {
						this.getTerminalConnectionCapabilities().setLeaveCapability(val);
						confTransChanged = true;
					}
						// test for PrivateData capabilities
					else if (key.equals(PROV + GET)) {
						this.getProviderCapabilities().setGetPDCapability(val);
					} else if (key.equals(PROV + SEND)) {
						this.getProviderCapabilities().setSendPDCapability(val);
					} else if (key.equals(PROV + SET)) {
						this.getProviderCapabilities().setSetPDCapability(val);
					}
					
					else if (key.equals(CALL + GET)) {
						this.getCallCapabilities().setGetPDCapability(val);
					} else if (key.equals(CALL + SEND)) {
						this.getCallCapabilities().setSendPDCapability(val);
					} else if (key.equals(CALL + SET)) {
						this.getCallCapabilities().setSetPDCapability(val);
					}
					
					else if (key.equals(ADDR + GET)) {
						this.getAddressCapabilities().setGetPDCapability(val);
					} else if (key.equals(ADDR + SEND)) {
						this.getAddressCapabilities().setSendPDCapability(val);
					} else if (key.equals(ADDR + SET)) {
						this.getAddressCapabilities().setSetPDCapability(val);
					}
					
					else if (key.equals(TERM + GET)) {
						this.getTerminalCapabilities().setGetPDCapability(val);
					} else if (key.equals(TERM + SEND)) {
						this.getTerminalCapabilities().setSendPDCapability(val);
					} else if (key.equals(TERM + SET)) {
						this.getTerminalCapabilities().setSetPDCapability(val);
					}
					
					else if (key.equals(CONN + GET)) {
						this.getConnectionCapabilities().setGetPDCapability(val);
					} else if (key.equals(CONN + SEND)) {
						this.getConnectionCapabilities().setSendPDCapability(val);
					} else if (key.equals(CONN + SET)) {
						this.getConnectionCapabilities().setSetPDCapability(val);
					}
					
					else if (key.equals(TERM_CONN + GET)) {
						this.getTerminalConnectionCapabilities().setGetPDCapability(val);
					} else if (key.equals(TERM_CONN + SEND)) {
						this.getTerminalConnectionCapabilities().setSendPDCapability(val);
					} else if (key.equals(TERM_CONN + SET)) {
						this.getTerminalConnectionCapabilities().setSetPDCapability(val);
					}
					
						// now insert the RawCapabilities
					else if (key.equals(THROTTLE)) {
						this.getRawCapabilities().throttle = val;
					} else if (key.equals(MEDIA)) {
						this.getRawCapabilities().media = val;
					} else if (key.equals(ALL_MEDIA_TERMINALS)) {
						this.getRawCapabilities().allMediaTerminals = val;
					} else if (key.equals(ALLOCATE_MEDIA)) {
						this.getRawCapabilities().allocateMedia = val;
					}

					// test if we need to update the conference and transfer capabilities
					if (confTransChanged)
						this.getCallCapabilities().updateCapabilities(this.getTerminalConnectionCapabilities());
				}
			}
		}
	}
}
}
