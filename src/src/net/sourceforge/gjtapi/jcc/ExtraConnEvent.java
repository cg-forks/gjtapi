package net.sourceforge.gjtapi.jcc;

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
 * Special purpose event for a Connection created event that is not reflected in JTAPI.
 * Creation date: (2000-11-15 14:34:09)
 * @author: Richard Deadman
 */
public class ExtraConnEvent extends CreatedCallEvent implements JccConnectionEvent {
	private JccConnection connection = null;
	private int id;
	private int cause;
/**
 * Create the event.
 * @param conn The Connection I hold an event for.
 */
public ExtraConnEvent(JccConnection conn, int id, int cause) {
	super(conn.getCall());

	this.connection = conn;
	this.id = id;
	this.cause = cause;
}
/**
 * getCause method comment.
 */
public int getCause() {
	return this.cause;
}
/**
 * getConnection method comment.
 */
public JccConnection getConnection() {
	return this.connection;
}
/**
 * getID method comment.
 */
public int getID() {
	return this.id;
}
/**
 * Describe myself
 * @return a string representation of the receiver
 */
public String toString() {
	return "Connection event for: " + this.getConnection();
}
}
