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
import javax.telephony.*;
import javax.telephony.events.*;
import javax.telephony.media.*;
import javax.telephony.media.events.*;
/**
 * Test class for trying out the 1.2 media commands
 * Creation date: (2000-03-31 16:03:44)
 * @author: Richard Deadman
 */
@SuppressWarnings("deprecation")
public class TestOldMediaCall extends TestMakeCall implements CallObserver {
/**
 * Print out the events I receive
 */
public void callChangedEvent(javax.telephony.events.CallEv[] eventList) {
	java.io.PrintStream out = System.out;
	for (int i = 0; i < eventList.length; i++) {
		switch(eventList[i].getID()) {
			case CallActiveEv.ID: {
				out.println("5.8.1 New Call noticed");
				break;
			}
			case CallInvalidEv.ID: {
				out.println("5.8.2 Call hungup");
				break;
			}
			case CallObservationEndedEv.ID: {
				out.println("5.8.3 Observation terminated");
				break;
			}
			case MediaTermConnDtmfEv.ID: {
				out.println("5.8.3 DTMF received: " + ((MediaTermConnDtmfEv)eventList[i]).getDtmfDigit());
				break;
			}
		}
	}
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 2) {
		System.err.println("Usage: java net.sourceforge.gjtapi.test.TestOldMediaCall from to");
		System.exit(1);
	}

	test("Emulator", args[0], args[1]);
}
/**
 * Performs a series of media unit tests
 * @param args an array of command-line arguments
 */
public static void test(String providerName, String fromAddr, String toAddr) {
	java.io.PrintStream out = System.out;
	out.println("In Media Test Script...");

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
		System.out.println("Call initiated...");

		// get the connection
		out.print("3.2: Attempting to get call connections...");
		Connection cons[] = c.getConnections();
		out.println(" success.");
		out.print("6.1 and 4.1: Attempting to get connection address and address name...");
		for (int i = 0; i < cons.length; i++) {
			// find the remote connection
			if (cons[i].getAddress().getName().equals(toAddr)) {
				out.println("success (including remote address match)");
				Connection conn = cons[i];
				
				out.print("6.2: Attempting to get TerminalConnections for remote address...");
				// See if it has any terminal connections
				TerminalConnection[] tc = conn.getTerminalConnections();
				
				if (tc != null && tc.length > 0) {
					out.println(" success");
					TerminalConnection termConn = tc[0];

					// answer first found terminal
					out.print("7.1: Attempting to answer call on first TerminalConnection...");
					termConn.answer();
					System.out.println(" success");
					
					// Test if a MediaTerminalConnection
					out.print("Casting to MediaTerminalConnection...");
					MediaTerminalConnection mtc = (MediaTerminalConnection)termConn;
					out.println(" success.");

					// check media state
					out.print("X.X: Checking Media State: ");
					out.println(mtc.getMediaState());
					
					// Set an Observer
					out.print("3.4: Attempting to observe events...");
					mtc.getConnection().getCall().addObserver(new TestOldMediaCall());
					out.println(" success.");

					prompt();
					
					// Turn Signal Detection on
					out.print("14.1: Attempting to turn on signal detection...");
					mtc.setDtmfDetection(true);
					out.println(" success.");

					// Generate a DTMF signal
					out.print("14.2: Attempting to generate DTMF...");
					mtc.generateDtmf("411");
					out.println(" success.");

					// play a message
					out.print("14.3 and 14.4: Attempting to play on the MediaTerminalConnection...");
					mtc.usePlayURL(new java.net.URL("http://24.114.65.197"));
					mtc.startPlaying();
					out.println(" success.");

					// play a second message
					out.print("X.X: Attempting to play a second message on the MediaTerminalConnection...");
					Thread.sleep(8000);
					mtc.usePlayURL(new java.net.URL("http://128.0.0.1"));
					mtc.startPlaying();
					out.println(" success.");

					// record a message
					out.print("14.5 and 14.6: Attempting to record from a Media Service...");
					mtc.useRecordURL(new java.net.URL("file:/tmp/test.txt"));
					mtc.startRecording();
					out.println(" success.");

					// check media availablity
					out.print("14.7: Checking Media Availability: ");
					out.println(mtc.getMediaAvailability());
					
					// stop playing
					out.print("14.8: Attempting to stop playing...");
					mtc.stopPlaying();
					out.println(" success");

					// stop recording
					out.print("14.9: Attempting to stop recording");
					mtc.stopRecording();
					out.println(" success.");
				}
			}
		}
	} catch (Exception e) {
		out.println(" failure: " + e);
		e.printStackTrace();
	}
}
}
