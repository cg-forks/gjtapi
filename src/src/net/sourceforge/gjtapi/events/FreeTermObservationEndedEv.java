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
import javax.telephony.events.TermObservationEndedEv;
/**
 * This is a Observer-style subclass of FreeTerminalEvent that notes when a terminal's observation is ended.
 * Creation date: (2000-02-15 14:44:33)
 * @author: Richard Deadman
 */
public class FreeTermObservationEndedEv extends FreeTerminalEvent implements javax.telephony.events.TermObservationEndedEv {
/**
 * This should only be created by calling getObserverEvent() on a FreeTerminalEvent
 * @param cause int
 * @param metaCode int
 * @param isNewMetaEvent boolean
 * @param t net.sourceforge.gjtapi.FreeTerminal
 */
public FreeTermObservationEndedEv(int cause, int metaCode, boolean isNewMetaEvent, net.sourceforge.gjtapi.FreeTerminal t) {
	super(cause, metaCode, isNewMetaEvent, t);
}
/**
 * Return the observer-style event id.
 */
public int getID() {
	return TermObservationEndedEv.ID;
}
}
