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

import javax.telephony.Provider;
import javax.telephony.ProviderUnavailableException;

import junit.framework.Assert;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author rdeadman
 *
 */
public class GenericPeerTest {
	private GenericJtapiPeer peer;

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
		peer = new GenericJtapiPeer();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericJtapiPeer#getDefaultProvider()}.
	 */
	@Test
	public void testGetDefaultProvider() {
		Assert.assertEquals("Emulator", peer.getDefaultProvider());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericJtapiPeer#getName()}.
	 */
	@Test
	public void testGetName() {
		Assert.assertEquals("net.sourceforge.gjtapi.GenericJtapiPeer", peer.getName());
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericJtapiPeer#getProvider(java.lang.String)}.
	 */
	@Test
	public void testGetProvider() {
		Provider prov = peer.getProvider("Emulator");
		Assert.assertNotNull(prov);
		Assert.assertEquals("Emulator", prov.getName());
		
		Assert.assertNotNull(peer.getProvider("net.sourceforge.gjtapi.raw.emulator.EmProvider"));
		try {
			peer.getProvider("Foo");
			Assert.fail();
		} catch(ProviderUnavailableException pue) {
			// expected
		}
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericJtapiPeer#getServices()}.
	 */
	@Test
	public void testGetServices() {
		String[] services = peer.getServices();
		
		Assert.assertNotNull(services);
		Assert.assertEquals(5, services.length);
	}

	/**
	 * Test method for {@link net.sourceforge.gjtapi.GenericJtapiPeer#findResource(java.lang.String)}.
	 */
	@Test
	public void testFindResource() {
		Assert.assertNotNull(peer.findResource("Emulator.props"));
		Assert.assertNull(peer.findResource("Foo.gjtapi"));
	}

}
