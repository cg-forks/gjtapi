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
import javax.telephony.MetaEvent;
import javax.telephony.ProviderEvent;
import javax.telephony.ProviderListener;
import javax.telephony.TerminalEvent;
import javax.telephony.TerminalListener;

/**
 * Helper class for unit tests
 * @author Richard Deadman
 *
 */
public class UnitTestListener implements CallListener, TerminalListener, AddressListener, ProviderListener {

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callActive(javax.telephony.CallEvent)
	 */
	public void callActive(CallEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callEventTransmissionEnded(javax.telephony.CallEvent)
	 */
	public void callEventTransmissionEnded(CallEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#callInvalid(javax.telephony.CallEvent)
	 */
	public void callInvalid(CallEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaMergeEnded(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaMergeEnded(MetaEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaMergeStarted(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaMergeStarted(MetaEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaTransferEnded(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaTransferEnded(MetaEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#multiCallMetaTransferStarted(javax.telephony.MetaEvent)
	 */
	public void multiCallMetaTransferStarted(MetaEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaProgressEnded(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaProgressEnded(MetaEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaProgressStarted(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaProgressStarted(MetaEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaSnapshotEnded(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaSnapshotEnded(MetaEvent arg0) {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see javax.telephony.CallListener#singleCallMetaSnapshotStarted(javax.telephony.MetaEvent)
	 */
	public void singleCallMetaSnapshotStarted(MetaEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void terminalListenerEnded(TerminalEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void addressListenerEnded(AddressEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void providerEventTransmissionEnded(ProviderEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void providerInService(ProviderEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void providerOutOfService(ProviderEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	public void providerShutdown(ProviderEvent arg0) {
		// TODO Auto-generated method stub
		
	}

}
