package net.sourceforge.gjtapi.raw;

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
import java.io.Serializable;
import javax.telephony.media.Symbol;
import net.sourceforge.gjtapi.*;
import net.sourceforge.gjtapi.util.*;
/**
 * This is a simple RawListener interceptor that catches all RawListener events and places them in
 * a BlockManager for later delivery.  This increases response time and approximates one-way
 * delivery.  This is particularily useful for the far side of a remote rpi provider so that
 * RawProvider callback events return quickly without regard for network latency.
 * Creation date: (2000-005-08 15:43:02)
 * @author: Richard Deadman
 */
public class RawListenerPool implements TelephonyListener {
	private TelephonyListener delegate;
	private BlockManager eventPool;
/**
 * Create a wrapper for a RawListener that delegates on the events in a separate ordered thread.
 * <P>This is useful, amoung other things, in allowing remote providers to make callbacks in a separate thread,
 * allowing processing to continue while the remote call occurs.
 * Creation date: (2000-05-08 14:18:39)
 * @author: Richard Deadman
 * @param rl net.sourceforge.gjtapi.RawListener
 */
public RawListenerPool(TelephonyListener rl) {
	super();
	
	// Create the event pool
	this.setEventPool(new OrderedBlockManager(null, "Raw Event Queue"));

	// Set the delegate
	this.setDelegate(rl);
}
/**
 * addressPrivateData method comment.
 */
public void addressPrivateData(final String address, final Serializable data, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().addressPrivateData(address, data, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void callActive(final CallId id, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().callActive(id, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void callInvalid(final CallId id, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().callInvalid(id, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * callOverloadCeased method comment.
 */
public void callOverloadCeased(final String address) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().callOverloadCeased(address);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * callOverloadCeased method comment.
 */
public void callOverloadEncountered(final String address) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().callOverloadEncountered(address);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * callPrivateData method comment.
 */
public void callPrivateData(final CallId call, final Serializable data, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().callPrivateData(call, data, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in own thread
 */
public void connectionAddressAnalyse(final CallId id, final String address, final int cause) {
		// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionAddressAnalyse(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in own thread
 */
public void connectionAddressCollect(final CallId id, final String address, final int cause) {
		// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionAddressCollect(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void connectionAlerting(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionAlerting(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in own thread
 */
public void connectionAuthorizeCallAttempt(final CallId id, final String address, final int cause) {
		// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionAuthorizeCallAttempt(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in own thread
 */
public void connectionCallDelivery(final CallId id, final String address, final int cause) {
		// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionCallDelivery(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void connectionConnected(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionConnected(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void connectionDisconnected(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionDisconnected(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void connectionFailed(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionFailed(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void connectionInProgress(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionInProgress(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in own thread
 */
public void connectionSuspended(final CallId id, final String address, final int cause) {
		// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().connectionSuspended(id, address, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Get the RawListener I delegate on to.
 * Creation date: (2000-05-08 14:22:40)
 * @author: Richard Deadman
 * @return the real RawListener
 */
private TelephonyListener getDelegate() {
	return delegate;
}
/**
 * Gets the event pool for holding delegate events until they can be sent to the RemoteListeners
 * Creation date: (2000-05-08 14:06:14)
 * @author: Richard Deadman
 * @return A pool of Events to be sent to the remote listener.
 */
private BlockManager getEventPool() {
	return eventPool;
}
/**
 * Forward on in our own thread.
 */
public void mediaPlayPause(final String terminal, final int index, final int offset, final Symbol trigger) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().mediaPlayPause(terminal, index, offset, trigger);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void mediaPlayResume(final String terminal, final Symbol trigger) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().mediaPlayResume(terminal, trigger);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void mediaRecorderPause(final String terminal, final int duration, final Symbol trigger) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().mediaRecorderPause(terminal, duration, trigger);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void mediaRecorderResume(final String terminal, final Symbol trigger) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().mediaRecorderResume(terminal, trigger);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void mediaSignalDetectorDetected(final String terminal, final Symbol[] sigs) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().mediaSignalDetectorDetected(terminal, sigs);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void mediaSignalDetectorOverflow(final String terminal, final Symbol[] sigs) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().mediaSignalDetectorOverflow(terminal, sigs);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void mediaSignalDetectorPatternMatched(final String terminal, final Symbol[] sigs, final int index) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().mediaSignalDetectorPatternMatched(terminal, sigs, index);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * providerPrivateData method comment.
 */
public void providerPrivateData(final Serializable data, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().providerPrivateData(data, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Set the RawListener I delegate to.
 * Creation date: (2000-05-08 14:22:40)
 * @author: Richard Deadman
 * @param newDelegate The delegate to eventually receive my calls.
 */
private void setDelegate(TelephonyListener newDelegate) {
	delegate = newDelegate;
}
/**
 * Sets the event pool for holding delegate events until they can be sent to the RemoteListeners
 * Creation date: (2000-05-08 14:06:14)
 * @author: Richard Deadman
 * @param A pool of Events to be sent to the remote listener.
 */
private void setEventPool(BlockManager newEventPool) {
	eventPool = newEventPool;
}
/**
 * Forward on in our own thread.
 */
public void terminalConnectionCreated(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().terminalConnectionCreated(id, address, terminal, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void terminalConnectionDropped(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().terminalConnectionDropped(id, address, terminal, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void terminalConnectionHeld(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().terminalConnectionHeld(id, address, terminal, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void terminalConnectionRinging(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().terminalConnectionRinging(id, address, terminal, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Forward on in our own thread.
 */
public void terminalConnectionTalking(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().terminalConnectionTalking(id, address, terminal, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * terminalPrivateData method comment.
 */
public void terminalPrivateData(final String terminal, final Serializable data, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Forward to real delegate in a separate thread
			getDelegate().terminalPrivateData(terminal, data, cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
}
