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
 * This is the abstract class for one of the Media Resource Events
 * Creation date: (2000-03-08 11:31:18)
 * @author: Richard Deadman
 */
public abstract class GenericResourceEvent extends GenericMediaEvent implements ResourceEvent {
	/**
	 * Serializable ID since we are serializable
	 */
	private static final long serialVersionUID = -8204246953834586181L;
	
	private SymbolHolder err = null;
	private SymbolHolder qualifier = null;
	private SymbolHolder trigger = null;
/**
 * Base constructor, for when a MediaService is not known, but its terminal is.
 * Creation date: (2000-03-08 12:35:52)
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param termName The name of the terminal the event's MediaService is associated with.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @author: Richard Deadman
 */
public GenericResourceEvent(Symbol evId, String termName, Symbol err, Symbol qual, Symbol trigger) {
	super(evId, termName);

	this.setError(err);
	this.setQualifier(qual);
	this.setRTCTrigger(trigger);
}
/**
 * Base constructor
 * Creation date: (2000-03-08 12:35:52)
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param ms The MediaService the event corresponds to.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @author: Richard Deadman
 */
public GenericResourceEvent(Symbol evId, MediaService ms, Symbol err, Symbol qual, Symbol trigger) {
	super(evId, ms);

	this.setError(err);
	this.setQualifier(qual);
	this.setRTCTrigger(trigger);
}
/**
 * getError method comment.
 */
public Symbol getError() {
	if (err != null)
		return err.getSymbol();
	return ResourceConstants.e_OK;
}
/**
 * getQualifier method comment.
 */
public Symbol getQualifier() {
	if (qualifier != null)
		return qualifier.getSymbol();
	return null;
}
/**
 * getRTCTrigger method comment.
 */
public Symbol getRTCTrigger() {
	if (trigger != null)
		return trigger.getSymbol();
	return null;
}
/**
 * Set the Error Symbol (null indicated e_OK)
 */
private void setError(Symbol errSym) {
	if (errSym != null)
		this.err = new SymbolHolder(errSym);
}
/**
 * Set the Qualifier Symbol
 */
private void setQualifier(Symbol q) {
	if (q != null)
		this.qualifier = new SymbolHolder(q);
}
/**
 * Set the RTC Trigger Symbol
 */
private void setRTCTrigger(Symbol trig) {
	if (trig != null)
		this.trigger = new SymbolHolder(trig);
}
}
