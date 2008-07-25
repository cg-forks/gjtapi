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
 * This filter takes two arrays of
eventID integers (values returned from event.getID()). For event IDs in the blockEvents array, the filter returns
EVENT_BLOCK. For event IDs in notifyEvents, the filter returns EVENT_NOTIFY. If any event ID is not listed in one
of the three arrays, the filter returns EVENT_DISCARD. The application is supposed to ensure that an event ID is not
listed in more than one array. If done, the filter may return any one of the listed event dispositions.
 * Creation date: (2000-11-08 12:45:35)
 * @author: Richard Deadman
 */
public class EventSetFilter implements EventFilter {
	private int[] blockSet;
	private int[] notifySet;
/**
 * EventSetFilter constructor comment.
 */
public EventSetFilter(int[] blockEvents, int[] notifyEvents) {
	super();

	this.setBlockSet(blockEvents);
	this.setNotifySet(notifyEvents);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-08 12:50:01)
 * @return int[]
 */
private int[] getBlockSet() {
	return blockSet;
}
/**
 * getEventDisposition method comment.
 */
public int getEventDisposition(JccEvent e) {
	int id = e.getID();

	int[] set = this.getBlockSet();
	for (int i = 0; i < set.length; i++) {
		if (id == set[i])
			return EVENT_BLOCK;
	}

	set = this.getNotifySet();
	for (int i = 0; i < set.length; i++) {
		if (id == set[i])
			return EVENT_NOTIFY;
	}

	return EVENT_DISCARD;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-08 12:50:01)
 * @return int[]
 */
private int[] getNotifySet() {
	return notifySet;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-08 12:50:01)
 * @param newBlockSet int[]
 */
private void setBlockSet(int[] newBlockSet) {
	blockSet = newBlockSet;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-11-08 12:50:01)
 * @param newNotifySet int[]
 */
private void setNotifySet(int[] newNotifySet) {
	notifySet = newNotifySet;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "EventSet Filter (Block: " + this.getBlockSet() + ", Notify: " + this.getNotifySet() + ")";
}
}
