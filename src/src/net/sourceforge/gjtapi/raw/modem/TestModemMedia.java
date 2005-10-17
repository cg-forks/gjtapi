package net.sourceforge.gjtapi.raw.modem;

/*
    Copyright (c) Westhawk Ltd 

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
import javax.telephony.media.MediaProvider;
import javax.telephony.media.SignalDetectorEvent;

import net.sourceforge.gjtapi.media.GenericMediaService;
import net.sourceforge.gjtapi.test.TestMakeCall;
/**
 * Test class for trying out the modem provider media commands
 * 
 * @author: Richard Deadman
 * @author: Ray Tran (Modifications for ModemProvider)
 */
public class TestModemMedia extends TestMakeCall {
/**
 * Starts the application.
 * @param args an array of command-line arguments
 */
public static void main(String[] args) {
    // Report error if insufficient parameters passed in
    if (args.length < 2) {
        System.err.println("Usage: java net.sourceforge.gjtapi.modem.TestModemMedia <from> <to>");
        System.exit(1);
    }

    test("net.sourceforge.gjtapi.raw.modem.ModemProvider", args[0], args[1]);
    System.exit(0);
}
/**
 * Performs a series of media unit tests
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

    // Ask it for the Modem Provider
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

        // answer args[1]
        out.print("3.2: Attempting to get call connections...");
        Connection cons[] = c.getConnections();
        out.println(" success: " + cons.length);

        // force a wait
        prompt();

        //  Create a media service
        out.print("Attempting to create a Media Service...");
        GenericMediaService ms = new GenericMediaService((MediaProvider)prov);
        out.println(" success.");

        // find the outgoing terminal
        out.print("2.4: Attempting to get a Terminal by name...");
        Terminal t = ts[0];
        out.println(" success.");

        // bind the media service
        out.print("13.1: Attempting to bind a Media Service to a terminal...");
        ms.bindToTerminal(null, t);
        out.println(" success.");

        // generate a signal
        out.print("13.6: Attempting to generate DTMF on a Media Service...");
        ms.sendSignals("411", null, null);
        out.println(" success.");

        // play a message
        out.print("13.4: Attempting to play on a Media Service...");
        ms.play("file:/project/jtapi/gjtapi/devel/source/Greeting.au", 0, null, null);
        out.println(" success.");
        
        // detect a signal
        out.print("13.7: Attempting to detect DTMF on a Media Service...");
        SignalDetectorEvent sde = ms.retrieveSignals(1, null, null, null);
        out.println(" success: " + sde.getSignalString());

        // record a message
        out.print("13.5: Attempting to record on a Media Service...");
        ms.record("file:/tmp/test.ul", null, null);
        //this modem outputs ulaw data - to convert to wav I use
        //$ sox -t ul -r 8000 test.ul test.wav
        out.println(" success.");

        // release the media service
        out.print("13.8: Attempting to release a Media Service...");
        ms.release();
        out.println(" success.");
    
    } catch (Exception e) {
        out.println(" failure: " + e);
        e.printStackTrace();
    }
}
}
