/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

/**
 * A JcatAddressListener interface is used to report the
 * association/disassociation of a JcatTerminal with a JcatAddress. 
 */
public interface JcatAddressListener {

	boolean terminalRegistered(JcatAddressEvent addressevent);
	
	void terminalDeregistered(JcatAddressEvent addressevent);
	
	void addressEventTransmissionEnded(JcatAddressEvent addressevent);
}
