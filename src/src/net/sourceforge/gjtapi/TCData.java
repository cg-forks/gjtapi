package net.sourceforge.gjtapi;

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
 * This is a simple Terminal Connection data carrier used for call snapshot delivery from dynamic
 * or throttled TelephonyProviders.  This is contained within a ConnectionData object that it connects
 * to.
 * <P>The state given should be of type CallControlTerminalConnection state public final variables.
 * Creation date: (2000-06-15 14:05:44)
 * @author: Richard Deadman
 */
public class TCData implements java.io.Serializable {
	static final long serialVersionUID = 8943763315867425595L;
	
	public int tcState;
	public TermData terminal;	// terminal name and media information
/**
 * Simple constructor for a TerminalConnection data holder.
 * Creation date: (2000-06-23 12:34:51)
 * @author: Richard Deadman
 * @param state The javax.telephony.callcontrol.CallControlTerminalConnection state of the TerminalConnection.
 * @param term A Terminal data holder.
 */
public TCData(int state, TermData term) {
	super();
	
	this.tcState = state;
	this.terminal = term;
}

/**
 * No-arg constructor required for JAX-RPC serialization.
 */
public TCData() {
	super();
}
}
