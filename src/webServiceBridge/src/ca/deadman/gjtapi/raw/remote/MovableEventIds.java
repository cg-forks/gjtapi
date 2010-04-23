package ca.deadman.gjtapi.raw.remote;

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
 * Simple class that holds the JAX-RPC movable event ids.
 * <P>This is separated from EventHolder since JAX-RPC movable objects
 * should not contain static final variables.
 */
public class MovableEventIds {

	// media event ids
	public final static int MEDIA_DTMF_DETECT = 1;
	public final static int MEDIA_DTMF_OVERFLOW = 2;
	public final static int MEDIA_DTMF_PATTERNMATCH = 3;
	public final static int MEDIA_PLAY_PAUSE = 4;
	public final static int MEDIA_PLAY_RESUME = 5;
	public final static int MEDIA_RECORD_PAUSE = 6;
	public final static int MEDIA_RECORD_RESUME = 7;

	public final static int INITIAL_TIMEOUT = 8;
	public final static int SIG_TIMEOUT = 9;
	public final static int MAX_DETECTED = 10;
	public final static int PATTERN_MATCHED = 11;
	public final static int RTC_STOPPED = 12;
	public final static int TIMEOUT = 13;

	// other ids without JTAPI 1.2 equivalents
	public final static int OVERLOAD_ENCOUNTERED = 14;
	public final static int OVERLOAD_CEASED = 15;
	public final static int ADDRESS_ANALYZE = 16;
	public final static int ADDRESS_COLLECT = 17;
	public final static int CONN_AUTH_CALL_ATTEMPT = 18;
	public final static int CONN_CALL_DELIVERY = 19;
	public final static int CONN_SUSPENDED = 20;

	// switch flags for MediaResourceEvents
	public final static int MEDIA_PLAY = 21;
	public final static int MEDIA_RECORD = 22;
	public final static int MEDIA_DETECT = 23;
	public final static int MEDIA_GENERATE = 24;

}
