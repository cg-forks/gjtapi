package net.sourceforge.gjtapi.raw.mux;

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
import net.sourceforge.gjtapi.*;
import java.util.*;
/**
 * This CallId is used to wrap one or more lower-level CallIds so that logical calls
 * can be created across multiple lower-level calls that span multiplexed TPIs.
 * Creation date: (2000-09-22 21:58:11)
 * @author: Richard Deadman
 */
public class MuxCallId implements CallId {
	private HashSet	ids = new HashSet();	// Set of CallHolders
	/**
	 * Maps the leg addresses to the CallHolders that define the sub-call that holds that leg.
	 **/
	private HashMap legMap = new HashMap();
/**
 * MuxCallId constructor comment.
 */
public MuxCallId() {
	super();
}
/**
 * Add a CallId associated with a certain low-level TelephonyProvider to my managed set.
 * Creation date: (2000-09-23 0:43:48)
 * @param id net.sourceforge.gjtapi.CallId
 * @param tpi net.sourceforge.gjtapi.TelephonyProvider
 */
void addCall(CallId id, TelephonyProvider tpi) {
	this.addCall(new CallHolder(id, tpi));
}
/**
 * Add a CallHolder to my managed set.
 * Creation date: (2000-09-23 0:43:48)
 * @param ch A CallHolder that holds a low-level CallId and its sub-provieder.
 */
void addCall(CallHolder ch) {
	this.getIds().add(ch);
}
/**
 * Add a mapping from a leg's address to the sub-provider information for the leg.
 * Creation date: (2000-09-25 13:34:25)
 * @param address java.lang.String
 * @param subCall The sub-provider, sub-provider CallId pair
 */
void addLeg(String address, CallHolder subCall) {
	this.getLegMap().put(address, subCall);
}
/**
 * Determine if a CallHolder -- sub-provider call part -- is part of the logical call.
 * Creation date: (2000-10-04 10:19:04)
 * @return true if the sub-call is already included, false otherwise.
 * @param ch A CallId-TelephonyProvider pair that uniquely ideintifes the sub-call.  Equality is used for testing, not identity, so the actual CallHolder is not needed.
 */
boolean contains(CallHolder ch) {
	return this.getIds().contains(ch);
}
/**
 * Clean up any references
 * Creation date: (2000-10-04 13:04:20)
 */
void free() {
	this.getIds().clear();
	this.getLegMap().clear();
}
/**
 * Find the set of all Addresses that are mapped to a sub-provider.
 * Creation date: (2000-10-04 12:32:24)
 * @return java.lang.String[]
 * @param subCall net.sourceforge.gjtapi.CallId
 */
String[] getAddsForSubCall(CallHolder callHolder) {
	Set adds = new HashSet();
	Iterator it = this.getLegMap().entrySet().iterator();
	while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		if (entry.getValue().equals(callHolder)) {
			adds.add(entry.getKey());
		}
	}

	return (String[])adds.toArray(new String[adds.size()]);
}
/**
 * Return an iteration over out set of CallHolders.
 * Creation date: (2000-09-25 12:38:50)
 * @return java.util.Iterator
 */
Iterator getCallHolders() {
	return this.getIds().iterator();
}
/**
 * Return the HashSet of lower-level TPI CallHolders that I manage as one logical CallId.
 * Creation date: (2000-09-22 22:04:08)
 * @return A set of CallHolders.
 */
private java.util.HashSet getIds() {
	return ids;
}
/**
 * Retrieve a mapping from a leg's address to the sub-provider information for the leg.
 * Creation date: (2000-09-25 13:34:25)
 * @param address java.lang.String
 * @return The sub-provider, sub-provider CallId pair
 */
CallHolder getLeg(String address) {
	return (CallHolder)this.getLegMap().get(address);
}
/**
 * Internal accessor
 * Creation date: (2000-09-23 0:38:49)
 * @return HashMap
 */
private HashMap getLegMap() {
	return legMap;
}
/**
 * Remove any entries from the address->CallHolder map that point to a CallHolder.a
 * Creation date: (2000-09-26 15:52:34)
 * @param ch net.sourceforge.gjtapi.raw.mux.CallHolder
 */
void removeAddresses(CallHolder ch) {
	Iterator it = this.getLegMap().entrySet().iterator();
	while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		if (entry.getValue().equals(ch)) {
			it.remove();
		}
	}
}
/**
 * Remove a CallHolder to my managed set.
 * Creation date: (2000-09-23 0:43:48)
 * @return true if this was the last sub-call in the set.
 * @param id net.sourceforge.gjtapi.CallId
 * @param tpi net.sourceforge.gjtapi.TelephonyProvider
 */
boolean removeCall(CallId id, TelephonyProvider tpi) {
	return this.removeCall(new CallHolder(id, tpi));
}
/**
 * Remove a CallHolder to my managed set.
 * Creation date: (2000-09-23 0:43:48)
 * @return true if this was the last sub-call in the set.
 * @param ch A CallHolder that holds a low-level CallId and its sub-provider.
 */
boolean removeCall(CallHolder ch) {
	Set ids = this.getIds();
	ids.remove(ch);
	this.removeAddresses(ch);
	return ids.isEmpty();
}
/**
 * Remove a mapping from a leg's address to the sub-provider information for the leg.
 * Creation date: (2000-09-25 13:34:25)
 * @param address java.lang.String
 * @return The sub-provider, sub-provider CallId pair
 */
CallHolder removeLeg(String address) {
	return (CallHolder)this.getLegMap().remove(address);
}
/**
 * Describe myself.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Multiplexed CallId for " + this.getIds().size() + " lower level CallIds.";
}
}
