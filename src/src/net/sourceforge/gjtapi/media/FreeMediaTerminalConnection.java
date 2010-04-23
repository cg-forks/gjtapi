package net.sourceforge.gjtapi.media;

import javax.telephony.*;
import javax.telephony.media.*;
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
import net.sourceforge.gjtapi.*;
/**
 * The JTAPI 1.2 Media TerminalConnection.
 * <P><B>Not yet fully implemented.</B>
 * This should only be created by FreeConnection.getLazyTerminalConnection()
 * Creation date: (2000-04-26 14:28:12)
 * @author: Richard Deadman
 */
@SuppressWarnings("deprecation")
public class FreeMediaTerminalConnection extends FreeTerminalConnection implements MediaTerminalConnection {
	public final static String MICROPHONE = "<Microphone>";	// The microphone play streamId.
	public final static String SPEAKER = "<Speaker>";		// The speaker record streamId.

	// note the play thread

	private String playerUrl = null;
	private String recorderUrl = null;

	private TCMediaService mediaService = null;

		// define availability media event
	class AvailEv extends BaseMediaEv implements javax.telephony.media.events.MediaTermConnAvailableEv {
		AvailEv(FreeMediaTerminalConnection tc) {
			super(tc);
		}

		public int getID() {
			return javax.telephony.media.events.MediaTermConnAvailableEv.ID;
		}
	}

		// define unavailability media event
	class UnavailEv extends BaseMediaEv implements javax.telephony.media.events.MediaTermConnUnavailableEv {
		UnavailEv(FreeMediaTerminalConnection tc) {
			super(tc);
		}

		public int getID() {
			return javax.telephony.media.events.MediaTermConnUnavailableEv.ID;
		}
	}


/**
 * Creates an instance of the deprecated JTAPI 1.2 MediaTerminalConnection between a Connection and
 * a MediaTerminal.
 * @param con The call connection to hook the TerminalConnection up to.
 * @param term The name of a media terminal to hook the TerminalConnection to.
 */
public FreeMediaTerminalConnection(javax.telephony.Connection con, String termName) {
	super(con, termName);
}
/**
 * Creates an instance of the deprecated JTAPI 1.2 MediaTerminalConnection between a Connection and
 * a MediaTerminal.
 * @param con The call connection to hook the TerminalConnection up to.
 * @param term The media terminal to hook the TerminalConnection to.
 */
public FreeMediaTerminalConnection(javax.telephony.Connection con, javax.telephony.media.MediaTerminal term) {
	super(con, term);
}
/**
 * Release the Pseudo MediaService that represents my binding to the terminal.
 * This is called by the MediaService when it gabage collects itself.
 * Creation date: (2000-05-11 16:17:36)
 * @author: Richard Deadman
 */
synchronized void freeMediaService() {
	this.mediaService = null;
}
/**
 * Forward a String of digits off to a DTMF signal generator.
 * @param digits The DTMF digits to play on the TerminalConnection
 * @exception InvalidStateException If the TerminalConnection is not ACTIVE
 * @exception ResourceUnavailableException If the raw provider threw an exception.
 */
public void generateDtmf(String digits) throws InvalidStateException, ResourceUnavailableException {
	// test if we are in an active state
	if (this.getMediaAvailability() != AVAILABLE)
		throw new InvalidStateException(this, InvalidStateException.TERMINAL_CONNECTION_OBJECT, this.getState());
	
	// delegate off
	this.getMediaService().generateDtmf(digits);
}
  /**
   * Returns the current media availability state, either AVAILABLE or
   * UNAVAILABLE.  Any TerminalConnection in state ACTIVE is considered AVAILABLE.
   * <p>
   * @return The current availability of the media channel.
   */
public int getMediaAvailability() {
	if (this.getState() == TerminalConnection.ACTIVE)
		return AVAILABLE;
	else
		return UNAVAILABLE;
}
/**
 * Get the Pseudo MediaService that represents my binding to the terminal, or create one if none
 * exists.
 * <P>This is synchronized to ensure that two overlapping requests do not attempt to bind the service
 * twice.
 * Creation date: (2000-05-11 16:17:36)
 * @author: Richard Deadman
 * @return A pseudo MediaService that handles binding and listener registration.
 */
private synchronized TCMediaService getMediaService() {
	if (this.mediaService == null)
		this.mediaService = new TCMediaService(this);
	return this.mediaService;
}
/**
 * Returns a bitwise OR of the PLAYING and RECODING bits.  Otherwise NOACTIVITY.
 * <p>
 * @return The current state of playing or recording.
 */
public int getMediaState() {
	if (this.isBound())
		return this.getMediaService().getMediaState();
	else
		return NOACTIVITY;
}
/**
 * Get the URL that defines the terminal connections play URL.
 * Creation date: (2000-05-08 16:23:05)
 * @author: Richard Deadman
 * @return A String representation of a URL of a resource to play on the terminal.
 */
private String getPlayerUrl() {
	return this.playerUrl;
}
/**
 * Get the URL that defines the terminal connections record URL.
 * Creation date: (2000-05-08 16:23:05)
 * @author: Richard Deadman
 * @return A String representation of a URL of a resource to record to from the terminal.
 */
private String getRecorderUrl() {
	return this.recorderUrl;
}
/**
 * Indicates if the TerminalConnection is bound to a pseudo MediaService
 * Creation date: (2000-05-15 11:10:06)
 * @author: Richard Deadman
 */
private boolean isBound() {
	return (this.mediaService != null);
}
  /*
   * Sets the DTMF tone detection either on or off. If the boolean flag
   * argument is true, then DTMF detection is turned on, otherwise, it is
   * turned off.
   * <p>
   * @param enable If true, turns DTMF-tone detection on, if false, turns
   * DTMF-tone detection off.
   * @exception MethodNotSupportedException The implementation does not
   * support the detection of DTMF-tones.
   * @exception ResourceUnavailableException Indicates DTMF-detection cannot
   * be started because some resource is unavailable.
   * @exception InvalidStateException Indicates the TerminalConnection is not
   * in the media channel available state.
   */
public void setDtmfDetection(boolean enable) throws InvalidStateException, ResourceUnavailableException {
	// test if we are in an active state
	if (this.getState() != TerminalConnection.ACTIVE)
		throw new InvalidStateException(this, InvalidStateException.TERMINAL_CONNECTION_OBJECT, this.getState());
	
	// Delegate on if we're turning detection on or already have a media service started
	if (enable || this.isBound())
		this.getMediaService().setDtmfDetection(enable);
}
/**
 * Start playing a URL set in the "usePlayURL()" method.
 */
public synchronized void startPlaying() throws InvalidStateException, ResourceUnavailableException {
	// test if we are in an active state
	if (this.getMediaAvailability() != AVAILABLE)
		throw new InvalidStateException(this, InvalidStateException.TERMINAL_CONNECTION_OBJECT, this.getState());

	String url = this.getPlayerUrl();
	if (url == null)
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "No Player URL set");
		
	// delegate off
	this.getMediaService().startPlaying(url);
}
/**
 * Start playing a URL set in the "usePlayURL()" method.
 */
public synchronized void startRecording() throws InvalidStateException, ResourceUnavailableException {
	// test if we are in an active state
	if (this.getMediaAvailability() != AVAILABLE)
		throw new InvalidStateException(this, InvalidStateException.TERMINAL_CONNECTION_OBJECT, this.getState());

	String url = this.getRecorderUrl();
	if (url == null)
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "No Recorder URL set");
		
	// delegate off
	this.getMediaService().startRecording(url);
}
/**
 * Stop playing a URL if I am currently bound to a media service
 */
public void stopPlaying() {
	if (this.isBound())
		this.getMediaService().stopPlaying();
}
/**
 * Stop recording if I am bound to a media service.
 */
public void stopRecording() {
	if (this.isBound())
		this.getMediaService().stopRecording();
}
/**
 * This TerminalConnection has been disconnected and must tell any listeners that media is no
 * longer available.
 * Creation date: (2000-05-15 10:57:25)
 * @author: Richard Deadman
 * @param cause The Event reason for the terminal connection "drop".
 * @return The removed terminal from the Call.
 */
protected Terminal toDropped(int cause) {
	Terminal t = super.toDropped(cause);

	// notify any media listeners
	((FreeCall)this.getConnection().getCall()).sendToObservers(new UnavailEv(this));

	return t;
}
/**
 * This TerminalConnection has been held and must tell any listeners that media is no
 * longer available.
 * Creation date: (2000-05-15 10:57:25)
 * @author: Richard Deadman
 * @param cause The Event reason for the terminal connection "hold".
 */
protected void toHeld(int cause) {
	super.toHeld(cause);

	// notify any media listeners
	((FreeCall)this.getConnection().getCall()).sendToObservers(new UnavailEv(this));
}
/**
 * Process a talking state request
 * Creation date: (2000-05-05 14:15:44)
 * @author: Richard Deadman
 * @param cause The cause of the creation
 */
protected void toTalking(int cause) {
	super.toTalking(cause);

	// notify any media listeners
	((FreeCall)this.getConnection().getCall()).sendToObservers(new AvailEv(this));
}
/**
 * Try to set the microphone as the player source.
 * We code this as "<Microphone>".
 */
public void useDefaultMicrophone() {
	this.recorderUrl = FreeMediaTerminalConnection.MICROPHONE;
}
/**
 * Try to set the Speaker as the output to the record command.
 * We code this as "<Speaker>".
 */
public void useDefaultSpeaker() {
	this.playerUrl = FreeMediaTerminalConnection.SPEAKER;
}
/**
 * Set the URL that defines which resource to play.
 */
public void usePlayURL(java.net.URL url) {
	this.playerUrl = url.toString();
}
/**
 * Set the URL that defines where to record to.
 */
public void useRecordURL(java.net.URL url) {
	this.recorderUrl = url.toString();
}
}
