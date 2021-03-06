package ca.deadman.gjtapi.raw.remote.webservices;

import javax.telephony.media.Symbol;

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
 * A JAX-RPC movable object that encapsulates information needed to create a ResourceEvent.
 */
public class MobileResourceEvent {
	public int type;	// The ResourceEvent sub-type being modelled
	public int id;
	public String termName;
	public int error;
	public int qualifier;
	public int rtcTrigger;

	public int playerChange;
	public int index;
	public int offset;

	public int duration;

	public int[] buffer;

	/**
	 * Empty Constructor for MobileResourceEvent needed by JAX-RPC
	 */
	public MobileResourceEvent() {
		super();
	}

	/**
	 * Full Constructor for MobileResourceEvent.
	 */
	public MobileResourceEvent(int reType, Symbol evId, String term, Symbol err, Symbol qual, Symbol trigger) {
		this();

		this.type = reType;
		this.id = evId.hashCode();
		this.termName = term;
		this.error = err.hashCode();
		this.qualifier = qual.hashCode();
		this.rtcTrigger = trigger.hashCode();
	}

}
