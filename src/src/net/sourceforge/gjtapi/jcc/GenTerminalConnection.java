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

import net.sourceforge.gjtapi.*;
import javax.csapi.cc.jcc.*;
import javax.jcat.JcatConnection;
import javax.jcat.JcatTerminal;
import javax.jcat.JcatTerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnection;
/**
 * A Jain Jcat terminal-connection adapter for a Generic JTAPI TerminalConnection object.
 * Creation date: (2003-11-04 14:17:06)
 * @author: Richard Deadman
 */
public class GenTerminalConnection implements JcatTerminalConnection {
	private Provider provider;
	private FreeTerminalConnection frameTc;
/**
 * Package-level constructor for the Jcat TerminalConnection object wrapping
 * the GJTAPI FreeTerminalConnection.
 */
GenTerminalConnection(Provider prov, FreeTerminalConnection ftc) {
	super();

	this.setProvider(prov);
	this.setFrameTC(ftc);
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
	if (obj instanceof GenTerminalConnection) {
		return this.getFrameTC().equals(((GenTerminalConnection)obj).getFrameTC());
	}
	return false;
}
/**
 * Accessor for the JTAPI terminal-connection I wrap.
 * Creation date: (2000-11-04 14:21:59)
 * @return net.sourceforge.gjtapi.FreeTerminalConnection
 */
net.sourceforge.gjtapi.FreeTerminalConnection getFrameTC() {
	return this.frameTc;
}
/**
 * Get my Jcat Provider
 */
public JccProvider getProvider() {
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
	return this.getFrameTC().hashCode();
}
/**
 * Set the wrapped GJTAPI terminal-connection.
 * Creation date: (2000-11-04 14:21:59)
 * @param newFrameAddr net.sourceforge.gjtapi.FreeTerminalConnection
 */
private void setFrameTC(net.sourceforge.gjtapi.FreeTerminalConnection newFrameTc) {
	this.frameTc = newFrameTc;
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
	return "Jain Jcat TerminalConnection adapter for: " + this.getFrameTC().toString();
}

	/**
	 * Answers a call appearing at a TerminalConnection
	 * @see javax.jcat.JcatTerminalConnection#answer()
	 */
	public void answer() {
		Exception ex = null;
		try {
			this.getFrameTC().answer();
		} catch (javax.telephony.ResourceUnavailableException rue) {
			ex = rue;
		} catch (javax.telephony.PrivilegeViolationException pve) {
			ex = pve;
		} catch (javax.telephony.InvalidStateException ise) {
			ex = ise;
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			ex = mnse;
		}
		if (ex != null) {
			// can't propagate
			System.err.println("Failed to answer call on " + this.toString() + ": " + ex.getMessage());
		}
	}

	/**
	 * Get a wrapper for my GJTAPI Connection
	 * @see javax.jcat.JcatTerminalConnection#getConnection()
	 */
	public JcatConnection getConnection() {
		return this.provider.findConnection((FreeConnection)this.getFrameTC().getConnection());
	}

	/**
	 * Morph the JTAPI TC state into a Jcat one
	 * @see javax.jcat.JcatTerminalConnection#getState()
	 */
	public int getState() {
		switch (((CallControlTerminalConnection)this.getFrameTC()).getCallControlState()) {
			case CallControlTerminalConnection.IDLE: {
				return JcatTerminalConnection.IDLE;
			}
			case CallControlTerminalConnection.BRIDGED: {
				return JcatTerminalConnection.BRIDGED;
			}
			case CallControlTerminalConnection.DROPPED: {
				return JcatTerminalConnection.DROPPED;
			}
			case CallControlTerminalConnection.HELD: {
				return JcatTerminalConnection.HELD;
			}
			case CallControlTerminalConnection.INUSE: {
				return JcatTerminalConnection.INUSE;
			}
			case CallControlTerminalConnection.RINGING: {
				return JcatTerminalConnection.RINGING;
			}
			case CallControlTerminalConnection.TALKING: {
				return JcatTerminalConnection.TALKING;
			}
		}
		return JcatTerminalConnection.IDLE;
	}

	/**
	 * Get a wrapper for my GJTAPI Terminal
	 * @see javax.jcat.JcatTerminalConnection#getTerminal()
	 */
	public JcatTerminal getTerminal() {
		return this.provider.findTerminal((FreeTerminal)this.getFrameTC().getTerminal());

	}

	/**
	 * Hold the TerminalConnection by passing it on to my delegate.
	 * @see javax.jcat.JcatTerminalConnection#hold()
	 */
	public void hold()
		throws
			InvalidStateException,
			MethodNotSupportedException,
			PrivilegeViolationException,
			ResourceUnavailableException {
		try {
			this.getFrameTC().hold();
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		}

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatTerminalConnection#join()
	 */
	public void join()
		throws
			InvalidStateException,
			MethodNotSupportedException,
			PrivilegeViolationException,
			ResourceUnavailableException {
		try {
			this.getFrameTC().join();
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		}

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatTerminalConnection#leave()
	 */
	public void leave()
		throws
			InvalidStateException,
			MethodNotSupportedException,
			PrivilegeViolationException,
			ResourceUnavailableException {
		try {
			this.getFrameTC().leave();
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		}

	}

	/* (non-Javadoc)
	 * @see javax.jcat.JcatTerminalConnection#unhold()
	 */
	public void unhold()
		throws
			InvalidStateException,
			MethodNotSupportedException,
			PrivilegeViolationException,
			ResourceUnavailableException {
		try {
			this.getFrameTC().unhold();
		} catch (javax.telephony.ResourceUnavailableException rue) {
			throw new ResourceUnavailableException(rue.getType());
		} catch (javax.telephony.PrivilegeViolationException pve) {
			throw new PrivilegeViolationException(pve.getType());
		} catch (javax.telephony.InvalidStateException ise) {
			throw new InvalidStateException(ise.getObject(), ise.getObjectType(), ise.getState(), ise.getMessage());
		} catch (javax.telephony.MethodNotSupportedException mnse) {
			throw new MethodNotSupportedException(mnse.getMessage());
		}

	}

}
