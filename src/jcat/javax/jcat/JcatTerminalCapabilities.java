/*
 * Created on Sep 11, 2003
 *
 */
package javax.jcat;

/**
 * The JcatTerminalCapabilities interface represents the capabilities interface for the JcatTerminal. Applications obtain the dynamic capabilities via the (@link JcatTerminal#getTerminalCapabilities()} method. 
 */
public interface JcatTerminalCapabilities {

	boolean canDisplayText();
}
