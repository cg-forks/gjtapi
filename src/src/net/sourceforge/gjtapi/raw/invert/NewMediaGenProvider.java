package net.sourceforge.gjtapi.raw.invert;

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
import net.sourceforge.gjtapi.media.GenericMediaService;
import javax.telephony.media.*;
/**
 * This is a concrete Jtapi Inverter provider for the Generic Framework that delegates through the
 * Generic JTAPI Framework GenericMediaService implementation.
 * Creation date: (2000-06-06 23:30:34)
 * @author: Richard Deadman
 */
public class NewMediaGenProvider extends NewMediaProvider {
/**
 * Delegate of to super class.
 */
public NewMediaGenProvider() {
	super();
}
/**
 * Factory to create a MediaService.
 * Here we hook up to a Generic JTAPI Framework replacement for the broken BasicMediaService.
 * Creation date: (2000-06-07 9:59:28)
 * @author: Richard Deadman
 * @return A MediaService that supports players, recorders, signal generators and signal detectors.
 * @param prov The JATPI Media Provider.
 */
protected MediaService createService(MediaProvider prov) {
	return new GenericMediaService(prov);
}
}
