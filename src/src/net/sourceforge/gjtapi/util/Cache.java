package net.sourceforge.gjtapi.util;

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
import java.lang.ref.Reference;
import java.util.*;
/**
 * This is an abstract cache Map of keys to values where the values are held in References which will
 * allow them to be collected by a garbage collector when memory gets low.
 * <P>Concreate subclasses define which type of references are used.
 * Creation date: (2000-06-08 12:16:14)
 * @author: Richard Deadman
 */
public abstract class Cache implements Map {
		// The map that does the actual storage
	private HashMap backingStore = new HashMap();
/**
 * Clear myself of entries
 */
public void clear() {
	backingStore.clear();
}
/**
 * Do I have an entry under the given key?
 */
public boolean containsKey(Object key) {
	return backingStore.containsKey(key);
}
/**
 * Does the map contain the given value?
 * <P><B>Note that this may not work if two WeakReferences to the same object do not
 * test as equal.
 */
public boolean containsValue(Object value) {
	return backingStore.containsValue(this.wrap(value));
}
/**
 * Return the set of all entries as instances of Map.Entry.
 */
public Set entrySet() {
	Set set = backingStore.entrySet();
	
	// now dereference entry values -- we extend HashMap whose Map.Entry
	// implementation implements setValue()
	Iterator it = set.iterator();
	while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		entry.setValue(this.unWrap(entry.getValue()));
	}
	return set;
}
/**
 * Get the dereferenced value object, clearing the key if the value is garbage-collected.
 */
public Object get(Object key) {
	Reference ref = (Reference)backingStore.get(key);
	Object o = null;
	if (ref != null) {
		o = ref.get();
		if (o == null)
			this.remove(key);
	}
	return o;
}
/**
 * Do I have any entries left
 */
public boolean isEmpty() {
	return backingStore.isEmpty();
}
/**
 * Return the keyset of this map
 */
public Set keySet() {
	return backingStore.keySet();
}
/**
 * put method comment.
 */
public Object put(Object key, Object value) {
	return backingStore.put(key, this.wrap(value));
}
/**
 * putAll method comment.
 */
public void putAll(Map t) {
	Set set = t.entrySet();
	
	Iterator it = set.iterator();
	while (it.hasNext()) {
		Map.Entry entry = (Map.Entry)it.next();
		this.put(entry.getKey(), entry.getValue());
	}
}
/**
 * Remove an entry from the map.
 */
public Object remove(Object key) {
	return backingStore.remove(key);
}
/**
 * How many entries do I have
 */
public int size() {
	return backingStore.size();
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "A HashMap with reference values: " + backingStore.toString();
}
/**
 * If the value is a Refernece, return its referent, otherwise return it.
 */
private Object unWrap(Object value) {
	if (value instanceof Reference)
		return ((Reference)value).get();
	else
		return value;
}
/**
 * values method comment.
 */
public Collection values() {
	Collection coll = backingStore.values();
	Collection newColl = new HashSet();

		// copy each item over while dereferencing all references
	Iterator it = coll.iterator();
	while (it.hasNext()) {
		Object o = it.next();
		if (o instanceof Reference)
			newColl.add(((Reference)o).get());
		else
			newColl.add(o);	// shouldn't occur
	}
	return newColl;
}
/**
 * If the value is not a SoftReference, wrap it in one.
 */
protected abstract Reference wrap(Object value);
}
