package ca.deadman.gjtapi.raw.remote.webservices;

import javax.telephony.media.RTC;
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
 * Holder for RTC information used by JTAPI media to map trigger symbols to action symbols.
 * <P>Here ints are used to hold the Symbol's identification and allow the Symbol to be recreated
 * at the far end of the RPC call.
 */
public class RTCPair {
	/**
	 * The id for the trigger Symbol.
	 */
	public int trigger;

	/**
	 * The id for the action symbol.
	 */
	public int action;

	/**
	 * Constructor for RTCPair.
	 */
	public RTCPair() {
		super();
	}

	public RTCPair(RTC rtc) {
		super();

		this.trigger = rtc.getTrigger().hashCode();
		this.action = rtc.getAction().hashCode();
	}

	public RTC toRTC() {
		return new RTC(Symbol.getSymbol(trigger), Symbol.getSymbol(action));
	}

}
