/*
 * Created on Dec 16, 2003
 *
 * Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca) 

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
package net.sourceforge.gjtapi.media;

import javax.telephony.media.MediaService;
import javax.telephony.media.MediaServiceEvent;
import javax.telephony.media.Symbol;

/**
 * @author Richard Deadman
 *
 * This is a MediaEvent that ties the event to the MediaService it
 * is associated with.
 * <P>Note that in JTAPI 1.3.1, the getMediaService() method was part of
 * the parent interface.
 * 
 * @since JTAPI 1.4
 */
public class GenericMediaServiceEvent
	extends GenericMediaEvent
	implements MediaServiceEvent {

	/**
	 * @param eventId
	 * @param termName
	 */
	public GenericMediaServiceEvent(Symbol eventId, String termName) {
		super(eventId, termName);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param eventId
	 * @param service
	 */
	public GenericMediaServiceEvent(Symbol eventId, MediaService service) {
		super(eventId, service);
		// TODO Auto-generated constructor stub
	}

}
