package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2003, Richard Deadman, Deadman Consulting (www.deadman.ca) 

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
import javax.jcat.JcatTerminalConnectionEvent;
import javax.jcat.JcatTerminalConnectionListener;
import javax.telephony.*;
/**
 * This is a simple adapter that listens for JTAPI TerminalConnection events and translates
 * them into JcatTerminalConnection events.
 * Creation date: (2003-10-11 15:09:31)
 * @author: Richard Deadman
 */
public class TerminalConnectionListenerAdapter implements TerminalConnectionListener {
	private Provider prov = null;
	private JcatTerminalConnectionListener realTerminalConnectionListener = null;
/**
 * Create an adapter for a Jcat TerminalListener so that it
 * can receive JTAPI events.
 */
public TerminalConnectionListenerAdapter(Provider prov,
			JcatTerminalConnectionListener listener) {
	super();

	this.setProv(prov);
	this.setRealTerminalConnectionListener(listener);
}
/**
 * Compares two objects for equality. Returns a boolean that indicates
 * whether this object is equivalent to the specified object. This method
 * is used when an object is stored in a hashtable.
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals(Object obj) {
	if (obj instanceof TerminalConnectionListenerAdapter) {
		// test if they have the same real Listener
		return this.getRealTerminalConnectionListener().equals(((TerminalConnectionListenerAdapter)obj).getRealTerminalConnectionListener());
	}
	return false;
}
/**
 * Get the provider that I am attached to
 * Creation date: (2003-10-30 10:43:58)
 * @return com.uforce.jain.generic.Provider
 */
Provider getProv() {
	return prov;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getRealTerminalConnectionListener().hashCode();
}
/**
 * Insert the method's description here.
 * Creation date: (2003-10-30 10:43:58)
 * @param newProv com.uforce.jain.generic.Provider
 */
private void setProv(Provider newProv) {
	prov = newProv;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Listener adapter for Jcat TerminalConnection Listener: " + this.getRealTerminalConnectionListener();
}
	/**
	 * Get the TerminalConnectionListener that I propagate JTAPI events to.
	 * @return a Jcat TerminalConnectionListener.
	 */
	private JcatTerminalConnectionListener getRealTerminalConnectionListener() {
		return realTerminalConnectionListener;
	}

	/**
	 * Set the TerminalConnectionListener that I propagate JTAPI events to.
	 * @param listener
	 */
	public void setRealTerminalConnectionListener(JcatTerminalConnectionListener listener) {
		realTerminalConnectionListener = listener;
	}

	/**
	 * Map JTAPI Active event to Talking since JTAPI 1.3 doesn't have CC events yet
	 * @see javax.telephony.TerminalConnectionListener#terminalConnectionActive(javax.telephony.TerminalConnectionEvent)
	 */
	public void terminalConnectionActive(TerminalConnectionEvent event) {
		JcatTerminalConnectionEvent ev = new GenTerminalConnectionEvent(this.getProv(), event);
		this.getRealTerminalConnectionListener().terminalConnectionTalking(ev);

	}

	/**
	 * Map JTAPI created event.
	 * @see javax.telephony.TerminalConnectionListener#terminalConnectionCreated(javax.telephony.TerminalConnectionEvent)
	 */
	public void terminalConnectionCreated(TerminalConnectionEvent event) {
		JcatTerminalConnectionEvent ev = new GenTerminalConnectionEvent(this.getProv(), event);
		this.getRealTerminalConnectionListener().terminalConnectionCreated(ev);

	}

	/**
	 * Map JTAPI Dropped event
	 * @see javax.telephony.TerminalConnectionListener#terminalConnectionDropped(javax.telephony.TerminalConnectionEvent)
	 */
	public void terminalConnectionDropped(TerminalConnectionEvent event) {
		JcatTerminalConnectionEvent ev = new GenTerminalConnectionEvent(this.getProv(), event);
		this.getRealTerminalConnectionListener().terminalConnectionDropped(ev);

	}

	/**
	 * Map JTAPI Passive event to Held since JATPI doesn't have CC Listener events in 1.3
	 * @see javax.telephony.TerminalConnectionListener#terminalConnectionPassive(javax.telephony.TerminalConnectionEvent)
	 */
	public void terminalConnectionPassive(TerminalConnectionEvent event) {
		JcatTerminalConnectionEvent ev = new GenTerminalConnectionEvent(this.getProv(), event);
		this.getRealTerminalConnectionListener().terminalConnectionHeld(ev);

	}

	/**
	 * Map JTAPI Ringing event.
	 * @see javax.telephony.TerminalConnectionListener#terminalConnectionRinging(javax.telephony.TerminalConnectionEvent)
	 */
	public void terminalConnectionRinging(TerminalConnectionEvent event) {
		JcatTerminalConnectionEvent ev = new GenTerminalConnectionEvent(this.getProv(), event);
		this.getRealTerminalConnectionListener().terminalConnectionRinging(ev);

	}

	/* (non-Javadoc)
	 * @see javax.telephony.TerminalConnectionListener#terminalConnectionUnknown(javax.telephony.TerminalConnectionEvent)
	 */
	public void terminalConnectionUnknown(TerminalConnectionEvent event) {
		// no Jcat event

	}

	/* (non-Javadoc)
	 * @see javax.telephony.ConnectionListener#connectionAlerting(javax.telephony.ConnectionEvent)
	 */
	public void connectionAlerting(ConnectionEvent event) {
		// don't propagate non TC events...

	}

	/* (non-Javadoc)
	 * @see javax.telephony.ConnectionListener#connectionConnected(javax.telephony.ConnectionEvent)
	 */
	public void connectionConnected(ConnectionEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.ConnectionListener#connectionCreated(javax.telephony.ConnectionEvent)
	 */
	public void connectionCreated(ConnectionEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.ConnectionListener#connectionDisconnected(javax.telephony.ConnectionEvent)
	 */
	public void connectionDisconnected(ConnectionEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.ConnectionListener#connectionFailed(javax.telephony.ConnectionEvent)
	 */
	public void connectionFailed(ConnectionEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.ConnectionListener#connectionInProgress(javax.telephony.ConnectionEvent)
	 */
	public void connectionInProgress(ConnectionEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.ConnectionListener#connectionUnknown(javax.telephony.ConnectionEvent)
	 */
	public void connectionUnknown(ConnectionEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callActive(javax.telephony.CallEvent)
	 */
	public void callActive(CallEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callEventTransmissionEnded(javax.telephony.CallEvent)
	 */
	public void callEventTransmissionEnded(CallEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callInvalid(javax.telephony.CallEvent)
	 */
	public void callInvalid(CallEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaMergeEnded(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaMergeEnded(MetaEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaMergeStarted(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaMergeStarted(MetaEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaTransferEnded(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaTransferEnded(MetaEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaTransferStarted(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaTransferStarted(MetaEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaProgressEnded(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaProgressEnded(MetaEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaProgressStarted(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaProgressStarted(MetaEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaSnapshotEnded(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaSnapshotEnded(MetaEvent event) {
		// don't propagate non TC events...
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaSnapshotStarted(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaSnapshotStarted(MetaEvent event) {
		// don't propagate non TC events...
	}

}
