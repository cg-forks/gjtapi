package ca.deadman.gjtapi.raw.remote.webservices;
import javax.telephony.media.Symbol;

import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.media.SymbolConvertor;

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
 * This is a XML-RPC serializable object that holds the information needed to reccreate an event on the far side.
 * Due to the way XML-RPC handles serialization, we need to include all possible information here.
 * <P>XML-RPC allows objects with empty constuctors and public (or getter/setter) attributes, as
 * long as all attributes are also XML-RPC serializable.
 *
 * @author Richard Deadman (rdeadman)
 *
 */
public class EventHolder {
	public long callId;
	public String address;
	public String terminal;
	public int evId;
	public int cause;

	public int mediaEv;
	public String signals;
	public int mediaIndex;
	public int mediaTrigger;
	public int mediaOffset;
	public int mediaDuration;

	// raw signal detect event qualifier
	public int mediaQualifier;
	public int mediaError;

	/**
	 * Constructor for EventHolder, needed for JAX-RPC serialization.
	 */
	public EventHolder() {
		super();
	}

	/**
	 * Call Event constructor
	 * @param call The unique id for the call.
	 * @param ev The id of the event.
	 * @param eCause The cuase field for the event.
	 */
	public EventHolder(long call, int ev, int eCause) {
		super();

		this.callId = call;
		this.evId = ev;
		this.cause = eCause;
	}

	/**
	 * Connection Event constructor
	 * @param call The unique id for the call.
	 * @param ev The id of the event.
	 * @param eCause The cuase field for the event.
	 */
	public EventHolder(long call, String addr, int ev, int eCause) {
		this(call, ev, eCause);

		this.address = addr;
	}

	/**
	 * TerminalConnection Event constructor
	 * @param call The unique id for the call.
	 * @param ev The id of the event.
	 * @param eCause The cuase field for the event.
	 */
	public EventHolder(long call, String addr, String term, int ev, int eCause) {
		this(call, addr, ev, eCause);

		this.terminal = term;
	}

	/**
	 * Create an Event from my data.
	 * @return Event
	 */
	public RawSigDetectEvent toRawSigDetectEvent() {
		Symbol[] syms = SymbolConvertor.convert(this.signals);
		int siz = syms.length;
		int[] sigs = new int[siz];
		for (int i = 0; i < siz; i++)
			sigs[i] = syms[i].hashCode();
		return RawSigDetectEvent.create(this.terminal, this.mediaQualifier, sigs, this.mediaIndex, this.mediaTrigger, this.mediaError);
	}

	/**
	 * Describe myself.
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		return "Event Holder for type: " + this.evId + " on call id: " + this.callId;
	}
}
