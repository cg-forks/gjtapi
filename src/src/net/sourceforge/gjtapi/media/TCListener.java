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
 * This is a helper class that delegates raw events off to the 1.2 Media TerminalConnection's
 * pseudo MediaService for handling.
 * Creation date: (2000-05-09 15:15:52)
 * @author: Richard Deadman
 */
class TCListener implements SignalDetectorListener {
	private TCMediaService ms = null;
/**
 * Create a simple delegate listener that sends signal detector events off to the service
 * Creation date: (2000-05-09 16:05:54)
 * @author: Richard Deadman
 * @param service A TCMediaService wrapping a MediaTerminalConnection's access to raw resources.
 */
TCListener(TCMediaService service) {
	super();
	
	this.ms = service;
}
/**
 * Internal accessor for the TC MediaService wrapper.
 * Creation date: (2000-05-09 16:07:57)
 * @author: Richard Deadman
 * @return A wrapper around a 1.2 MediaTerminalConnection's access to raw media services.
 */
private TCMediaService getMediaService() {
	return this.ms;
}
/**
 * Ignore for now.
 */
public void onOverflow(javax.telephony.media.SignalDetectorEvent event) {}
/**
 * Ignore for now.
 */
public void onPatternMatched(javax.telephony.media.SignalDetectorEvent event) {}
/**
 * Forward off to the TC MediaService for delegation to the TerminalConnection's observers
 */
public void onSignalDetected(javax.telephony.media.SignalDetectorEvent event) {
	@SuppressWarnings("deprecation")
	class DtmfEv extends BaseMediaEv implements javax.telephony.media.events.MediaTermConnDtmfEv {
		private char dtmf;
		DtmfEv(FreeMediaTerminalConnection tc, char c) {
			super(tc);
			this.dtmf = c;
		}

		public char getDtmfDigit() {
			return this.dtmf;
		}

		public int getID() {
			return javax.telephony.media.events.MediaTermConnDtmfEv.ID;
		}
	}

	String sig = SymbolConvertor.convert(event.getSignalBuffer());
	if (sig != null) {
		int len = sig.length();
		TCMediaService ms = this.getMediaService();
		FreeMediaTerminalConnection mtc = ms.getTermConn();
		for (int i = 0; i < len; i++) {
			ms.send(new DtmfEv(mtc, sig.charAt(i)));
		}
	}
}
}
