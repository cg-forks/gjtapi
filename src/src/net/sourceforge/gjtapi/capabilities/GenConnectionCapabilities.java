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
 * Creation date: (2000-03-15 9:22:45)
 * @author: 
 */
public class GenConnectionCapabilities extends BaseCap implements CallControlConnectionCapabilities, ConnectionCapabilities {
/**
 * canAccept method comment.
 */
public boolean canAccept() {
	return false;
}
/**
 * canAddToAddress method comment.
 */
public boolean canAddToAddress() {
	return false;
}
/**
 * canDisconnect method comment.
 */
public boolean canDisconnect() {
	return false;
}
/**
 * canPark method comment.
 */
public boolean canPark() {
	return false;
}
/**
 * canRedirect method comment.
 */
public boolean canRedirect() {
	return false;
}
/**
 * canReject method comment.
 */
public boolean canReject() {
	return false;
}
/**
 * Return the dynamic capabilities for a Connection.
 * Creation date: (2000-03-15 11:39:53)
 * @author: Richard Deadman
 * @return javax.telephony.capabilities.ConnectionCapabilities
 * @param c The connection to use to alter the capabilities.
 */
public GenConnectionCapabilities getDynamic(javax.telephony.Connection c) {
	return this;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Connection and CallControlConnection " + (this.isDynamic() ? "dynamic" : "static") + " Capabilities";
}
}
