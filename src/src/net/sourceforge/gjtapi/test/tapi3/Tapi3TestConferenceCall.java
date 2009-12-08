package net.sourceforge.gjtapi.test.tapi3;

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
import javax.telephony.privatedata.PrivateData;
import javax.telephony.*;

import net.sourceforge.gjtapi.raw.tapi3.PrivateConferenceCommand;
/**
 * Simple test script for transfering a call.
 * This tests consult, conference, transfer and TransferControllers, as well as hold and release
 * Creation date: (2000-02-03 15:40:58)
 * @author: Richard Deadman
 */
public class Tapi3TestConferenceCall {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 4) {
		System.err.println("Usage: java net.sourceforge.gjtapi.test.TestTransferCall provider from to newDest");
		System.exit(1);
	}

	transfer(args[0], args[1], args[2], args[3]);
}
/**
 * Prompt for user input.
 * Creation date: (2000-02-17 10:49:50)
 * @author: Richard Deadman
 */
private static void prompt() throws IOException {
	InputStream in = System.in;

	System.out.println("Hit return to continue...");
	// wait
	in.read();
	in.skip(in.available());
}
/**
 * Describe myself
 * @return a string representation of myself
 */
public String toString() {
	return "Simple test class for transferring a call from one address to another";
}
/**
 * Performs a test of transfer
 * @param args an array of command-line arguments
 */
public static void transfer(String providerName, String fromAddr, String toAddr, String finalDest) {
	java.io.PrintStream out = System.out;

	// Get a JTAPI Peer
	System.out.println(System.getProperty("java.library.path"));
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
		Address addr = prov.getAddress(fromAddr);
		out.println(" success.");

		// Make the Call
		out.print("2.3: Attempting to create call...");
		Call c = prov.createCall();
		out.println(" success.");
		
		out.print("4.3: Attempting to get terminals for an address...");
		Terminal[] ts = addr.getTerminals();
		out.println(" success.");

		out.print("3.1: Attempting to connect call...");
		c.connect(ts[0], addr, toAddr);
		out.println(" success.");

		// Notify progress
		out.println("Call initiated...");

		// force a wait
		prompt();
		
		// Do tapi3 conference using private data
		out.print("X.x: Attempting to set up consultation call...");
		PrivateData privateDataCall = (PrivateData)c;
		PrivateConferenceCommand command = new PrivateConferenceCommand(fromAddr, finalDest);
		Integer consultationCallId = (Integer)privateDataCall.sendPrivateData(command);
		if (consultationCallId < 0) {
			out.println(" failure.");
		} else {
			out.println(" success. New call id = " + consultationCallId);
			
			// force a wait
			prompt();
			
			// Do tapi3 conference complete using private data
			out.print("X.x: Attempting to finish conference call...");
			command.setComplete(true);
			Integer result = (Integer)privateDataCall.sendPrivateData(command);
			if (result < 0) {
				out.println(" failure.");
			} else {
				out.println(" success.");
			}

		}

		prompt();

	} catch (Exception e) {
		out.println(" failure: " + e);
		e.printStackTrace();
	}
}
}
