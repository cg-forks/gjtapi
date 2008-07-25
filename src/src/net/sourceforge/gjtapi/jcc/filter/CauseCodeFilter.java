package net.sourceforge.gjtapi.jcc.filter;

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
import javax.csapi.cc.jcc.*;
/**
    For all events that require filtering by this {@link EventFilter}, apply the following:
    <ul>
    <li>If the cause code is matched, the filter returns the value matchDisposition. 
    <li>If the cause code is not matched, then return nomatchDisposition.
    </ul>
 * @author: Richard Deadman
 */
public class CauseCodeFilter implements EventFilter {
	private int causeCode = 0;
	private int matchValue = -1;
	private int noMatchValue = -1;

/**
 * AddressRangeFilter constructor comment.
 */
public CauseCodeFilter(int cause, int match, int noMatch) {
	super();

	this.causeCode = cause;
	this.setMatchValue(match);
	this.setNoMatchValue(noMatch);
}
/**
 * Compares two objects for equality. Returns a boolean that indicates
 * whether this object is equivalent to the specified object. This method
 * is used when an object is stored in a hashtable.
 * @param obj the Object to compare with
 * @return true if these Objects are equal; false otherwise.
 * @see java.util.Hashtable
 */
public boolean equals(Object obj) {
	if (obj instanceof CauseCodeFilter) {
		CauseCodeFilter other = (CauseCodeFilter)obj;
		return (this.getCauseCode() == other.getCauseCode()) &&
			(this.getMatchValue() == other.getMatchValue()) &&
			(this.getNoMatchValue() == other.getNoMatchValue());
	}
	return false;
}
/**
 * Return the match disposition value if the event address is in the range.
 */
public int getEventDisposition(JccEvent e) {
	if(e.getCause() == this.getCauseCode())
		return this.getMatchValue();
	else
		return this.getNoMatchValue();
}
/**
 * Get the disposition value to return if the event cause code matches.
 * Creation date: (2000-11-08 12:25:50)
 * @return int
 */
private int getMatchValue() {
	return matchValue;
}
/**
 * Get the disposition value to return if the event cause code doesn't match.
 * Creation date: (2000-11-08 12:25:50)
 * @return int
 */
private int getNoMatchValue() {
	return noMatchValue;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getCauseCode() +
		this.getMatchValue() + this.getNoMatchValue();
}
/**
 * Set the disposition value to return when a cause code match is found for an event.
 * Creation date: (2000-11-08 12:25:50)
 * @param newMatchValue int
 */
private void setMatchValue(int newMatchValue) {
	matchValue = newMatchValue;
}
/**
 * Set the disposition value to return when a cause code match isn't found for an event.
 * Creation date: (2000-11-08 12:25:50)
 * @param newNoMatchValue int
 */
private void setNoMatchValue(int newNoMatchValue) {
	noMatchValue = newNoMatchValue;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Cause Code Filter for " + this.getCauseCode() + " ( " + this.getMatchValue() + ":" + this.getNoMatchValue() + ")";
}
	/**
	 * Gets the causeCode
	 * @return Returns a int
	 */
	public int getCauseCode() {
		return causeCode;
	}

}
