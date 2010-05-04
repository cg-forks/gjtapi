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

import java.util.Set;

import javax.telephony.Address;
import javax.telephony.CallEvent;
import javax.telephony.CallListener;
import javax.telephony.CallObserver;
import javax.telephony.InvalidArgumentException;
import javax.telephony.MetaEvent;
import javax.telephony.Terminal;

import junit.framework.Assert;

import net.sourceforge.gjtapi.events.FreeCallActiveEv;
import net.sourceforge.gjtapi.events.FreeCallEvent;
import net.sourceforge.gjtapi.events.FreeCallInvalidEv;
import net.sourceforge.gjtapi.events.FreeCallObservationEndedEv;
import net.sourceforge.gjtapi.events.FreeConnAlertingEv;
import net.sourceforge.gjtapi.events.FreeConnConnectedEv;
import net.sourceforge.gjtapi.events.FreeConnCreatedEv;
import net.sourceforge.gjtapi.events.FreeConnDisconnectedEv;
import net.sourceforge.gjtapi.events.FreeConnFailedEv;
import net.sourceforge.gjtapi.events.FreeConnInProgressEv;
import net.sourceforge.gjtapi.events.FreeConnUnknownEv;
import net.sourceforge.gjtapi.events.FreeTermConnActiveEv;
import net.sourceforge.gjtapi.events.FreeTermConnCreatedEv;
import net.sourceforge.gjtapi.events.FreeTermConnDroppedEv;
import net.sourceforge.gjtapi.events.FreeTermConnPassiveEv;
import net.sourceforge.gjtapi.events.FreeTermConnRingingEv;
import net.sourceforge.gjtapi.events.FreeTermConnUnknownEv;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests of the internal ListenerManager
 * @author Richard Deadman
 *
 */
public class ListenerManagerTest extends TestBase {
	private FreeCall call = null;
	private ListenerManager mgr = null;

	class TestMetaEvent implements MetaEvent {

		public int getCause() {
			return 0;
		}

		public int getID() {
			return 0;
		}

		public MetaEvent getMetaEvent() {
			return null;
		}

		public Object getSource() {
			return null;
		}
		
	}
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Before
	public void setUp() throws Exception {
		call = (FreeCall)prov.createCall();
		mgr = new ListenerManager(call);
	}

	@After
	public void tearDown() throws Exception {
	}
	@Test
	public void testAddCallListener() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		Assert.assertEquals(1, cl.getEventCount());
		Assert.assertEquals(0, cl.getTransmissionEndedCount());
		
		// Now remove
		mgr.remove(cl);
		Assert.assertEquals(2, cl.getEventCount());
		Assert.assertEquals(1, cl.getTransmissionEndedCount());

		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// ensure another event didn't arrive
		Assert.assertEquals(2, cl.getEventCount());
	}
	
	@Test
	public void testAddCallListenerAddress() {
		UnitTestListener cl = new UnitTestListener();
		Address addr = null;
		try {
			addr = prov.getAddress("21");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		mgr.add(cl, addr);
		
		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// see if event arrived
		Assert.assertEquals(1, cl.getEventCount());
		Assert.assertEquals(0, cl.getTransmissionEndedCount());

		mgr.remove(addr);
		Assert.assertEquals(2, cl.getEventCount());
		Assert.assertEquals(1, cl.getTransmissionEndedCount());

		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// ensure event didn't arrive
		Assert.assertEquals(2, cl.getEventCount());
	}

	@Test
	public void testAddCallListenerAddressTwoAddresses() {
		UnitTestListener cl = new UnitTestListener();
		Address addr = null;
		Address addr2 = null;
		try {
			addr = prov.getAddress("21");
			addr2 = prov.getAddress("22");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		mgr.add(cl, addr);
		mgr.add(cl, addr2);
		
		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// see if event arrived
		Assert.assertEquals(1, cl.getEventCount());
		Assert.assertEquals(0, cl.getTransmissionEndedCount());

		mgr.remove(addr);
		
		Assert.assertEquals(1, cl.getEventCount());
		Assert.assertEquals(0, cl.getTransmissionEndedCount());

		mgr.remove(addr2);
		
		Assert.assertEquals(2, cl.getEventCount());
		Assert.assertEquals(1, cl.getTransmissionEndedCount());

		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// ensure event didn't arrive
		Assert.assertEquals(2, cl.getEventCount());
	}

	@Test
	public void testAddCallListenerTerminal() {
		UnitTestListener cl = new UnitTestListener();
		Terminal term = null;
		try {
			term = prov.getTerminal("21");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		mgr.add(cl, term);
		
		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// see if event arrived
		Assert.assertEquals(1, cl.getEventCount());
		Assert.assertEquals(0, cl.getTransmissionEndedCount());

		mgr.remove(term);
		Assert.assertEquals(2, cl.getEventCount());
		Assert.assertEquals(1, cl.getTransmissionEndedCount());

		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// see if event didn't arrive
		Assert.assertEquals(2, cl.getEventCount());
	}

	@Test
	public void testAddCallListenerTerminalTwoTerminals() {
		UnitTestListener cl = new UnitTestListener();
		Terminal term = null;
		Terminal term2 = null;
		try {
			term = prov.getTerminal("21");
			term2 = prov.getTerminal("22");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		mgr.add(cl, term);
		mgr.add(cl, term2);
		
		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// see if event arrived
		Assert.assertEquals(1, cl.getEventCount());
		Assert.assertEquals(0, cl.getTransmissionEndedCount());

		mgr.remove(term);
		Assert.assertEquals(1, cl.getEventCount());
		Assert.assertEquals(0, cl.getTransmissionEndedCount());

		mgr.remove(term2);
		Assert.assertEquals(2, cl.getEventCount());
		Assert.assertEquals(1, cl.getTransmissionEndedCount());

		mgr.callActive(new FreeCallActiveEv(CallEvent.CALL_ACTIVE, call));
		
		// see if event didn't arrive
		Assert.assertEquals(2, cl.getEventCount());
	}

	@Test
	public void testAddCallObserver() {
		UnitTestObserver obs = new UnitTestObserver();
		mgr.add(obs);
		Assert.assertEquals(0, obs.getEventCount());

		FreeCallEvent[] evs = new FreeCallEvent[1];
		evs[0] = new FreeCallActiveEv(0, call);
		mgr.sendEvents(evs);
		
		// see if event arrived
		Assert.assertEquals(1, obs.getEventCount());
		Assert.assertEquals(0, obs.getTerminationEventCount());

		// remove -- trigger termination event
		mgr.remove(obs);
		Assert.assertEquals(2, obs.getEventCount());
		Assert.assertEquals(1, obs.getTerminationEventCount());

		mgr.sendEvents(evs);

		// see if event didn't arrive
		Assert.assertEquals(2, obs.getEventCount());
	}

	@Test
	public void testAddCallObserverAddress() {
		UnitTestObserver obs = new UnitTestObserver();
		Address addr = null;
		try {
			addr = prov.getAddress("21");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		mgr.add(obs, addr);
		Assert.assertEquals(0, obs.getEventCount());
		
		FreeCallEvent[] evs = new FreeCallEvent[1];
		evs[0] = new FreeCallActiveEv(0, call);
		mgr.sendEvents(evs);
				
		// see if event arrived
		Assert.assertEquals(1, obs.getEventCount());
		Assert.assertEquals(0, obs.getTerminationEventCount());

		// try to remove from addr
		mgr.remove(addr);	//addr.removeObserver(obs);
		Assert.assertEquals(2, obs.getEventCount());
		Assert.assertEquals(1, obs.getTerminationEventCount());

		mgr.sendEvents(evs);
		
		// see if event arrived anyway
		Assert.assertEquals(2, obs.getEventCount());
	}

	@Test
	public void testAddCallObserverAddressTwoAddresses() {
		UnitTestObserver obs = new UnitTestObserver();
		Address addr = null;
		Address addr2 = null;
		try {
			addr = prov.getAddress("21");
			addr2 = prov.getAddress("22");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		mgr.add(obs, addr);
		mgr.add(obs, addr2);
		Assert.assertEquals(0, obs.getEventCount());
		
		FreeCallEvent[] evs = new FreeCallEvent[1];
		evs[0] = new FreeCallActiveEv(0, call);
		mgr.sendEvents(evs);
						
		// see if event arrived
		Assert.assertEquals(1, obs.getEventCount());
		Assert.assertEquals(0, obs.getTerminationEventCount());

		// try to remove from addr
		mgr.remove(addr);
		Assert.assertEquals(1, obs.getEventCount());
		Assert.assertEquals(0, obs.getTerminationEventCount());

		// try to remove from addr
		mgr.remove(addr2);
		Assert.assertEquals(2, obs.getEventCount());
		Assert.assertEquals(1, obs.getTerminationEventCount());

		mgr.sendEvents(evs);
		
		// see if event arrived anyway
		Assert.assertEquals(2, obs.getEventCount());
	}

	@Test
	public void testAddCallObserverTerminal() {
		UnitTestObserver obs = new UnitTestObserver();
		Terminal term = null;
		try {
			term = prov.getTerminal("21");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		mgr.add(obs, term);
		
		FreeCallEvent[] evs = new FreeCallEvent[1];
		evs[0] = new FreeCallActiveEv(0, call);
		mgr.sendEvents(evs);
						
		// see if event arrived
		Assert.assertEquals(1, obs.getEventCount());
		Assert.assertEquals(0, obs.getTerminationEventCount());

		// try to remove from addr
		mgr.remove(term);
		Assert.assertEquals(2, obs.getEventCount());
		Assert.assertEquals(1, obs.getTerminationEventCount());

		mgr.sendEvents(evs);
		
		// see if event arrived anyway
		Assert.assertEquals(2, obs.getEventCount());
	}

	@Test
	public void testAddCallObserverTerminalTwoTerminals() {
		UnitTestObserver obs = new UnitTestObserver();
		Terminal term = null;
		Terminal term2 = null;
		try {
			term = prov.getTerminal("21");
			term2 = prov.getTerminal("22");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		mgr.add(obs, term);
		mgr.add(obs, term2);
		
		FreeCallEvent[] evs = new FreeCallEvent[1];
		evs[0] = new FreeCallActiveEv(0, call);
		mgr.sendEvents(evs);
								
		// see if event arrived
		Assert.assertEquals(1, obs.getEventCount());
		Assert.assertEquals(0, obs.getTerminationEventCount());

		// try to remove from addr
		mgr.remove(term);
		// see if event arrived
		Assert.assertEquals(1, obs.getEventCount());
		Assert.assertEquals(0, obs.getTerminationEventCount());

		// try to remove from addr
		mgr.remove(term2);
		Assert.assertEquals(2, obs.getEventCount());
		Assert.assertEquals(1, obs.getTerminationEventCount());

		mgr.sendEvents(evs);
		
		// see if event arrived anyway
		Assert.assertEquals(2, obs.getEventCount());
	}

	@Test
	public void testCallEventTransmissionEnded() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.callEventTransmissionEnded(new FreeCallObservationEndedEv(call));
		
		Assert.assertEquals(1, cl.getTransmissionEndedCount());
	}

	@Test
	public void testCallInvalid() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.callInvalid(new FreeCallInvalidEv(0, call));
		
		Assert.assertEquals(1, cl.invalidCount);
	}

	@Test
	public void testConnectionAlerting() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.connectionAlerting(new FreeConnAlertingEv(0, new FreeConnection(call, "21")));
		
		Assert.assertEquals(1, cl.connAlerting);
	}

	@Test
	public void testConnectionConnected() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.connectionConnected(new FreeConnConnectedEv(0, new FreeConnection(call, "21")));
		
		Assert.assertEquals(1, cl.connConnected);
	}

	@Test
	public void testConnectionCreated() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.connectionCreated(new FreeConnCreatedEv(0, new FreeConnection(call, "21")));
		
		Assert.assertEquals(1, cl.connCreated);
	}

	@Test
	public void testConnectionDisconnected() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.connectionDisconnected(new FreeConnDisconnectedEv(0, new FreeConnection(call, "21")));
		
		Assert.assertEquals(1, cl.connDisconnected);
	}

	@Test
	public void testConnectionFailed() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.connectionFailed(new FreeConnFailedEv(0, new FreeConnection(call, "21")));
		
		Assert.assertEquals(1, cl.connFailed);
	}

	@Test
	public void testConnectionInProgress() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.connectionInProgress(new FreeConnInProgressEv(0, new FreeConnection(call, "21")));
		
		Assert.assertEquals(1, cl.connInProgress);
	}

	@Test
	public void testConnectionUnknown() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.connectionUnknown(new FreeConnUnknownEv(0, new FreeConnection(call, "21")));
		
		Assert.assertEquals(1, cl.connUnknown);
	}

	@Test
	public void testGetCallListeners() {
		Assert.assertEquals(0, mgr.getCallListeners().size());
		
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		Set<CallListener> listeners = mgr.getCallListeners();
		Assert.assertEquals(1, listeners.size());
		Assert.assertTrue(listeners.contains(cl));
		
		mgr.remove(cl);
		Assert.assertEquals(0, mgr.getCallListeners().size());
		
	}

	@Test
	public void testGetCallObservers() {
		Assert.assertEquals(0, mgr.getCallObservers().size());
		
		UnitTestObserver obs = new UnitTestObserver();
		mgr.add(obs);
		
		Set<CallObserver> observers = mgr.getCallObservers();
		Assert.assertEquals(1, observers.size());
		Assert.assertTrue(observers.contains(obs));
		
		mgr.remove(obs);
		Assert.assertEquals(0, mgr.getCallObservers().size());
	}

	@Test
	public void testIsEmptyListener() {
		Assert.assertTrue(mgr.isEmpty());
		
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		Assert.assertFalse(mgr.isEmpty());
		
		mgr.remove(cl);
		
		Assert.assertTrue(mgr.isEmpty());		
	}

	@Test
	public void testIsEmptyObserver() {
		Assert.assertTrue(mgr.isEmpty());
		
		UnitTestObserver obs = new UnitTestObserver();
		mgr.add(obs);
		
		Assert.assertFalse(mgr.isEmpty());
		
		mgr.remove(obs);
		
		Assert.assertTrue(mgr.isEmpty());		
	}

	@Test
	public void testMultiCallMetaMergeEnded() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);

		mgr.multiCallMetaMergeEnded(new TestMetaEvent());
		
		Assert.assertEquals(1, cl.multiMergeEnded);
	}

	@Test
	public void testMultiCallMetaMergeStarted() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);

		mgr.multiCallMetaMergeStarted(new TestMetaEvent());
		
		Assert.assertEquals(1, cl.multiMergeStarted);
	}

	@Test
	public void testMultiCallMetaTransferEnded() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);

		mgr.multiCallMetaTransferEnded(new TestMetaEvent());
		
		Assert.assertEquals(1, cl.multiTransferEnded);
	}

	@Test
	public void testMultiCallMetaTransferStarted() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);

		mgr.multiCallMetaTransferStarted(new TestMetaEvent());
		
		Assert.assertEquals(1, cl.multiTransferStarted);
	}

	@Test
	public void testRemoveAll() {
		Assert.assertTrue(mgr.isEmpty());
		
		mgr.add(new UnitTestObserver());
		mgr.add(new UnitTestObserver());
		mgr.add(new UnitTestListener());
		mgr.add(new UnitTestListener());
		mgr.add(new UnitTestListener());
		
		Assert.assertFalse(mgr.isEmpty());
		
		mgr.removeAll();
		
		Assert.assertTrue(mgr.isEmpty());		
	}

	@Test
	public void testSendEvents() {
		UnitTestObserver obs = new UnitTestObserver();
		mgr.add(obs);
		
		FreeCallEvent[] evs = new FreeCallEvent[1];
		evs[0] = new FreeCallActiveEv(0, call);
		mgr.sendEvents(evs);
		
		Assert.assertEquals(1, obs.getEventCount());
	}

	@Test
	public void testSingleCallMetaProgressEnded() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);

		mgr.singleCallMetaProgressEnded(new TestMetaEvent());
		
		Assert.assertEquals(1, cl.metaProgressEnded);
	}

	@Test
	public void testSingleCallMetaProgressStarted() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);

		mgr.singleCallMetaProgressStarted(new TestMetaEvent());
		
		Assert.assertEquals(1, cl.metaProgressStarted);
	}

	@Test
	public void testSingleCallMetaSnapshotEnded() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);

		mgr.singleCallMetaSnapshotStarted(new TestMetaEvent());
		
		Assert.assertEquals(1, cl.metaSnapshotStarted);
	}

	@Test
	public void testSingleCallMetaSnapshotStarted() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);

		mgr.singleCallMetaSnapshotEnded(new TestMetaEvent());
		
		Assert.assertEquals(1, cl.metaSnapshotEnded);
	}

	@Test
	public void testTerminalConnectionActive() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.terminalConnectionActive(new FreeTermConnActiveEv(0, new FreeTerminalConnection(new FreeConnection(call, "21"), "21"), false));
		
		Assert.assertEquals(1, cl.termConnActive);
	}

	@Test
	public void testTerminalConnectionCreated() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.terminalConnectionCreated(new FreeTermConnCreatedEv(0, new FreeTerminalConnection(new FreeConnection(call, "21"), "21")));
		
		Assert.assertEquals(1, cl.termConnCreated);
	}

	@Test
	public void testTerminalConnectionDropped() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.terminalConnectionDropped(new FreeTermConnDroppedEv(0, new FreeTerminalConnection(new FreeConnection(call, "21"), "21")));
		
		Assert.assertEquals(1, cl.termConnDropped);
	}

	@Test
	public void testTerminalConnectionPassive() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.terminalConnectionPassive(new FreeTermConnPassiveEv(0, new FreeTerminalConnection(new FreeConnection(call, "21"), "21")));
		
		Assert.assertEquals(1, cl.termConnPassive);
	}

	@Test
	public void testTerminalConnectionRinging() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.terminalConnectionRinging(new FreeTermConnRingingEv(0, new FreeTerminalConnection(new FreeConnection(call, "21"), "21")));
		
		Assert.assertEquals(1, cl.termConnRinging);
	}

	@Test
	public void testTerminalConnectionUnknown() {
		UnitTestListener cl = new UnitTestListener();
		mgr.add(cl);
		
		mgr.terminalConnectionUnknown(new FreeTermConnUnknownEv(0, new FreeTerminalConnection(new FreeConnection(call, "21"), "21")));
		
		Assert.assertEquals(1, cl.termConnUnknown);
	}

}
