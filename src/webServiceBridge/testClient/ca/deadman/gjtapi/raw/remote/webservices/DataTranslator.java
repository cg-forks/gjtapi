package ca.deadman.gjtapi.raw.remote.webservices;

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
import javax.telephony.media.*;

import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.media.*;

import ca.deadman.gjtapi.raw.remote.webservices.MobileResourceEvent;
import ca.deadman.gjtapi.raw.remote.webservices.MobileResourceException;

public class DataTranslator {

public DataTranslator() {
	super();
}

public net.sourceforge.gjtapi.ConnectionData translateConnectionData(ca.deadman.gjtapi.raw.remote.webservices.ConnectionData cd) {
	return new net.sourceforge.gjtapi.ConnectionData(cd.getConnState(), cd.getAddress(), cd.isIsLocal(),
		this.translateTCData(cd.getTerminalConnections()));
}
public net.sourceforge.gjtapi.ConnectionData[] translateConnectionData(ca.deadman.gjtapi.raw.remote.webservices.ConnectionData[] cds) {
	int len = cds.length;
	net.sourceforge.gjtapi.ConnectionData[] realCds = new net.sourceforge.gjtapi.ConnectionData[len];
	for (int i = 0; i<len; i++) {
		realCds[i] = this.translateConnectionData(cds[i]);
	}
	return realCds;
}
public net.sourceforge.gjtapi.TCData translateTCData(ca.deadman.gjtapi.raw.remote.webservices.TCData tcd) {
	return new net.sourceforge.gjtapi.TCData(tcd.getTcState(),
		this.translateTermData(tcd.getTerminal()));
}
public net.sourceforge.gjtapi.TCData[] translateTCData(ca.deadman.gjtapi.raw.remote.webservices.TCData[] tcds) {
	int len = tcds.length;
	net.sourceforge.gjtapi.TCData[] realTCds = new net.sourceforge.gjtapi.TCData[len];
	for (int i = 0; i<len; i++) {
		realTCds[i] = this.translateTCData(tcds[i]);
	}
	return realTCds;
}
public net.sourceforge.gjtapi.TermData translateTermData(ca.deadman.gjtapi.raw.remote.webservices.TermData td) {
	return new net.sourceforge.gjtapi.TermData(td.getTerminal(), td.isIsMedia());
}
public net.sourceforge.gjtapi.TermData[] translateTermData(ca.deadman.gjtapi.raw.remote.webservices.TermData[] tds) {
	int len = tds.length;
	net.sourceforge.gjtapi.TermData[] realTds = new net.sourceforge.gjtapi.TermData[len];
	for (int i = 0; i<len; i++) {
		realTds[i] = this.translateTermData(tds[i]);
	}
	return realTds;
}
	/**
	 * Turn a MobileResourceException into the JTapi MediaResourceException
	 * @return MediaResourceException
	 */
	public MediaResourceException morph(MobileResourceException ex) {
		MobileResourceEvent ev = ex.getResourceEvent();
		ResourceEvent resEv = null;
		// create the proper sub-event
		switch (ev.getType()) {
			case MovableEventIds.MEDIA_PLAY :
				resEv = new GenericPlayerEvent(Symbol.getSymbol(ev.getId()), ev.getTermName(), Symbol.getSymbol(ev.getError()), Symbol.getSymbol(ev.getQualifier()), Symbol.getSymbol(ev.getRtcTrigger()), Symbol.getSymbol(ev.getPlayerChange()), ev.getIndex(), ev.getOffset());
				break;

			case MovableEventIds.MEDIA_RECORD :
				resEv = new GenericRecorderEvent(Symbol.getSymbol(ev.getId()), ev.getTermName(), Symbol.getSymbol(ev.getError()), Symbol.getSymbol(ev.getQualifier()), Symbol.getSymbol(ev.getRtcTrigger()), ev.getDuration());
				break;

			case MovableEventIds.MEDIA_DETECT :
			    int[] evBuf = ev.getBuffer();
			    int len = evBuf.length;
			    Symbol[] sBuf = new Symbol[len];
			    for (int i = 0; i < len; i++)
			    	sBuf[i] = Symbol.getSymbol(evBuf[i]);
				resEv = new GenericSignalDetectorEvent(Symbol.getSymbol(ev.getId()), ev.getTermName(), Symbol.getSymbol(ev.getError()), Symbol.getSymbol(ev.getQualifier()), Symbol.getSymbol(ev.getRtcTrigger()), ev.getIndex(), sBuf);
				break;

			case MovableEventIds.MEDIA_GENERATE :
				resEv = new GenericSignalGeneratorEvent(Symbol.getSymbol(ev.getId()), ev.getTermName(), Symbol.getSymbol(ev.getError()), Symbol.getSymbol(ev.getQualifier()), Symbol.getSymbol(ev.getRtcTrigger()));
				break;

			default :
				break;
		}
		if (resEv == null)
			return null;
		else
			return new MediaResourceException(resEv);
	}
	/**
	 * Create an Event from my data.
	 * @return Event
	 */
	public RawSigDetectEvent toRawSigDetectEvent(EventHolder eh) {
		Symbol[] syms = SymbolConvertor.convert(eh.getSignals());
		int siz = syms.length;
		int[] sigs = new int[siz];
		for (int i = 0; i < siz; i++)
			sigs[i] = syms[i].hashCode();
		return RawSigDetectEvent.create(eh.getTerminal(), eh.getMediaQualifier(), sigs, eh.getMediaIndex(), eh.getMediaTrigger(), eh.getMediaError());
	}
}