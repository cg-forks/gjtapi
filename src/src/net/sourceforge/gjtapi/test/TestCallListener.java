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
import javax.telephony.*;
/**
 * Simple test script for the emulator raw provider
 * Creation date: (2000-02-03 15:40:58)
 * @author: Richard Deadman
 */
public class TestCallListener implements TerminalConnectionListener {
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
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(java.lang.String[] args) {
    // Report error if insufficient parameters passed in
    if (args.length < 3) {
        System.err.println("Usage: java net.sourceforge.gjtapi.test.TestCallListener Provider from to");
        System.exit(1);
    }

    test(args[0], args[1], args[2]);
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
 * terminalConnectionActive method comment.
 */
public void terminalConnectionActive(javax.telephony.TerminalConnectionEvent event) {
    System.out.println("5.2.2: Terminal Connection Active event with cause: " + this.causeToString(event.getCause()));
}
/**
 * terminalConnectionCreated method comment.
 */
public void terminalConnectionCreated(javax.telephony.TerminalConnectionEvent event) {
    System.out.println("5.2.1: Terminal Connection Created event with cause: " + this.causeToString(event.getCause()));
}
/**
 * terminalConnectionDropped method comment.
 */
public void terminalConnectionDropped(javax.telephony.TerminalConnectionEvent event) {
    System.out.println("5.2.3: Terminal Connection Dropped event with cause: " + this.causeToString(event.getCause()));
}
/**
 * terminalConnectionPassive method comment.
 */
public void terminalConnectionPassive(javax.telephony.TerminalConnectionEvent event) {
    System.out.println("5.2.4: Terminal Connection Passive event with cause: " + this.causeToString(event.getCause()));
}
/**
 * terminalConnectionRinging method comment.
 */
public void terminalConnectionRinging(javax.telephony.TerminalConnectionEvent event) {
    System.out.println("5.2.5: Terminal Connection Ringing event with cause: " + this.causeToString(event.getCause()));
}
/**
 * terminalConnectionUnknown method comment.
 */
public void terminalConnectionUnknown(javax.telephony.TerminalConnectionEvent event) {
    System.out.println("5.2.6: Terminal Connection Unknown event with cause: " + this.causeToString(event.getCause()));
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

        // Make the Call
        out.print("2.3: Attempting to create call...");
        Call c = prov.createCall();
        out.println(" success.");

        // assocate a listener with the call
        out.print("3.3: Setting Listener on the call...");
        c.addCallListener(new TestCallListener());
        out.println(" success.");
        
        out.print("4.3: Attempting to get terminals for an address...");
        Terminal[] ts = addr.getTerminals();
        out.println(" success.");

        out.print("3.1: Attempting to connect call...");
        c.connect(ts[0], addr, toAddr);
        out.println(" success.");

        // Notify progress
        System.out.println("Call initiated...");
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
    return "Simple test class for testing call listeners";
}
}
