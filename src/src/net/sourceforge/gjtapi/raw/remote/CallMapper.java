package net.sourceforge.gjtapi.raw.remote;

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
import net.sourceforge.gjtapi.CallId;
import java.util.*;
/**
 * Map a provider's CallId to a serializable CallId.
 * Creation date: (2000-02-17 22:57:06)
 * @author: Richard Deadman
 */
public class CallMapper {
		// map provider CallIds to SerializableCallId
	private Map cToSMap = new HashMap();
		// map serializable call ids to providers
	private Map sToCMap = new HashMap();
	private long nextId = 0L;
	private java.util.LinkedList freeRefs = new LinkedList();
/**
 * Translate a CallId to a remote integer handle, storing the relationship as well.
 */
public int callToInt(CallId call) {
	return (int)this.swapId(call).getId();
}
/**
 * Free a SerializableCallId from both maps
 * Creation date: (2000-02-17 23:51:15)
 * @author: Richard Deadman
 * @return CallId The real id that id was mapped to, or null
 * @param id A proxy call id.
 */
public synchronized CallId freeId(SerializableCallId id) {
	CallId ci = null;
	Map sToC = this.getsToCMap();

	if (sToC.containsKey(id)) {
		ci = (CallId)sToC.remove(id);

		if (ci != null)
			this.getcToSMap().remove(ci);

		// add to free list
		this.getFreeRefs().add(id);
	}

	return ci;
}
/**
 * Get the map that maps provider to serializable call ids.
 * Creation date: (2000-02-17 23:20:20)
 * @author: Richard Deadman
 * @return A map
 */
private java.util.Map getcToSMap() {
	return cToSMap;
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
 * Return the next available id
 * Creation date: (2000-02-17 23:21:25)
 * @author: Richard Deadman
 * @return The next available id
 */
private long getNextId() {
	return nextId++;
}
/**
 * Get the map that maps serializable to provider call ids.
 * Creation date: (2000-02-17 23:20:20)
 * @author: Richard Deadman
 * @return A map
 */
private java.util.Map getsToCMap() {
	return sToCMap;
}
/**
 * Translate a remote integer handle back to the original CallId.
 */
public CallId intToCall(int callRef) {
	return this.providerId(this.makeSerializableId(callRef));
}

/**
 * Factory method, to allow subclasses to use their own subclass for the mapping. * @param id The unique integer value to assign to the CallId * @return SerializableCallId A serializable CallId with the given unique id. */
protected SerializableCallId makeSerializableId(long id) {
	return new SerializableCallId(id);
}

/**
 * Look up a Provider's Call Id for a Serializable call id.
 * Return null if no entry found
 */
public CallId providerId(SerializableCallId id) {
	return (CallId)this.getsToCMap().get(id);
}
/**
 * Look up a Serialized Call Id for a Provider's call id
 */
public synchronized SerializableCallId swapId(CallId id) {
	if (id == null)
		return null;

	Map refs = this.getcToSMap();
	
	// look for a Serialized CallId
	SerializableCallId sci = (SerializableCallId)refs.get(id);

	// Lazily create one if necessary
	if (sci == null) {
		// look for a dead one before constructing one
		try {
			sci = (SerializableCallId)this.getFreeRefs().removeFirst();
		} catch (NoSuchElementException nsee) {
			sci = this.makeSerializableId(this.getNextId());
		}

		// add to the maps
		refs.put(id, sci);
		this.getsToCMap().put(sci, id);
	}

	return sci;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "CallId mapper with " + this.getcToSMap().size() + " entries.";
}
}
