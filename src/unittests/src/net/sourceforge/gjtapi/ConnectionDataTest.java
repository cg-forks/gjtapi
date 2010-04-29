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
import javax.telephony.TerminalConnection;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit Test for the simple connection data holder
 * @author Richard Deadman
 *
 */
public class ConnectionDataTest {
	private ConnectionData connData;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		TCData[] termConns = new TCData[2];
		termConns[0] = new TCData(TerminalConnection.ACTIVE, new TermData("Term1", false));
		termConns[1] = new TCData(TerminalConnection.PASSIVE, new TermData("Term2", true));
		this.connData = new ConnectionData(Connection.CONNECTED, "Addr3", false, termConns);
	}
	
	@Test public void testVariables() {
    	Assert.assertEquals(Connection.CONNECTED, this.connData.connState);
    	Assert.assertEquals("Addr3", this.connData.address);
    	Assert.assertFalse(this.connData.isLocal);
    	Assert.assertEquals(2, this.connData.terminalConnections.length);
    }

}
