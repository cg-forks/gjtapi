package net.sourceforge.gjtapi.media;

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
import javax.telephony.InvalidStateException;
import javax.telephony.ResourceUnavailableException;
import javax.telephony.Terminal;
import javax.telephony.media.*;
import javax.telephony.media.events.MediaTermConnStateEv;

import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.events.FreeCallEvent;
import java.util.*;
/**
 * This is a MediaService that wraps a 1.2-style MediaTerminalConnection's access to raw media services.
 * Most of these methods are not implemented, since we are really only interested in using this
 * Listener management and binding to resources.
 * <P>Wrapping a MediaTerminalConnection's access to a media terminal in a MediaService allows us to
 * register this MediaService with the MediaMgr as assigned to the terminal.  Then events back from
 * the RawProvider can be properly delegated off and handled.
 * <P>This is also responsible for tracking play and record threads as well as the media bind and
 * release and the thread that manages this.  Basically we delay "free" requests for X seconds such
 * we can catch a subsequent allocate and only release if the resource is not re-allocated in the
 * timeout period.
 * <P>Play and Record thread lazily created and triggered through a "notify()" semaphore signal.
 *
 * <P>The only methods that are implemented are:
 * <UL>
 * <li>send(MediaTermConnDtmfEv) called by TCListener to handle DTMF events
 * <li>getListeners()
 * </UL>
 * Creation date: (2000-05-09 15:53:37)
 * @author: Richard Deadman
 */
@SuppressWarnings("deprecation")
class TCMediaService implements MediaServiceHolder {

	/**
	 * This is an abstract private Runnable block used as a common parent for Play and Record threads.
	 * @author Richard Deadman
	 **/
	abstract class BaseMediaThread extends Thread {
		protected FreeMediaTerminalConnection tc;
		protected boolean runFlag = true;
		protected boolean playFlag = false;

		BaseMediaThread(GenericProvider gp, FreeMediaTerminalConnection termConn) {
			tc = termConn;
			this.setDaemon(true);
		}
	}

	/**
	 * This is a private Runnable block for running Play requests asynchronously
	 * @author Richard Deadman
	 **/
	private class PlayThread extends BaseMediaThread {
		private String[] streams = new String[1];
		
		PlayThread(GenericProvider gp, FreeMediaTerminalConnection termConn) {
			super(gp, termConn);
		}

		void playStream(String streamId) {
			streams[0] = streamId;
				// notify the running thread
			synchronized(playerThread) {
				playFlag = true;
				playerThread.notify();
			}
		}

		public void run() {
			while (runFlag) {
				synchronized(playerThread) {
					if (!playFlag) {
						try {
								// sleep until signalled
							playerThread.wait();
						} catch (InterruptedException ie) {
							runFlag = false;
							playerThread = null;
						}
					}
					playFlag = false;	
				}
				if (runFlag) {	// not interruped
					try {
						prov.getRaw().play(tc.getTerminal().getName(), streams, 0, null, null);
					} catch (MediaResourceException mre) {
						// should we throw an event
					}

					// notify that play has stopped
					playingStopped();
				}
			}
		}
	}

	/**
	 * This is a private Runnable block for running Record requests asynchronously
	 * @author Richard Deadman
	 **/
	private class RecordThread extends BaseMediaThread {
		private String recordId;
			
		RecordThread(GenericProvider gp, FreeMediaTerminalConnection termConn) {
			super(gp, termConn);
		}

		void recordStream(String streamId) {
			recordId = streamId;
				// notify the running thread
			synchronized(recorderThread) {
				playFlag = true;
				recorderThread.notify();
			}
		}

		public void run() {
			while (runFlag) {
				// sleep until signalled
				synchronized(recorderThread) {
					if (!playFlag) {
						try {
							recorderThread.wait();
						} catch (InterruptedException ie) {
							runFlag = false;
							recorderThread = null;
						}
					}
					playFlag = false;	
				}
				if (runFlag) {	// not interruped
					try {
						prov.getRaw().record(tc.getTerminal().getName(), recordId, null, null);
					} catch (MediaResourceException mre) {
						// should we throw an event
					}

					// notify that recording has stopped
					recordingStopped();
				}
			}
		}
	}

	/**
	 * This is a private Runnable block for running release requests asynchronously
	 * @author Richard Deadman
	 **/
	private class Reaper implements Runnable {
		int milliDelay;
		
		Reaper(int wait) {
			milliDelay = wait;
		}
		
		public void run() {
			// wait before releasing
			try {
				Thread.sleep(milliDelay);
			} catch (InterruptedException ie) {
				// well let's just continue then
			}

			reallyRelease();
		}
	}

		// define state media event
	class StateEv extends BaseMediaEv implements MediaTermConnStateEv {
		StateEv(FreeMediaTerminalConnection tc) {
			super(tc);
		}

		  /**
		   * Returns the current state of playing/recording on the TerminalConnection
		   * in the form of a bit mask.
		   * <p>
		   * @return The current playing/recording state.
		   */
	 	public int getMediaState() {
		 	return ((FreeMediaTerminalConnection)getTerminalConnection()).getMediaState();
	 	}
		 	
		public int getID() {
			return javax.telephony.media.events.MediaTermConnStateEv.ID;
		}
	}
	private final static int PLAY_REC = MediaTerminalConnection.PLAYING | MediaTerminalConnection.RECORDING;
	private final static int DTMF_DET = 0x04;
	public final static int ALLOCATED = 0;
	public final static int REAPING = 1;
	public final static int FREE = 2;
	private MediaMgr mgr = null;
	private GenericProvider prov = null;
	private FreeMediaTerminalConnection termConn = null;
	private Vector<MediaListener> listeners = new Vector<MediaListener>(1);	// a single listener, set in the constructor
	private PlayThread playerThread = null;
	private RecordThread recorderThread = null;
	private Thread garbageCollector = null;
	private int mediaState = MediaTerminalConnection.NOACTIVITY;
	private int allocateState = FREE;
	private int freeDelay = 2000;	// default is 2 seconds
/**
 * Create an instance of the MediaService for a 1.2 type of MediaTerminal.
 * Creation date: (2000-05-09 15:59:38)
 * @author: Richard Deadman
 * @param tc The terminalConnection the service is connected to.
 */
TCMediaService(FreeMediaTerminalConnection tc) {
	this(tc, -1);	// use default time
}
/**
 * Create an instance of the MediaService for a 1.2 type of MediaTerminal.
 * This hasn't been allocated yet.
 * Creation date: (2000-05-09 15:59:38)
 * @author: Richard Deadman
 * @param tc The terminalConnection the service is connected to.
 * @param deallocateDelay The time to delay a "free" request for, in milliseconds.
 */
TCMediaService(FreeMediaTerminalConnection tc, int deallocateDelay) {
	super();

	GenericProvider gp;
	this.setProv(gp = (GenericProvider)tc.getTerminal().getProvider());
	this.setMgr(gp.getMediaMgr());
	this.setTermConn(tc);
	this.listeners.add(new TCListener(this));	// the only addition ever
	if (deallocateDelay >= 0)
		this.setFreeDelay(deallocateDelay);

	// tell the media manager to bind us to the terminal as the event listener
	String termName = this.getTerminalName();
	this.getMgr().bind(termName, this);
}
/**
 * Add this state flag from the MediaState holder.
 * Creation date: (2000-05-15 13:09:53)
 * @author: Richard Deadman
 * @param newState The new state to add to the media statge holder.
 */
private void addMediaState(int newState) {
	if ((this.getPrivateMediaState() & newState) == 0) {
		this.mediaState |= newState;

		// do we need to notify any call listeners
		if ((newState & PLAY_REC) != 0) {
			((FreeCall)this.getTermConn().getConnection().getCall()).sendToObservers(new StateEv(this.getTermConn()));
		}
	}
}
/**
 * Make sure we are allocated.
 * Creation date: (2000-05-12 10:42:20)
 * @author: Richard Deadman
 */
private void allocate() {
	if (this.getAllocateState() != TCMediaService.ALLOCATED) {
		this.allocate(null, false);
	}
}
/**
 * Ask the system to allocate the required resources
 * Creation date: (2000-05-12 10:42:20)
 * @author: Richard Deadman
 * @param dict The resource parameters to pass down
 * @param force Force the allocation even if the RawProvider doesn't require null dictionary allocations.  This is useful to reset a resources dictionary to null.
 */
@SuppressWarnings("unchecked")
private void allocate(java.util.Dictionary dict, boolean force) {
		// ask raw provider to allocate new resources, if necessary
	GenericProvider prov = this.getProv();
	if (prov.getRawCapabilities().allocateMedia || dict != null || force) {
		prov.getRaw().allocateMedia(this.getTerminalName(), TelephonyProvider.MEDIA_RES_ALL, dict);
	}

	this.setAllocateState(TCMediaService.ALLOCATED);
}
/**
 * Forward a String of digits off to a DTMF signal generator.
 * @param digits The DTMF digits to play on the TerminalConnection
 * @exception InvalidStateException If the TerminalConnection is not ACTIVE
 * @exception ResourceUnavailableException If the raw provider threw an exception.
 */
public void generateDtmf(String digits) throws ResourceUnavailableException {
	// ensure we've allocated
	this.allocate();
	
	try {
		this.getProv().getRaw().sendSignals(this.getTerminalName(), SymbolConvertor.convert(digits), null, null);
	} catch (MediaResourceException mre) {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN);
	}
}
/**
 * Access the allocation state I'm in.  Allocated if an active media service, Reaping if in the process
 * of unbinding, and Free once unbound.
 * Creation date: (2000-05-11 15:06:28)
 * @author: Richard Deadman
 * @return ALLOCATED, REAPING or FREE
 */
private int getAllocateState() {
	return allocateState;
}
/**
 * Get the reaping delay.
 * Creation date: (2000-05-11 15:27:02)
 * @author: Richard Deadman
 * @return The number of milliseconds after a release call to wait incase I'm un-released.
 */
private int getFreeDelay() {
	return freeDelay;
}
/**
 * Internal lazy accessor for the Garbage Collector.
 * Creation date: (2000-05-11 15:24:09)
 * @author: Richard Deadman
 * @return A new or existing Garbage collector thread.
 */
private Thread getGarbageCollector() {
	if (this.garbageCollector == null) {
		Thread t = this.garbageCollector = new Thread(new Reaper(this.getFreeDelay()));
		t.setDaemon(true);
	}
	return garbageCollector;
}
/**
 * getListeners method comment.
 */
public Iterator<MediaListener> getListeners() {
	return this.listeners.iterator();
}
/**
 * I'm not a real MediaService...
 */
public javax.telephony.media.MediaService getMediaService() {
	return null;
}
/**
 * Return the current media playing state.
 * Creation date: (2000-05-15 11:18:29)
 * @author: Richard Deadman
 * @return int
 */
int getMediaState() {
	return this.getPrivateMediaState() & PLAY_REC;
}
/**
 * Get the media service manager
 * Creation date: (2000-05-11 15:43:30)
 * @author: Richard Deadman
 * @return The manager that maps bound media service holders to terminals
 */
private MediaMgr getMgr() {
	return mgr;
}
/**
 * Internal lazy accessor for the Player Thread.
 * Creation date: (2000-05-11 15:24:09)
 * @author: Richard Deadman
 * @return A new or existing Garbage collector thread.
 */
private PlayThread getPlayerThread() {
	if (this.playerThread == null) {
		Thread t = this.playerThread = new PlayThread(this.getProv(), this.getTermConn());
		t.start();
	}
	return playerThread;
}
/**
 * Return the current media true state.
 * Creation date: (2000-05-15 11:18:29)
 * @author: Richard Deadman
 * @return int
 */
private int getPrivateMediaState() {
	return this.mediaState;
}
/**
 * Get the Framework manager.
 * Creation date: (2000-05-11 15:24:09)
 * @author: Richard Deadman
 * @return The root controller for the Generic JTAPI framework
 */
private GenericProvider getProv() {
	return prov;
}
/**
 * Internal lazy accessor for the Record Thread.
 * Creation date: (2000-05-11 15:24:09)
 * @author: Richard Deadman
 * @return A new or existing Record thread.
 */
private RecordThread getRecordThread() {
	if (this.recorderThread == null) {
		Thread t = this.recorderThread = new RecordThread(this.getProv(), this.getTermConn());
		t.start();
	}
	return recorderThread;
}
/**
 * Return the MediaTerminalConnection I manage media binding for.
 * Creation date: (2000-05-11 15:24:09)
 * @author: Richard Deadman
 * @return A MediaTerminalConnection that has requested to use old 1.2 media calls.
 */
FreeMediaTerminalConnection getTermConn() {
	return termConn;
}
/**
 * Return the Terminal I am associated with
 */
private Terminal getTerminal() throws NotBoundException {
	return this.termConn.getTerminal();
}
/**
 * Get the name of the Terminal I am associated with.
 */
private String getTerminalName() throws NotBoundException {
	return this.getTerminal().getName();
}
/**
 * Note that the playing thread has stopped.
 * Creation date: (2000-05-11 14:28:16)
 * @author: Richard Deadman
 */
void playingStopped() {
	this.removeMediaState(MediaTerminalConnection.PLAYING);

	// see if we need to release now
	this.release();
}
/**
 * Really release the pseudo media service from the terminal.
 *
 * @author Richard Deadman
 */
private synchronized void reallyRelease() {
	if ((this.getAllocateState() == REAPING) &&
		(this.getPrivateMediaState() == MediaTerminalConnection.NOACTIVITY)) {
		// free the media
		this.getProv().getRaw().freeMedia(this.getTermConn().getTerminal().getName(),
			TelephonyProvider.MEDIA_RES_ALL);

		// unbind from the media manager
		this.getMgr().release(this.getTerminalName());

		// unbind from the TerminalConnection
		this.getTermConn().freeMediaService();

		// we should be garbage collected now, but just to be safe.
		this.setAllocateState(FREE);
	}
}
/**
 * Note that the recording thread has stopped.
 * Creation date: (2000-05-11 14:28:16)
 * @author: Richard Deadman
 */
void recordingStopped() {
	this.removeMediaState(MediaTerminalConnection.RECORDING);

	// see if we need to release now
	this.release();
}
/**
 * Release the pseudo media service from the terminal.
 *
 * <P>This launches a thread that delays the release for a couple of seconds to take care
 * of us needing the service a short time later.
 *
 * @author Richard Deadman
 */
private synchronized  void release() throws NotBoundException {
	// test current bind state
	int state = this.getAllocateState();
	if (state == FREE)
		throw new NotBoundException();

	if (state == REAPING)
		return;	// we're already releasing

	// test if we are really not doing anything
	if (this.getPrivateMediaState() == MediaTerminalConnection.NOACTIVITY) {
		this.setAllocateState(REAPING);
		this.getGarbageCollector().start();
	}
}
/**
 * Remove this state flag from the MediaState holder.
 * Creation date: (2000-05-15 13:09:53)
 * @author: Richard Deadman
 * @param removedState int
 */
private void removeMediaState(int removedState) {
	if ((this.getPrivateMediaState() & removedState) != 0) {
		this.mediaState &= (Integer.MAX_VALUE - removedState);

		// do we need to notify any call listeners
		if ((removedState & PLAY_REC) != 0) {
			((FreeCall)this.getTermConn().getConnection().getCall()).sendToObservers(new StateEv(this.getTermConn()));
		}
	}
}
/**
 * Forward the 1.2 event onto the call.  Note that 1.2 media reported events on Observers of Calls,
 * whereas in 1.3 media events are reported to Listener's of MediaServices.  Since we are a bridge
 * from a 1.2 MediaTerminalConnection media triggered events, we know that we only need to send to
 * Call Observers.
 * Creation date: (2000-05-09 16:11:06)
 * @author: Richard Deadman
 * @param ev FreeCallEvent that is a 1.2 style Observer event.
 */
void send(FreeCallEvent ev) {
	((FreeCall)this.getTermConn().getConnection().getCall()).sendToObservers(ev);
}
/**
 * Set my current media allocation state.  The state machine is FREE -&gt; ALLOCATED &lt;-&gt; REAPING -&gt; FREE.
 * Creation date: (2000-05-11 15:06:28)
 * @author: Richard Deadman
 * @param newAllocateState The new state for the pseudo media service -- FREE, ALLOCATED or REAPING.
 */
private void setAllocateState(int newAllocateState) {
	allocateState = newAllocateState;
}
  /*
   * Sets the DTMF tone detection either on or off. If the boolean flag
   * argument is true, then DTMF detection is turned on, otherwise, it is
   * turned off.
   * <p>
   * @param enable If true, turns DTMF-tone detection on, if false, turns
   * DTMF-tone detection off.
   * @exception MethodNotSupportedException The implementation does not
   * support the detection of DTMF-tones.
   * @exception ResourceUnavailableException Indicates DTMF-detection cannot
   * be started because some resource is unavailable.
   * @exception InvalidStateException Indicates the TerminalConnection is not
   * in the media channel available state.
   */
public synchronized void setDtmfDetection(boolean enable) throws InvalidStateException, ResourceUnavailableException {
	if (enable) {
			// turn on signal detection
		Dictionary<Symbol, Symbol> dict = new Hashtable<Symbol, Symbol>();
		dict.put(SignalDetectorConstants.p_EnabledEvents, SignalDetectorConstants.ev_RetrieveSignals);
			// reallocate in order to force change in dictionary
		this.allocate(dict, true);
			// note that we are in detecting mode
		this.addMediaState(DTMF_DET);
	} else {
			// reallocate in order to force change in dictionary
		this.allocate(null, true);
			// note that we've not detecting anymore
		this.removeMediaState(DTMF_DET);
			// release media service if that's are only activity
		this.release();
	}
}
/**
 * Set the REAPING delay for the garbage collector thread.
 * Once the garbage collector is lazily created, this value is not used again and so setting
 * it will have no effect.
 * Creation date: (2000-05-11 15:27:02)
 * @author: Richard Deadman
 * @param newFreeDelay The number of milliseconds the garbage collector should wait before really
 * unbinding this pseudo media service.
 */
private void setFreeDelay(int newFreeDelay) {
	freeDelay = newFreeDelay;
}
/**
 * Set the media service manager
 * Creation date: (2000-05-11 15:43:30)
 * @author: Richard Deadman
 * @param newMgr The manager that maps bound media service holders to terminals
 */
private void setMgr(MediaMgr newMgr) {
	mgr = newMgr;
}
/**
 * Sets the root Generic JTAPI manager object.
 * Creation date: (2000-05-11 15:24:09)
 * @author: Richard Deadman
 * @param newProv The root handle to the framework.
 */
private void setProv(net.sourceforge.gjtapi.GenericProvider newProv) {
	prov = newProv;
}
/**
 * Set the TerminalConnection I handle media binding for.
 * Creation date: (2000-05-11 15:24:09)
 * @author: Richard Deadman
 * @param newTermConn A 1.2 media TerminalConnection I am the pseeudo MediaService for.
 */
private void setTermConn(FreeMediaTerminalConnection newTermConn) {
	termConn = newTermConn;
}
/**
 * Start playing a URL set in the "usePlayURL()" method.
 * @author: Richard Deadman
 * @param The MediaTerminalConnection current play url.
 */
synchronized void startPlaying(String playUrl) throws ResourceUnavailableException {
	if ((this.getMediaState() & MediaTerminalConnection.PLAYING) == MediaTerminalConnection.PLAYING) {
		// we're already playing
		return;
	}

	if (playUrl == null) {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "No play URL defined");
	}

		// ensure we're allocated
	this.allocate();
	this.getPlayerThread().playStream(playUrl);
	this.addMediaState(MediaTerminalConnection.PLAYING);
}
/**
 * Start playing a URL set in the "usePlayURL()" method.
 */
synchronized void startRecording(String recordUrl) throws ResourceUnavailableException {
	if ((this.getMediaState() & MediaTerminalConnection.RECORDING) == MediaTerminalConnection.RECORDING) {
		// we're already playing
		return;
	}

	if (recordUrl == null) {
		throw new ResourceUnavailableException(ResourceUnavailableException.UNKNOWN, "No record URL defined");
	}
	
		// ensure we're allocated
	this.allocate();
	this.getRecordThread().recordStream(recordUrl);
	this.addMediaState(MediaTerminalConnection.RECORDING);
}
/**
 * Tell the playing thread to stop by sending a stop RTC to the media player
 */
void stopPlaying() {
	this.getProv().getRaw().triggerRTC(this.getTerminalName(), PlayerConstants.rtca_Stop);
}
/**
 * Tell the recoding thread to stop by sending an RTC to the media recorder.
 */
void stopRecording() {
	this.getProv().getRaw().triggerRTC(this.getTerminalName(), RecorderConstants.rtca_Stop);
}
}
