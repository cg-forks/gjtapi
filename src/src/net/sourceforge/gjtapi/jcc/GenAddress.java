package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2002 Deadman Consulting (www.deadman.ca) 

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
import javax.jcat.JcatAddressListener;
import javax.jcat.JcatTerminal;
import javax.telephony.Connection;
import javax.telephony.Terminal;
/**
 * A Jain Jcc address adapter for a Generic JTAPI Address object.
 * Creation date: (2000-10-10 14:17:06)
 * @author: Richard Deadman
 */
public class GenAddress implements JccAddress, JcatAddress {
	private Provider provider;
	private FreeAddress frameAddr;
	private int type;	// look up lazily
/**
 * GenAddress constructor comment.
 */
public GenAddress(Provider prov, FreeAddress fa) {
	super();

	this.setProvider(prov);
	this.setFrameAddr(fa);
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
	if (obj instanceof GenAddress) {
		return this.getFrameAddr().equals(((GenAddress)obj).getFrameAddr());
	}
	return false;
}
/**
 * Accessor for the JTAPI address I wrap.
 * Creation date: (2000-10-10 14:21:59)
 * @return net.sourceforge.gjtapi.FreeAddress
 */
net.sourceforge.gjtapi.FreeAddress getFrameAddr() {
	return frameAddr;
}
/**
 * getName method comment.
 */
public String getName() {
	return this.getFrameAddr().getName();
}
/**
 * getProvider method comment.
 */
public JccProvider getProvider() {
	return this.provider;
}
/**
 * JTAPI has no equivalent -- drill through TelephonyProvider.
 */
public synchronized int getType() {
	if (this.type == UNDEFINED) {
		this.type = ((Provider)this.getProvider()).getGenProv().getRaw().getAddressType(this.getName());
	}
	return this.type;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getFrameAddr().hashCode();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-10 14:21:59)
 * @param newFrameAddr net.sourceforge.gjtapi.FreeAddress
 */
private void setFrameAddr(net.sourceforge.gjtapi.FreeAddress newFrameAddr) {
	frameAddr = newFrameAddr;
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
	return "Jain Jcc Address adapter for: " + this.getFrameAddr().toString();
}
	/**
	 * Add a JcatAddressListener with a filter.
	 * @see javax.jcat.JcatAddress#addAddressListener(javax.jcat.JcatAddressListener, javax.csapi.cc.jcc.EventFilter)
	 */
	public void addAddressListener(
		JcatAddressListener addrListener,
		EventFilter eventFilter) {
			this.getFrameAddr().addAddressListener(new AddressListenerAdapter((Provider)this.getProvider(), addrListener, eventFilter));

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatAddress#deregisterTerminal(javax.jcat.JcatTerminal)
	 */
	public void deregisterTerminal(JcatTerminal term)
		throws
			InvalidPartyException,
			MethodNotSupportedException,
			PrivilegeViolationException {
		// TODO Auto-generated method stub

	}

	/**
	 * Get the connections currently associated with this
	 * address.
	 * @see javax.jcat.JcatAddress#getConnections()
	 */
	@SuppressWarnings("unchecked")
	public Set getConnections() {
		// Get the array or GJTAPI connections
		Connection[] conns = this.getFrameAddr().getConnections();
		if (conns == null) 
			return null;
			
		// now go through the Connections and create wrappers
		Set results = new HashSet();
		int len = conns.length;
		Provider prov = this.provider;
		for (int i = 0; i < len; i++) {
			results.add(prov.findConnection((FreeConnection)conns[i]));
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatAddress#getDisplayAllowed()
	 */
	public boolean getDisplayAllowed() {
		// TODO This appears to be an error in the specification -- should go in Terminal
		return false;
	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatAddress#getDisplayText()
	 */
	public String getDisplayText() {
		// TODO This appears to be an error in the specification -- should go in Terminal
		return null;
	}

	/**
	 * Get the set of terminals currently mapped to this
	 * address.
	 * <P>Unlike JTAPI, under Jcat Terminals can be registered
	 * and deregistered dynamically with an Address.
	 * @see javax.jcat.JcatAddress#getTerminals()
	 */
	@SuppressWarnings("unchecked")
	public Set getTerminals() {
		// Get the array or GJTAPI connections
		Terminal[] terms = this.getFrameAddr().getTerminals();
		if (terms == null) 
			return null;
			
		// now go through the Terminals and create wrappers
		Set results = new HashSet();
		int len = terms.length;
		Provider prov = this.provider;
		for (int i = 0; i < len; i++) {
			results.add(prov.findTerminal((FreeTerminal)terms[i]));
		}
		return results;
	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatAddress#registerTerminal(javax.jcat.JcatTerminal)
	 */
	public void registerTerminal(JcatTerminal term)
		throws
			InvalidPartyException,
			MethodNotSupportedException,
			PrivilegeViolationException {
		// TODO Auto-generated method stub

	}

	/**
	 * Remove an AddressListener.
	 * <P>This creates an "equality" tested listener and removes it from
	 * the GJTAPI Address.
	 * @see javax.jcat.JcatAddress#removeAddressListener(javax.jcat.JcatAddressListener)
	 */
	public void removeAddressListener(JcatAddressListener addrListener) {
		this.getFrameAddr().removeAddressListener(new AddressListenerAdapter((Provider)this.getProvider(), addrListener, null));

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatAddress#setDisplayText(java.lang.String, boolean)
	 */
	public void setDisplayText(String text, boolean displayAllowed)
		throws InvalidArgumentException {
		// TODO This appears to be an error in the specification -- should go in Terminal

	}

}
