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
 * An event on a Media Player.
 * Creation date: (2000-03-08 11:32:37)
 * @author: Richard Deadman
 */
public class GenericPlayerEvent extends GenericResourceEvent implements PlayerEvent {
	static final long serialVersionUID = -6994059946219801405L;
	
	private SymbolHolder changeType = null;
	private int index = 0;
	private int offset = 0;
/**
 * Create a player event, when the MediaService is not known, but the terminal is.
 * Creation date: (2000-03-08 13:58:13)
 * @author: Richard Deadman
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param termName The name of the terminal the event's MediaService is associated with.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @param change The Symbol that identifies the type or speed or volume adjustment
 * @param index The stream being played when stopped.
 * @param offset The number of milliseconds into an audio stream when stopped.
 * @author: Richard Deadman
 */
public GenericPlayerEvent(Symbol evId, String termName, Symbol err, Symbol qual, Symbol trigger,
	Symbol change, int index, int offset) {
	super(evId, termName, err, qual, trigger);

	this.setChangeType(change);
	this.setIndex(index);
	this.setOffset(offset);
}
/**
 * Create a player event
 * Creation date: (2000-03-08 13:58:13)
 * @author: Richard Deadman
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param ms The MediaService the event corresponds to.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @param change The Symbol that identifies the type or speed or volume adjustment
 * @param index The stream being played when stopped.
 * @param offset The number of milliseconds into an audio stream when stopped.
 * @author: Richard Deadman
 */
public GenericPlayerEvent(Symbol evId, MediaService ms, Symbol err, Symbol qual, Symbol trigger,
	Symbol change, int index, int offset) {
	super(evId, ms, err, qual, trigger);

	this.setChangeType(change);
	this.setIndex(index);
	this.setOffset(offset);
}
/**
 * getChangeType method comment.
 */
public Symbol getChangeType() {
	if (this.changeType != null)
		return this.changeType.getSymbol();
	return null;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-08 14:05:05)
 * @author: 
 * @return int
 */
public int getIndex() {
	return index;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-08 14:05:25)
 * @author: 
 * @return int
 */
public int getOffset() {
	return offset;
}
/**
 * getChangeType method comment.
 */
private void setChangeType(Symbol changeSymbol) {
	if (changeSymbol != null)
		this.changeType = new SymbolHolder(changeSymbol);
}
/**
 * Set the stream id that was being played.
 * Creation date: (2000-03-08 14:05:05)
 * @author: Richard Deadman
 * @param newIndex int
 */
private void setIndex(int newIndex) {
	index = newIndex;
}
/**
 * Set the streamId offset when stopped.
 * Creation date: (2000-03-08 14:05:25)
 * @author: Richard Deadman
 * @param newOffset The number of milliseconds into the current streamId when stopped.
 */
private void setOffset(int newOffset) {
	offset = newOffset;
}
}
