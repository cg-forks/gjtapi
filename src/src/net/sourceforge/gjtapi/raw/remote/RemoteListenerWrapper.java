package net.sourceforge.gjtapi.raw.remote;

/*
	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 

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
import java.io.Serializable;
import javax.telephony.media.Symbol;
import net.sourceforge.gjtapi.media.*;
import java.rmi.RemoteException;
import net.sourceforge.gjtapi.*;
/**
 * This wraps a RemoteListener and makes it look like a RawListener again. The RawProvider can then
 * deal with RawObservers as if they are local.
 * Creation date: (2000-02-17 13:36:23)
 * @author: Richard Deadman
 */
public class RemoteListenerWrapper implements TelephonyListener {
	private RemoteListener delegate;
	private CallMapper refMapper = null;
/**
 * Create a wrapper RawObserver that delegates of to a RemoteObserver.
 * Creation date: (2000-02-17 13:36:49)
 * @author: Richard Deadman
 * @param rl The wrapped RemoteListener stub
 * @param mapper A map between RP call ids and serializable call ids.
 */
public RemoteListenerWrapper(RemoteListener rl, CallMapper mapper) {
	super();

	if (rl == null)
		throw new NullPointerException();
	else
		this.setDelegate(rl);

	this.setRefMapper(mapper);
}
/**
 * addressPrivateData method comment.
 */
public void addressPrivateData(String address, Serializable data, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().addressPrivateData(address, data, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * callActive method comment.
 */
public void callActive(net.sourceforge.gjtapi.CallId id, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().callActive(this.getRefMapper().swapId(id), cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * callInvalid method comment.
 */
public void callInvalid(net.sourceforge.gjtapi.CallId id, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().callInvalid(this.getRefMapper().swapId(id), cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * callOverloadCeased method comment.
 */
public void callOverloadCeased(java.lang.String address) {
	// Try to send the event to the client side
	try {
		this.getDelegate().callOverloadCeased(address);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * callOverloadEncountered method comment.
 */
public void callOverloadEncountered(java.lang.String address) {
	// Try to send the event to the client side
	try {
		this.getDelegate().callOverloadEncountered(address);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * callPrivateData method comment.
 */
public void callPrivateData(CallId call, Serializable data, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().callPrivateData(this.getRefMapper().swapId(call), data, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionAddressAnalyse method comment.
 */
public void connectionAddressAnalyse(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionAddressAnalyse(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionAddressCollect method comment.
 */
public void connectionAddressCollect(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionAddressCollect(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionAlerting method comment.
 */
public void connectionAlerting(CallId id, String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionAlerting(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionAuthorizeCallAttempt method comment.
 */
public void connectionAuthorizeCallAttempt(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionAuthorizeCallAttempt(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionCallDelivery method comment.
 */
public void connectionCallDelivery(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionCallDelivery(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionConnected method comment.
 */
public void connectionConnected(CallId id, String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionConnected(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionDisconnected method comment.
 */
public void connectionDisconnected(CallId id, String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionDisconnected(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionFailed method comment.
 */
public void connectionFailed(CallId id, String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionFailed(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionInProgress method comment.
 */
public void connectionInProgress(CallId id, String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionInProgress(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * connectionSuspended method comment.
 */
public void connectionSuspended(net.sourceforge.gjtapi.CallId id, java.lang.String address, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().connectionSuspended(this.getRefMapper().swapId(id), address, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * Sees if the two objects hold equal delegates.
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 */
public boolean equals(Object obj) {
	if (obj instanceof RemoteListenerWrapper) {
		RemoteListenerWrapper rlw = (RemoteListenerWrapper)obj;
		return this.getDelegate().equals(rlw.getDelegate());
	}
	return false;
}
/**
 * Internal accessor for the stub of the Remote Listener.
 * Creation date: (2000-02-17 13:38:44)
 * @author: Richard Deadman
 * @return The remote stub that implements the RemoteListener interface.
 */
private RemoteListener getDelegate() {
	return delegate;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-04-25 0:15:49)
 * @author: 
 * @return net.sourceforge.gjtapi.raw.remote.CallMapper
 */
private CallMapper getRefMapper() {
	return refMapper;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getDelegate().hashCode();
}
/**
 * mediaPlayPause method comment.
 */
public void mediaPlayPause(java.lang.String terminal, int index, int offset, javax.telephony.media.Symbol trigger) {
	// Try to send the event to the client side
	try {
		this.getDelegate().mediaPlayPause(terminal, index, offset, new SymbolHolder(trigger));
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * mediaPlayResume method comment.
 */
public void mediaPlayResume(java.lang.String terminal, javax.telephony.media.Symbol trigger) {
	// Try to send the event to the client side
	try {
		this.getDelegate().mediaPlayResume(terminal, new SymbolHolder(trigger));
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * mediaRecorderPause method comment.
 */
public void mediaRecorderPause(java.lang.String terminal, int duration, javax.telephony.media.Symbol trigger) {
	// Try to send the event to the client side
	try {
		this.getDelegate().mediaRecorderPause(terminal, duration, new SymbolHolder(trigger));
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * mediaRecorderResume method comment.
 */
public void mediaRecorderResume(java.lang.String terminal, javax.telephony.media.Symbol trigger) {
	// Try to send the event to the client side
	try {
		this.getDelegate().mediaRecorderResume(terminal, new SymbolHolder(trigger));
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * mediaSignalDetectorDetected method comment.
 */
public void mediaSignalDetectorDetected(java.lang.String terminal, Symbol[] sigs) {
	// Try to send the event to the client side
	try {
		this.getDelegate().mediaSignalDetectorDetected(terminal, SymbolHolder.create(sigs));
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * mediaSignalDetectorOverflow method comment.
 */
public void mediaSignalDetectorOverflow(java.lang.String terminal, javax.telephony.media.Symbol[] sigs) {
	// Try to send the event to the client side
	try {
		this.getDelegate().mediaSignalDetectorOverflow(terminal, SymbolHolder.create(sigs));
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * mediaSignalDetectorPatternMatched method comment.
 */
public void mediaSignalDetectorPatternMatched(java.lang.String terminal, javax.telephony.media.Symbol[] sigs, int index) {
	// Try to send the event to the client side
	try {
		this.getDelegate().mediaSignalDetectorPatternMatched(terminal, SymbolHolder.create(sigs), index);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * providerPrivateData method comment.
 */
public void providerPrivateData(Serializable data, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().providerPrivateData(data, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * Internal settor for the RemoteListener stub.
 * Creation date: (2000-02-17 13:38:44)
 * @author: Richard Deadman
 * @param newDelegate Remote stub I delegate to.
 */
private void setDelegate(RemoteListener newDelegate) {
	delegate = newDelegate;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-04-25 0:15:49)
 * @author: 
 * @param newRefMapper net.sourceforge.gjtapi.raw.remote.CallMapper
 */
private void setRefMapper(CallMapper newRefMapper) {
	refMapper = newRefMapper;
}
/**
 * terminalConnectionCreated method comment.
 */
public void terminalConnectionCreated(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().terminalConnectionCreated(this.getRefMapper().swapId(id), address, terminal, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * terminalConnectionDropped method comment.
 */
public void terminalConnectionDropped(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().terminalConnectionDropped(this.getRefMapper().swapId(id), address, terminal, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * terminalConnectionHeld method comment.
 */
public void terminalConnectionHeld(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().terminalConnectionHeld(this.getRefMapper().swapId(id), address, terminal, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * terminalConnectionRinging method comment.
 */
public void terminalConnectionRinging(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().terminalConnectionRinging(this.getRefMapper().swapId(id), address, terminal, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * terminalConnectionTalking method comment.
 */
public void terminalConnectionTalking(CallId id, String address, String terminal, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().terminalConnectionTalking(this.getRefMapper().swapId(id), address, terminal, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * terminalPrivateData method comment.
 */
public void terminalPrivateData(String terminal, Serializable data, int cause) {
	// Try to send the event to the client side
	try {
		this.getDelegate().terminalPrivateData(terminal, data, cause);
	} catch (RemoteException re) {
		// eat
		System.err.println("Error pushing events back to client: ");
		re.printStackTrace(System.err);
	}
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "RawObserver wrapper for RemoteObserver: " + this.getDelegate().toString();
}
}
