package net.sourceforge.gjtapi.jcc.filter;

/*
	Copyright (c) 2002 Richard Deadman, Deadman Consulting (www.deadman.ca) 

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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.csapi.cc.jcc.*;
/**
 * This requires a complete ordering of values in JCPAddress. The ordering is arranged by defining the order to be by JCPAddress.getName()'s string order.
For each address in the call obtained by event.getCall(), apply the following. Obtain a string using address.getName(). If
this string matches the regular expression addressRE, the filter returns the value matchDisposition. If no such addresses
are matched, then return nomatchDisposition.
 *
 * <P>I haven't got a regular expression package yet, so getEventDisposition() throws a RuntimeException.
 * Creation date: (2000-11-08 12:59:01)
 * @author: Richard Deadman
 */
public class AddressREFilter implements EventFilter {
	private String regEx = null;
	private Pattern pattern = null; 
	private int match = -1;
	private int noMatch = -1;
/**
 * AddressREFilter constructor comment.
 */
public AddressREFilter(String addressRE, int matchDisposition, int noMatchDisposition) {
	super();

	this.setRegEx(addressRE);
	this.setMatch(matchDisposition);
	this.setNoMatch(noMatchDisposition);
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
	if (obj instanceof AddressREFilter) {
		AddressREFilter af = (AddressREFilter)obj;
		return this.getRegEx().equals(af.getRegEx()) &&
			(this.getMatch() == af.getMatch()) &&
			(this.getNoMatch() == af.getNoMatch());
	}
	return false;
}
/**
 * getEventDisposition method comment.
 */
public int getEventDisposition(JccEvent e) {
	if (e instanceof JccCallEvent) {
		JccConnection[] conns = ((JccCallEvent)e).getCall().getConnections();
		for (int i = 0; i < conns.length; i++) {
			String addr = conns[i].getAddress().getName();
			Matcher m = this.pattern.matcher(addr);
			if (m.matches()) {
					return this.getMatch();
				}
		}
	}
	return this.getNoMatch();
}
/**
 * Get the disposition value to return on a match.
 * Creation date: (2000-11-08 13:00:10)
 * @return int
 */
private int getMatch() {
	return match;
}
/**
 * Get the no-match disposition flag to return on no match.
 * Creation date: (2000-11-08 13:00:10)
 * @return int
 */
private int getNoMatch() {
	return noMatch;
}
/**
 * Get the Regular expression for the match.
 * Creation date: (2000-11-08 13:00:10)
 * @return java.lang.String
 */
private java.lang.String getRegEx() {
	return regEx;
}
/**
 * Generates a hash code for the receiver.
 * This method is supported primarily for
 * hash tables, such as those provided in java.util.
 * @return an integer hash code for the receiver
 * @see java.util.Hashtable
 */
public int hashCode() {
	return this.getRegEx().hashCode() + this.getMatch() + this.getNoMatch();
}
/**
 * Set the match return value.
 * Creation date: (2000-11-08 13:00:10)
 * @param newMatch int
 */
private void setMatch(int newMatch) {
	match = newMatch;
}
/**
 * Set the value to return if no match is found
 * Creation date: (2000-11-08 13:00:10)
 * @param newNoMatch int
 */
private void setNoMatch(int newNoMatch) {
	noMatch = newNoMatch;
}
/**
 * Set the Regular Expression I match on.
 * Creation date: (2000-11-08 13:00:10)
 * @param newRegEx java.lang.String
 */
private void setRegEx(java.lang.String newRegEx) {
	regEx = newRegEx;
	this.pattern = Pattern.compile(newRegEx);
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Regular Expression Filter for: " + this.getRegEx();
}
}
