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
 * Hold Media Recorder event information
 * Creation date: (2000-03-08 11:33:51)
 * @author: Richard Deadman
 */
public class GenericRecorderEvent extends GenericResourceEvent implements RecorderEvent {
	static final long serialVersionUID = 2506605644792960275L;
	
	private int duration = 0;
/**
 * Create a  recorder event, when the MediaService is not known, but the terminal is.
 * Author: Richard Deadman
 *
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param termName The name of the terminal the event's MediaService is associated with.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @param duration The number of milliseconds in a completed recording.
 * @author: Richard Deadman
 */
public GenericRecorderEvent(Symbol evId, String termName, Symbol err, Symbol qual, Symbol trigger,
	int duration) {
	super(evId, termName, err, qual, trigger);

	this.setDuration(duration);
}
/**
 * Create a  recorder event, fully instantiated.
 * Author: Richard Deadman
 *
 * @param evId The Symbol that represents the event according to the ECTF.
 * @param ms The MediaService the event corresponds to.
 * @param err The Symbol that represents the error, or null.
 * @param qual Additional information about why an event occured, such as q_Duration.
 * @param trigger The RTC trigger that caused the event.
 * @param duration The number of milliseconds in a completed recording.
 * @author: Richard Deadman
 */
public GenericRecorderEvent(Symbol evId, MediaService ms, Symbol err, Symbol qual, Symbol trigger,
	int duration) {
	super(evId, ms, err, qual, trigger);

	this.setDuration(duration);
}
/**
 * Get the number of milliseconds in a completed recording or -1 for pause and resume events.
 * Creation date: (2000-03-08 14:05:50)
 * @author: Richard Deadman
 * @return milliseconds in completed recording.
 */
public int getDuration() {
	return duration;
}
/**
 * Internall setter.
 * Creation date: (2000-03-08 14:05:50)
 * @author: Richard Deadman
 * @param newDuration The number of milliseconds in a completed recording.
 */
private void setDuration(int newDuration) {
	duration = newDuration;
}
}
