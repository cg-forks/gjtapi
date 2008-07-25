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
 * This filter takes as input an array of EventFilters. For a given event, it applies the
 * filters in order. If the values returned from all filters are the same, then this
value is returned as the filter value. Otherwise, the filter returns nomatchDisposition. This means that as soon as any filter
returns nomatchDisposition, or as soon as two filters return different values, the filter can immediately return
nomatchDisposition. This would normally be called with nomatchDisposition set to EVENT_DISCARD to discard any
events that any filter wants to discard (logical AND).
 * Creation date: (2000-11-08 14:22:53)
 * @author: Richard Deadman
 */
public class AndFilter implements EventFilter {
	private EventFilter[] filters = null;
	private int noMatch = -1;
/**
 * OrFilter constructor comment.
 */
public AndFilter(EventFilter[] filters, int noMatchDisposition) {
	super();

	this.setFilters(filters);
	this.setNoMatch(noMatchDisposition);
}
/**
 * getEventDisposition method comment.
 */
public int getEventDisposition(JccEvent e) {
	EventFilter[] flts = this.getFilters();
	int noMatch = this.getNoMatch();
	int prev = EVENT_DISCARD;	// default action if no filters
	
	for(int i = 0; i < flts.length; i++) {
		int res = flts[i].getEventDisposition(e);
		if (res == noMatch)		// check if we know that all will be noMatch or one won't!
			return res;
		if ((i > 0) && (res != prev))	// check if this is not the same as the last.
			return noMatch;
		prev = res;				// store for next filter
	}
	return prev;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-08 14:28:09)
 * @return jain.application.services.jcc.EventFilter[]
 */
private EventFilter[] getFilters() {
	return filters;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-08 14:28:09)
 * @return int
 */
private int getNoMatch() {
	return noMatch;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-08 14:28:09)
 * @param newFilters jain.application.services.jcc.EventFilter[]
 */
private void setFilters(EventFilter[] newFilters) {
	filters = newFilters;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-08 14:28:09)
 * @param newNoMatch int
 */
private void setNoMatch(int newNoMatch) {
	noMatch = newNoMatch;
}
/**
 * Describe myself.
 * @return a string representation of the receiver
 */
public String toString() {
	return "And event filter for: " + this.getFilters();
}
}
