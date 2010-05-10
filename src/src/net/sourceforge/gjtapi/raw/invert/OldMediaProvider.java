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
import javax.telephony.*;
import javax.telephony.media.*;
import java.util.*;
/**
 * This is a concrete Jtapi Inverter Provider for the Generic JTAPI Framework that delegates
 * through the old 1.2 media methods.
 * Creation date: (2000-06-06 23:27:24)
 * @author: Richard Deadman
 */
@SuppressWarnings("deprecation")
public class OldMediaProvider extends InverterProvider {

	/**
	 * This manages synchronous TelephonyProvider request threads with the asynchronous
	 * model of the 1.2 JTAPI media model.
	 **/
	class RequestManager {
		private String termName = null;
		private int state = MediaTerminalConnection.NOACTIVITY;
		
		private Thread playThread = null;
		private Thread recordThread = null;
		private Thread retrieveThread = null;
		private int maxBufferSize = 0;
		private StringBuffer buf = new StringBuffer();

		RequestManager(String term) {
			super();

			this.termName = term;
		}

		synchronized void registerPlayThread(Thread pt) {
			this.playThread = pt;
			this.state |= MediaTerminalConnection.PLAYING;

			synchronized(pt) {
				try {
					pt.wait();
				} catch (InterruptedException ie) {
					// continue on then
				}
			}
		}

		synchronized Thread releasePlayThread() {
			Thread pt = this.playThread;
			if (pt != null) {
				synchronized (pt) {
					pt.notify();
				}
				this.playThread = null;
				this.removeMediaState(MediaTerminalConnection.PLAYING);
				this.cleanUp();
			}
			return pt;
		}

		synchronized void registerRecordThread(Thread rt) {
			this.recordThread = rt;
			this.state |= MediaTerminalConnection.RECORDING;

			synchronized(rt) {
				try {
					rt.wait();
				} catch (InterruptedException ie) {
					// continue on then
				}
			}
		}

		synchronized Thread releaseRecordThread() {
			Thread rt = this.recordThread;
			if (rt != null) {
				synchronized (rt) {
					rt.notify();
				}
				this.recordThread = null;
				this.removeMediaState(MediaTerminalConnection.RECORDING);
				this.cleanUp();
			}
			return rt;
		}

		synchronized void registerRetrieveThread(Thread rt, int bufSize) {
			this.retrieveThread = rt;
			this.maxBufferSize = bufSize;

			synchronized(rt) {
				try {
					rt.wait();
				} catch (InterruptedException ie) {
					// continue on then
				}
			}
		}

		synchronized Thread releaseRetrieveThread() {
			Thread rt = this.retrieveThread;
			if (rt != null) {
				synchronized (rt) {
					rt.notify();
				}
				this.retrieveThread = null;
				this.cleanUp();
			}
			return rt;
		}

		private void cleanUp() {
			if (this.playThread == null &&
				this.recordThread == null &&
				this.retrieveThread == null) {
				getActiveRequests().remove(this.getTerminal());
				}
		}

		private String getTerminal() {
			return this.termName;
		}

		String getDtmfs() {
			String results = this.buf.toString();
			this.buf.setLength(0);
			return results;
		}

		private void removeMediaState(int removedState) {
			this.state &= (Integer.MAX_VALUE - removedState);
		}
		
		synchronized void updateState(int newState) {
			if (((this.state | MediaTerminalConnection.PLAYING) > 0) &&
				((newState | MediaTerminalConnection.PLAYING) == 0)) {
				this.releasePlayThread();
				}
			if (((this.state | MediaTerminalConnection.RECORDING) > 0) &&
				((newState | MediaTerminalConnection.RECORDING) == 0)) {
				this.releaseRecordThread();
				}
			// now update state
			this.state = newState;
		}

		synchronized void takeDtmf(char dtmfChar) {
			StringBuffer b = this.buf;
			b.append(dtmfChar);
			if (b.length() >= this.maxBufferSize) {
				this.releaseRetrieveThread();
			}
		}
	}
	// map of terminal names to active request sets
	private Map<String, RequestManager> activeRequests = new HashMap<String, RequestManager>();
/**
 * OldMediaProvider constructor comment.
 */
public OldMediaProvider() {
	super();
}
/**
 * This may safely be ignored.
 */
@SuppressWarnings("unchecked")
public boolean allocateMedia(String terminal, int type, java.util.Dictionary resourceArgs) {
	return true;
}
/**
 * This may safely be ignored.
 */
public boolean freeMedia(String terminal, int type) {
	return true;
}
/**
 * Internal accessor for the active request map.
 * Creation date: (2000-06-07 14:20:10)
 * @author: Richard Deadman
 * @return java.util.Map
 */
private Map<String, RequestManager> getActiveRequests() {
	return activeRequests;
}
/**
 * Add to the abstract base capabilities a note that media is not supported.
 */
public java.util.Properties getCapabilities() {
	java.util.Properties props = super.getCapabilities();

	javax.telephony.capabilities.TerminalConnectionCapabilities tcCap = this.getJtapiProv().getTerminalConnectionCapabilities();
	boolean media = (tcCap instanceof javax.telephony.media.capabilities.MediaTerminalConnectionCapabilities);
	props.put(net.sourceforge.gjtapi.capabilities.Capabilities.MEDIA, new Boolean(media));
	return props;
}
/**
 * Resolve which MediaTerminalConnection has active media for a terminal name.
 * The first active MediaTerminalConnection found for the terminal is assumed
 * to support the media request.
 * Creation date: (2000-06-07 11:14:37)
 * @author: Richard Deadman
 * @return An instance of MediaTerminalConnection, or null.
 * @param termName The name of a terminal
 */
private MediaTerminalConnection getMTC(String termName) {
	MediaTerminalConnection mtc = null;
	Terminal term = null;
	
	try {
		term = this.getJtapiProv().getTerminal(termName);
	} catch (InvalidArgumentException iae) {
		return null;
	}
	TerminalConnection[] tcs = term.getTerminalConnections();
	for (int i = 0; i < tcs.length && mtc == null; i++) {
		if (tcs[i] instanceof MediaTerminalConnection && tcs[i].getState() == TerminalConnection.ACTIVE)
			mtc = (MediaTerminalConnection) tcs[i];
	}
	return mtc;
}
/**
 * Return a RequestManager associated with a certain terminal, or create one if necessary.
 * Creation date: (2000-06-07 15:01:23)
 * @author: Richard Deadman
 */
private RequestManager getRequestManager(String term) {
	Map<String, RequestManager> activeRms = this.getActiveRequests();
	RequestManager rm = (RequestManager)activeRms.get(term);
	if (rm == null) {
		rm = new RequestManager(term);
		activeRms.put(term, rm);
	}

	return rm;
}
/**
 * Play the first stream id only.
 * In future we would need a way of turning a collection of streamIds into one URL, possibly
 * through an external media source manager.
 */
@SuppressWarnings("unchecked")
public void play(String terminal, java.lang.String[] streamIds, int offset, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws MediaResourceException {
	MediaTerminalConnection mtc = this.getMTC(terminal);
	if (mtc != null) {
		// start playing
		try {
			mtc.usePlayURL(new java.net.URL(streamIds[0]));
			mtc.startPlaying();
		} catch (Exception e) {
			throw new MediaResourceException("Low-level 1.2 exception: " + e.toString());
		}

		// now wait for an event that says playing has finished
		this.getRequestManager(terminal).registerPlayThread(Thread.currentThread());

		// I've finished -- no events to send
	} else
		throw new MediaResourceException("No MediaTerminalConnection found for terminal: " + terminal);
}
/**
 * Record to a url build from the stream id.
 * The 1.2 media ignores RTCs and optional arguments.
 */
@SuppressWarnings("unchecked")
public void record(String terminal, String streamId, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws MediaResourceException {
	MediaTerminalConnection mtc = this.getMTC(terminal);
	if (mtc != null) {
		// start recording
		try {
			mtc.useRecordURL(new java.net.URL(streamId));
			mtc.startRecording();
		} catch (Exception e) {
			throw new MediaResourceException("Low-level 1.2 exception: " + e.toString());
		}

		// now wait for an event that says playing has finished
		this.getRequestManager(terminal).registerRecordThread(Thread.currentThread());

		// I've finished -- no events to send
	} else
		throw new MediaResourceException("No MediaTerminalConnection found for terminal: " + terminal);
}
/**
 * Waits on the collection of num DTMF signals and then returns them.
 * 1.2 media does not support patterns, RTCs or optional arguments, so these are ignored.
 */
@SuppressWarnings("unchecked")
public net.sourceforge.gjtapi.RawSigDetectEvent retrieveSignals(String terminal, int num, javax.telephony.media.Symbol[] patterns, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws MediaResourceException {
	MediaTerminalConnection mtc = this.getMTC(terminal);
	if (mtc != null) {
		// Register for DTMF collection
		RequestManager rm = this.getRequestManager(terminal);
		rm.registerRetrieveThread(Thread.currentThread(), num);

		// I've finished -- collect dtmf and return them
		return net.sourceforge.gjtapi.RawSigDetectEvent.maxDetected(terminal,
			net.sourceforge.gjtapi.media.SymbolConvertor.convert(rm.getDtmfs()));
	} else
		throw new MediaResourceException("No MediaTerminalConnection found for terminal: " + terminal);
}
/**
 * Send the signals out.
 * For now we ignore the RTCs and optional arguments.
 */
@SuppressWarnings("unchecked")
public void sendSignals(String terminal, javax.telephony.media.Symbol[] syms, javax.telephony.media.RTC[] rtcs, java.util.Dictionary optArgs) throws MediaResourceException {
	MediaTerminalConnection mtc = this.getMTC(terminal);
	
	if (mtc != null) {
		try {
			mtc.generateDtmf(net.sourceforge.gjtapi.media.SymbolConvertor.convert(syms));
		} catch (Exception e) {
			// MethodNotAvailableException, ResourceUnavailableException, InvalidStateException
			throw new MediaResourceException("Provider exception: " + e.toString());
		}
	}
}
/**
 * Stop any active media actions.
 */
public void stop(String terminal) {
	MediaTerminalConnection mtc = this.getMTC(terminal);
	if (mtc != null) {
		try {
			mtc.setDtmfDetection(false);
		} catch (Exception e) {
			// ignore
		}
		mtc.stopPlaying();
		mtc.stopRecording();
	}
}
/**
 * Handle dtmf character event updates and use this information to affect waiting threads.
 * Creation date: (2000-06-07 14:25:52)
 * @author: Richard Deadman
 * @param term The name of the terminal the media state is associated with.
 * @param dtmfChar The character added to the Dtmf buffer.
 */
void takeDtmf(String term, char dtmfChar) {
	RequestManager rm = (RequestManager)this.getActiveRequests().get(term);

	if (rm != null) {
		rm.takeDtmf(dtmfChar);
	}

	// also report them to the Framework listener.
	char[] chars = new char[0];
	chars[0] = dtmfChar;
	String sigs = new String(chars);
	this.getListener().getTListener().mediaSignalDetectorDetected(term,
		net.sourceforge.gjtapi.media.SymbolConvertor.convert(sigs));
}
/**
 * We detect some signals and turn them into old media commands.
 */
public void triggerRTC(String terminal, Symbol action) {
	MediaTerminalConnection mtc = this.getMTC(terminal);
	if (mtc != null) {
		if (action.equals(PlayerConstants.rtca_Stop))
			mtc.stopPlaying();
		else
			if (action.equals(RecorderConstants.rtca_Stop))
				mtc.stopRecording();
			else
				if (action.equals(SignalDetectorConstants.rtca_Stop))
				try {
					mtc.setDtmfDetection(false);
				} catch (Exception e) {
					// MethodNotSupportedException, ResourceUnavailableException, InvalidStateException
					throw new RuntimeException("Error stopping Signal Detector");
				}
	}
}
/**
 * Handle player and recorder state updates and use this information to affect waiting threads.
 * Creation date: (2000-06-07 14:25:52)
 * @author: Richard Deadman
 * @param term The name of the terminal the media state is associated with.
 * @param state MediaTerminalConnection media state
 */
void updateState(String term, int state) {
	RequestManager rm = (RequestManager)this.getActiveRequests().get(term);

	if (rm != null) {
		rm.updateState(state);
	}
}
}
