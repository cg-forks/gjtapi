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

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PlatformException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.TerminalConnection;
import javax.telephony.callcontrol.CallControlTerminalConnection;
import javax.telephony.callcontrol.capabilities.CallControlTerminalConnectionCapabilities;
import javax.telephony.capabilities.TerminalConnectionCapabilities;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author rdeadman
 *
 */
public class FreeTerminalConnectionTest extends TestCallBase {

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#answer()}.
	 */
	@Test
	public void testAnswer() {
		Assert.assertEquals(CallControlTerminalConnection.RINGING, termConnIn.getCallControlState());
		
		try {
			termConnIn.answer();
		} catch (PrivilegeViolationException e1) {
			Assert.fail(e1.getLocalizedMessage());
		} catch (ResourceUnavailableException e1) {
			Assert.fail(e1.getLocalizedMessage());
		} catch (MethodNotSupportedException e1) {
			Assert.fail(e1.getLocalizedMessage());
		} catch (InvalidStateException e1) {
			Assert.fail(e1.getLocalizedMessage());
		}

		Assert.assertEquals(CallControlTerminalConnection.TALKING, termConnIn.getCallControlState());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#getCallControlState()}.
	 */
	@Test
	public void testGetCallControlState() {
		Assert.assertEquals(CallControlTerminalConnection.TALKING, termConnOut.getCallControlState());
		
		Assert.assertEquals(CallControlTerminalConnection.RINGING, termConnIn.getCallControlState());	
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#getCapabilities()}.
	 */
	@Test
	public void testGetCapabilities() {
		TerminalConnectionCapabilities caps = termConnOut.getCapabilities();
		
		Assert.assertEquals(false, caps.canAnswer());
		CallControlTerminalConnectionCapabilities ccCaps = (CallControlTerminalConnectionCapabilities)caps;
		Assert.assertEquals(true, ccCaps.canHold());
		Assert.assertEquals(false, ccCaps.canJoin());
		Assert.assertEquals(false, ccCaps.canLeave());
		Assert.assertEquals(true, ccCaps.canUnhold());
		
		Assert.assertEquals(true, termConnIn.getCapabilities().canAnswer());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#getConnection()}.
	 */
	@Test
	public void testGetConnection() {
		Assert.assertEquals(connOut, termConnOut.getConnection());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#getPrivateData()}.
	 */
	@Test
	public void testGetPrivateData() {
		Assert.assertEquals(null, termConnOut.getPrivateData());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#getState()}.
	 */
	@Test
	public void testGetState() {
		Assert.assertEquals(TerminalConnection.ACTIVE, termConnOut.getState());
		
		Assert.assertEquals(TerminalConnection.RINGING, termConnIn.getState());	
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#getTerminal()}.
	 */
	@Test
	public void testGetTerminal() {
		Assert.assertEquals(term1, termConnOut.getTerminal());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#getTerminalConnectionCapabilities(javax.telephony.Terminal, javax.telephony.Address)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetTerminalConnectionCapabilities() {
		try {
			Assert.assertEquals(false, termConnOut.getTerminalConnectionCapabilities(null, null).canAnswer());
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#getTerminalName()}.
	 */
	@Test
	public void testGetTerminalName() {
		Assert.assertEquals("21", termConnOut.getTerminalName());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#hold()}.
	 */
	@Test
	public void testHold() {
		try {
			termConnIn.answer();
		} catch (PrivilegeViolationException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (ResourceUnavailableException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (MethodNotSupportedException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (InvalidStateException e) {
			Assert.fail(e.getLocalizedMessage());
		}
		
		try {
			termConnIn.hold();
		} catch (InvalidStateException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (MethodNotSupportedException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (PrivilegeViolationException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (ResourceUnavailableException e) {
			Assert.fail(e.getLocalizedMessage());
		}
		
		try {
			termConnIn.unhold();
		} catch (InvalidStateException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (MethodNotSupportedException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (PrivilegeViolationException e) {
			Assert.fail(e.getLocalizedMessage());
		} catch (ResourceUnavailableException e) {
			Assert.fail(e.getLocalizedMessage());
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#join()}.
	 */
	@Test
	public void testJoin() {
		try {
			termConnOut.join();
			Assert.fail();
		} catch (MethodNotSupportedException mnse) {
			// good
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#leave()}.
	 */
	@Test
	public void testLeave() {
		try {
			termConnOut.leave();
			Assert.fail();
		} catch (MethodNotSupportedException mnse) {
			// good
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#sendPrivateData(java.lang.Object)}.
	 */
	@Test
	public void testSendPrivateData() {
		// TODO: How to test?
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#setPrivateData(java.lang.Object)}.
	 */
	@Test
	public void testSetPrivateData() {
		// TODO: How to test?
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminalConnection#toString()}.
	 */
	@Test
	public void testToString() {
		
		Assert.assertTrue(termConnOut.toString().startsWith("FreeTerminalConnection from Terminal '21' to Call 'A Call with phones: 2"));
	}

}
