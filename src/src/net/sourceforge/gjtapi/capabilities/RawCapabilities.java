package net.sourceforge.gjtapi.capabilities;

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
 * This defines the behavioural capabilities of the RawProvider implementation we are hooked up to.
 * This information can be used to avoid unnecessary API calls.
 * <P>For instance, in the default case when a media service is attached to a terminal, the
 * RawProvider will be asked to allocate the required resources for the terminal.  Some providers,
 * however, may always have such resources available.  By setting "allocateMedia" to false, the
 * MediaGroup can determine that the resource allocation call is unecessary and avoid it.  While the
 * RawProvider could just implement "allocateResource(String term, int type)" as a NOP, avoiding the
 * call altogether improves performance, particularily where the RawProvider is separated from the
 * Generic Framework by a proxy RMI bridge.
 * <P>dynamicAddresses notes if new Addresses may be queried for that are not in any returned static
 * set.  It also implies that dynamic terminals may be queried for.
 * <P>RawProvider implementors should return any non-default capabilities as part of the Properties
 * object returned from the "getCapabilities()" method.
 * <P><I>Yes, the variables are public, but this is purely a private data holder anyway.</I>
 * Creation date: (2000-05-04 11:31:40)
 * @author: Richard Deadman
 */
public class RawCapabilities {
	public boolean throttle = true;
	public boolean media = true;
	public boolean allMediaTerminals = false;
	public boolean allocateMedia = true;

	public boolean dynamicAddresses = false;
/**
 * Basic Constructor for building a default RawCapabilities object
 * Creation date: (2000-05-04 12:42:28)
 * @author: Richard Deadman
 */
public RawCapabilities() {}
/**
 * Constructor for building a default RawCapabilities object from a Properties object.
 * Creation date: (2000-05-04 12:42:28)
 * @author: Richard Deadman
 */
public RawCapabilities(java.util.Properties props) {
	this();

	this.throttle = this.toBoolean(props.get(Capabilities.THROTTLE), this.throttle);
	this.media = this.toBoolean(props.get(Capabilities.MEDIA), this.media);
	this.allMediaTerminals = this.toBoolean(props.get(Capabilities.ALL_MEDIA_TERMINALS), this.allMediaTerminals);
	this.allocateMedia = this.toBoolean(props.get(Capabilities.ALLOCATE_MEDIA), this.allocateMedia);

	this.dynamicAddresses = this.toBoolean(props.get(Capabilities.DYNAMIC_ADDRESSES), this.dynamicAddresses);
}
/**
 * Parse an Object and turn it into a boolean, or return the default value if it cannot be parsed.
 * Creation date: (2000-05-04 12:51:13)
 * @author: 
 * @return boolean
 * @param o An Object to convert to a boolean
 * @param defaultValue The dafault value to return if o is null, or not a Boolean or String
 */
private boolean toBoolean(Object o, boolean val) {
	if (o instanceof Boolean || o instanceof String) {
		val = (o instanceof Boolean) ?
			((Boolean)o).booleanValue() :
			Character.toLowerCase(((String)o).charAt(0)) == 't';
	}
	return val;
}
}
