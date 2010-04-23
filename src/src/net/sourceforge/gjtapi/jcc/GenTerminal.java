package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2003 Deadman Consulting (www.deadman.ca) 

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
import java.util.HashSet;
import java.util.Set;

import net.sourceforge.gjtapi.*;
import javax.csapi.cc.jcc.*;
import javax.jcat.JcatAddress;
import javax.jcat.JcatProvider;
import javax.jcat.JcatTerminal;
import javax.jcat.JcatTerminalCapabilities;
import javax.jcat.JcatTerminalListener;
import javax.telephony.Address;
import javax.telephony.TerminalConnection;
/**
 * A Jain Jcat terminal adapter for a Generic JTAPI Terminal object.
 * Creation date: (2003-11-04 094:00:06)
 * @author: Richard Deadman
 */
public class GenTerminal implements JcatTerminal {
	private Provider provider;
	private FreeTerminal frameTerm;
	
	private GenTerminalCapabilites caps = null;
/**
 * GenTerminal constructor comment.
 */
public GenTerminal(Provider prov, FreeTerminal ft) {
	super();

	this.setProvider(prov);
	this.setFrameTerm(ft);
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
	if (obj instanceof GenTerminal) {
		return this.getFrameTerm().equals(((GenTerminal)obj).getFrameTerm());
	}
	return false;
}
/**
 * Accessor for the JTAPI address I wrap.
 * Creation date: (2003-11-04 14:21:59)
 * @return net.sourceforge.gjtapi.FreeTerminal
 */
net.sourceforge.gjtapi.FreeTerminal getFrameTerm() {
	return frameTerm;
}
/**
 * Get the name of my Terminal.
 */
public String getName() {
	return this.getFrameTerm().getName();
}
/**
 * getProvider method comment.
 */
public JcatProvider getProvider() {
	return this.provider;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getFrameTerm().hashCode();
}
/**
 * Set the GJTAPI Terminal that I wrap
 * Creation date: (2003-11-04 14:21:59)
 * @param newFrameTerm net.sourceforge.gjtapi.FreeTerminal
 */
private void setFrameTerm(net.sourceforge.gjtapi.FreeTerminal newFrameTerm) {
	frameTerm = newFrameTerm;
}
/**
 * Note the Jain Provider that created me.
 * Creation date: (2000-10-10 14:21:59)
 * @param prov A Jain Jcc Provider.
 */
private void setProvider(Provider prov) {
	this.provider = prov;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Jain Jcat Terminal adapter for: " + this.getFrameTerm().toString();
}
	/**
	 * Attach a CallListener to the Terminal.
	 * @see javax.jcat.JcatTerminal#addCallListener(javax.csapi.cc.jcc.JccCallListener)
	 */
	public void addCallListener(JccCallListener listener)
		throws MethodNotSupportedException, ResourceUnavailableException {
			try {
				this.getFrameTerm().addCallListener(new CallListenerAdapter((Provider)this.getProvider(), listener));
			} catch (javax.telephony.MethodNotSupportedException mnse) {
				throw new MethodNotSupportedException(mnse.getMessage());
			} catch (javax.telephony.ResourceUnavailableException rue) {
				throw new ResourceUnavailableException(rue.getType());
			}

	}

	/**
	 * Wrap and forward the TerminalListener on to GJTAPI
	 * @see javax.jcat.JcatTerminal#addTerminalListener(javax.jcat.JcatTerminalListener)
	 */
	public void addTerminalListener(JcatTerminalListener listener) {
		this.getFrameTerm().addTerminalListener(new TerminalListenerAdapter((Provider)this.getProvider(), listener));

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatTerminal#deregisterAddress(javax.jcat.JcatAddress)
	 */
	public void deregisterAddress(JcatAddress addr)
		throws
			InvalidPartyException,
			MethodNotSupportedException,
			PrivilegeViolationException {
		// TODO Auto-generated method stub

	}

	/**
	 * Get all the JcatAddresses on a Terminal
	 * @see javax.jcat.JcatTerminal#getAddresses()
	 */
	public Set<GenAddress> getAddresses() {
		Address[] addresses = this.getFrameTerm().getAddresses();
		if (addresses == null)
			return null;
		Set<GenAddress> results = new HashSet<GenAddress>();
		int len = addresses.length;
		for (int i = 0; i < len; i++) {
			results.add(new GenAddress(this.provider, (FreeAddress)addresses[i]));
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatTerminal#getTerminalCapabilities()
	 */
	public synchronized JcatTerminalCapabilities getTerminalCapabilities() {
		// get the JTAPI capabilites for the Terminal
		//TerminalCapabilities termCaps = this.getFrameTerm().getCapabilities();
		if (this.caps == null) {
			this.caps = new GenTerminalCapabilites();
			// find out if GJTAPI has displayText on for a terminal. 
		}
		return this.caps;
	}

	/**
	 * Get the TerminalConnections associated with the Terminal.
	 * These must be tested using equality, since there may be more than one
	 * wrapper for a GJTAPI FreeTerminalConnection.
	 * @see javax.jcat.JcatTerminal#getTerminalConnections()
	 */
	public Set<GenTerminalConnection> getTerminalConnections() {
		TerminalConnection[] tcs = this.getFrameTerm().getTerminalConnections();
		if (tcs == null)
			return null;
		Set<GenTerminalConnection> results = new HashSet<GenTerminalConnection>();
		int len = tcs.length;
		for (int i = 0; i < len; i++) {
			results.add(new GenTerminalConnection(this.provider, (FreeTerminalConnection)tcs[i]));
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatTerminal#registerAddress(javax.jcat.JcatAddress)
	 */
	public void registerAddress(JcatAddress addr)
		throws
			InvalidPartyException,
			MethodNotSupportedException,
			PrivilegeViolationException {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatTerminal#removeCallListener(javax.csapi.cc.jcc.JccCallListener)
	 */
	public void removeCallListener(JccCallListener callListener) {
		this.getFrameTerm().removeCallListener(new CallListenerAdapter(this.provider, callListener));

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatTerminal#removeTerminalListener(javax.jcat.JcatTerminalListener)
	 */
	public void removeTerminalListener(JcatTerminalListener termListener) {
		this.getFrameTerm().removeTerminalListener(new TerminalListenerAdapter((Provider)this.getProvider(), termListener));

	}

}
