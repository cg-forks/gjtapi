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
import javax.telephony.events.CallEv;
import javax.telephony.media.*;
import javax.telephony.media.events.*;
/**
 * Listen for old-style media events and use them to update the OldMediaProvider state machine.
 * Creation date: (2000-06-07 12:16:20)
 * @author: Richard Deadman
 */
public class OldMediaListener implements MediaCallObserver {
	OldMediaProvider prov = null;
/**
 * Create a listener on the JTAPI Provider that listens for media events and reports them back
 * to the adapter OldMediaProvider.
 * Creation date: (2000-06-07 12:17:54)
 * @author: Richard Deadman
 * @param p The OldMediaProvider to report media events to.
 */
public OldMediaListener(OldMediaProvider p) {
	super();
	
	this.prov = p;
}
/**
 * Listen for media events and report them to the adapter provider.
 */
public void callChangedEvent(javax.telephony.events.CallEv[] eventList) {
	int size = eventList.length;
	for (int i = 0; i < size; i++) {
		CallEv ev = eventList[i];
		switch (ev.getID()) {
			case MediaTermConnDtmfEv.ID: {
				MediaTermConnDtmfEv mev = (MediaTermConnDtmfEv)ev;
				this.getProv().takeDtmf(mev.getTerminalConnection().getTerminal().getName(), mev.getDtmfDigit());
				break;
			}
			case MediaTermConnStateEv.ID: {
				MediaTermConnStateEv msev = (MediaTermConnStateEv)ev;
				this.getProv().updateState(msev.getTerminalConnection().getTerminal().getName(), msev.getMediaState());
				break;
			}
		}
	}
}
/**
 * Internal OldMediaProvider accessor.
 * Creation date: (2000-06-07 13:05:08)
 * @author: Richard Deadman
 * @return A OldMediaProvider
 */
private OldMediaProvider getProv() {
	return prov;
}
}
