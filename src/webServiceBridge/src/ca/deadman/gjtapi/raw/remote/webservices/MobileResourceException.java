package ca.deadman.gjtapi.raw.remote.webservices;

import javax.telephony.media.*;

import ca.deadman.gjtapi.raw.remote.MovableEventIds;

import net.sourceforge.gjtapi.media.*;

/*
	Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca)

	All rights reserved.

	This software is dual licenced under the GPL and a commercial license.
	If you wish to use under the GPL, the following license applies, otherwise
	please contact Deadman Consulting at sales@deadman.ca for commercial licensing.

    ---

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
/**
 * @author rdeadman
 *
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 */
public class MobileResourceException extends Exception {
	private MobileResourceEvent resEv = null;

	/**
	 * Constructor for a MobileResourceException containg a MobileResourceEvent.
	 * @param message
	 */
	public MobileResourceException(MobileResourceEvent ev) {
		super();

		this.resEv = ev;
	}

	/**
	 * Create a MobileResourceException from the JTAPI exception.
	 * @param mrex
	 */
	public MobileResourceException(MediaResourceException mrex) {
		super();

		ResourceEvent re = mrex.getResourceEvent();
		if (re instanceof PlayerEvent) {
			PlayerEvent pe = (PlayerEvent)re;
			this.resEv = new MobileResourceEvent(MovableEventIds.MEDIA_PLAY, pe.getEventID(), pe.getMediaService().getTerminalName(), pe.getError(), pe.getQualifier(), pe.getRTCTrigger());
			this.resEv.playerChange = pe.getChangeType().hashCode();
			this.resEv.index = pe.getIndex();
			this.resEv.offset = pe.getOffset();
		} else if (re instanceof RecorderEvent) {
			RecorderEvent ev = (RecorderEvent)re;
			this.resEv = new MobileResourceEvent(MovableEventIds.MEDIA_RECORD, ev.getEventID(), ev.getMediaService().getTerminalName(), ev.getError(), ev.getQualifier(), ev.getRTCTrigger());
			this.resEv.duration = ev.getDuration();
		} else if (re instanceof SignalDetectorEvent) {
			SignalDetectorEvent ev = (SignalDetectorEvent)re;
			this.resEv = new MobileResourceEvent(MovableEventIds.MEDIA_DETECT, ev.getEventID(), ev.getMediaService().getTerminalName(), ev.getError(), ev.getQualifier(), ev.getRTCTrigger());
			this.resEv.index = ev.getPatternIndex();
			Symbol[] sbuf = ev.getSignalBuffer();
			int len = sbuf.length;
			int[] buf = new int[len];
			for (int i = 0; i < len; i++)
				buf[i] = sbuf[i].hashCode();
			this.resEv.buffer = buf;
		} else if (re instanceof SignalGeneratorEvent) {
			SignalGeneratorEvent ev = (SignalGeneratorEvent)re;
			this.resEv = new MobileResourceEvent(MovableEventIds.MEDIA_GENERATE, ev.getEventID(), ev.getMediaService().getTerminalName(), ev.getError(), ev.getQualifier(), ev.getRTCTrigger());
		} else {
			// unknown event
		}
	}
	/**
	 * Get the Exception data
	 * @return MobileResourceEvent
	 */
	public MobileResourceEvent getResourceEvent() {
		return this.resEv;
	}

	/**
	 * Turn myself into the JTapi MediaResourceException
	 * @return MediaResourceException
	 */
	public MediaResourceException morph() {
		MobileResourceEvent ev = this.getResourceEvent();
		ResourceEvent resEv = null;
		// create the proper sub-event
		switch (ev.type) {
			case MovableEventIds.MEDIA_PLAY :
				resEv = new GenericPlayerEvent(Symbol.getSymbol(ev.id), ev.termName, Symbol.getSymbol(ev.error), Symbol.getSymbol(ev.qualifier), Symbol.getSymbol(ev.rtcTrigger), Symbol.getSymbol(ev.playerChange), ev.index, ev.offset);
				break;

			case MovableEventIds.MEDIA_RECORD :
				resEv = new GenericRecorderEvent(Symbol.getSymbol(ev.id), ev.termName, Symbol.getSymbol(ev.error), Symbol.getSymbol(ev.qualifier), Symbol.getSymbol(ev.rtcTrigger), ev.duration);
				break;

			case MovableEventIds.MEDIA_DETECT :
			    int[] evBuf = ev.buffer;
			    int len = evBuf.length;
			    Symbol[] sBuf = new Symbol[len];
			    for (int i = 0; i < len; i++)
			    	sBuf[i] = Symbol.getSymbol(evBuf[i]);
				resEv = new GenericSignalDetectorEvent(Symbol.getSymbol(ev.id), ev.termName, Symbol.getSymbol(ev.error), Symbol.getSymbol(ev.qualifier), Symbol.getSymbol(ev.rtcTrigger), ev.index, sBuf);
				break;

			case MovableEventIds.MEDIA_GENERATE :
				resEv = new GenericSignalGeneratorEvent(Symbol.getSymbol(ev.id), ev.termName, Symbol.getSymbol(ev.error), Symbol.getSymbol(ev.qualifier), Symbol.getSymbol(ev.rtcTrigger));
				break;

			default :
				break;
		}
		if (resEv == null)
			return null;
		else
			return new MediaResourceException(resEv);
	}
}
