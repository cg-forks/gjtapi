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
 * Holds an event from the media signal generator.
 * Currently SignalGeneratorEvent defines no additional behaviour, but is a required subtype.
 * Creation date: (2000-03-08 11:35:27)
 * @author: Richard Deadman
 */
public class GenericSignalGeneratorEvent extends GenericResourceEvent implements SignalGeneratorEvent {
	static final long serialVersionUID = -1670520278372601398L;
	
/**
 * Create a signal generator event, when the MediaService is not known, but the terminal is.
 * Creation date: (2000-03-08 13:58:13)
 * @author: Richard Deadman
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param termName The name of the terminal the event's MediaService is associated with.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @author: Richard Deadman
 */
public GenericSignalGeneratorEvent(Symbol evId, String termName, Symbol err, Symbol qual, Symbol trigger) {
	super(evId, termName, err, qual, trigger);
}
/**
 * Create a signal generator event
 * Creation date: (2000-03-08 13:58:13)
 * @author: Richard Deadman
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param ms The MediaService the event corresponds to.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @author: Richard Deadman
 */
public GenericSignalGeneratorEvent(Symbol evId, MediaService ms, Symbol err, Symbol qual, Symbol trigger) {
	super(evId, ms, err, qual, trigger);
}
}
