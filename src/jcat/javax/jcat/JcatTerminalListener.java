/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

/**
 * A JcatTerminalListener interface is used to report
 * the association/disassociation of a JcatAddress with
 * a JcatTerminal.
 */
public interface JcatTerminalListener {

	void addressRegistered(JcatTerminalEvent termconnectionevent);
	
	void addressDeregistered(JcatTerminalEvent termconnectionevent);
	
	void eventTransmissionEnded(JcatTerminalEvent termconnectionevent);
}
