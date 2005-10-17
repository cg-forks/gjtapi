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
 * Insert the type's description here.
 * Creation date: (2000-03-08 11:34:33)
 * @author: 
 */
public class GenericSignalDetectorEvent extends GenericResourceEvent implements SignalDetectorEvent {
	static final long serialVersionUID = -5627163403891841155L;
	
	private int patternIndex = -1;
	private SymbolHolder[] sigs = null;
/**
 * Create a signal detector event, when the MediaService is not known, but the terminal is.
 * Creation date: (2000-03-08 13:58:13)
 * Author: Richard Deadman
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param termName The name of the terminal the event's MediaService is associated with.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @param index The index into a pattern array if a pattern caused the event to be triggered.
 * @param buf Array of Symbols representing the received signals.
 * @author: Richard Deadman
 */
public GenericSignalDetectorEvent(Symbol evId, String termName, Symbol err, Symbol qual, Symbol trigger,
	int index, Symbol[] buf) {
	super(evId, termName, err, qual, trigger);

	this.setPatternIndex(index);
	this.setSignalBuffer(buf);
}
/**
 * Create a signal detector event
 * Creation date: (2000-03-08 13:58:13)
 * Author: Richard Deadman
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param ms The MediaService the event corresponds to.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @param index The index into a pattern array if a pattern caused the event to be triggered.
 * @param buf Array of Symbols representing the received signals.
 * @author: Richard Deadman
 */
public GenericSignalDetectorEvent(Symbol evId, MediaService ms, Symbol err, Symbol qual, Symbol trigger,
	int index, Symbol[] buf) {
	super(evId, ms, err, qual, trigger);

	this.setPatternIndex(index);
	this.setSignalBuffer(buf);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-08 14:11:00)
 * @author: 
 * @return int
 */
public int getPatternIndex() {
	return patternIndex;
}
/**
 * Get the set of signals in the buffer
 */
public Symbol[] getSignalBuffer() {
	if (this.sigs != null)
		return SymbolHolder.decode(this.sigs);
	return null;
}
/**
 * Turn the set of signals into a string
 */
public String getSignalString() {
	Symbol[] sigs = this.getSignalBuffer();
	if (sigs != null) {
		return SymbolConvertor.convert(sigs, "?");
	}
	return null;
}
/**
 * Internal setter for the pattern index that indexes the symbol pattern that caused this event.
 * Creation date: (2000-03-08 14:11:00)
 * @author: Richard Deadman
 * @param newPatternIndex Index into pattern array that indicates reason for event.
 */
private void setPatternIndex(int newPatternIndex) {
	patternIndex = newPatternIndex;
}
/**
 * Set the signal set for the event
 */
private void setSignalBuffer(Symbol[] syms) {
	if (syms != null)
		this.sigs = SymbolHolder.create(syms);
}
}
