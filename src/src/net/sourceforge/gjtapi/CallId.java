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
 * An abstraction for a call id.
 * CallIds should implement equals() and hashCode() properly to ensure call identity is preserved.
 * <P>This interface allows different providers to define call ids that hold provider
 * specific information.  An alternative may be to have a raw provider identify calls with unique
 * numbers (longs) and then look up its real call structure when a call action is requested against
 * the id (long).  Using an interface instead provides more flexibility for call identification.  If
 * a raw provider wishes to wrap a (long) id in a CallId implementation, that may be done.  Alternatively
 * the raw provider's own concept of a call object can simply be declared to implement the CallId
 * interface, eliminating the need to map the id to a call object.  For muliplexors, numeric ids are
 * problematic since care must be taken to ensure that two calls from different providers are not given
 * the same id.
 * <P>This is not serializable to reduce implementation requirements.  Management
 * for remote identity should be handled through a remote raw provider proxy.
 *
 * @author: Richard Deadman
 **/

public interface CallId {
}
