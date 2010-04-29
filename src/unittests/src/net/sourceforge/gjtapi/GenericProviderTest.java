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

import java.util.Properties;

import javax.telephony.Address;
import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidStateException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PlatformException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.Provider;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.capabilities.AddressCapabilities;
import javax.telephony.capabilities.CallCapabilities;
import javax.telephony.capabilities.ConnectionCapabilities;
import javax.telephony.capabilities.ProviderCapabilities;
import javax.telephony.capabilities.TerminalCapabilities;
import javax.telephony.capabilities.TerminalConnectionCapabilities;

import net.sourceforge.gjtapi.capabilities.RawCapabilities;
import net.sourceforge.gjtapi.raw.ProviderFactory;
import net.sourceforge.gjtapi.raw.emulator.EmProvider;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author rdeadman
 *
 */
public class GenericProviderTest {

	private GenericProvider prov;
	
	/**
	 * @throws java.lang.Exception
	 */
	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		prov = createEmulator(false, false);
	}
	
	private GenericProvider createEmulator(boolean display, boolean throttle) {
		Properties emulatorProps = new Properties();
		emulatorProps.setProperty("Address1", "21");
		emulatorProps.setProperty("Address2", "22");
		emulatorProps.setProperty("Address3", "23");
		emulatorProps.setProperty("display", display ? "true" : "false");
		emulatorProps.setProperty("throttle", throttle ? "t" : "f");
		emulatorProps.setProperty("termSendPrivateData", "t");
		emulatorProps.setProperty("tcSendPrivateData", "t");
		
		TelephonyProvider tp = ProviderFactory.createProvider(new EmProvider());
		tp.initialize(emulatorProps);
		return new GenericProvider("Emulator", tp, emulatorProps);
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		prov.shutdown();
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#addObserver(javax.telephony.ProviderObserver)}.
	 */
	@Test
	public void testAddObserver() {
		UnitTestObserver obs = new UnitTestObserver();
		try {
			prov.addObserver(obs);
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(prov.getObservers());
		Assert.assertEquals(1, prov.getObservers().length);
		Assert.assertEquals(obs, prov.getObservers()[0]);
		
		prov.removeObserver(obs);
		
		Assert.assertNull(prov.getObservers());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#addProviderListener(javax.telephony.ProviderListener)}.
	 */
	@Test
	public void testAddProviderListener() {
		UnitTestListener l = new UnitTestListener();
		
		try {
			prov.addProviderListener(l);
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(prov.getProviderListeners());
		Assert.assertEquals(1, prov.getProviderListeners().length);
		Assert.assertEquals(l, prov.getProviderListeners()[0]);
		
		prov.removeProviderListener(l);
		
		Assert.assertNull(prov.getProviderListeners());
		
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#createCall()}.
	 */
	@Test
	public void testCreateCall() {
		try {
			Assert.assertNotNull(prov.createCall());
		} catch (InvalidStateException e) {
			Assert.fail();
		} catch (PrivilegeViolationException e) {
			Assert.fail();
		} catch (MethodNotSupportedException e) {
			Assert.fail();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getAddress(java.lang.String)}.
	 */
	@Test
	public void testGetAddress() {
		Address addr = null;
		try {
			addr = prov.getAddress("21");
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(addr);
		Assert.assertEquals("21", addr.getName());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getAddressCapabilities()}.
	 */
	@Test
	public void testGetAddressCapabilities() {
		AddressCapabilities caps = prov.getAddressCapabilities();
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getAddressCapabilities(javax.telephony.Terminal)}.
	 */
	@Test
	public void testGetAddressCapabilitiesTerminal() {
		AddressCapabilities caps = null;
		try {
			caps = prov.getAddressCapabilities(null);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getAddresses()}.
	 */
	@Test
	public void testGetAddresses() {
		Address[] addresses = null;
		try {
			addresses = prov.getAddresses();
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(addresses);
		Assert.assertEquals(3, addresses.length);
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getAvailableMediaTerminal()}.
	 */
	@Test
	public void testGetAvailableMediaTerminal() {
		Assert.assertNotNull(prov.getAvailableMediaTerminal());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getCallCapabilities()}.
	 */
	@Test
	public void testGetCallCapabilities() {
		CallCapabilities caps = prov.getCallCapabilities();
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.canConnect());
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getCallCapabilities(javax.telephony.Terminal, javax.telephony.Address)}.
	 */
	@Test
	public void testGetCallCapabilitiesTerminalAddress() {
		CallCapabilities caps = null;
		try {
			caps = prov.getCallCapabilities(null, null);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.canConnect());
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getCallMgr()}.
	 */
	@Test
	public void testGetCallMgr() {
		Assert.assertNotNull(prov.getCallMgr());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getCalls()}.
	 */
	@Test
	public void testGetCalls() {
		try {
			Assert.assertNull(prov.getCalls());
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
		
		try {
			prov.createCall();
		} catch (Exception e) {
			Assert.fail();
		}
		
		try {
			Assert.assertEquals(1, prov.getCalls().length);
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getCapabilities()}.
	 */
	@Test
	public void testGetCapabilities() {
		ProviderCapabilities caps = null;
		caps = prov.getCapabilities();
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getConnectionCapabilities()}.
	 */
	@Test
	public void testGetConnectionCapabilities() {
		ConnectionCapabilities caps = null;
		caps = prov.getConnectionCapabilities();
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.canDisconnect());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getConnectionCapabilities(javax.telephony.Terminal, javax.telephony.Address)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetConnectionCapabilitiesTerminalAddress() {
		ConnectionCapabilities caps = null;
		try {
			caps = prov.getConnectionCapabilities(null, null);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.canDisconnect());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getMediaMgr()}.
	 */
	@Test
	public void testGetMediaMgr() {
		Assert.assertNotNull(prov.getMediaMgr());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getName()}.
	 */
	@Test
	public void testGetName() {
		Assert.assertEquals("Emulator", prov.getName());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getPrivateData()}.
	 */
	@Test
	public void testGetPrivateData() {
		Assert.assertNull(prov.getPrivateData());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getProviderCapabilities()}.
	 */
	@Test
	public void testGetProviderCapabilities() {
		ProviderCapabilities caps = null;
		try {
			caps = prov.getProviderCapabilities();
		} catch (PlatformException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getProviderCapabilities(javax.telephony.Terminal)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetProviderCapabilitiesTerminal() {
		ProviderCapabilities caps = null;
		try {
			caps = prov.getProviderCapabilities(null);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getRaw()}.
	 */
	@Test
	public void testGetRaw() {
		Assert.assertNotNull(prov.getRaw());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getRawCapabilities()}.
	 */
	@Test
	public void testGetRawCapabilities() {
		RawCapabilities caps = prov.getRawCapabilities();
		
		Assert.assertNotNull(caps);
		Assert.assertFalse(caps.allMediaTerminals);
		Assert.assertTrue(caps.allocateMedia);
		Assert.assertFalse(caps.dynamicAddresses);
		Assert.assertTrue(caps.media);
		Assert.assertFalse(caps.throttle);
		
		GenericProvider otherProvider = createEmulator(true, true);
		caps = otherProvider.getRawCapabilities();
		
		Assert.assertNotNull(caps);
		Assert.assertFalse(caps.allMediaTerminals);
		Assert.assertTrue(caps.allocateMedia);
		Assert.assertFalse(caps.dynamicAddresses);
		Assert.assertTrue(caps.media);
		Assert.assertTrue(caps.throttle);
		
		otherProvider.shutdown();
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getState()}.
	 */
	@Test
	public void testGetState() {
		Assert.assertEquals(Provider.IN_SERVICE, prov.getState());
		
		prov.shutdown();
		
		Assert.assertEquals(Provider.SHUTDOWN, prov.getState());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getTerminal(java.lang.String)}.
	 */
	@Test
	public void testGetTerminal() {
		try {
			Assert.assertEquals("21", prov.getTerminal("21").getName());
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		try {
			prov.getTerminal("99");
			Assert.fail();
		} catch (InvalidArgumentException e) {
			// expected path
		}
		
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getTerminalCapabilities()}.
	 */
	@Test
	public void testGetTerminalCapabilities() {
		TerminalCapabilities caps = null;
		try {
			caps = prov.getTerminalCapabilities();
		} catch (PlatformException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getTerminalCapabilities(javax.telephony.Terminal)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetTerminalCapabilitiesTerminal() {
		TerminalCapabilities caps = null;
		try {
			caps = prov.getTerminalCapabilities(null);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.isObservable());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getTerminalConnectionCapabilities()}.
	 */
	@Test
	public void testGetTerminalConnectionCapabilities() {
		TerminalConnectionCapabilities caps = null;
		try {
			caps = prov.getTerminalConnectionCapabilities();
		} catch (PlatformException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.canAnswer());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getTerminalConnectionCapabilities(javax.telephony.Terminal)}.
	 */
	@SuppressWarnings("deprecation")
	@Test
	public void testGetTerminalConnectionCapabilitiesTerminal() {
		TerminalConnectionCapabilities caps = null;
		try {
			caps = prov.getTerminalConnectionCapabilities(null);
		} catch (PlatformException e) {
			Assert.fail();
		} catch (InvalidArgumentException e) {
			Assert.fail();
		}
		
		Assert.assertNotNull(caps);
		Assert.assertTrue(caps.canAnswer());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getTerminals()}.
	 */
	@Test
	public void testGetTerminals() {
		try {
			Assert.assertEquals(3, prov.getTerminals().length);
		} catch (ResourceUnavailableException e) {
			Assert.fail();
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#hookupJainCallback(net.sourceforge.gjtapi.jcc.Provider)}.
	 */
	@Test
	public void testHookupJainCallback() {
		net.sourceforge.gjtapi.jcc.Provider jainProv = new net.sourceforge.gjtapi.jcc.Provider(prov);
		prov.hookupJainCallback(jainProv);
		
		Assert.assertEquals(jainProv, prov.getJainProvider());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#sendPrivateData(java.lang.Object)}.
	 */
	@Test
	public void testSendPrivateData() {
		prov.setPrivateData("Foo");
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#setPrivateData(java.lang.Object)}.
	 */
	@Test
	public void testSetPrivateData() {
		prov.setPrivateData("Bar");
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#shutdown()}.
	 */
	@Test
	public void testShutdown() {
		prov.shutdown();
		
		Assert.assertEquals(Provider.SHUTDOWN, prov.getState());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#getMediaService()}.
	 */
	@Test
	public void testGetMediaService() {
		Assert.assertNotNull(prov.getMediaService());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericProvider#setDisconnectOnMediaRelease(boolean)}.
	 */
	@Test
	public void testSetDisconnectOnMediaRelease() {
		prov.setDisconnectOnMediaRelease(true);
		Assert.assertTrue(prov.disconnectOnMediaRelease());
		
		prov.setDisconnectOnMediaRelease(false);
		Assert.assertFalse(prov.disconnectOnMediaRelease());
	}

}
