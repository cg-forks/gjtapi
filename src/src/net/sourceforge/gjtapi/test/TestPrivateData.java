package net.sourceforge.gjtapi.test;

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
import javax.telephony.privatedata.events.PrivateTermEv;
import javax.telephony.privatedata.PrivateData;
import javax.telephony.privatedata.capabilities.PrivateDataCapabilities;
import javax.telephony.capabilities.TerminalCapabilities;
import javax.telephony.*;
/**
 * Test class for trying out the media commands
 * Creation date: (2000-03-31 16:03:44)
 * @author: Richard Deadman
 */
public class TestPrivateData extends TestMakeCall implements TerminalObserver {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 1) {
		System.err.println("Usage: java net.sourceforge.gjtapi.test.TestPrivateData terminal");
		System.exit(1);
	}

	test("Emulator", args[0]);
}
/**
 * terminalChangedEvent method comment.
 */
public void terminalChangedEvent(javax.telephony.events.TermEv[] eventList) {
	for (int i = 0; i < eventList.length; i++) {
		if (eventList[i] instanceof PrivateTermEv) {
			PrivateTermEv ev = (PrivateTermEv)eventList[i];
			System.out.println(ev.getPrivateData());
		}
	}
}
/**
 * Performs a series of media unit tests
 * @param args an array of command-line arguments
 */
public static void test(String providerName, String fromAddr) {
	java.io.PrintStream out = System.out;
	out.println("In PrivateData Test Script...");

	// Get a JTAPI Peer
	JtapiPeer peer = new net.sourceforge.gjtapi.GenericJtapiPeer();

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
		out.print("X.X: Attempting to get terminal...");
		Terminal term = prov.getTerminal(fromAddr);
		out.println(" success.");

		// Register for events with the terminal
		out.print("X.X: Attempting to register observer on terminal...");
		term.addObserver(new TestPrivateData());
		out.println(" success.");

		// Check if the Terminal can receive "send" PrivateData
		out.print("X.X: Attempting to check capabilities...");
		boolean success = false;
		TerminalCapabilities termCap = term.getCapabilities();
		if (termCap instanceof PrivateDataCapabilities) {
			if (((PrivateDataCapabilities)termCap).canSendPrivateData())
				success = true;
		}
		if (success)
			out.println(" success.");
		else
			out.println(" failure.");
		
		// Try to send Private Data to terminal
		out.print("X.X: Attempting to send Private Data to terminal...");
		((PrivateData)term).sendPrivateData("Open the pod bay doors, Hal");
		out.println(" check emulator!");
	
	} catch (Exception e) {
		out.println(" failure: " + e);
		e.printStackTrace();
	}
}
}
