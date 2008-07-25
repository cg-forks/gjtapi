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
import net.sourceforge.gjtapi.*;
/**
 * This is the base media event class.
 *<P>Currently it is serializable and provides for terminal-name construction so that it
 * is possible for media service providers (RawProvider implementors) to create instances of these
 * events as error events encapsulated within MediaResourceExceptions.
 * Creation date: (2000-05-03 11:31:18)
 * @author: Richard Deadman
 */
public class GenericMediaEvent implements MediaEvent, java.io.Serializable {
	static final long serialVersionUID = -8635515624336827001L;
	
	private SymbolHolder eventID = null;
	private transient MediaService ms = null;

	// needed if created with remote id
	private String terminal = null;
	private boolean morphed = true;
/**
 * Create a basic MediaEvent for a terminal, when the media service is not known.
 * Creation date: (2000-05-03 15:52:14)
 * @author: Richard Deadman
 * @param eventId The identifier of the event.
 * @param termName The name of the terminal the event's MediaService is associated with.
 */
public GenericMediaEvent(Symbol eventId, String termName) {
	this.setEventID(eventId);
	this.setTerminal(termName);
	this.morphed = false;
}
/**
 * Create a basic MediaEvent
 * Creation date: (2000-05-03 15:52:14)
 * @author: Richard Deadman
 * @param eventId The identifier of the event.
 * @param service The service the event comes from.
 */
public GenericMediaEvent(Symbol eventId, MediaService service) {
	this.setEventID(eventId);
	this.setMediaService(service);
}
/**
 * getEventID method comment.
 */
public Symbol getEventID() {
	return this.eventID.getSymbol();
}
/**
 * getMediaService method comment.
 */
public MediaService getMediaService() {
	return this.ms;
}
/**
 * Return the terminal that defines which media service I was generated for.
 * Creation date: (2000-05-07 23:13:49)
 * @author: Richard Deadman
 * @return The name of a terminal a media service is attached to.
 */
private java.lang.String getTerminal() {
	return terminal;
}
/**
 * Have I been resolved to a MediaService.
 * Creation date: (2000-05-07 23:12:19)
 * @author: Richard Deadman
 * @return boolean
 */
private boolean isMorphed() {
	return this.morphed;
}
/**
 * Change my reference to one that reflects the current media service.
 * Creation date: (2000-05-07 23:16:15)
 * @author: Richard Dead,am
 * @param prov A visitor that is used to find the media service.
 */
public void morph(GenericProvider prov) {
	if (!this.isMorphed()) {
		this.setMediaService(prov.getMediaMgr().findForTerminal(this.getTerminal()).getMediaService());
		this.morphed = true;
	}
}
/**
 * Set the Event Id Symbol
 */
private void setEventID(Symbol id) {
	this.eventID = new SymbolHolder(id);
}
/**
 * getMediaService method comment.
 */
private void setMediaService(MediaService service) {
	this.ms = service;
}
/**
 * Set the terminal that defines which MediaService I belong to.
 * Creation date: (2000-05-07 23:13:49)
 * @author: Richard Deadman
 * @param newTerminal java.lang.String
 */
private void setTerminal(java.lang.String newTerminal) {
	terminal = newTerminal;
}
}
