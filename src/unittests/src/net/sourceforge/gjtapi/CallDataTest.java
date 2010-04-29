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
import javax.telephony.Connection;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for CallData object
 * @author Richard Deadman
 *
 */
public class CallDataTest {

	private CallData callData;
	/**
     * Set up the test environment.
     */
    @Before
    public void setUp() {
    	ConnectionData[] cd = new ConnectionData[3];
    	cd[0] = new ConnectionData(Connection.CONNECTED, "Addr1", true, null);
    	cd[1] = new ConnectionData(Connection.ALERTING, "Addr2", true, null);
    	cd[2] = new ConnectionData(Connection.CONNECTED, "Addr3", false, null);
    	
        this.callData = new CallData(new CallId() {
        	public int hashCode() {
        		return 1;
        	}
        	public boolean equals(Object o) {
        		return ((o.getClass().equals(getClass())) &&
        				(o.hashCode() == this.hashCode()));
        	}
		}
        , Call.ACTIVE, cd);
    }
    
    @Test public void testVariables() {
    	Assert.assertEquals(Call.ACTIVE, this.callData.callState);
    	Assert.assertEquals(1, this.callData.id.hashCode());
    	Assert.assertEquals(3, this.callData.connections.length);
    }
    @Test public void testGetLocalAddresses() {
    	Assert.assertEquals(2, callData.getLocalAddresses().length);
    }
    
    @Test public void testGetRemoteAddresses() {
    	Assert.assertEquals(1, callData.getRemoteAddresses().length);
    }
}
