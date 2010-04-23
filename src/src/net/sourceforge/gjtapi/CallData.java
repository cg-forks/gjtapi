package net.sourceforge.gjtapi;

/*
	Copyright (c) 2002 8x8 Inc. (www.8x8.com) 

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
/**
 * This is a simple Call data carrier used for call snapshot delivery from dynamic
 * or throttled TelephonyProviders.
 * <P>The state given should be of type Call state public final variables.
 * Creation date: (2000-06-15 14:05:44)
 * @author: Richard Deadman
 */
public class CallData implements java.io.Serializable {
	static final long serialVersionUID = -5155933247539613243L;
	
	public CallId id;
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
public CallData(CallId cid, int state, ConnectionData[] cd) {
	super();
	
	this.id = cid;
	this.callState = state;
	this.connections = cd;
}

/**
 * No-arg constructor required for JAX-RPC serialization.
 */
public CallData() {
	super();
}
/**
 * Return the array of addresses with the given "isLocal" flag.
 * Creation date: (2000-10-02 14:23:15)
 * @return java.lang.String[]
 */
private String[] getAddresses(boolean isLocal) {
	java.util.Set<String> set = new java.util.HashSet<String>();
	for (int i = 0; i < this.connections.length; i++) {
		ConnectionData cd = this.connections[i];
		if (cd.isLocal == isLocal)
			set.add(cd.address);
	}
	return set.toArray(new String[set.size()]);
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
