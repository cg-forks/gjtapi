package ca.deadman.gjtapi.raw.remote.webservices;

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;

/*
	Copyright (c) 2003 Richard Deadman, Deadman Consulting (www.deadman.ca)

	All rights reserved.

	This software is dual licenced under the GPL and a commercial license.
	If you wish to use under the GPL, the following license applies, otherwise
	please contact Deadman Consulting at sales@deadman.ca for commercial licensing.

    ---

	This program is free software; you can redistribute it and/or
	modify it under the terms of the GNU General Public License
	as published by the Free Software Foundation; either version 2
	of the License, or (at your option) any later version.

	This program is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with this program; if not, write to the Free Software
	Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
*/
/**
 * @author rdeadman
 *
 * Encapsulates javax.telephony Exceptions in a way JAX-RPC can move.
 */
public class MobileJavaxException extends Exception {

	private MobileJavaxState state = null;

	public MobileJavaxException(MobileJavaxState mjs) {
		setState(mjs);
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public MobileJavaxException(MethodNotSupportedException mnse) {
		super();
		this.state = new MobileJavaxState();
		this.state.insertException(mnse);
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public MobileJavaxException(PrivilegeViolationException pve) {
		super();
		this.state = new MobileJavaxState();
		this.state.insertException(pve);
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public MobileJavaxException(ResourceUnavailableException rue) {
		super();
		this.state = new MobileJavaxState();
		this.state.insertException(rue);
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public MobileJavaxException(InvalidPartyException ipe) {
		super();
		this.state = new MobileJavaxState();
		this.state.insertException(ipe);
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public MobileJavaxException(InvalidArgumentException iae) {
		super();
		this.state = new MobileJavaxState();
		this.state.insertException(iae);
	}

	public String toString() {
		return "JAX-RPC wrapper for remote javax exception of type: " + this.state.wrappedType;
	}

	public void setState(MobileJavaxState state) {
		this.state = state;
	}

	public MobileJavaxState getState() {
		return state;
	}
}
