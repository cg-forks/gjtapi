package net.sourceforge.gjtapi.events;

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
import javax.telephony.events.*;
import net.sourceforge.gjtapi.*;
/**
 * This is a morphable event for a Terminal
 * Creation date: (2000-02-15 11:01:12)
 * @author: Richard Deadman
 */
public abstract class FreeTerminalEvent extends FreeEv implements TermEv, TerminalEvent {
	private FreeTerminal terminal = null;
/**
 * FreeTerminalEvent constructor hooked to JTAPI objects.
 * @param cause int
 * @param metaCode int
 * @param isNewMetaEvent boolean
 * @param t The affected terminal object
 */
public FreeTerminalEvent(int cause, int metaCode, boolean isNewMetaEvent, FreeTerminal t) {
	super(cause, metaCode, isNewMetaEvent);

	this.setTerminal(t);
}
/**
 * FreeTerminalEvent constructor hooked to JTAPI objects.
 * @param cause int
 * @param metaCode int
 * @param isNewMetaEvent boolean
 * @param t The affected terminal object
 */
public FreeTerminalEvent(int cause, FreeTerminal t) {
	this(cause, 0, false, t);
}
/**
 * Return the source of the event.
 */
public Object getSource() {
	return this.getTerminal();
}
/**
 * Terminal accessor
 * Creation date: (2000-02-15 11:48:09)
 * @author: Richard Deadman
 * @return javax.telephony.Terminal
 */
public Terminal getTerminal() {
	return terminal;
}
/**
 * Internal setter
 * Creation date: (2000-02-15 11:48:09)
 * @author: Richard Deadman
 * @param newTerminal The Terminal associated with this event
 */
private void setTerminal(FreeTerminal newTerminal) {
	terminal = newTerminal;
}
}
