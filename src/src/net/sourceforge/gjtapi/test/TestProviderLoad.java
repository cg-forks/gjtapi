package net.sourceforge.gjtapi.test;

import javax.telephony.JtapiPeer;
import javax.telephony.JtapiPeerFactory;
import javax.telephony.JtapiPeerUnavailableException;
import javax.telephony.Provider;

/**
 * This class tests the auto-loading of provider files
 * @author rdeadman
 *
 */
public class TestProviderLoad {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
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
		
		String[] services = peer.getServices();
		for(int i = 0; i<services.length; i++) {
			out.println("Service: " + services[i]);
		}

		// now check if a provider is requested
		if(args.length > 0) {
			Provider prov = peer.getProvider(args[0]);
			
			try {
				Thread.sleep(10000);
			} catch (InterruptedException ie) {
				// just end
			}
			
			prov.shutdown();
		}
	}

}
