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

import javax.telephony.AddressEvent;
import javax.telephony.AddressListener;
import javax.telephony.CallEvent;
import javax.telephony.CallListener;
import javax.telephony.ConnectionEvent;
import javax.telephony.ConnectionListener;
import javax.telephony.MetaEvent;
import javax.telephony.ProviderEvent;
import javax.telephony.ProviderListener;
import javax.telephony.TerminalConnectionEvent;
import javax.telephony.TerminalConnectionListener;
import javax.telephony.TerminalEvent;
import javax.telephony.TerminalListener;

/**
 * Helper class for unit tests
 * @author Richard Deadman
 *
 */
public class UnitTestListener implements CallListener, TerminalListener, AddressListener, ProviderListener, ConnectionListener, TerminalConnectionListener {
	
	private int eventCount = 0;
	private int transmissionEndedCount = 0;
	int invalidCount = 0;
	int connAlerting = 0;
	int connConnected = 0;
	int connCreated = 0;
	int connDisconnected = 0;
	int connFailed = 0;
	int connInProgress = 0;
	int connUnknown = 0;
	int termConnActive = 0;
	int termConnCreated = 0;
	int termConnRinging = 0;
	int termConnDropped = 0;
	int termConnPassive = 0;
	int termConnUnknown = 0;
	int metaProgressStarted = 0;
	int metaProgressEnded = 0;
	int metaSnapshotStarted = 0;
	int metaSnapshotEnded = 0;
	int multiMergeStarted = 0;
	int multiMergeEnded = 0;
	int multiTransferStarted = 0;
	int multiTransferEnded = 0;

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callActive(javax.telephony.CallEvent)
	 */
	public void callActive(CallEvent arg0) {
		this.eventCount++;

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callEventTransmissionEnded(javax.telephony.CallEvent)
	 */
	public void callEventTransmissionEnded(CallEvent arg0) {
		this.eventCount++;
		this.transmissionEndedCount++;
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callInvalid(javax.telephony.CallEvent)
	 */
	public void callInvalid(CallEvent arg0) {
		this.eventCount++;
		this.invalidCount++;

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaMergeEnded(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaMergeEnded(MetaEvent arg0) {
		this.eventCount++;
		this.multiMergeEnded++;
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaMergeStarted(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaMergeStarted(MetaEvent arg0) {
		this.eventCount++;
		this.multiMergeStarted++;
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaTransferEnded(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaTransferEnded(MetaEvent arg0) {
		this.eventCount++;
		this.multiTransferEnded++;
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaTransferStarted(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaTransferStarted(MetaEvent arg0) {
		this.eventCount++;
		this.multiTransferStarted++;
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaProgressEnded(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaProgressEnded(MetaEvent arg0) {
		this.eventCount++;
		this.metaProgressEnded++;
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaProgressStarted(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaProgressStarted(MetaEvent arg0) {
		this.eventCount++;
		this.metaProgressStarted++;
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaSnapshotEnded(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaSnapshotEnded(MetaEvent arg0) {
		this.eventCount++;
		this.metaSnapshotEnded++;
	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaSnapshotStarted(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaSnapshotStarted(MetaEvent arg0) {
		this.eventCount++;
		this.metaSnapshotStarted++;
	}

	public void terminalListenerEnded(TerminalEvent arg0) {
		this.eventCount++;
		
	}

	public void addressListenerEnded(AddressEvent arg0) {
		this.eventCount++;
		
	}

	public void providerEventTransmissionEnded(ProviderEvent arg0) {
		this.eventCount++;
		
	}

	public void providerInService(ProviderEvent arg0) {
		this.eventCount++;
		
	}

	public void providerOutOfService(ProviderEvent arg0) {
		this.eventCount++;
		
	}

	public void providerShutdown(ProviderEvent arg0) {
		this.eventCount++;
		
	}

	void setEventCount(int eventCount) {
		this.eventCount = eventCount;
	}

	int getEventCount() {
		return eventCount;
	}

	int getTransmissionEndedCount() {
		return this.transmissionEndedCount;
	}

	public void connectionAlerting(ConnectionEvent arg0) {
		this.eventCount++;
		this.connAlerting++;
	}

	public void connectionConnected(ConnectionEvent arg0) {
		this.eventCount++;
		this.connConnected++;
	}

	public void connectionCreated(ConnectionEvent arg0) {
		this.eventCount++;
		this.connCreated++;
	}

	public void connectionDisconnected(ConnectionEvent arg0) {
		this.eventCount++;
		this.connDisconnected++;
	}

	public void connectionFailed(ConnectionEvent arg0) {
		this.eventCount++;
		this.connFailed++;
	}

	public void connectionInProgress(ConnectionEvent arg0) {
		this.eventCount++;
		this.connInProgress++;
	}

	public void connectionUnknown(ConnectionEvent arg0) {
		this.eventCount++;
		this.connUnknown++;
	}

	public void terminalConnectionActive(TerminalConnectionEvent arg0) {
		this.eventCount++;
		this.termConnActive++;
	}

	public void terminalConnectionCreated(TerminalConnectionEvent arg0) {
		this.eventCount++;
		this.termConnCreated++;
	}

	public void terminalConnectionDropped(TerminalConnectionEvent arg0) {
		this.eventCount++;
		this.termConnDropped++;
	}

	public void terminalConnectionPassive(TerminalConnectionEvent arg0) {
		this.eventCount++;
		this.termConnPassive++;
	}

	public void terminalConnectionRinging(TerminalConnectionEvent arg0) {
		this.eventCount++;
		this.termConnRinging++;
	}

	public void terminalConnectionUnknown(TerminalConnectionEvent arg0) {
		this.eventCount++;
		this.termConnUnknown++;
	}

}
