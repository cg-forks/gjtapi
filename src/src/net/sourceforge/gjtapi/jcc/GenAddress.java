package net.sourceforge.gjtapi.jcc;

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
import javax.csapi.cc.jcc.*;
/**
 * A Jain Jcc address adapter for a Generic JTAPI Address object.
 * Creation date: (2000-10-10 14:17:06)
 * @author: Richard Deadman
 */
public class GenAddress implements JccAddress {
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
}
