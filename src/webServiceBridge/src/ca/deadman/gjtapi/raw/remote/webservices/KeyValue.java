package ca.deadman.gjtapi.raw.remote.webservices;

import java.io.Serializable;
/**
 * Simple class to allow serailization of dictionary-like objects.
 * @author rdeadman
 *
 */
public class KeyValue implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = -1647603902826584335L;
	public String key;
	public String value;
}
