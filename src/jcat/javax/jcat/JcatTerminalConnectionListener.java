/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

/**
 * This interface hence reports all state changes occurring in the JcatTerminalConnection object. 
 */
public interface JcatTerminalConnectionListener {

	void terminalConnectionCreated(JcatTerminalConnectionEvent termconnectionevent);
	
	void terminalConnectionRinging(JcatTerminalConnectionEvent termconnectionevent);
	
	void terminalConnectionDropped(JcatTerminalConnectionEvent termconnectionevent);
	
	void terminalConnectionBridged(JcatTerminalConnectionEvent termconnectionevent);
	
	void terminalConnectionTalking(JcatTerminalConnectionEvent termconnectionevent);
	
	void terminalConnectionInuse(JcatTerminalConnectionEvent termconnectionevent);
	
	void terminalConnectionHeld(JcatTerminalConnectionEvent termconnectionevent);
	
	void terminalConnectionEventTransmissionEnded(JcatTerminalConnectionEvent termconnectionevent);
}
