package net.sourceforge.gjtapi.raw.invert;

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
import java.util.Properties;
/**
 * This is a concrete Jtapi Inverter provider that delegates Generic JTAPI raw Telephony Provider
 * Media calls to MethodNotSupportedException.
 * Creation date: (2000-06-06 23:33:45)
 * @author: Richard Deadman
 */
public class NonMediaProvider extends InverterProvider {
/**
 * Delegate off to parent.
 */
public NonMediaProvider() {
	super();
}
/**
 * Should never be called.
 */
public boolean allocateMedia(String terminal, int type, java.util.Dictionary resourceArgs) {
	throw new javax.telephony.PlatformException("Media not supported");
}
/**
 * Should never be called since media is not supported.
 */
public boolean freeMedia(String terminal, int type) {
	throw new javax.telephony.PlatformException("Media not supported");
}
/**
 * Add to the abstract base capabilities a note that media is not supported.
 */
public Properties getCapabilities() {
	Properties props = super.getCapabilities();
	props.put(net.sourceforge.gjtapi.capabilities.Capabilities.MEDIA, new Boolean(false));
	return props;
}
/**
 * No media is supported for this adapter.
 */
public boolean isMediaTerminal(String terminal) {
	return false;
}
/**
 * Should never be called since media is not supported.
 */
public void play(String terminal, java.lang.String[] streamIds, int offset, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	throw new javax.telephony.PlatformException("Media not supported");
}
/**
 * Should never be called since media is not supported.
 */
public void record(String terminal, String streamId, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	throw new javax.telephony.PlatformException("Media not supported");
}
/**
 * Should never be called since media is not supported.
 */
public net.sourceforge.gjtapi.RawSigDetectEvent retrieveSignals(String terminal, int num, javax.telephony.media.Symbol[] patterns, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	throw new javax.telephony.PlatformException("Media not supported");
}
/**
 * Should never be called since media is not supported.
 */
public void sendSignals(String terminal, javax.telephony.media.Symbol[] syms, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws javax.telephony.media.MediaResourceException {
	throw new javax.telephony.PlatformException("Media not supported");
}
/**
 * Should never be called since media is not supported.
 */
public void stop(String terminal) {
	throw new javax.telephony.PlatformException("Media not supported");
}
/**
 * Should never be called since media is not supported.
 */
public void triggerRTC(String terminal, javax.telephony.media.Symbol action) {
	throw new javax.telephony.PlatformException("Media not supported");
}
}
