/**
 * Copyright (c) 2010 Deadman Consulting Inc. (www.deadman.ca)

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
package net.sourceforge.gjtapi;

import javax.telephony.AddressObserver;
import javax.telephony.CallObserver;
import javax.telephony.ProviderObserver;
import javax.telephony.TerminalObserver;
import javax.telephony.events.AddrEv;
import javax.telephony.events.CallEv;
import javax.telephony.events.CallObservationEndedEv;
import javax.telephony.events.ProvEv;
import javax.telephony.events.TermEv;

/**
 * Call Observer used for unit tests
 * @author Richard Deadman
 *
 */
public class UnitTestObserver implements CallObserver, TerminalObserver, AddressObserver, ProviderObserver {

	private int eventCount = 0;
	private int terminationEventCount = 0;

	/* (non-Javadoc)
	 * @see javax.telephony.CallObserver#callChangedEvent(javax.telephony.events.CallEv[])
	 */
	public void callChangedEvent(CallEv[] arg0) {
		for(CallEv ev : arg0) {
			this.eventCount++;
			if(ev instanceof CallObservationEndedEv) {
				this.terminationEventCount++;
			}
		}

	}

	public void terminalChangedEvent(TermEv[] arg0) {
		for(@SuppressWarnings("unused") TermEv ev: arg0) {
			this.eventCount++;
		}
		
	}

	public void addressChangedEvent(AddrEv[] arg0) {
		for(@SuppressWarnings("unused") AddrEv ev: arg0) {
			this.eventCount++;
		}
		
	}

	public void providerChangedEvent(ProvEv[] arg0) {
		for(@SuppressWarnings("unused") ProvEv ev: arg0) {
			this.eventCount++;
		}
		
	}

	/**
	 * @param eventCount the eventCount to set
	 */
	void setEventCount(int eventCount) {
		this.eventCount = eventCount;
	}

	/**
	 * @return the eventCount
	 */
	int getEventCount() {
		return eventCount;
	}

	/**
	 * @return the terminationEventCount
	 */
	int getTerminationEventCount() {
		return this.terminationEventCount;
	}

}
