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
import javax.telephony.callcontrol.capabilities.*;
import javax.telephony.capabilities.*;
/**
 * Insert the type's description here.
 * Creation date: (2000-03-15 9:23:56)
 * @author: 
 */
public class GenTerminalCapabilities extends BaseCap implements CallControlTerminalCapabilities, TerminalCapabilities {
/**
 * canGetDoNotDisturb method comment.
 */
public boolean canGetDoNotDisturb() {
	return false;
}
/**
 * canPickup method comment.
 */
public boolean canPickup() {
	return false;
}
/**
 * canPickup method comment.
 */
public boolean canPickup(javax.telephony.Address address1, javax.telephony.Address address2) {
	return false;
}
/**
 * canPickup method comment.
 */
public boolean canPickup(javax.telephony.Connection connection, javax.telephony.Address address) {
	return false;
}
/**
 * canPickup method comment.
 */
public boolean canPickup(javax.telephony.TerminalConnection tc, javax.telephony.Address address) {
	return false;
}
/**
 * canPickupFromGroup method comment.
 */
public boolean canPickupFromGroup() {
	return false;
}
/**
 * canPickupFromGroup method comment.
 */
public boolean canPickupFromGroup(String group, javax.telephony.Address address) {
	return false;
}
/**
 * canPickupFromGroup method comment.
 */
public boolean canPickupFromGroup(javax.telephony.Address address) {
	return false;
}
/**
 * canSetDoNotDisturb method comment.
 */
public boolean canSetDoNotDisturb() {
	return false;
}
/**
 * Return the dynamic capabilities for a terminal.
 * Creation date: (2000-03-15 11:41:30)
 * @author: Richard Deadman
 * @return javax.telephony.capabilities.TerminalCapabilities
 * @param t The terminal to base the capabilities on.
 */
public GenTerminalCapabilities getDynamic(javax.telephony.Terminal t) {
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
	return "Terminal and CallControlTerminal " + (this.isDynamic() ? "dynamic" : "static") + " Capabilities";
}
}
