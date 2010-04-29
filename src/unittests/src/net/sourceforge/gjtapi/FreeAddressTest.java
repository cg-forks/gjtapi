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

import javax.telephony.AddressListener;
import javax.telephony.Call;
import javax.telephony.Connection;
import javax.telephony.InvalidArgumentException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PlatformException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;
import javax.telephony.capabilities.AddressCapabilities;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test class for the Address implementation
 * @author Richard Deadman
 *
 */
public class FreeAddressTest extends TestBase {

	private FreeAddress addr;

	@Before
	public void setUp() throws Exception {
		addr = (FreeAddress) prov.getAddress("21");
	}

	@Test
	public void testAddAddressListener() {
		AddressListener l = new UnitTestListener();
		addr.addAddressListener(l);
		
		Assert.assertEquals(1, addr.getAddressListeners().length);
		Assert.assertEquals(l, addr.getAddressListeners()[0]);
		
		addr.removeAddressListener(l);
		
		Assert.assertEquals(null, addr.getAddressListeners());

	}

	@Test
	public void testAddCallListener() {
		UnitTestListener cl = new UnitTestListener();
		try {
			addr.addCallListener(cl);
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(1, addr.getCallListeners().length);
		Assert.assertEquals(cl, addr.getCallListeners()[0]);
		
		addr.removeCallListener(cl);
		
		Assert.assertEquals(null, addr.getCallListeners());
	}

	@Test
	public void testAddCallObserver() {
		UnitTestObserver obs = new UnitTestObserver();
		try {
			addr.addCallObserver(obs);
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(1, addr.getCallObservers().length);
		Assert.assertEquals(obs, addr.getCallObservers()[0]);
		
		addr.removeCallObserver(obs);
		Assert.assertEquals(null, addr.getCallObservers());
	}

	@Test
	public void testAddObserver() {
		UnitTestObserver obs = new UnitTestObserver();
		try {
			addr.addObserver(obs);
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(1, addr.getObservers().length);
		Assert.assertEquals(obs, addr.getObservers()[0]);
		
		addr.removeObserver(obs);
		Assert.assertEquals(null, addr.getObservers());
	}

	@Test
	public void testGetAddressCapabilities() {
		AddressCapabilities caps = null;
		try {
			caps = addr.getAddressCapabilities(null);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertTrue(caps.isObservable());
	}

	@Test
	public void testGetCalls() {
		Call[] calls = addr.getCalls();
		
		Assert.assertEquals(null, calls);
	}

	@Test
	public void testGetCapabilities() {
		AddressCapabilities caps = addr.getCapabilities();
		
		Assert.assertTrue(caps.isObservable());
	}

	@Test
	public void testGetConnections() {
		Connection[] connections = addr.getConnections();
		
		Assert.assertEquals(null, connections);
	}

	@Test
	public void testGetName() {
		Assert.assertEquals("21", addr.getName());
	}

	@Test
	public void testGetPrivateData() {
		Assert.assertEquals(null, addr.getPrivateData());
	}

	@Test
	public void testGetProvider() {
		Assert.assertEquals(GenericProvider.class, addr.getProvider().getClass());
	}

	@Test
	public void testGetTerminals() {
		Terminal[] terminals = addr.getTerminals();
		
		Assert.assertEquals(1, terminals.length);
		Assert.assertEquals("21", terminals[0].getName());
	}

	@Test
	public void testIsLocal() {
		Assert.assertTrue(addr.isLocal());
	}

	@Test
	public void testSendPrivateData() {
		addr.sendPrivateData("Light up");
	}

	@Test
	public void testSetName() {
		String oldName = addr.getName();
		addr.setName("12");
		
		Assert.assertEquals("12", addr.getName());
		addr.setName(oldName);
	}

	@Test
	public void testSetPrivateData() {
		addr.setPrivateData(null);
	}

	@Test
	public void testSetProvider() {
		addr.setProvider(addr.getProvider());
	}

	@Test
	public void testToString() {
		Assert.assertEquals("Address: 21", addr.toString());
	}

}
