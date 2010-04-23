package net.sourceforge.gjtapi.raw;

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
import java.util.Dictionary;
import javax.telephony.media.*;
import net.sourceforge.gjtapi.RawSigDetectEvent;
/**
 * These are the methods required by TelephonyProviders that support media for JTAPI
 * Creation date: (2000-10-04 13:46:43)
 * @author: Richard Deadman
 */
public interface MediaTpi extends BasicJtapiTpi {
	// Media Resource types - these can be ORed together
	public final static int MEDIA_RES_NONE = 0;
	public final static int MEDIA_RES_PLAYER = 1;
	public final static int MEDIA_RES_RECORDER = 2;
	public final static int MEDIA_RES_GENERATOR = 4;
	public final static int MEDIA_RES_DETECTOR = 8;
	public final static int MEDIA_RES_ALL = 15;
/**
  * Allocate a media resource for a terminal.
  * <P>This may be called subsequent times for the same resource on the same terminal in order
  * to replace the resourceArgs dictionary associated with the resource.
  * <P>One resourceArgs entry that must be handled is the mapping of p_EnableEvents to ev_SignalDetected.
  * This indicates that detected signals should be reported through an event.
  *
  * @param terminal The terminal to attach a media resource to.
  * @param type A constant defining the type of resource to add.
  * @param resourceArgs Optional values and parameters for configuring the resource.
  * @return true if the resource is allocated
  **/
@SuppressWarnings("unchecked")
boolean allocateMedia(String terminal, int type, Dictionary resourceArgs);
/**
  * Free a media resource from a terminal
  *
  * @param terminal The terminal to release a media resource from.
  * @param type A constant defining the type of resource to release.
  * @return true if the resource is freed.
  **/
boolean freeMedia(String terminal, int type);
/**
 * Ask the RawProvider if the named terminal is media-capable.
 * Creation date: (2000-03-07 15:35:02)
 * @author: Richard Deadman
 * @return true if the terminal can be used with media control commands.
 * @param terminal The raw-provider specific unique name for the terminal
 */
boolean isMediaTerminal(String terminal);
	 /**
	  * Playing a set of audio streams named by the streamIds (may be urls). This method is synchronous
	  * and returns once all the streams have been played or play has halted for some other reason.
	  *
	  * @param terminal The terminal to play the audio on.
	  * @param streamIds The ids for the audi streams to play, usually URLs
	  * @param int offset The number of milliseconds into the audio to start
	  * @param rtcs A set of runtime control sets that tune th playing.
	  * @param optArgs A dictionary of optional arguments and commands for controlling the play.
	  *
	  * @exception MediaResourceException A wrapper for a PlayerEvent that describes what went wrong.
	  **/
	 @SuppressWarnings("unchecked")
	void play(String terminal, String[] streamIds, int offset, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException; 
/**
  * Recording an audio streams named by the streamId (may be urls). This method is synchronous.
  * The method returns once recording has stopped.
  *
  * @param terminal The terminal to record the audio from.
  * @param streamId The id for the audio streams to create, usually a URL
  * @param rtcs A set of runtime control sets that tune the recording.
  * @param optArgs A dictionary of optional arguments and commands for controlling the play.
  *
  * @exception MediaResourceException A wrapper for a RecorderEvent that describes what went wrong.
  **/
@SuppressWarnings("unchecked")
void record(String terminal, String streamId, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException;
/**
  * Receive DTMF tones from a terminal
  *
  * @param terminal The terminal the signal receiver is attached to.
  * @param num The number of signals to retrieve
  * @param syms A set of symbols patterns to return
  * @param rtcs A set of runtime control sets that tune the signaling.
  * @param optArgs A dictionary of optional arguments and commands for controlling the play.
  * @return A GenericSignalDectectorEvent that can be sent getSignalBuffer() to retrieve the signals.
  *
  * @exception MediaResourceException A wrapper for a SignalDetectorEvent that describes what went wrong.
  **/
@SuppressWarnings("unchecked")
RawSigDetectEvent retrieveSignals(String terminal,
	int num,
	Symbol[] patterns,
	RTC[] rtcs,
	Dictionary optArgs) throws MediaResourceException;
/**
  * Play DTMF tones on a terminal
  *
  * @param terminal The terminal to record the audio from.
  * @param syms A set of symbols to play
  * @param rtcs A set of runtime control sets that tune the signalling.
  * @param optArgs A dictionary of optional arguments and commands for controlling the play.
  *
  * @exception MediaResourceException A wrapper for a SignalGeneratorEvent that describes what went wrong.
  **/
@SuppressWarnings("unchecked")
void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException;
/**
 * Stop any media resources attached to a terminal.
 * <P>The implmentation of this method should be synchronous in the the raw provider is expected to
 * have stopped all media resources before it returns, although this behaviour is not required by
 * the framework.
 * Creation date: (2000-03-09 16:08:12)
 * @author: Richard Deadman
 * @param terminal The terminal name.
 */
void stop(String terminal);
/**
 * Send Runtime control actions to media resources bound to a terminal.
 * <P>At the very least, implementors should handle the Symbols PlayerConstants.rtca_Stop
 * and RecorderConstants.rtca_Stop to stop any play or record calls.
 * Creation date: (2000-03-09 16:09:09)
 * @author: Richard Deadman
 * @param terminal The name of the terminal the media resources are bound to.
 * @param action The RTC action symbol to invoke on the media resources.
 */
void triggerRTC(String terminal, Symbol action);
}
