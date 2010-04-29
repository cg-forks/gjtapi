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

import javax.telephony.Call;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PlatformException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.callcontrol.capabilities.CallControlCallCapabilities;
import javax.telephony.capabilities.CallCapabilities;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author rdeadman
 *
 */
public class FreeCallTest extends TestCallBase {

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#addCallListener(javax.telephony.CallListener)}.
	 */
	@Test
	public void testAddCallListener() {
		UnitTestListener cl = new UnitTestListener();
		call.addCallListener(cl);
		
		Assert.assertEquals(1, call.getCallListeners().length);
		Assert.assertEquals(cl, call.getCallListeners()[0]);
		
		call.removeCallListener(cl);
		
		Assert.assertEquals(null, call.getCallListeners());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#addObserver(javax.telephony.CallObserver)}.
	 */
	@Test
	public void testAddObserver() {
		UnitTestObserver obs = new UnitTestObserver();

		try {
			call.addObserver(obs);
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		}
		Assert.assertEquals(1, call.getObservers().length);
		Assert.assertEquals(obs, call.getObservers()[0]);
		
		call.removeObserver(obs);
		
		Assert.assertEquals(null, call.getObservers());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#addParty(java.lang.String)}.
	 */
	@Test
	public void testAddParty() {
		
		Assert.assertEquals(2, call.getConnections().length);
		
		try {
			call.addParty("23");
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		}

		Assert.assertEquals(3, call.getConnections().length);
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#conference(javax.telephony.Call)}.
	 */
	@Test
	public void testConference() {
		try {
			termConnOut.hold();
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (PrivilegeViolationException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		}
		
		Call otherCall = null;
		try {
			otherCall = prov.createCall();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		try {
			otherCall.connect(term1, addr1, "23");
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		}
		
		try {
			call.conference(otherCall);
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		try {
			call.drop();
		} catch (Exception ex) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#consult(javax.telephony.TerminalConnection)}.
	 */
	@Test
	public void testConsultTerminalConnection() {
		try {
			call.consult(null);
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			// expected flow
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#consult(javax.telephony.TerminalConnection, java.lang.String)}.
	 */
	@Test
	public void testConsultTerminalConnectionString() {
		FreeCall otherCall = null;
		try {
			otherCall = (FreeCall)prov.createCall();
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (PrivilegeViolationException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		}
		
		try {
			otherCall.consult(termConnOut, "23");
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		}
		
		try {
			otherCall.drop();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#drop()}.
	 */
	@Test
	public void testDrop() {
		try {
			call.drop();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(Call.INVALID, call.getState());
		Assert.assertEquals(null, call.getConnections());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getCallCapabilities(javax.telephony.Terminal, javax.telephony.Address)}.
	 */
	@Test
	public void testGetCallCapabilities() {
		CallCapabilities caps = null;
		try {
			caps = call.getCallCapabilities(term1, addr1);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(true, caps.canConnect());
		Assert.assertEquals(true, caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getCalledAddress()}.
	 */
	@Test
	public void testGetCalledAddress() {
		Assert.assertEquals("22", call.getCalledAddress().getName());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getCallID()}.
	 */
	@Test
	public void testGetCallID() {
		Assert.assertNotNull(call.getCallID());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getCallingAddress()}.
	 */
	@Test
	public void testGetCallingAddress() {
		Assert.assertEquals(addr1, call.getCallingAddress());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getCallingTerminal()}.
	 */
	@Test
	public void testGetCallingTerminal() {
		Assert.assertEquals(term1, call.getCallingTerminal());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getCapabilities(javax.telephony.Terminal, javax.telephony.Address)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetCapabilities() {
		CallCapabilities caps = null;
		try {
			caps = call.getCapabilities(term1, addr1);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(true, caps.canConnect());
		Assert.assertEquals(true, caps.isObservable());
		
		Assert.assertTrue(caps instanceof CallControlCallCapabilities);
		CallControlCallCapabilities ccCaps = (CallControlCallCapabilities)caps;
		
		Assert.assertEquals(true, ccCaps.canAddParty());
		Assert.assertEquals(true, ccCaps.canConference());
		Assert.assertEquals(true, ccCaps.canConsult());
//		Assert.assertEquals(true, ccCaps.canConsult(arg0));
//		Assert.assertEquals(true, ccCaps.canConsult(arg0, arg1));
		Assert.assertEquals(true, ccCaps.canDrop());
		Assert.assertEquals(false, ccCaps.canOffHook());
		Assert.assertEquals(true, ccCaps.canSetConferenceController());
		Assert.assertEquals(true, ccCaps.canSetConferenceEnable());
		Assert.assertEquals(false, ccCaps.canSetTransferController());
		Assert.assertEquals(false, ccCaps.canSetTransferEnable());
		Assert.assertEquals(false, ccCaps.canTransfer());
//		Assert.assertEquals(true, ccCaps.canTransfer(arg0));
//		Assert.assertEquals(true, ccCaps.canTransfer(arg0));
		
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getConnections()}.
	 */
	@Test
	public void testGetConnections() {
		Assert.assertEquals(2, call.getConnections().length);
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getLastRedirectedAddress()}.
	 */
	@Test
	public void testGetLastRedirectedAddress() {
		Assert.assertNull(call.getLastRedirectedAddress());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getPrivateData()}.
	 */
	@Test
	public void testGetPrivateData() {
		Assert.assertEquals(null, call.getPrivateData());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getProvider()}.
	 */
	@Test
	public void testGetProvider() {
		Assert.assertEquals(prov, call.getProvider());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#getState()}.
	 */
	@Test
	public void testGetState() {
		Assert.assertEquals(Call.ACTIVE, this.call.getState());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#offHook(javax.telephony.Address, javax.telephony.Terminal)}.
	 */
	@Test
	public void testOffHook() {
		try {
			call.offHook(addr1, term1);
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			// Expected path
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
	}

	/**
	 * Modification of Conference to set the controller
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#setConferenceController(javax.telephony.TerminalConnection)}.
	 */
	@Test
	public void testSetConferenceController() {
		try {
			termConnOut.hold();
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (PrivilegeViolationException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		}
		
		Call otherCall = null;
		try {
			otherCall = prov.createCall();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		try {
			otherCall.connect(term1, addr1, "23");
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		}
		
		// try the wrong transfer controller
		try {
			call.setConferenceController(termConnIn);
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		} catch (InvalidArgumentException e1) {
			Assert.fail();
		}
		
		Assert.assertEquals(termConnIn, call.getConferenceController());
		Assert.assertNull(call.getTransferController());
		
		// this should fail
		try {
			call.conference(otherCall);
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			// expected path
		}
		
		// now reset and try again
		try {
			call.setConferenceController(termConnOut);
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		} catch (InvalidArgumentException e1) {
			Assert.fail();
		}
		
		Assert.assertEquals(termConnOut, call.getConferenceController());
		
		// this should succeed
		try {
			call.conference(otherCall);
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#setConferenceEnable(boolean)}.
	 */
	@Test
	public void testSetConferenceEnable() {
		Assert.assertEquals(true, call.getConferenceEnable());
		try {
			call.setConferenceEnable(false);
			Assert.fail();
		} catch (InvalidStateException e2) {
			// should fail -- not IDLE
		} catch (MethodNotSupportedException e2) {
			Assert.fail();
		} catch (PrivilegeViolationException e2) {
			Assert.fail();
		} catch (InvalidArgumentException e2) {
			Assert.fail();
		}
		Assert.assertEquals(true, call.getConferenceEnable());
		
		try {
			termConnOut.hold();
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (PrivilegeViolationException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		}
		
		FreeCall otherCall = null;
		try {
			otherCall = (FreeCall)prov.createCall();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertTrue(otherCall.getConferenceEnable());
		try {
			otherCall.setConferenceEnable(false);
		} catch (InvalidStateException e2) {
			Assert.fail();
		} catch (MethodNotSupportedException e2) {
			Assert.fail();
		} catch (PrivilegeViolationException e2) {
			Assert.fail();
		} catch (InvalidArgumentException e2) {
			Assert.fail();
		}
		Assert.assertEquals(false, otherCall.getConferenceEnable());
		
		try {
			otherCall.connect(term1, addr1, "23");
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		}
		
		// this should fail
		try {
			call.conference(otherCall);
			Assert.fail();
		} catch (InvalidStateException e) {
			// expected path
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		try {
			call.drop();
			otherCall.drop();
		} catch (Exception e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#setProvider(javax.telephony.Provider)}.
	 */
	@Test
	public void testSetProvider() {
		call.setProvider(null);
		Assert.assertEquals(null, call.getProvider());
		
		call.setProvider(prov);
		Assert.assertEquals(prov, call.getProvider());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#setTransferController(javax.telephony.TerminalConnection)}.
	 */
	@Test
	public void testSetTransferController() {
		try {
			termConnOut.hold();
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (PrivilegeViolationException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		}
		
		Call otherCall = null;
		try {
			otherCall = prov.createCall();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		try {
			otherCall.connect(term1, addr1, "23");
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		}
		
		// try the wrong transfer controller
		try {
			call.setTransferController(termConnIn);
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		} catch (InvalidArgumentException e1) {
			Assert.fail();
		}
		
		Assert.assertEquals(termConnIn, call.getTransferController());
		Assert.assertNull(call.getConferenceController());
		
		// this should fail
		try {
			call.transfer(otherCall);
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			// expected path
		} catch (InvalidPartyException e) {
			Assert.fail();
		}
		
		// now reset and try again
		try {
			call.setTransferController(termConnOut);
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		} catch (InvalidArgumentException e1) {
			Assert.fail();
		}
		
		Assert.assertEquals(termConnOut, call.getTransferController());
		
		// this should succeed
		try {
			call.transfer(otherCall);
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#setTransferEnable(boolean)}.
	 */
	@Test
	public void testSetTransferEnable() {
		Assert.assertEquals(true, call.getConferenceEnable());
		try {
			call.setTransferEnable(false);
			Assert.fail();
		} catch (InvalidStateException e2) {
			// should fail -- not IDLE
		} catch (MethodNotSupportedException e2) {
			Assert.fail();
		} catch (PrivilegeViolationException e2) {
			Assert.fail();
		} catch (InvalidArgumentException e2) {
			Assert.fail();
		}
		Assert.assertEquals(true, call.getTransferEnable());
		
		try {
			termConnOut.hold();
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (PrivilegeViolationException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		}
		
		FreeCall otherCall = null;
		try {
			otherCall = (FreeCall)prov.createCall();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertTrue(otherCall.getTransferEnable());
		try {
			otherCall.setTransferEnable(false);
		} catch (InvalidStateException e2) {
			Assert.fail();
		} catch (MethodNotSupportedException e2) {
			Assert.fail();
		} catch (PrivilegeViolationException e2) {
			Assert.fail();
		} catch (InvalidArgumentException e2) {
			Assert.fail();
		}
		Assert.assertEquals(false, otherCall.getTransferEnable());
		
		try {
			otherCall.connect(term1, addr1, "23");
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		}
		
		// this should fail
		try {
			call.transfer(otherCall);
			Assert.fail();
		} catch (InvalidStateException e) {
			// expected path
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		}
		
		try {
			call.drop();
			otherCall.drop();
		} catch (Exception e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#transfer(java.lang.String)}.
	 */
	@Test
	public void testTransferString() {
		try {
			call.transfer("23");
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#transfer(javax.telephony.Call)}.
	 */
	@Test
	public void testTransferCall() {
		try {
			termConnOut.hold();
		} catch (InvalidStateException e1) {
			Assert.fail();
		} catch (MethodNotSupportedException e1) {
			Assert.fail();
		} catch (PrivilegeViolationException e1) {
			Assert.fail();
		} catch (ResourceUnavailableException e1) {
			Assert.fail();
		}
		
		Call otherCall = null;
		try {
			otherCall = prov.createCall();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		try {
			otherCall.connect(term1, addr1, "23");
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		}
		
		try {
			call.transfer(otherCall);
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		} catch (InvalidPartyException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(2, call.getConnections().length);
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeCall#toString()}.
	 */
	@Test
	public void testToString() {
		Assert.assertEquals("Active call with 2 connections.", call.toString());
	}

}
