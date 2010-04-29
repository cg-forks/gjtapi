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

import javax.telephony.Connection;
import javax.telephony.InvalidArgumentException;
import javax.telephony.PlatformException;
import javax.telephony.TerminalConnection;

import junit.framework.Assert;

import org.junit.Test;

/**
 * Unit tests for a connection
 * @author Richard Deadman
 *
 */
public class FreeConnectionTest extends TestCallBase {

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#getAddress()}.
	 */
	@Test
	public void testGetAddress() {
		Assert.assertEquals(addr1, connOut.getAddress());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#getCall()}.
	 */
	@Test
	public void testGetCall() {
		Assert.assertEquals(call, connOut.getCall());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#getCapabilities()}.
	 */
	@Test
	public void testGetCapabilities() {
		Assert.assertEquals(true, connOut.getCapabilities().canDisconnect());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#getConnectionCapabilities(javax.telephony.Terminal, javax.telephony.Address)}.
	 */
	@Test
	public void testGetConnectionCapabilities() {
		try {
			Assert.assertEquals(true, connOut.getConnectionCapabilities(null, null).canDisconnect());
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for Connection.disconnect()
	 */
	@Test
	public void testDisconnect() {
		try {
			connOut.disconnect();
		} catch (Exception e) {
			Assert.fail();
		}
		
		Assert.assertEquals(Connection.DISCONNECTED, connOut.getState());

	}
	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#getPrivateData()}.
	 */
	@Test
	public void testGetPrivateData() {
		Assert.assertEquals(null, connOut.getPrivateData());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#getState()}.
	 */
	@Test
	public void testGetState() {
		Assert.assertEquals(Connection.CONNECTED, connOut.getState());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#getTerminalConnections()}.
	 */
	@Test
	public void testGetTerminalConnections() {
		TerminalConnection[] tcs = connOut.getTerminalConnections();
		
		Assert.assertEquals(1, tcs.length);
		Assert.assertEquals(connOut, tcs[0].getConnection());
		Assert.assertEquals(term1, tcs[0].getTerminal());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#sendPrivateData(java.lang.Object)}.
	 */
	@Test
	public void testSendPrivateData() {
		// TODO: How do we test?
		connOut.sendPrivateData(null);
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeConnection#setPrivateData(java.lang.Object)}.
	 */
	@Test
	public void testSetPrivateData() {
		// TODO: How do we test?
		connOut.setPrivateData(null);
	}

}
