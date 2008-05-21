/*
	Copyright (c) 2005 Richard Deadman, www.deadman.ca 
	
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
package net.sourceforge.gjtapi.test.tapi3;

import javax.telephony.Address;
import javax.telephony.Call;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.Provider;
import javax.telephony.Terminal;
import javax.telephony.privatedata.PrivateData;

import net.sourceforge.gjtapi.raw.tapi3.PrivateDialCommand;

/**
 * @author rdeadman
 *
 * Test the dialing on an active call
 */
public class DialTest {

	/**
	 * 
	 */
	public DialTest(String address, String remoteNumber, String accessCode) {
		super();
		
		try {
			// first create a provider
			JtapiPeer peer = JtapiPeerFactory.getJtapiPeer("net.sourceforge.gjtapi.GenericJtapiPeer");
			
			Provider provider = peer.getProvider("Tapi3");
			
			// now create a call
			Address addr = provider.getAddress(address);
			Terminal term = addr.getTerminals()[0];
			Call call = provider.createCall();
			
			// dial the call
			call.connect(term, addr, remoteNumber);
			
			if (call instanceof PrivateData) {
				// wait a few seconds
				Thread.sleep(3000);
				
				// now dial on the call
				PrivateDialCommand dialCommand = new PrivateDialCommand(accessCode);
				Object result = ((PrivateData)call).sendPrivateData(dialCommand);
				System.out.println("Dialing access code returned: " + result);
			}
	
			// shutdown
			provider.shutdown();
		
		} catch (Exception ex) {
			System.err.println("Error: " + ex.getLocalizedMessage());
		}
	}
	
	public static void main(String[] args) {
		// check that we have two arguments
		if (args.length < 3) {
			System.err.println("Usage: DialTest address remoteNumber dialCode");
			return;
		}
		new DialTest(args[0], args[1], args[2]);
	}

}
