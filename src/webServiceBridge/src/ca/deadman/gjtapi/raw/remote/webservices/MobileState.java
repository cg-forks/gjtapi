package ca.deadman.gjtapi.raw.remote.webservices;

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
public class MobileState {
	public long call;
	public String address;
	public String terminal;
	public int type;
	public int state;
	public String info;

	/**
	 * Constructor for MobileState.
	 */
	public MobileState() {
		super();
	}

	public MobileState(long callId, String addr, String term, int eType, int eState, String cause) {
		this();

		this.call = callId;
		this.address = addr;
		this.terminal = term;
		this.type = eType;
		this.state = eState;
		this.info = cause;
	}

}
