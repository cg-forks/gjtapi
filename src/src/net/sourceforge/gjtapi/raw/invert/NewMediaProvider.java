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
import net.sourceforge.gjtapi.*;
import javax.telephony.*;
import javax.telephony.media.*;
import java.util.*;
import net.sourceforge.gjtapi.TelephonyProvider;
/**
 * This is a concrete Jtapi Inverter provider for the Generic Framework that delegates through the
 * 1.3 Media MediaService implementation.
 * Creation date: (2000-06-06 23:30:34)
 * @author: Richard Deadman
 */
public class NewMediaProvider extends InverterProvider {
	private static int BIND_TIMEOUT = 500;	// bind timeout in milliseconds
	
	private Map<String, MediaService> serviceMap = new HashMap<String, MediaService>();
/**
 * NewMediaGenProvider constructor comment.
 */
public NewMediaProvider() {
	super();
}
/**
 * allocateMedia method comment.
 */
@SuppressWarnings("unchecked")
public boolean allocateMedia(String terminal, int type, java.util.Dictionary resourceArgs) {
	// first see if it is already allocated.
	MediaService ms = this.getService(terminal);
	if (ms == null) {
		// create the service
		Provider prov = this.getJtapiProv();
		ms = this.createService((MediaProvider)prov);
		ResourceSpec[] resSpec = new ResourceSpec[4];
		resSpec[0] = ResourceSpec.basicPlayer;
		resSpec[1] = ResourceSpec.basicRecorder;
		resSpec[2] = ResourceSpec.basicSignalDetector;
		resSpec[4] = ResourceSpec.basicSignalGenerator;
		ConfigSpec spec = new ConfigSpec(resSpec, NewMediaProvider.BIND_TIMEOUT, null, resourceArgs, null);
		try {
			ms.bindToTerminal(spec, prov.getTerminal(terminal));
		} catch (MediaBindException mbe) {
			return false;
		} catch (MediaConfigException mce) {
			throw new RuntimeException("Bind configuration exception!");
		} catch (InvalidArgumentException iae) {
			throw new RuntimeException("Terminal unknown: " + terminal);
		}
	} else {
		// update the dictionary
		ms.setParameters(resourceArgs);
	}
	
	// now store the service
	this.getServiceMap().put(terminal, ms);
	return true;
}
/**
 * Factory to create a MediaService.
 * Subclasses may override this to use different sorts of MediaServices.
 * Creation date: (2000-06-07 9:59:28)
 * @author: Richard Deadman
 * @return A MediaService that supports players, recorders, signal generators and signal detectors.
 * @param prov The JATPI Media Provider.
 */
protected MediaService createService(MediaProvider prov) {
	return new BasicMediaService(prov);
}
/**
 * This will free the MediaService that is handling my media calls.
 * Note that under the JTAPI specification, this will cause the Connection to
 * the Terminal to be disconnected, which we may not want.
 */
public boolean freeMedia(String terminal, int type) {
	MediaService ms = this.getService(terminal);
	if (ms == null)
		return false;
	else
		if (type == TelephonyProvider.MEDIA_RES_ALL) {
			if (this.mediaFreeRelease())
				try {
					ms.release();
				} catch (MediaBindException mbe) {
					return false;
				}
			// remove from map
			this.getServiceMap().remove(terminal);
		} else {
			// ignore partial resource freeing
		}
	return true;
}
/**
 * Find a service for a given terminal
 * Creation date: (2000-06-07 9:30:29)
 * @author: Richard Deadman
 * @param The terinal name
 * @return The MediaService controlling the terminal, or null
 */
private MediaService getService(String terminal) {
	return (MediaService)this.getServiceMap().get(terminal);
}
/**
 * Internal accessor for the service map.
 * Creation date: (2000-06-07 9:30:29)
 * @author: Richard Deadman
 * @return A Map of terminal names to MediaServices.
 */
private Map<String, MediaService> getServiceMap() {
	return this.serviceMap;
}
/**
 * play method comment.
 */
@SuppressWarnings("unchecked")
public void play(String terminal, java.lang.String[] streamIds, int offset, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws MediaResourceException {
	MediaService ms = this.getService(terminal);
	if (ms != null && ms instanceof Player) {
		((Player)ms).play(streamIds, offset, rtcs, optArgs);
	} else {
		throw new MediaResourceException("No MediaService for terminal: " + terminal);
	}
}
/**
 * record method comment.
 */
@SuppressWarnings("unchecked")
public void record(String terminal, String streamId, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws MediaResourceException {
	MediaService ms = this.getService(terminal);
	if (ms != null && ms instanceof Recorder) {
		((Recorder)ms).record(streamId, rtcs, optArgs);
	} else {
		throw new MediaResourceException("No MediaService for terminal: " + terminal);
	}
}
/**
 * retrieveSignals method comment.
 */
@SuppressWarnings("unchecked")
public RawSigDetectEvent retrieveSignals(String terminal, int num, Symbol[] patterns, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
	MediaService ms = this.getService(terminal);
	if (ms != null && ms instanceof SignalDetector) {
		return RawSigDetectEvent.create(((SignalDetector) ms).retrieveSignals(num, patterns, rtcs, optArgs));
	} else {
		throw new MediaResourceException("No MediaService for terminal: " + terminal);
	}
}
/**
 * sendSignals method comment.
 */
@SuppressWarnings("unchecked")
public void sendSignals(String terminal, Symbol[] syms, RTC[] rtcs, Dictionary optArgs) throws MediaResourceException {
	MediaService ms = this.getService(terminal);
	if (ms != null && ms instanceof SignalGenerator) {
		((SignalGenerator) ms).sendSignals(syms, rtcs, optArgs);
	} else {
		throw new MediaResourceException("No MediaService for terminal: " + terminal);
	}
}
/**
 * Stop any action on a MediaService bound to this terminal, otherwise silently ignore.
 */
public void stop(String terminal) {
	MediaService ms = this.getService(terminal);
	if (ms != null) {
		ms.stop();
	}
}
/**
 * Trigger an RTC action on the MediaService if one is bound to this terminal.
 * Otherwise silently ignore.
 */
public void triggerRTC(String terminal, Symbol action) {
	MediaService ms = this.getService(terminal);
	if (ms != null) {
		ms.triggerRTC(action);
	}
}
}
