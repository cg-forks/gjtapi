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
import net.sourceforge.gjtapi.media.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import net.sourceforge.gjtapi.*;

/**
 * This is a wrapper class for a RawListener that lets the RawListener receive remote updates.
 * On the Framework side, this translates RemoteListener skeleton calls to be passed on to the real
 * RawListener.
 * Creation date: (2000-02-17 13:09:32)
 * @author: Richard Deadman
 */
public class RemoteListenerImpl extends UnicastRemoteObject implements RemoteListener {
	static final long serialVersionUID = 137460616322975486L;
	
	private TelephonyListener delegate;
/**
 * Create a Remote proxy for a RawObserver
 * Creation date: (2000-02-17 13:16:33)
 * @author: Richard Deadman
 * @param obs The listner to forward events to
 */
public RemoteListenerImpl(TelephonyListener obs) throws RemoteException {
	super();
	
	if (obs == null)
		throw new NullPointerException();
	else
		this.setDelegate(obs);
}
/**
 * addressPrivateData method comment.
 */
public void addressPrivateData(String address, Serializable data, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.addressPrivateData(address, data, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the callActive event
 */
public void callActive(SerializableCallId id, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.callActive(id, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the callInvalid event.
 */
public void callInvalid(SerializableCallId id, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.callInvalid(id, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * callOverloadCeased method comment.
 */
public void callOverloadCeased(java.lang.String address) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.callOverloadCeased(address);
	} else {
		throw new RemoteException();
	}
}
/**
 * callOverloadEncountered method comment.
 */
public void callOverloadEncountered(java.lang.String address) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.callOverloadEncountered(address);
	} else {
		throw new RemoteException();
	}
}
/**
 * callPrivateData method comment.
 */
public void callPrivateData(SerializableCallId call, Serializable data, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.callPrivateData(call, data, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * connectionAddressAnalyse method comment.
 */
public void connectionAddressAnalyse(SerializableCallId id, java.lang.String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionAddressAnalyse(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * connectionAddressCollect method comment.
 */
public void connectionAddressCollect(SerializableCallId id, java.lang.String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionAddressCollect(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the connectionAlerting event.
 */
public void connectionAlerting(SerializableCallId id, String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionAlerting(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * connectionAuthorizeCallAttempt method comment.
 */
public void connectionAuthorizeCallAttempt(SerializableCallId id, java.lang.String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionAuthorizeCallAttempt(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * connectionCallDelivery method comment.
 */
public void connectionCallDelivery(SerializableCallId id, java.lang.String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionCallDelivery(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the connectionConnected event.
 */
public void connectionConnected(SerializableCallId id, String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionConnected(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the connectionDisconnected event.
 */
public void connectionDisconnected(SerializableCallId id, String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionDisconnected(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the connectionFailed event.
 */
public void connectionFailed(SerializableCallId id, String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionFailed(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the connectionInProgress event.
 */
public void connectionInProgress(SerializableCallId id, String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionInProgress(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * connectionSuspended method comment.
 */
public void connectionSuspended(SerializableCallId id, java.lang.String address, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.connectionSuspended(id, address, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Internal accessor
 * Creation date: (2000-02-17 13:15:53)
 * @author: Richard Deadman
 * @return The real RawListener I delegate my RemoteListener events to.
 */
private TelephonyListener getDelegate() {
	return delegate;
}
/**
 * Forward on the mediaPlayPause event.
 */
public void mediaPlayPause(String terminal, int index, int offset, SymbolHolder trigger) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.mediaPlayPause(terminal, index, offset, trigger.getSymbol());
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the mediaPlayResume event.
 */
public void mediaPlayResume(String terminal, SymbolHolder trigger) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.mediaPlayResume(terminal, trigger.getSymbol());
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the mediaRecorderPause event.
 */
public void mediaRecorderPause(String terminal, int duration, SymbolHolder trigger) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.mediaRecorderPause(terminal, duration, trigger.getSymbol());
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the mediaRecorderResume event.
 */
public void mediaRecorderResume(String terminal, SymbolHolder trigger) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.mediaRecorderResume(terminal, trigger.getSymbol());
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the mediaSignalDetectorDetected event.
 */
public void mediaSignalDetectorDetected(String terminal, SymbolHolder[] sigs) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.mediaSignalDetectorDetected(terminal, SymbolHolder.decode(sigs));
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the mediaSignalDetectorOverflow event.
 */
public void mediaSignalDetectorOverflow(String terminal, SymbolHolder[] sigs) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.mediaSignalDetectorOverflow(terminal, SymbolHolder.decode(sigs));
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the mediaSignalDetectorPatternMatched event.
 */
public void mediaSignalDetectorPatternMatched(String terminal, SymbolHolder[] sigs, int index) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.mediaSignalDetectorPatternMatched(terminal, SymbolHolder.decode(sigs), index);
	} else {
		throw new RemoteException();
	}
}
/**
 * providerPrivateData method comment.
 */
public void providerPrivateData(Serializable data, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.providerPrivateData(data, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Internal settor for the real listener delegate.
 * Creation date: (2000-02-17 13:15:53)
 * @author: Richard Deadman
 * @param newDelegate The real RawListener I forward RemoteListner events to.
 */
private void setDelegate(TelephonyListener newDelegate) {
	delegate = newDelegate;
}
/**
 * Forward on the terminalConnectionCreated event.
 */
public void terminalConnectionCreated(SerializableCallId id, String address, String terminal, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.terminalConnectionCreated(id, address, terminal, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the terminalConnectionDropped event.
 */
public void terminalConnectionDropped(SerializableCallId id, String address, String terminal, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.terminalConnectionDropped(id, address, terminal, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the terminalConnectionHeld event.
 */
public void terminalConnectionHeld(SerializableCallId id, String address, String terminal, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.terminalConnectionHeld(id, address, terminal, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the terminalConnectionRinging event.
 */
public void terminalConnectionRinging(SerializableCallId id, String address, String terminal, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.terminalConnectionRinging(id, address, terminal, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * Forward on the terminalConnectionTalking event.
 */
public void terminalConnectionTalking(SerializableCallId id, String address, String terminal, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.terminalConnectionTalking(id, address, terminal, cause);
	} else {
		throw new RemoteException();
	}
}
/**
 * terminalPrivateData method comment.
 */
public void terminalPrivateData(String terminal, Serializable data, int cause) throws RemoteException {
	TelephonyListener rl = this.getDelegate();
	
	if (rl != null) {
		rl.terminalPrivateData(terminal, data, cause);
	} else {
		throw new RemoteException("Unknown delegate");
	}
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Remote wrapper for: " + this.getDelegate().toString();
}
}
