package net.sourceforge.gjtapi.jcc;

/*
	Copyright (c) 2002 Deadman Consulting (www.deadman.ca) 

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
 * Special purpose event for a Call created event that is not reflected in
 * JTAPI.
 * Creation date: (2000-11-14 14:31:46)
 * @author: Richard Deadman
 */
public class CreatedCallEvent implements JccCallEvent {
	private JccCall call = null;
/**
 * CreatedCallEvent constructor comment.
 */
public CreatedCallEvent(JccCall c) {
	super();

	this.call = c;
}
/**
 * getCall method comment.
 */
public JccCall getCall() {
	return this.call;
}
/**
 * getCause method comment.
 */
public int getCause() {
	return JccCallEvent.CALL_CREATED;
}
/**
 * getID method comment.
 */
public int getID() {
		// must use JcpCallEvent constant due to Jcc 1.0b error
	return JccCallEvent.CAUSE_NEW_CALL;
}
/**
 * getSource method comment.
 */
public Object getSource() {
	return this.getCall();
}
/**
 * Describe myself.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Call created event: " + this.getCall();
}
}
