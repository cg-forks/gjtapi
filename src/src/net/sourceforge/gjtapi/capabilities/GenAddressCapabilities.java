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
import javax.telephony.*;
import javax.telephony.callcontrol.capabilities.*;
/**
 * This is a capabilities holder that manages both core and call control address capabilities.
 * This may be extended to other address capabilities packages in the future.
 * Creation date: (2000-03-14 13:16:05)
 * @author: Richard Deadman
 */
public class GenAddressCapabilities extends BaseCap implements CallControlAddressCapabilities {
/**
 * canCancelForwarding method comment.
 */
public boolean canCancelForwarding() {
	return false;
}
/**
 * canGetDoNotDisturb method comment.
 */
public boolean canGetDoNotDisturb() {
	return false;
}
/**
 * canGetForwarding method comment.
 */
public boolean canGetForwarding() {
	return false;
}
/**
 * canGetMessageWaiting method comment.
 */
public boolean canGetMessageWaiting() {
	return false;
}
/**
 * canSetDoNotDisturb method comment.
 */
public boolean canSetDoNotDisturb() {
	return false;
}
/**
 * canSetForwarding method comment.
 */
public boolean canSetForwarding() {
	return false;
}
/**
 * canSetMessageWaiting method comment.
 */
public boolean canSetMessageWaiting() {
	return false;
}
/**
 * Create a dynamic AddresssCapabilities object for a particular Address.
 * Creation date: (2000-03-15 11:37:06)
 * @author: Richard Deadman
 * @return javax.telephony.capabilities.AddressCapabilities
 * @param a The address to set the capabilities against.
 */
public GenAddressCapabilities getDynamic(Address a) {
	return this;
}
/**
 * isObservable method comment.
 */
public boolean isObservable() {
	return true;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Address and CallControlAddress " + (this.isDynamic() ? "dynamic" : "static") + " Capabilities";
}
}
