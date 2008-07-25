package net.sourceforge.gjtapi.media;

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
import javax.telephony.media.*;
/**
 * This is a holder class that allows RTCs to be serialized and re-constituted.
 * Note that it depends on the Signal values being accessible through hashCode().
 * Creation date: (2000-03-08 10:46:23)
 * @author: Richard Deadman
 */
public class RTCHolder implements java.io.Serializable {
	static final long serialVersionUID = 3598158952554758181L;
	
	private int trigger = 0;
	private int action = 0;

	private transient RTC value = null;
/**
 * Create a holder for an RTC.
 * Relies on hashCode() returning the value!
 * Creation date: (2000-03-08 10:50:44)
 * @author: Richard Deadman
 * @param rtc The RTC to hold the values for
 */
public RTCHolder(RTC rtc) {
	super();
	
	this.value = rtc;
	
	this.trigger = rtc.getTrigger().hashCode();
	this.action = rtc.getAction().hashCode();
}
/**
 * Lazy accessor for an RTC.  Recreates it if the transient handle was lost.
 * Creation date: (2000-03-08 10:56:30)
 * @author: Richard Deadman
 */
public RTC getRTC() {
	if (this.value == null) {
		Symbol trigClone = Symbol.getSymbol(this.trigger);
		Symbol actClone = Symbol.getSymbol(this.action);
		this.value = new RTC(trigClone, actClone);
	}

	return this.value;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Holder for: " + this.getRTC().toString();
}
}
