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

package net.sourceforge.gjtapi;

import net.sourceforge.gjtapi.raw.*;

/**
 * This is the pluggable interface that low-level protocol
 * or vendor providers can implement, often known as a Service Provider 
 * Interface or SPI.
 * Alternatively this may be referred to as the RawProvider interface or RPI, 
 * although technically the Generic SPI or RPI also involves the definition of 
 * the helper classes, such as FreeCallEvent and RawObserver.
 * 
 * <P>Each provider is associated in the ProviderManager with a unique name. 
 * This allows the JTapi Provider to select the correct Peer based on
 * input parameters.  Thus when the GenericJtapiPeer is sent the message
 * JTapiPeer.getProvider(String name), the GenericJTapiPeer can look
 * for a provider by a name in that string, load it and plug it into the generic
 * JTapi classes.
 **/
public interface TelephonyProvider extends FullJtapiTpi, JccTpi {
}
