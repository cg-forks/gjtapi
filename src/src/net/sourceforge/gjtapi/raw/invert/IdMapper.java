package net.sourceforge.gjtapi.raw.invert;

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
import net.sourceforge.gjtapi.raw.remote.SerializableCallId;
import net.sourceforge.gjtapi.CallId;
import javax.telephony.Call;
import java.util.*;
/**
 * Map a CallId to a JTAPI Call.
 * Creation date: (2000-06-06 22:57:06)
 * @author: Richard Deadman
 */
public class IdMapper {
		// map provider CallIds to SerializableCallId
	private Map idToCallMap = new HashMap();
		// map serializable call ids to providers
	private Map callToIdMap = new HashMap();
	private long nextId = 0L;
	private java.util.LinkedList freeRefs = new LinkedList();
/**
 * Free a CallId from both maps
 * Creation date: (2000-02-17 23:51:15)
 * @author: Richard Deadman
 * @return Call The real call that id was mapped to, or null
 * @param id A proxy raw telephony call id.
 */
public synchronized Call freeId(CallId id) {
	Call call = null;
	Map idToCall = this.getIdToCallMap();

	if (idToCall.containsKey(id)) {
		call = (Call)idToCall.remove(id);

		if (call != null)
			this.getCallToIdMap().remove(call);

		// add to free list
		this.getFreeRefs().add(id);
	}

	return call;
}
/**
 * Get the map that maps JTAPI call to raw call ids.
 * Creation date: (2000-02-17 23:20:20)
 * @author: Richard Deadman
 * @return A map
 */
private Map getCallToIdMap() {
	return callToIdMap;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-02-17 23:24:17)
 * @author: 
 * @return java.util.LinkedList
 */
private java.util.LinkedList getFreeRefs() {
	return freeRefs;
}
/**
 * Look up a raw Call Id for a JTAPI Call object.
 */
public synchronized CallId getId(Call call) {
	Map refs = this.getCallToIdMap();
	
	// look for a Serialized CallId
	CallId ci = (CallId)refs.get(call);

	// Lazily create one if necessary
	if (ci == null) {
		// look for a dead one before constructing one
		try {
			ci = (CallId)this.getFreeRefs().removeFirst();
		} catch (NoSuchElementException nsee) {
			ci = new SerializableCallId(this.getNextId());
		}

		// add to the maps
		refs.put(call, ci);
		this.getIdToCallMap().put(ci, call);
	}

	return ci;
}
/**
 * Get the map that maps raw CallIds to JTAPI Calls.
 * Creation date: (2000-02-17 23:20:20)
 * @author: Richard Deadman
 * @return A map
 */
private Map getIdToCallMap() {
	return idToCallMap;
}
/**
 * Return the next available id
 * Creation date: (2000-02-17 23:21:25)
 * @author: Richard Deadman
 * @return The next available id
 */
private long getNextId() {
	return nextId++;
}
/**
 * Look up a JTAPI Call Id for a raw call id.
 * Return null if no entry found
 */
public Call jtapiCall(CallId id) {
	return (Call)this.getIdToCallMap().get(id);
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "CallId mapper with " + this.getCallToIdMap().size() + " entries.";
}
}
