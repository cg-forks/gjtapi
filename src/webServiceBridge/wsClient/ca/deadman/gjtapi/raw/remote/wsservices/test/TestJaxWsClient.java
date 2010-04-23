package ca.deadman.gjtapi.raw.remote.wsservices.test;

import javax.telephony.Address;
import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.Provider;
import javax.telephony.ProviderUnavailableException;
import javax.telephony.ResourceUnavailableException;

/**
 * Test the call to the JaxWS GJTAPI server
 * @author rdeadman
 *
 */
public class TestJaxWsClient {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
	    java.io.PrintStream out = System.out;

	    // Expect the url and port to be passed in
		if((args.length > 1) && (args[0].equalsIgnoreCase("-h"))) {
			out.println("Usage: java " + TestJaxWsClient.class.getCanonicalName() + " url, port");
			System.exit(0);
		}

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
	        prov = peer.getProvider("JaxWsClient");
	        out.println("1.2: Successfully loaded JAX-WS Provider");
	    } catch (ProviderUnavailableException pue) {
	        out.println("1.2: Failed to load Provider");
	        pue.printStackTrace();
	        System.exit(1);
	    }

	    // As provider for number of addresses
	    Address[] addresses = null;
	    try {
			addresses = prov.getAddresses();
		} catch (ResourceUnavailableException rue) {
	        out.println("1.3: Failed to get addresses");
	        rue.printStackTrace();
	        System.exit(1);
		}
		
		if (addresses != null) {
			out.println("Addresses (" + addresses.length + "):");
			for(int i = 0; i < addresses.length; i++) {
				out.println(addresses[i].getName());
			}
		} else {
			out.println("No addresses found.");			
		}
		
		prov.shutdown();

	}

}
