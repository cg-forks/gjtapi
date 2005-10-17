package net.sourceforge.gjtapi;

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
import javax.telephony.*;

/**
 * Proxy for an InvalidStateException that holds enough information to determine the object
 * whose state is not correct.  This may be morphed at the Generic JTAPI layer into an
 * InvalidStateExcedption and this thrown back to the calling application.
 * Creation date: (2000-02-09 10:45:24)
 * @author: Richard Deadman
 */
public class RawStateException extends InvalidStateException {
	static final long serialVersionUID = 5577229303226856758L;
	
	private CallId call;
	private java.lang.String address;
	private java.lang.String terminal;
/**
 * Create a denormalized version of the exception.
 * Creation date: (2000-02-21 13:20:08)
 * @author: Richard Deadman
 * @param name The name of the terminal or address affected.
 * @param type The object type
 * @param state The current state of the object (see javax.telephony.Terminal).
 */
public RawStateException(String name, int type, int state) {
	super(null, type, state);

	if (type == RawStateException.ADDRESS_OBJECT)
		this.initialize(null, name, null);
	if (type == RawStateException.TERMINAL_OBJECT)
		this.initialize(null, null, name);
}
/**
 * Create a denormalized version of the exception.
 * Creation date: (2000-02-21 13:20:08)
 * @author: Richard Deadman
 * @param id The raw call id associated with the call
 * @param state The current state of the call (see javax.telephony.Call).
 */
public RawStateException(CallId id, int state) {
	super(null, InvalidStateException.CALL_OBJECT, state);
	
	this.initialize(id, null, null);
}
/**
 * Create a denormalized version of the exception.
 * Creation date: (2000-02-21 13:20:08)
 * @author: Richard Deadman
 * @param id The raw call id associated with the call, if appropriate
 * @param address The address name associated with the call, if appropriate
 * @param terminal The terminal name associated with the call, if appropriate
 * @param type The type of the object in the wrong state.  See IllegalStateException
 * @param state The current state of the object, as known by the raw provider.
 */
public RawStateException(CallId id, String address, String terminal, int type, int state) {
	super(null, type, state);
	
	this.initialize(id, address, terminal);
}
/**
 * Create a denormalized version of the exception.
 * Creation date: (2000-02-21 13:20:08)
 * @author: Richard Deadman
 * @param id The raw call id associated with the call, if appropriate
 * @param address The address name associated with the call, if appropriate
 * @param terminal The terminal name associated with the call, if appropriate
 * @param type The type of the object in the wrong state.  See IllegalStateException
 * @param state The current state of the object, as known by the raw provider.
 * @param info Text info about the exception.
 */
public RawStateException(CallId id, String address, String terminal, int type, int state, String info) {
	super(null, type, state);
	
	this.initialize(id, address, terminal);
}
/**
 * Return the address my object is associated with.
 * Creation date: (2000-02-21 13:27:12)
 * @author: 
 * @return java.lang.String
 */
public java.lang.String getAddress() {
	return address;
}
/**
 * Return the call my object is associated with.
 * Creation date: (2000-02-21 13:26:04)
 * @author: 
 * @return net.sourceforge.gjtapi.CallId
 */
public CallId getCall() {
	return call;
}
/**
 * Return the terminal my object is associated with.
 * Creation date: (2000-02-21 13:27:33)
 * @author: 
 * @return java.lang.String
 */
public java.lang.String getTerminal() {
	return terminal;
}
/**
 * Fill in the internal state for later morphing.
 * Creation date: (2000-02-21 13:29:09)
 * @author: Richard Deadman
 * @param id The raw call id
 * @param address String version of the address
 * @param terminal Stringer version of the terminal
 */
private void initialize(CallId id, String address, String terminal) {
	this.setCall(id);
	this.setAddress(address);
	this.setTerminal(terminal);
}
/**
 * Update and return myself
 * Creation date: (2000-02-21 13:32:54)
 * @author: Richard Deadman
 * @return The JTAPI state exception after object resolution.
 * @param gp The top-level JTAPI object manager
 */
public InvalidStateException morph(FreeTerminal ft) {
	return this.morph((GenericProvider)ft.getProvider());
}
/**
 * Update and return myself
 * Creation date: (2000-02-21 13:32:54)
 * @author: Richard Deadman
 * @return The JTAPI state exception after object resolution.
 * @param gp The top-level JTAPI object manager
 */
public InvalidStateException morph(GenericProvider gp) {
	Object o = null;
	String msg = this.getMessage();

	// depending on object type, retrieve the real object
	switch (this.getObjectType()) {
		case InvalidStateException.CALL_OBJECT: {
			o = gp.getCallMgr().getLazyCall(this.getCall());
			break;
		}
		case InvalidStateException.ADDRESS_OBJECT: {
			o = gp.getDomainMgr().getLazyAddress(this.getAddress());
			break;
		}
		case InvalidStateException.TERMINAL_OBJECT: {
			try {
				o = gp.getDomainMgr().getFaultedTerminal(this.getTerminal());
			} catch (InvalidArgumentException iae) {
				// note problem
				msg = msg + "(Could not resolve Terminal due to illegal Terminal name: " +
						this.getTerminal() + ")";
			}
			break;
		}
		case InvalidStateException.CONNECTION_OBJECT: {
			o = gp.getDomainMgr().getLazyAddress(this.getAddress())
					.getLazyConnection(gp.getCallMgr().getLazyCall(this.getCall()));
			break;
		}
		case InvalidStateException.TERMINAL_CONNECTION_OBJECT: {
			try {
				FreeTerminal t = (FreeTerminal)gp.getTerminal(this.getTerminal());
				o = gp.getDomainMgr().getLazyAddress(this.getAddress())
						.getLazyConnection(gp.getCallMgr().getLazyCall(this.getCall()))
						.getLazyTermConn(t);
			} catch (InvalidArgumentException iae) {
				// note problem
				msg = msg + "(Could not resolve TerminalConnection due to illegal Terminal name: " +
						this.getTerminal() + ")";
			}
			break;
		}
	}
	
	return new InvalidStateException(o, this.getObjectType(), this.getState(), msg);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-21 13:27:12)
 * @author: 
 * @param newAddress java.lang.String
 */
private void setAddress(java.lang.String newAddress) {
	address = newAddress;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-21 13:26:04)
 * @author: 
 * @param newCall net.sourceforge.gjtapi.CallId
 */
private void setCall(CallId newCall) {
	call = newCall;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-21 13:27:33)
 * @author: 
 * @param newTerminal java.lang.String
 */
private void setTerminal(java.lang.String newTerminal) {
	terminal = newTerminal;
}
}
