package net.sourceforge.gjtapi.test;

/*
Copyright (c) 2009 Deadman Consulting Inc. (www.deadman.ca) 

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


import javax.telephony.Address;
import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.InvalidStateException;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.Provider;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;
import java.io.*;

/**
 * Test class to ensure that the Call Connection HashMap
 * and the Connection TerminalConnection HashMaps are no
 * longer experiencing ConcurrencyModificationExceptions
 * that prevent calls from being cleaned out of the DomainMgr.
 * 
 * This issue was causing a memory leak as calls would never
 * be freed since the removal of Connections when a call terminates
 * would trigger a ConncurrentModificationException on the
 * underlying connection due to synchronization problems and
 * so the toInvalid() event would never get fired to free
 * the call from the DomainMgr.
 * @author Richard Deadman
 *
 */
public class TestConcurrentHandling {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    // Report error if insufficient parameters passed in
	    if (args.length < 3) {
	        System.err.println("Usage: java net.sourceforge.gjtapi.test.TestConcurrentHandling Provider from to");
	        System.exit(1);
	    }

	    test(args[0], args[1], args[2]);
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
	        // Load the Terminals
	        out.print("2.1: Attempting to get terminals...");
	        Terminal[] terminals = prov.getTerminals();
	        out.println(" success.");
	        for(Terminal term : terminals) {
	        	term.addCallObserver(new TestConcurrentObserver(term));
	        }

	        Address addr = prov.getAddress(fromAddr);
	        Terminal[] ts = addr.getTerminals();
	        
	        // make a series of calls to ensure the call manager disposes of them properly
	        for(int i = 0; i < 100; i++) {
	        	processCall(prov, addr, ts, toAddr);
	        }
	        prompt();
	        System.out.println("Provider shutting down");
	        //Thread.sleep(1000);
	        try {
	        	System.out.println("Test ending. Number of calls in provider: " + prov.getCalls().length);
	        } catch (NullPointerException npe) {
	        	System.out.println("Test ending. Number of calls in provider: " + 0);
	        }

	        prov.shutdown();
	    } catch (Exception e) {
	        out.println(" failure: " + e);
	        e.printStackTrace();
	    }
	}

	private static void processCall(Provider prov, Address addr, Terminal[] ts, String toAddr) {
		java.io.PrintStream out = System.out;
		
        // Make the Call
		try {
	        Call c = prov.createCall();
	   
	        c.connect(ts[0], addr, toAddr);
	
	        // Notify progress
	        System.out.println("Call created. Number of calls in provider: " + prov.getCalls().length);
	        Connection[] cons = c.getConnections();
	        if(cons != null && cons.length > 0 ) {
	        	cons[0].disconnect();
	        }
	        //Thread.sleep(1000);
	        try {
	        	System.out.println("Call ending. Number of calls in provider: " + prov.getCalls().length);
	        } catch (NullPointerException npe) {
	        	System.out.println("Call ending. Number of calls in provider: " + 0);
	        }
		} catch (PrivilegeViolationException pve) {
			pve.printStackTrace();
		} catch (MethodNotSupportedException pve) {
			pve.printStackTrace();
		} catch (InvalidStateException pve) {
			pve.printStackTrace();
		} catch (ResourceUnavailableException pve) {
			pve.printStackTrace();
		} catch (InvalidArgumentException pve) {
			pve.printStackTrace();
		} catch (InvalidPartyException pve) {
			pve.printStackTrace();
		}

	}
	/**
	 * Prompt for user input.
	 * @author: Richard Deadman
	 */
	protected static void prompt() throws IOException {
	    InputStream in = System.in;

	    System.out.println("Hit return to continue...");
	    // wait
	    in.read();
	    in.skip(in.available());
	}

}
