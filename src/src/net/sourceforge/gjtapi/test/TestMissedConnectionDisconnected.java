package net.sourceforge.gjtapi.test;

/*
	Copyright (c) 2010 Deadman Consulting Inc. (www.deadman.ca) 

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

import javax.telephony.callcontrol.CallControlCall;
import javax.telephony.events.CallEv;
import javax.telephony.events.ConnAlertingEv;
import javax.telephony.events.ConnDisconnectedEv;
import javax.telephony.*;

/**
 * Test for observer event race condition that sometimes prevents all events being received.
 * Creation date: (2010-07-09)
 * @author: Richard Deadman
 */
public class TestMissedConnectionDisconnected implements CallObserver {
	private Provider prov = null;
	private Terminal[] ts = null;
	private Address addr = null;
	private String toAddr = null;
	private Call call = null;

/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
	// Report error if insufficient parameters passed in
	if (args.length < 3) {
		System.err.println("Usage: java net.sourceforge.gtapi.test.TestMissedConnectionDisconnected Provider from to");
		System.exit(1);
	}

	new TestMissedConnectionDisconnected().test(args[0], args[1], args[2]);
}
/**
 * Prompt for user input.
 * Creation date: (2000-02-17 10:49:50)
 * @author: Richard Deadman
 */
protected void prompt() throws IOException {
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
public void test(String providerName, String fromAddr, String toAddr) {
	this.toAddr = toAddr;
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

	// Ask it for the Provider
	try {
		prov = peer.getProvider(providerName);
		out.println("1.2: Successfully loaded Provider");
	} catch (ProviderUnavailableException pue) {
		out.println("1.2: Failed to load Provider");
		pue.printStackTrace();
		System.exit(1);
	}
	
	try {
		// Load the Address
		out.print("2.1: Attempting to get address...");
		addr = prov.getAddress(fromAddr);
		out.println(" success.");

		out.print("2.2: Attempting to get terminals for from address...");
		ts = addr.getTerminals();
		out.println(" success.");

		out.print("2.3: Attempting to get destination address...");
		Address destAddr = prov.getAddress(toAddr);
		out.println(" success.");

		out.print("2.4: Attempting to get terminals for to address...");
		Terminal[] toTerms = destAddr.getTerminals();
		out.println(" success.");
		
		out.print("2.5: Adding observer to first destination terminal...");
		Terminal destTerm = toTerms[0];
		destTerm.addCallObserver(this);
		out.println(" success.");
		
		out.print("2.6: Adding no-op observer to first source terminal...");
		Terminal sourceTerm = ts[0];
		sourceTerm.addCallObserver(new CallObserver() {

			public void callChangedEvent(CallEv[] events) {
				for(CallEv event : events) {
					// note the event
					System.out.println("Source Event received: " + event.toString() + " with ID: " + event.getID());
				}
			}
			
		});
		out.println(" success.");

		// Now we will make and hangup calls in a loop driven by the callback events
		// That is, make a call from A->B, have B's alerting event trigger hanging up the call
		// Have B's disconnected event cause the creation of a new call.
		this.makeCall();


	} catch (Exception e) {
		out.println(" failure: " + e);
		e.printStackTrace();
	}
}

/**
 * Make a call from the source to destination number
 */
private void makeCall() {
	java.io.PrintStream out = System.out;
	
	out.print("Attempting to create call...");
	call = null;
	try {
		call = (CallControlCall)prov.createCall();
	} catch (ResourceUnavailableException e) {
		e.printStackTrace();
		return;
	} catch (InvalidStateException e) {
		e.printStackTrace();
		return;
	} catch (PrivilegeViolationException e) {
		e.printStackTrace();
		return;
	} catch (MethodNotSupportedException e) {
		e.printStackTrace();
		return;
	}
	out.println(" success.");
	
	out.print("Attempting to connect call...");
	try {
		call.connect(ts[0], addr, toAddr);
	} catch (ResourceUnavailableException e) {
		e.printStackTrace();
		return;
	} catch (PrivilegeViolationException e) {
		e.printStackTrace();
		return;
	} catch (InvalidPartyException e) {
		e.printStackTrace();
		return;
	} catch (InvalidArgumentException e) {
		e.printStackTrace();
		return;
	} catch (InvalidStateException e) {
		e.printStackTrace();
		return;
	} catch (MethodNotSupportedException e) {
		e.printStackTrace();
		return;
	}
	out.println(" success.");

}

/**
 * Hangup the call
 */
private void hangup() {
	System.out.println("Attempting to hang up call");
	
	Connection[] conns = call.getConnections();
	Connection outgoingConnection = null;
	// find which one is mine and store it
	for(Connection conn : conns) {
		if(conn.getAddress().equals(addr)) {
			outgoingConnection = conn;
		}
	}

	try {
		outgoingConnection.disconnect();
	} catch (PrivilegeViolationException e) {
		e.printStackTrace();
		return;
	} catch (ResourceUnavailableException e) {
		e.printStackTrace();
		return;
	} catch (MethodNotSupportedException e) {
		e.printStackTrace();
		return;
	} catch (InvalidStateException e) {
		e.printStackTrace();
		return;
	}
}
/**
 * Describe myself
 * @return a string representation of myself
 */
public String toString() {
	return "Simple test class to ensure disconnect events are always received";
}

/**
 * Handle callback events for the destination address
 */
public void callChangedEvent(CallEv[] events) {
	java.io.PrintStream out = System.out;
	boolean alerting = false;
	boolean disconnected = false;
	
	for(CallEv event : events) {
		// note the event
		out.println("Event received: " + event.toString() + " with ID: " + event.getID());
		
		if(event instanceof ConnAlertingEv) {
			out.println("Received alerting event. Hanging up call...");
			alerting = true;
		} else if(event instanceof ConnDisconnectedEv) {
			out.println("Received disconnected event. Creating new call...");
			disconnected = true;
		}
	}
	
	if(alerting) {
		new Thread(new Runnable() {
			
			public void run() {
				hangup();
			}
			
		}).start();
		
	} else if(disconnected) {
		new Thread(new Runnable() {
	
			public void run() {
				makeCall();
			}
			
		}).start();
	}
	
}
}
