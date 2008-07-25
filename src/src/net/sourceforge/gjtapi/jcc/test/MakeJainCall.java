package net.sourceforge.gjtapi.jcc.test;

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
import javax.csapi.cc.jcc.*;
/**
 * Simple test script for running a Jcc program against the emulator raw provider
 * Creation date: (2000-11-16 15:40:58)
 * @author: Richard Deadman
 */
public class MakeJainCall {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 2) {
		System.err.println("Usage: java net.sourceforge.gjtapi.jcc.test.MakeJainCall from to");
		System.exit(1);
	}

	test("Emulator", args[0], args[1]);
}
/**
 * Prompt for user input.
 * Creation date: (2000-02-17 10:49:50)
 * @author: Richard Deadman
 */
protected static void prompt() throws java.io.IOException {
	java.io.InputStream in = System.in;

	System.out.println("Hit return to continue...");
	// wait
	in.read();
	in.skip(in.available());
}
/**
 * Performs a series of unit tests
 * @param args an array of command-line arguments
 */
public static void test(String providerName, String fromAddr, String toAddr) {
	java.io.PrintStream out = System.out;

	// Get a Jain Peer
	JccPeer peer = null;
	try {
		peer = JccPeerFactory.getJccPeer("net.sourceforge.gjtapi.jcc.Peer");
		out.println("1.1: Successfully loaded the Jcp Peer");
	} catch (ClassNotFoundException jpue) {
		out.println("1.1: Failed to locate Peer with the factory");
		jpue.printStackTrace();
		System.exit(1);
	}

	// Ask it for the Emulator Provider
	JccProvider prov = null;
	try {
		prov = (JccProvider)peer.getProvider(providerName);
		out.println("1.2: Successfully loaded Provider");
	} catch (ProviderUnavailableException pue) {
		out.println("1.2: Failed to load Provider");
		pue.printStackTrace();
		System.exit(1);
	}
	
	try {
		// Get an address
		out.print("2.1: Attempting to get address...");
		JccAddress addr = (JccAddress)prov.getAddress(fromAddr);
		out.println(" success:" + addr.getName());

		// Make the Call
		out.print("2.3: Attempting to create call...");
		JccCall c = (JccCall)prov.createCall();
		out.println(" success.");
		
		out.print("3.1: Attempting to create a connection...");
		JccConnection conn = (JccConnection)c.createConnection(toAddr, fromAddr, null, null);
		out.println(" success.");

		out.print("3.2: Attempting to route a connection...");
		conn.routeConnection(true);
		out.println(" success.");

		// Notify progress
		System.out.println("Call Connected, press Enter to release...");

		// force a wait
		prompt();
		
		out.print("3.3: Attempting to release a call...");
			// had to use the Jcp constant due to an error in Jcc 1.0b
		c.release(JccCallEvent.CAUSE_NORMAL);
		out.println(" success.");

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
	return "Simple test class for making a Jcc call from one address to another";
}
}
