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

import javax.telephony.Address;
import javax.telephony.InvalidArgumentException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PlatformException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.capabilities.TerminalCapabilities;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for the Terminal implementation class
 * @author Richard Deadman
 *
 */
public class FreeTerminalTest extends TestBase {
	private FreeTerminal term = null;
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		term = (FreeTerminal)prov.getTerminal("21");
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#addCallListener(javax.telephony.CallListener)}.
	 */
	@Test
	public void testAddCallListener() {
		UnitTestListener cl = new UnitTestListener();
		try {
			term.addCallListener(cl);
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(1, term.getCallListeners().length);
		Assert.assertEquals(cl, term.getCallListeners()[0]);
		
		term.removeCallListener(cl);
		
		Assert.assertEquals(null, term.getCallListeners());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#addCallObserver(javax.telephony.CallObserver)}.
	 */
	@Test
	public void testAddCallObserver() {
		UnitTestObserver obs = new UnitTestObserver();
		try {
			term.addCallObserver(obs);
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(1, term.getCallObservers().length);
		Assert.assertEquals(obs, term.getCallObservers()[0]);
		
		term.removeCallObserver(obs);
		Assert.assertEquals(null, term.getCallObservers());
		
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#addObserver(javax.telephony.TerminalObserver)}.
	 */
	@Test
	public void testAddObserver() {
		UnitTestObserver obs = new UnitTestObserver();
		try {
			term.addObserver(obs);
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertEquals(1, term.getObservers().length);
		Assert.assertEquals(obs, term.getObservers()[0]);
		
		term.removeObserver(obs);
		Assert.assertEquals(null, term.getObservers());
		
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#addTerminalListener(javax.telephony.TerminalListener)}.
	 */
	@Test
	public void testAddTerminalListener() {
		UnitTestListener l = new UnitTestListener();
		term.addTerminalListener(l);

		Assert.assertEquals(null, term.getCallListeners());
		Assert.assertEquals(1, term.getTerminalListeners().length);
		Assert.assertEquals(l, term.getTerminalListeners()[0]);
		
		term.removeTerminalListener(l);
		
		Assert.assertEquals(null, term.getTerminalListeners());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#getAddresses()}.
	 */
	@Test
	public void testGetAddresses() {
		Address[] addresses = term.getAddresses();
		
		Assert.assertEquals(1, addresses.length);
		Assert.assertEquals("21", addresses[0].getName());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#getCapabilities()}.
	 */
	@Test
	public void testGetCapabilities() {
		TerminalCapabilities caps = term.getCapabilities();
		
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#getName()}.
	 */
	@Test
	public void testGetName() {
		Assert.assertEquals("21", term.getName());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#getPrivateData()}.
	 */
	@Test
	public void testGetPrivateData() {
		Assert.assertEquals(null, term.getPrivateData());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#getProvider()}.
	 */
	@Test
	public void testGetProvider() {
		Assert.assertEquals(GenericProvider.class, term.getProvider().getClass());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#getTerminalCapabilities(javax.telephony.Terminal, javax.telephony.Address)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetTerminalCapabilities() {
		try {
			Assert.assertEquals(true, term.getTerminalCapabilities(term, term.getAddresses()[0]).isObservable());
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#getTerminalCapabilities(javax.telephony.Terminal, javax.telephony.Address)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetTerminalCapabilitiesBad() {
		try {
			term.getTerminalCapabilities(null, null);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#getTerminalConnections()}.
	 */
	@Test
	public void testGetTerminalConnections() {
		Assert.assertEquals(null, term.getTerminalConnections());
	}

//	/**
//	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#send(net.sourceforge.gjtapi.events.FreeTerminalEvent)}.
//	 */
//	@Test
//	public void testSend() {
//		term.send(new FreeTerminalEvent(0, term) {
//			
//			@Override
//			public int getID() {
//				return 0;
//			}
//		});
//	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#sendPrivateData(java.lang.Object)}.
	 */
	@Test
	public void testSendPrivateData() {
		term.sendPrivateData("Hang up");
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#setName(java.lang.String)}.
	 */
	@Test
	public void testSetName() {
		String oldName = term.getName();
		term.setName("12");
		
		Assert.assertEquals("12", term.getName());
		term.setName(oldName);
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#setPrivateData(java.lang.Object)}.
	 */
	@Test
	public void testSetPrivateData() {
		term.setPrivateData(null);
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#setProvider(javax.telephony.Provider)}.
	 */
	@Test
	public void testSetProvider() {
		term.setProvider(term.getProvider());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.FreeTerminal#toString()}.
	 */
	@Test
	public void testToString() {
		Assert.assertEquals("Terminal: 21", term.toString());
	}

}
