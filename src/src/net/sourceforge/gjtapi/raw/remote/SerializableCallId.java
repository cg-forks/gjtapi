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
import java.io.Serializable;
/**
 * This is a lookup CallId that can be sent across a serailizable wire and back.  It can be used as a
 * safe mobile handle to match against a provider's arbitrary CallId in a CallMapper instance.
 * Creation date: (2000-02-17 22:58:52)
 * @author: Richard Deadman
 */
public class SerializableCallId implements CallId, Serializable {
	static final long serialVersionUID = 8561783071688424308L;
	
	private long id;
/**
 * Create an call id with a set identifier.
 * Creation date: (2000-02-17 23:02:15)
 * @author: Richard Deadman
 * @param id A unique id for the life of the CallId
 */
public SerializableCallId(long id) {
	super();
	
	this.setId(id);
}
/**
 * Null constructor used by subclass for XML-RPC "serialization"
 */
protected SerializableCallId() {
	super();
}
/**
 * Determine if I am logically equal to another object
 * Creation date: (2000-02-17 23:04:53)
 * @author: Richard Deadman
 * @return true if I am equal, false otherwise
 * @param o The object to compare with
 */
public boolean equals(Object o) {
	if (o instanceof SerializableCallId && this.getId() == ((SerializableCallId) o).getId())
		return true;
	return false;
}
/**
 * Get the long id for the call id.
 * Creation date: (2000-02-17 22:59:17)
 * @author: Richard Deadman
 * @return long
 */
protected long getId() {
	return id;
}
/**
 * Return a hashcode used by hashtables.
 * Creation date: (2000-02-17 23:09:02)
 * @author: Richard Deadman
 * @return int
 */
public int hashCode() {
	return (int)this.getId();
}
/**
 * Set the identifier id.
 * Creation date: (2000-02-17 22:59:17)
 * @author: 
 * @param newId long
 */
protected void setId(long newId) {
	id = newId;
}
}
