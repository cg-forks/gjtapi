package net.sourceforge.gjtapi.raw.mux;

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
import net.sourceforge.gjtapi.*;
/**
 * This is a simple holder for a low-level CallId that describes which TPI the CallId belongs
 * to.  The MuxCallId holds onto a set of these holders.
 * Creation date: (2000-09-22 23:37:29)
 * @author: Richard Deadman
 */
class CallHolder {
	private CallId call;
	private TelephonyProvider tpi;
/**
 * Create a CallHolder for a call from a certain low-level TPI.
 */
CallHolder(CallId lowLevelId, TelephonyProvider lowTpi) {
	super();

	this.setCall(lowLevelId);
	this.setTpi(lowTpi);
}
/**
 * Are the two objects equivalent?
 * Creation date: (2000-09-23 0:47:57)
 * @return boolean
 * @param value java.lang.Object
 */
public boolean equals(Object value) {
	if (value instanceof CallHolder) {
		CallHolder ch = (CallHolder)value;
		if (ch.getCall().equals(this.getCall()) && ch.getTpi().equals(this.getTpi())) {
			return true;
		}
	}
	return false;
}
/**
 * Package accessor for the Call I hold.
 * Creation date: (2000-09-23 0:38:49)
 * @return The low-level CallId.
 */
CallId getCall() {
	return call;
}
/**
 * Package accessor for the low-level TPI I hold Call information for.
 * Creation date: (2000-09-23 0:38:49)
 * @return The low-level TelephonyProvider
 */
TelephonyProvider getTpi() {
	return tpi;
}
/**
 * Return my identification hashcode
 * Creation date: (2000-09-23 0:50:34)
 * @return an object-invariant number
 */
public int hashCode() {
	return this.getCall().hashCode() + this.getTpi().hashCode();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-09-23 0:38:49)
 * @param newCall net.sourceforge.gjtapi.CallId
 */
private void setCall(net.sourceforge.gjtapi.CallId newCall) {
	call = newCall;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-09-23 0:38:49)
 * @param newTpi net.sourceforge.gjtapi.TelephonyProvider
 */
private void setTpi(net.sourceforge.gjtapi.TelephonyProvider newTpi) {
	tpi = newTpi;
}
/**
 * Describe myself.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Holder for CallId: " + this.getCall().toString() + " at low-level TPI: " + this.getTpi();
}
}
