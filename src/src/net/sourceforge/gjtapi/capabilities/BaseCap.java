package net.sourceforge.gjtapi.capabilities;

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
import javax.telephony.privatedata.capabilities.PrivateDataCapabilities;
/**
 * A base class for the capability holders
 * Creation date: (2000-03-14 15:47:44)
 * @author: Richard Dedadman
 */
abstract class BaseCap implements PrivateDataCapabilities {
	// note if I hold static or Address-specific capabilies
	private boolean dynamic = false;

	// PrivateData
	// note if my object or class of objects can get PrivateData
	private boolean getPD = false;
	// note if my object or class of objects can send PrivateData
	private boolean sendPD = false;
	// note if my object or class of objects can set PrivateData
	private boolean setPD = false;
/**
 * Can Private Data be retrieved from the Object?
 * Creation date: (2000-08-05 22:50:35)
 * @return boolean
 * @author: Richard Deadman
 */
public boolean canGetPrivateData() {
	return getPD;
}
/**
 * Can Private Data be sent to the Object?
 * Creation date: (2000-08-05 22:50:35)
 * @return boolean
 * @author: Richard Deadman
 */
public boolean canSendPrivateData() {
	return sendPD;
}
/**
 * Can Private Data be set on the Object?
 * Creation date: (2000-08-05 22:50:35)
 * @return boolean
 */
public boolean canSetPrivateData() {
	return setPD;
}
/**
 * Internal accessor.
 * Creation date: (2000-03-14 13:22:25)
 * @author: Richard Deadman
 * @return true if these capabilities are for a dynamic address.
 */
protected boolean isDynamic() {
	return dynamic;
}
/**
 * Set the dynamic state of the capabilities.  Static capabilities applie to the system, whereas
 * dynamic capabilities refer to the capabilities based on the current Address's state.
 * Creation date: (2000-03-14 13:22:25)
 * @author: Richard Deadman
 * @param newDynamic boolean
 */
protected void setDynamic(boolean newDynamic) {
	dynamic = newDynamic;
}
/**
 * Can the object I hold capabilities for return PrivateData?
 * Creation date: (2000-08-05 23:22:15)
 * @author: Richard Deadman
 * @param newGetPD true if the object can return PrivateData
 */
void setGetPDCapability(boolean newGetPD) {
	getPD = newGetPD;
}
/**
 * Can the object I hold capabilities act on PrivateData?
 * Creation date: (2000-08-05 23:22:15)
 * @author: Richard Deadman
 * @param newGetPD true if the object can act on PrivateData
 */
void setSendPDCapability(boolean newSendPD) {
	sendPD = newSendPD;
}
/**
 * Can the object I hold capabilities receive PrivateData?
 * Creation date: (2000-08-05 23:22:15)
 * @author: Richard Deadman
 * @param newGetPD true if the object can receive PrivateData
 */
void setSetPDCapability(boolean newSetPD) {
	setPD = newSetPD;
}
}
