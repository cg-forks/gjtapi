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
import java.io.*;

import javax.telephony.callcontrol.CallControlCall;
import javax.telephony.events.*;
import javax.telephony.*;
/**
 * Simple test script for the emulator raw provider that tests if Address.addCallObserver() and
 * Address.addCallListener() are both working properly.
 * Creation date: (2000-02-03 15:40:58)
 * @author: Richard Deadman
 */
public class TestIncomingCallListenerObserver implements ConnectionListener, CallObserver {
/**
 * callActive method comment.
 */
public void callActive(javax.telephony.CallEvent event) {
    System.out.println("5.1.1: Active Call event with cause: " + this.causeToString(event.getCause()));
}
/**
 * callEventTransmissionEnded method comment.
 */
public void callEventTransmissionEnded(javax.telephony.CallEvent event) {
    System.out.println("5.1.3: Event Transmission Ended Call event with cause: " + this.causeToString(event.getCause()));
}
/**
 * callInvalid method comment.
 */
public void callInvalid(javax.telephony.CallEvent event) {
    System.out.println("5.1.2: Invalid Call event with cause: " + this.causeToString(event.getCause()));
}
/**
 * Convert the event cause string to a cause.
 * Creation date: (2000-05-01 9:58:39)
 * @author: Richard Deadman
 * @return English description of the cause
 * @param cause The Event cause id.
 */
public String causeToString(int cause) {
    switch (cause) {
        case Event.CAUSE_CALL_CANCELLED: {
            return "Call cancelled";
        }
        case Event.CAUSE_DEST_NOT_OBTAINABLE: {
            return "Destination not obtainable";
        }
        case Event.CAUSE_INCOMPATIBLE_DESTINATION: {
            return "Incompatable destination";
        }
        case Event.CAUSE_LOCKOUT: {
            return "Lockout";
        }
        case Event.CAUSE_NETWORK_CONGESTION: {
            return "Network congestion";
        }
        case Event.CAUSE_NETWORK_NOT_OBTAINABLE: {
            return "Network not obtainable";
        }
        case Event.CAUSE_NEW_CALL: {
            return "New call";
        }
        case Event.CAUSE_NORMAL: {
            return "Normal";
        }
        case Event.CAUSE_RESOURCES_NOT_AVAILABLE: {
            return "Resource not available";
        }
        case Event.CAUSE_SNAPSHOT: {
            return "Snapshot";
        }
        case Event.CAUSE_UNKNOWN: {
            return "Unknown";
        }
    }
    return "Cause mapping error: " + cause;
}
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
    // Report error if insufficient parameters passed in
    if (args.length < 2) {
        System.err.println("Usage: java net.sourceforge.gjtapi.test.TestCallListener Provider listeningAddress");
        System.exit(1);
    }

    test(args[0], args[1]);
}
/**
 * multiCallMetaMergeEnded method comment.
 */
public void multiCallMetaMergeEnded(javax.telephony.MetaEvent event) {
    System.out.println("X.X: Multicall merge ended event with cause: " + this.causeToString(event.getCause()));
}
/**
 * multiCallMetaMergeStarted method comment.
 */
public void multiCallMetaMergeStarted(javax.telephony.MetaEvent event) {
    System.out.println("X.X: Multicall merge started event with cause: " + this.causeToString(event.getCause()));
}
/**
 * multiCallMetaTransferEnded method comment.
 */
public void multiCallMetaTransferEnded(javax.telephony.MetaEvent event) {
    System.out.println("X.X: Multicall transfer ended event with cause: " + this.causeToString(event.getCause()));
}
/**
 * multiCallMetaTransferStarted method comment.
 */
public void multiCallMetaTransferStarted(javax.telephony.MetaEvent event) {
    System.out.println("X.X: Multicall transfer started event with cause: " + this.causeToString(event.getCause()));
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
 * singleCallMetaProgressEnded method comment.
 */
public void singleCallMetaProgressEnded(javax.telephony.MetaEvent event) {
    System.out.println("X.X: Multicall progress ended event with cause: " + this.causeToString(event.getCause()));
}
/**
 * singleCallMetaProgressStarted method comment.
 */
public void singleCallMetaProgressStarted(javax.telephony.MetaEvent event) {
    System.out.println("X.X: Multicall progress started event with cause: " + this.causeToString(event.getCause()));
}
/**
 * singleCallMetaSnapshotEnded method comment.
 */
public void singleCallMetaSnapshotEnded(javax.telephony.MetaEvent event) {
    System.out.println("X.X: Singlecall snapshot ended event with cause: " + this.causeToString(event.getCause()));
}
/**
 * singleCallMetaSnapshotStarted method comment.
 */
public void singleCallMetaSnapshotStarted(javax.telephony.MetaEvent event) {
    System.out.println("X.X: Singlecall snapshot started event with cause: " + this.causeToString(event.getCause()));
}
/**
 * Performs a series of unit tests
 * @param args an array of command-line arguments
 */
public static void test(String providerName, String listeningAddr) {
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
        // Load the Address
        out.print("2.1: Attempting to get address...");
        Address addr = prov.getAddress(listeningAddr);
        out.println(" success.");

		TestIncomingCallListenerObserver obsListener = new TestIncomingCallListenerObserver();
		
        // assocate an observer with the Address
        out.print("1.0: Setting an observer on the address...");
        addr.addCallObserver(obsListener);
        out.println("success.");
        
        // associate a listener with the Address
        out.print("1.1: Setting a listener on the address...");
        addr.addCallListener(obsListener);
        out.println(" success.");
        
        // Notify progress
        System.out.println("Waiting for incoming calls. Press enter to shut down...");
        prompt();
        System.out.println("Provider shutting down");
        prov.shutdown();
        Thread.sleep(1000);
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
    return "Simple test class for testing call listeners and observers on an Address";
}
	/**
	 * Report old-style observer events on a call attached to the Address.
	 * @see javax.telephony.CallObserver#callChangedEvent(javax.telephony.events.CallEv)
	 */
	public void callChangedEvent(CallEv[] eventList) {
		String event = null;
		int id = eventList[0].getID();
		switch(id) {
			case CallActiveEv.ID: {
				event = "call active";
				break;
			}
			case CallInvalidEv.ID: {
				event = "call invalid";
				break;
			}
			case ConnAlertingEv.ID: {
				event = "Connection alerting";
				break;
			}
			case ConnConnectedEv.ID: {
				event = "Connection connected";
				break;
			}
			case ConnCreatedEv.ID: {
				event = "Connection created";
				break;
			}
			case ConnDisconnectedEv.ID: {
				event = "Connection disconnected";
				break;
			}
			case ConnFailedEv.ID: {
				event = "Connection failed";
				break;
			}
			case ConnInProgressEv.ID: {
				event = "Connection in progress";
				break;
			}
			case ConnUnknownEv.ID: {
				event = "Connection unknown";
				break;
			}
			case TermConnActiveEv.ID: {
				event = "Terminal Connection active";
				break;
			}
			case TermConnCreatedEv.ID: {
				event = "Terminal Connection created";
				break;
			}
			case TermConnDroppedEv.ID: {
				event = "Terminal Connection dropped";
				break;
			}
			case TermConnPassiveEv.ID: {
				event = "Terminal Connection passive";
				break;
			}
			case TermConnRingingEv.ID: {
				event = "Terminal Connection ringing";
				break;
			}
			case TermConnUnknownEv.ID: {
				event = "Terminal Connection unknown";
				break;
			}
			default: event = "unknown: " + id;
		}
		System.out.println("Observer event: " + event);
	}
/**
 * connectionAlerting method comment.
 */
public void connectionAlerting(javax.telephony.ConnectionEvent event) {
    System.out.println("5.3.3: Alerting Connection event with cause: " + this.causeToString(event.getCause()));
}

/**
 * connectionConnected method comment.
 */
public void connectionConnected(javax.telephony.ConnectionEvent event) {
    System.out.println("5.3.4: Connection Connected event with cause: " + this.causeToString(event.getCause()));
    CallControlCall call = (CallControlCall)event.getCall();
    System.out.println("CallingAddress: " + call.getCallingAddress().getName());
    System.out.println("CalledAddress: " + call.getCalledAddress().getName());
}

/**
 * connectionCreated method comment.
 */
public void connectionCreated(javax.telephony.ConnectionEvent event) {
    System.out.println("5.3.1: Connection Created event with cause: " + this.causeToString(event.getCause()));
}

/**
 * connectionDisconnected method comment.
 */
public void connectionDisconnected(javax.telephony.ConnectionEvent event) {
    System.out.println("5.3.5: Connection Disconnected event with cause: " + this.causeToString(event.getCause()));
}

/**
 * connectionFailed method comment.
 */
public void connectionFailed(javax.telephony.ConnectionEvent event) {
    System.out.println("5.3.6: Connection Failed event with cause: " + this.causeToString(event.getCause()));
}

/**
 * connectionInProgress method comment.
 */
public void connectionInProgress(javax.telephony.ConnectionEvent event) {
    System.out.println("5.3.2: Connection in Progress event with cause: " + this.causeToString(event.getCause()));
}

/**
 * connectionUnknown method comment.
 */
public void connectionUnknown(javax.telephony.ConnectionEvent event) {
    System.out.println("5.3.7: Connection Unknown event with cause: " + this.causeToString(event.getCause()));
}


}
