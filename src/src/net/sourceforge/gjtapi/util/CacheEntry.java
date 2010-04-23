package net.sourceforge.gjtapi.util;

import java.util.Map.Entry;


/**
 * And entry that mirrors the key-Reference<value> one
 * 
 * @author rdeadman
 *
 * @param <K>
 * @param <V>
 */
public class CacheEntry<K, V> implements Entry<K, V> {
	private K key;
	private V value;
	
	public void setKey(K k) {
		this.key = k;
	}
	
	public K getKey() {
		return key;
	}

	public V getValue() {
		return value;
	}

	public V setValue(V value) {
		V oldValue = this.value;
		this.value = value;
		return oldValue;
	}
	
}
