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
 * This is a simple Terminal data carrier used to group terminal name and media capability information.
 * This may be contained within a TCData object or returned from "TelephonyProvider:getTerminal[s]()".
 * Creation date: (2000-06-21 10:05:44)
 * @author: Richard Deadman
 */
public class TermData implements java.io.Serializable {
	static final long serialVersionUID = -5353564600885755166L;
	
	public String terminal;
	public boolean isMedia;		// does this terminal support media
/**
 * Simple constructor for a collector of Terminal data.
 * Creation date: (2000-06-22 15:39:32)
 * @author: Richard Deadman
 * @param termName The represented Terminal's name
 * @param isMedia Does this terminal support media?
 */
public TermData(String termName, boolean isMedia) {
	super();
	
	this.terminal = termName;
	this.isMedia = isMedia;
}

/**
 * No-arg constructor required for JAX-RPC serialization.
 */
public TermData() {
	super();
}
}
