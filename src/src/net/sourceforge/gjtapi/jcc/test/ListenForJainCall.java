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
 * Simple test to see if we can register for incoming calls.
 * Creation date: (2003-02-07 09:54:08)
 * @author: Richard Deadman
 */
public class ListenForJainCall {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 1) {
		System.err.println("Usage: java net.sourceforge.gjtapi.jcc.test.MakeJainCall subProviderName");
		System.exit(1);
	}

	test(args[0]);
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
 * Performs a series of unit tests to see if we can listen for incoming calls.
 * @param args an array of command-line arguments
 */
public static void test(String providerName) {
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
		// Ask provider to listen for incoming calls.
		prov.addCallListener(new JccConnectionListener() {
			public void callActive(JccCallEvent ce) {
				System.out.println("CallActive: " + ce);
			}
			public void callCreated(JccCallEvent ce) {
				System.out.println("CallCreated: " + ce);
			}
			public void callEventTransmissionEnded(JccCallEvent ce) {
				System.out.println("CallEvent transmission ended: " + ce);
			}
			public void callSuperviseStart(JccCallEvent ce) {
				System.out.println("CallSupervise start: " + ce);
			}
			public void callSuperviseEnd(JccCallEvent ce) {
				System.out.println("CallSupervise ended: " + ce);
			}
			public void callInvalid(JccCallEvent ce) {
				System.out.println("Call Invalid: " + ce);
			}
			public void connectionAuthorizeCallAttempt(JccConnectionEvent ce) {
				System.out.println("Conn AuthorizeCallEvent: " + ce);
			}
			public void connectionCreated(JccConnectionEvent ce) {
				System.out.println("Connection Created: " + ce);
			}
			public void connectionConnected(JccConnectionEvent ce) {
				System.out.println("Connection Connected: " + ce);
			}
			public void connectionAddressCollect(JccConnectionEvent ce) {
				System.out.println("Connection Address Collect: " + ce);
			}
			public void connectionAddressAnalyze(JccConnectionEvent ce) {
				System.out.println("Connection Address Analyze: " + ce);
			}
			public void connectionCallDelivery(JccConnectionEvent ce) {
				System.out.println("Connection Call Delivery: " + ce);
			}
			public void connectionAlerting(JccConnectionEvent ce) {
				System.out.println("Connection Alerting: " + ce);
			}
			public void connectionDisconnected(JccConnectionEvent ce) {
				System.out.println("Connection Disconnected: " + ce);
			}
			public void connectionMidCall(JccConnectionEvent ce) {
				System.out.println("Connection Mid Call: " + ce);
			}
//			public void connectionCallDeliveryl(JccConnectionEvent ce) {
//				System.out.println("Connection Call Delivery: " + ce);
//			}
			public void connectionFailed(JccConnectionEvent ce) {
				System.out.println("Connection Failed: " + ce);
			}
			public String toString() {
				return "Jcc ConnectionListener for ListenForJainCall unit test";
			}
		});

		// force a wait
		prompt();
		
		out.print("ending...");
		prov.shutdown();
		
		out.print("Provider shut down");
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
