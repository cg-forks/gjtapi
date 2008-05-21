package net.sourceforge.gjtapi.test;

/*
	Copyright (c) 2002 Richard Deadman

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
import javax.telephony.*;
/**
 * Simple test script for the emulator raw provider, with Listeners applied to the
 * TerminalConnection.
 * Creation date: (2000-02-03 15:40:58)
 * @author: Richard Deadman
 */
public class TestTCListener {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 2) {
		System.err.println("Usage: java net.sourceforge.gtapi.test.TestMakeCall from to");
		System.exit(1);
	}

	test("Emulator", args[0], args[1]);
}
/**
 * Prompt for user input.
 * Creation date: (2000-02-17 10:49:50)
 * @author: Richard Deadman
 */
protected static void prompt() throws IOException {
	//InputStream in = System.in;

	System.out.println("Hit return to continue...");
	// wait
	//in.read();
	//in.skip(in.available());
}
/**
 * Performs a series of unit tests
 * @param args an array of command-line arguments
 */
public static void test(String providerName, String fromAddr, String toAddr) {
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
		Address addr = prov.getAddress(fromAddr);
		out.println(" success.");
		
		// Now we add a TerminalConnectionListener to the Address
		addr.addCallListener(new TerminalConnectionListener() {
			public void callActive(CallEvent ce) {
				System.out.println("  ==> Call Active");
			}
			public void callEventTransmissionEnded(CallEvent ce) {
				System.out.println("  ==> Call transmission ended");
			}
			public void callInvalid(CallEvent ce) {
				System.out.println("  ==> Call Invalid");
			}
			public void multiCallMetaMergeEnded(MetaEvent ce) {
				System.out.println("  ==> meta");
			}
			public void multiCallMetaMergeStarted(MetaEvent ce) {
				System.out.println("  ==> meta");
			}
			public void multiCallMetaTransferEnded(MetaEvent ce) {
				System.out.println("  ==> meta");
			}
			public void multiCallMetaTransferStarted(MetaEvent ce) {
				System.out.println("  ==> meta");
			}
			public void singleCallMetaProgressEnded(MetaEvent ce) {
				System.out.println("  ==> meta");
			}
			public void singleCallMetaProgressStarted(MetaEvent ce) {
				System.out.println("  ==> meta");
			}
			public void singleCallMetaSnapshotEnded(MetaEvent ce) {
				System.out.println("  ==> meta");
			}
			public void singleCallMetaSnapshotStarted(MetaEvent ce) {
				System.out.println("  ==> meta");
			}
			public void connectionAlerting(ConnectionEvent ce) {
				System.out.println("  ==> connection alerting");
			}
			public void connectionConnected(ConnectionEvent ce) {
				System.out.println("  ==> connection connected");
			}
			public void connectionCreated(ConnectionEvent ce) {
				System.out.println("  ==> connection created");
			}
			public void connectionDisconnected(ConnectionEvent ce) {
				System.out.println("  ==> connection disconnected");
			}
			public void connectionFailed(ConnectionEvent ce) {
				System.out.println("  ==> connection failed");
			}
			public void connectionInProgress(ConnectionEvent ce) {
				System.out.println("  ==> connection in progress");
			}
			public void connectionUnknown(ConnectionEvent ce) {
				System.out.println("  ==> connection unknown");
			}
			public void terminalConnectionActive(TerminalConnectionEvent ce) {
				System.out.println("  ==> termconn active");
			}
			public void terminalConnectionCreated(TerminalConnectionEvent ce) {
				System.out.println("  ==> termconn created");
			}
			public void terminalConnectionDropped(TerminalConnectionEvent ce) {
				System.out.println("  ==> termconn dropped");
			}
			public void terminalConnectionPassive(TerminalConnectionEvent ce) {
				System.out.println("  ==> termconn passive");
			}
			public void terminalConnectionRinging(TerminalConnectionEvent ce) {
				System.out.println("  ==> termconn ringing");
			}
			public void terminalConnectionUnknown(TerminalConnectionEvent ce) {
				System.out.println("  ==> termconn unknown");
			}
		});

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

		// answer args[1]
		out.print("3.2: Attempting to get call connections...");
		Connection cons[] = c.getConnections();
		out.println(" success.");

		// force a wait
		prompt();
		
		out.print("6.1 and 4.1: Attempting to get connection address and address name...");
		for (int i = 0; i < cons.length; i++) {
			// find the remote connection
			if (cons[i].getAddress().getName().equals(toAddr)) {
				out.println("success (including remote address match)");
				Connection conn = cons[i];
				
				out.print("6.2: Attempting to get TerminalConnections for remote address");
				// See if it has any terminal connections
				TerminalConnection[] tc = conn.getTerminalConnections();
				
				if (tc != null && tc.length > 0) {
					out.println(" success");
					TerminalConnection termConn = tc[0];

					// wait to continue
					prompt();
					
					// answer first found terminal
					out.print("7.1: Attempting to answer call on first TerminalConnection...");
					termConn.answer();
					System.out.println(" success");

					if (termConn instanceof CallControlTerminalConnection) {
						CallControlTerminalConnection cctc = (CallControlTerminalConnection)termConn;
						
						// hold the terminal connection
						prompt();
						
						out.print("8.1: Attempting to hold the Terminal Connection...");
						cctc.hold();
						out.println(" success");

						// unhold
						prompt();
						
						out.print("8.2: Attempting to unhold the Terminal Connection...");
						cctc.unhold();
						out.println(" success");
					} else {
						out.println("Call Control functions not supported: 8.1 and 8.2");
					}
					
				} else {
					out.println(" failed to find any TerminalConnections");
				}

				// now disconnect the call
				// Wait for input
				prompt();

				// use the CallCntrolCall.drop to trigger InvalidCall events
				out.print("6.3: Attempting to drop the call...");
				((javax.telephony.callcontrol.CallControlCall)c).drop();
				out.println(" success.");

			}
		}

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
	return "Simple test class for making a call from one address to another";
}
}
