package net.sourceforge.gjtapi.raw.xtapi.test;

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
import javax.telephony.callcontrol.CallControlTerminalConnection;
import javax.telephony.media.MediaProvider;

import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.media.GenericMediaService;

import javax.telephony.*;
/**
 * Simple test script for the emulator raw provider
 * Creation date: (2000-02-03 15:40:58)
 * @author: Richard Deadman
 */
public class TestMakeTapiCallAndSendDigits {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 2) {
		System.err.println("Usage: java net.sourceforge.gtapi.test.TestMakeTapiCallAndSendDigits to digits");
		System.exit(1);
	}

	test("net.sourceforge.gjtapi.raw.xtapi.XtapiProvider; XtapiSp = TAPI", args[0], args[1]);
}
/**
 * Prompt for user input.
 * Creation date: (2000-02-17 10:49:50)
 * @author: Richard Deadman
 */
protected static void prompt() throws IOException {
	InputStream in = System.in;

	System.out.println("Hit return to continue...");
	// wait
	in.read();
	in.skip(in.available());
}
/**
 * Performs a series of unit tests
 * @param args an array of command-line arguments
 */
public static void test(String providerName, String toAddr, String digitsToSend) {
	java.io.PrintStream out = System.out;

	// Get a JTAPI Peer
	JtapiPeer peer = null;
	try {
		peer = JtapiPeerFactory.getJtapiPeer("net.sourceforge.gjtapi.GenericJtapiPeer");
		out.println("1.1: Successfully loaded the JTapi Peer");
	} catch (JtapiPeerUnavailableException jpue) {
		out.println("1.1: Failed to locate Peer with the factory");
		jpue.printStackTrace();
		System.exit(1);
	}

	// Ask it for the Emulator Provider
	Provider prov = null;
	try {
		prov = peer.getProvider(providerName);
		out.println("1.2: Successfully loaded Provider");
	} catch (ProviderUnavailableException pue) {
		out.println("1.2: Failed to load Provider");
		pue.printStackTrace();
		System.exit(1);
	}
	
	try {
		// Load the Terminal
		out.print("2.1: Attempting to get address...");
		Terminal[] ts = prov.getTerminals();
		Terminal term = ts[0];
		Address[] addrs = term.getAddresses();
		Address addr = addrs[0];
		out.println(" success.");

		// Make the Call
		out.print("2.3: Attempting to create call...");
		Call c = prov.createCall();
		out.println(" success.");
		
		out.print("3.1: Attempting to connect call...");
		c.connect(term, addr, toAddr);
		out.println(" success.");

		// Notify progress
		System.out.println("Call initiated...");

		// answer args[1]
		out.print("3.2: Attempting to get call connections...");
		Connection cons[] = c.getConnections();
		out.println(" success.");

		// force a wait
		prompt();
		
		//  Create a media service
		out.print("Attempting to create a Media Service...");
		GenericMediaService ms = new GenericMediaService((MediaProvider)prov);
		out.println(" success.");

		// find the outgoing terminal
		Terminal t = ts[0];

		// bind the media service
		out.print("13.1: Attempting to bind a Media Service to a terminal...");
		ms.bindToTerminal(null, t);
		out.println(" success.");

		// generate a signal
		out.print("13.6: Attempting to generate DTMF on a Media Service...");
		ms.sendSignals("411", null, null);
		out.println(" success.");

		// release the call
		out.print("Attempting to release the media service (and therefore the call)...");
		ms.release();
		out.println(" success.");
		
		// test the state of the call
		out.print("Testing if call released...");
		out.println(cons[0].getState() == Connection.DISCONNECTED);
		out.println(cons[0].getState());

	} catch (Exception e) {
		out.println(" failure: " + e);
		e.printStackTrace();
	}
}
/**
 * Describe myself
 * @return a string representation of myself
 */
public String toString() {
	return "Simple test class for making a call from one address to another and playing a signal";
}
}
