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
 * A JAX-RPC movable state object help as the only parameter to a MovableStateException.
 * This partitioning is required to meet the handling of Exceptions by JAX-RPC.
 */
public class MobileJavaxState {
	final static int PRIVILEGE_VIOLATION = 1;
	final static int RESOURCE_UNAVAILABLE = 2;
	final static int METHOD_NOT_SUPPORTED = 3;
	final static int INVALID_PARTY = 4;
	final static int INVALID_ARGUMENT = 5;

	public int exType = -1;
	public String message = null;
	public int wrappedType = -1;

	/**
	 * Constructor for MobileState.
	 */
	public MobileJavaxState() {
		super();
	}

	public MobileJavaxState(int type, String message, int wrappedType) {
		this();

		this.exType = type;
		this.message = message;
		this.wrappedType = wrappedType;
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public void insertException(PrivilegeViolationException pve) {
		this.exType = PRIVILEGE_VIOLATION;
		this.message = pve.getMessage();
		this.wrappedType = pve.getType();
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public void insertException(ResourceUnavailableException rue) {
		this.exType = RESOURCE_UNAVAILABLE;
		this.message = rue.getMessage();
		this.wrappedType = rue.getType();
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public void insertException(MethodNotSupportedException mnse) {
		this.exType = METHOD_NOT_SUPPORTED;
		this.message = mnse.getMessage();
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public void insertException(InvalidPartyException ipe) {
		this.exType = INVALID_PARTY;
		this.message = ipe.getMessage();
		this.wrappedType = ipe.getType();
	}

	/**
	 * Create a MobileJavaxException by passing in the causing error message.
	 * @param message
	 */
	public void insertException(InvalidArgumentException iae) {
		this.exType = INVALID_ARGUMENT;
		this.message = iae.getMessage();
	}

	/**
	 * Return a newly created copy of the exception
	 * @return A javax.telephony exception I wrap.
	 */
	public Exception getException() {
		switch(this.exType) {
			case PRIVILEGE_VIOLATION: {
				return new PrivilegeViolationException(this.wrappedType, this.message);
			}
			case RESOURCE_UNAVAILABLE: {
				return new ResourceUnavailableException(this.wrappedType, this.message);
			}
			case METHOD_NOT_SUPPORTED: {
				return new MethodNotSupportedException(this.message);
			}
			case INVALID_PARTY: {
				return new InvalidPartyException(this.wrappedType, this.message);
			}
			case INVALID_ARGUMENT: {
				return new InvalidArgumentException(this.message);
			}
		}
		return null;
	}


}
