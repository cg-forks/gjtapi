package ca.deadman.gjtapi.raw.remote.webservices;

import net.sourceforge.gjtapi.ConnectionData;

/*
	Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca)

	All rights reserved.

	This software is dual licenced under the GPL and a commercial license.
	If you wish to use under the GPL, the following license applies, otherwise
	please contact Deadman Consulting at sales@deadman.ca for commercial licensing.

    ---

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
/**
 * This is a simple Call data carrier copied from net.sourceforge.gjtapi.CallData.
 * <P><B>Note:</B>This is copied over since CallData is not JAX-RPC serializable. This means we
 * have copied methods -- bad design.
 * <P>The state given should be of type Call state public final variables.
 * Creation date: (20030-03-05 14:05:44)
 * @author: Richard Deadman
 */
public class MovableCallData implements java.io.Serializable {
	public int id;
	public int callState;
	public ConnectionData[] connections;
/**
 * Simple constructor for a Call snapshot.
 * Creation date: (2000-06-23 12:32:16)
 * @author: Richard Deadman
 * @param cid The id handle for the call being described.
 * @param state The javax.telephony.Call state
 * @param cd An array of ConnectionData holders.
 */
public MovableCallData(int cid, int state, ConnectionData[] cd) {
	super();

	this.id = cid;
	this.callState = state;
	this.connections = cd;
}

/**
 * No-arg constructor required for JAX-RPC serialization.
 */
public MovableCallData() {
	super();
}
/**
 * Return the array of addresses with the given "isLocal" flag.
 * Creation date: (2000-10-02 14:23:15)
 * @return java.lang.String[]
 */
private String[] getAddresses(boolean isLocal) {
	java.util.Set set = new java.util.HashSet();
	for (int i = 0; i < this.connections.length; i++) {
		ConnectionData cd = this.connections[i];
		if (cd.isLocal == isLocal)
			set.add(cd.address);
	}
	return (String[])set.toArray(new String[set.size()]);
}
/**
 * Return the array of local addresses
 * Creation date: (2000-10-02 14:23:15)
 * @return An array of Addresses that represent call legs inside the provider's domain.
 */
public String[] getLocalAddresses() {
	return this.getAddresses(true);
}
/**
 * Return the array of remote addresses
 * Creation date: (2000-10-02 14:23:15)
 * @return An array of Addresses that represent call legs on another outside of the provider's domain.
 */
public String[] getRemoteAddresses() {
	return this.getAddresses(false);
}
}
