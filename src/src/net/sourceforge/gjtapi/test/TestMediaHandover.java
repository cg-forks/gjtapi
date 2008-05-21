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
import javax.telephony.media.*;
import net.sourceforge.gjtapi.media.GenericMediaService;
import javax.telephony.*;
/**
 * Test class for trying out the media call handover from one media service to another.
 * Creation date: (2000-03-31 16:03:44)
 * @author: Richard Deadman
 */
public class TestMediaHandover extends TestMakeCall {
	private static final String SERVICE_NAME = "HandoffService";
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 3) {
		System.err.println("Usage: java net.sourceforge.gjtapi.test.TestMediaHandover from to secondsToWait");
		System.exit(1);
	}

	test("Emulator", args[0], args[1], args[2]);
}
/**
 * Performs a series of media unit tests
 * @param args an array of command-line arguments
 */
public static void test(String providerName, String fromAddr, String toAddr, String secondsForThreadToWait) {
	final java.io.PrintStream out = System.out;
	out.println("In Media Test Script...");

	final int secs = Integer.parseInt(secondsForThreadToWait);
	
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
	Provider provTemp = null;
	try {
		provTemp = peer.getProvider(providerName);
		out.println("1.2: Successfully loaded Provider");
	} catch (ProviderUnavailableException pue) {
		out.println("1.2: Failed to load Provider");
		pue.printStackTrace();
		System.exit(1);
	}
	final Provider prov = provTemp;
	
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

		// answer args[1]
		out.print("3.2: Attempting to get call connections...");
		Connection cons[] = c.getConnections();
		out.println(" success: " + cons.length);

		// force a wait
		prompt();

		// create a MediaService and register it to wait for 20 seconds for a call
		out.println("Creating a sub-thread for the waiting service");
		Runnable handlerThread = new Runnable() {
			public void run() {
				//  Create a media service
				out.print("Attempting to create a handoff Media Service...");
				GenericMediaService ms = new GenericMediaService((MediaProvider)prov);
				out.println(" success.");

				// sleep
				try {
					Thread.sleep(secs * 1000);
				} catch (InterruptedException ie) {
					// continue
				}
				
				// Register it for later hand off
				out.print("13.2: Waiting for a service name...");
				try {
					ms.bindToServiceName(null, SERVICE_NAME);
					out.println(" success (X.X).");
				} catch (MediaBindException mbe) {
					out.println("Failed bind: " + mbe);
					mbe.printStackTrace();
				}

				// play the message
				out.print("13.4: Attempting to play on the second Media Service...");
				try {
					ms.play("Second", 0, null, null);
					out.println(" success.");
				} catch (MediaResourceException mre) {
					out.println("Failed play: " + mre);
					mre.printStackTrace();
				}

				// release the media service
				ms.release();
			}
		};
		new Thread(handlerThread).start();
		
		//  Create a media service
		out.print("Attempting to create a Media Service...");
		GenericMediaService ms = new GenericMediaService((MediaProvider)prov);
		out.println(" success.");

		// find the outgoing terminal
		out.print("2.4: Attempting to get a Terminal by name...");
		Terminal t = prov.getTerminal(fromAddr);
		out.println(" success.");

		// bind the media service
		out.print("13.1: Attempting to bind a Media Service to a terminal...");
		ms.bindToTerminal(null, t);
		out.println(" success.");

		// play the message
		out.print("13.4: Attempting to play on a Media Service...");
		ms.play("Hello", 0, null, null);
		out.println(" success.");

		// release the media service
		out.print("13.3: Attempting to hand off the Media Service...");
		try {
			ms.releaseToService(SERVICE_NAME, 20000);
			out.println(" success.");
		} catch (javax.telephony.media.NoServiceReadyException nsre) {
			out.println("Failed: No service picked up call");
		}
	
	} catch (Exception e) {
		out.println(" failure: " + e);
		e.printStackTrace();
	}
}
}
