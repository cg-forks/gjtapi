package ca.deadman.gjtapi.raw.remote.wsservices;

/*
	Copyright (c) 2008 Richard Deadman (www.deadman.ca)

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

import javax.telephony.InvalidArgumentException;
import javax.telephony.InvalidPartyException;
import javax.telephony.MethodNotSupportedException;
import javax.telephony.PrivilegeViolationException;
import javax.telephony.ResourceUnavailableException;

import ca.deadman.gjtapi.raw.remote.webservices.MobileJavaxState;

/**
 * @author rdeadman
 *
 * A JAX-WS movable state object help as the only parameter to a MovableStateException.
 * This partitioning is required to meet the handling of Exceptions by JAX-RPC.
 */
public class MobileJavaxStateHelper {
	final static int PRIVILEGE_VIOLATION = 1;
	final static int RESOURCE_UNAVAILABLE = 2;
	final static int METHOD_NOT_SUPPORTED = 3;
	final static int INVALID_PARTY = 4;
	final static int INVALID_ARGUMENT = 5;

	/**
	 * Constructor for MobileStateHelper.
	 */
	public MobileJavaxStateHelper() {
		super();
	}

	/**
	 * Return a newly created copy of the exception
	 * @return A javax.telephony exception I wrap.
	 */
	public Exception getException(MobileJavaxState st) {
		switch(st.getExType()) {
			case PRIVILEGE_VIOLATION: {
				return new PrivilegeViolationException(st.getWrappedType(), st.getMessage());
			}
			case RESOURCE_UNAVAILABLE: {
				return new ResourceUnavailableException(st.getWrappedType(), st.getMessage());
			}
			case METHOD_NOT_SUPPORTED: {
				return new MethodNotSupportedException(st.getMessage());
			}
			case INVALID_PARTY: {
				return new InvalidPartyException(st.getWrappedType(), st.getMessage());
			}
			case INVALID_ARGUMENT: {
				return new InvalidArgumentException(st.getMessage());
			}
		}
		return null;
	}


}
