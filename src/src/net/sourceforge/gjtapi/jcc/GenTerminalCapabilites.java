package net.sourceforge.gjtapi.jcc;
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
/*
 * Created on Nov 5, 2003
 *
 * This is a simple Capabilites package for the Terminal.
 * No other part of Jcc or Jcat has capabilities, so this may
 * disappear in the final draft.
 */

import javax.jcat.JcatTerminalCapabilities;

/**
 * @author rdeadman
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class GenTerminalCapabilites implements JcatTerminalCapabilities {
	private boolean displayText = false;

	GenTerminalCapabilites() {
		super();
	}
	/**
	 * Can I display text?
	 * @see javax.jcat.JcatTerminalCapabilities#canDisplayText()
	 */
	public boolean canDisplayText() {
		return this.displayText;
	}

	/**
	 * Package-level setter of the DisplayText capability
	 * @param flag
	 */
	void setDisplayText(boolean flag) {
		this.displayText = flag;
	}
}
