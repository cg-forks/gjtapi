package net.sourceforge.gjtapi.events;

/*
	Copyright (c) 1999,2002 Westhawk Ltd (www.westhawk.co.uk) 
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

/**
 * This is a base AddressEvent class.  It is created by service providers and passed to the
 * RawObserver interface.  It has the ability to hook itself to the JTAPI objects, change their state
 * and create Observer-style subclass instances.
 */

import javax.telephony.*;
import javax.telephony.events.AddrEv;
import net.sourceforge.gjtapi.FreeAddress;

public abstract class FreeAddressEvent extends FreeEv implements AddrEv, AddressEvent {
	private FreeAddress address;
public FreeAddressEvent(int id, int cause, FreeAddress a) {
	this(cause, 0, false, a);
}
public FreeAddressEvent(int cause, int metaCode, boolean isNewMetaEvent, FreeAddress a) {
	super(cause, metaCode, isNewMetaEvent);
	address = a;
}
public Address getAddress() {
	return address;
}
/**
 * getSource method comment.
 */
public java.lang.Object getSource() {
	return this.getAddress();
}
}
