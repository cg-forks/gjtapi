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
import net.sourceforge.gjtapi.capabilities.*;
import javax.telephony.media.capabilities.*;
/**
 * This defines extended media capabilities that a MediaTerminal may have as part of 1.2 media.
 * <P>Note: This is based on top of the 1.3 media support, which is contractual.  In other words, if
 * media is supported, it is supposed to all be supported.  We could have defined finer-grained capability
 * support, but this would have complicated the RawProvider creation for little added benefit.
 * Creation date: (2000-05-15 15:02:03)
 * @author: Richard Deadman
 */
@SuppressWarnings("deprecation")
public class GenMediaTermConnCapabilities extends GenTermConnCapabilities implements MediaTerminalConnectionCapabilities {
	private boolean avail = true;	// is media currently available, when this is used on an instance?
/**
 * canDetectDtmf method comment.
 */
public boolean canDetectDtmf() {
	return this.avail;
}
/**
 * canGenerateDtmf method comment.
 */
public boolean canGenerateDtmf() {
	return this.avail;
}
/**
 * canStartPlaying method comment.
 */
public boolean canStartPlaying() {
	return this.avail;
}
/**
 * canStartRecording method comment.
 */
public boolean canStartRecording() {
	return this.avail;
}
/**
 * canStopPlaying method comment.
 */
public boolean canStopPlaying() {
	return this.avail;
}
/**
 * canStopRecording method comment.
 */
public boolean canStopRecording() {
	return this.avail;
}
/**
 * canUseDefaultMicrophone method comment.
 */
public boolean canUseDefaultMicrophone() {
	return this.avail;
}
/**
 * canUseDefaultSpeaker method comment.
 */
public boolean canUseDefaultSpeaker() {
	return this.avail;
}
/**
 * canUsePlayURL method comment.
 */
public boolean canUsePlayURL() {
	return this.avail;
}
/**
 * canUseRecordURL method comment.
 */
public boolean canUseRecordURL() {
	return this.avail;
}
/**
 * Set the current media availability of the MediaTerminalConnection.
 * Creation date: (2000-05-15 15:18:06)
 * @author: Richard Deadman
 * @param availability boolean
 */
public void setAvailable(boolean availability) {
	this.avail = availability;
}
}
