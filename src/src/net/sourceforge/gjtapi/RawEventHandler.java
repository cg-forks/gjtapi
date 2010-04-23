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
import net.sourceforge.gjtapi.jcc.ConnListenerAdapter;
import java.io.Serializable;
import javax.telephony.media.*;
import java.util.Iterator;
import net.sourceforge.gjtapi.events.*;
import net.sourceforge.gjtapi.media.*;
import javax.telephony.*;
import net.sourceforge.gjtapi.util.*;
import net.sourceforge.gjtapi.util.ExceptionHandler;
/**
 * This is a helper class for the GenericProvider that takes care or receiving and dispatching
 * TelephonyProvider events.
 * Creation date: (2000-04-17 0:01:57)
 * @author: Richard Deadman
 */
class RawEventHandler implements TelephonyListener {

	// define a common block for sending events observers and listeners
	private class ClientNotifier implements EventHandler {
		private Dispatchable event = null;
		ClientNotifier(Dispatchable ev) {
			this.event = ev;
		}
		public void process(Object o) {
			this.event.dispatch();
		}
	}

	// define a Synchronous Block manager for (paradoxically) processing events synchronously
	// this is a test of synchronous event processing to see if it fixes race conditions
	private class SynchronousBlockManager extends BlockManager{
		private GenericProvider prov;
		//private ExceptionHandler exHandler = null;

		SynchronousBlockManager(GenericProvider gp) {
			this(gp, new NullExceptionHandler());
		}
		
		SynchronousBlockManager(GenericProvider gp, ExceptionHandler exh) {
			super(exh);

			this.prov = gp;
		}

		private GenericProvider getGenProvider() {
			return this.prov;
		}
		
		/**
		 * Process the object within its own thread.
		 * Creation date: (2000-06-26 17:05:21)
		 * @author: Richard Deadman
		 * @param eh The EventHandler to process
		 */
		public void put(EventHandler eh) {
			try {
				eh.process(this.getGenProvider());
			} catch (RuntimeException ex) {
				ExceptionHandler exh = this.exHandler;
				if (exh != null) {
					// handle the exception
					exh.handleException(eh, ex, this.getGenProvider());
				}
			}
		}
	}
		// incoming raw provider event queue
	private BlockManager eventPool;
	// outgoing client callback event queue
	private BlockManager dispatchPool;
/**
 * Package constructor for the event handler
 * Creation date: (2000-04-18 0:08:28)
 * @author: Richard Deadman
 * @param prov net.sourceforge.gjtapi.GenericProvider
 */
RawEventHandler(GenericProvider prov) {
	super();
	
	// Create the event pool with an Exception handler that rethrows exception -- we want these
	//this.eventPool = new OrderedBlockManager(prov, "Raw Event Queue", new ExceptionHandler());
	this.eventPool = new SynchronousBlockManager(prov);

	// Create the client output event pool
	this.dispatchPool = new OrderedBlockManager(null, "JTAPI Event Dispatch Queue");	// it doesn't need a visitor and by default exceptions are swallowed
}
/**
 * Dispatch private data to all Address observers who are registered for it.
 * @Author Richard Deadman
 * @param address The name of the Address that the data is associated with.
 * @paramdata The data that is to be asynchronously delivered.
 * @param cause The Ev cause code indicating what caused the event.
 */
public void addressPrivateData(final String address, final Serializable data, final int cause) {
	// define action block for doing the event processing in
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			try {
				// Find the Address
				FreeAddress addr = (FreeAddress)((GenericProvider)o).getAddress(address);
				
				// Create the event
				GenPrivateAddrEv pae = new GenPrivateAddrEv(addr, cause, data);

				// dispatch the event
				addr.sendToObservers(pae);

			} catch (InvalidArgumentException iae) {
				// address is unknown -- eat this event
			}
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Call Active notification event.
 * @Author Richard Deadman
 * @param id The id for the call that has become active.
 * @param cause The Event cause id that details why the state changed.
 */
public void callActive(final CallId id, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			FreeCall call = ((GenericProvider)o).getCallMgr().getLazyCall(id);
			
			// Update the call state
			call.toActive(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Call Invalid notification event.
 * @Author Richard Deadman
 * @param id The id for the call that has become invalid.
 * @param cause The Event cause id that details why the state changed.
 */
public void callInvalid(final CallId id, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// find, but don't create, the call and invalidate it.
			FreeCall call = ((GenericProvider)o).getCallMgr().getCachedCall(id);
			if (call != null) {
					// unHook the call
				call.toInvalid(cause);
			}
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
			GenericProvider gp = (GenericProvider)o;
			net.sourceforge.gjtapi.jcc.Provider prov = gp.getJainProvider();
			
			// Notify the jain provider
			if (prov != null)
				prov.callOverloadCeased(gp.getDomainMgr().getLazyAddress(address));
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * callOverloadEncountered method comment.
 */
public void callOverloadEncountered(final String address) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			GenericProvider gp = (GenericProvider)o;
			net.sourceforge.gjtapi.jcc.Provider prov = gp.getJainProvider();
			
			// Notify the jain provider
			if (prov != null)
				prov.callOverloadEncountered(gp.getDomainMgr().getLazyAddress(address));
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Dispatch private data to all Call observers who are registered for it.
 * @Author Richard Deadman
 * @param call The name of the Call that the data is associated with.
 * @paramdata The data that is to be asynchronously delivered.
 * @param cause The Ev cause code indicating what caused the event.
 */
public void callPrivateData(final CallId id, final Serializable data, final int cause) {
	// define action block for doing the event processing in
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			FreeCall call = ((GenericProvider)o).getCallMgr().getLazyCall(id);

			// Create the event
			GenPrivateCallEv pce = new GenPrivateCallEv(call, cause, data);

			// dispatch the event
			call.sendToObservers(pce);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * connectionAddressAnalyze method comment.
 */
public void connectionAddressAnalyse(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			GenericProvider gp = (GenericProvider)o;
			FreeConnection conn = gp.getCallMgr().getLazyConnection(id, address);
			conn.toAlerting(cause);

				// notify each CallListenerAdapter we find
			CallListener[] cls = conn.getCall().getCallListeners();
			int size = cls.length;
			for (int i = 0; i < size; i++) {
				if (cls[i] instanceof ConnListenerAdapter) {
					((ConnListenerAdapter)cls[i]).connectionAddressAnalyse(conn, cause);
				}
			}
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * connectionAddressCollect method comment.
 */
public void connectionAddressCollect(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			GenericProvider gp = (GenericProvider)o;
			FreeConnection conn = gp.getCallMgr().getLazyConnection(id, address);
			conn.toAlerting(cause);

				// notify each CallListenerAdapter we find
			CallListener[] cls = conn.getCall().getCallListeners();
			int size = cls.length;
			for (int i = 0; i < size; i++) {
				if (cls[i] instanceof ConnListenerAdapter) {
					((ConnListenerAdapter)cls[i]).connectionAddressCollect(conn, cause);
				}
			}
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Connection Alerting notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void connectionAlerting(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			((GenericProvider)o).getCallMgr().getLazyConnection(id, address).toAlerting(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * connectionAuthorizeCallAttempt method comment.
 */
public void connectionAuthorizeCallAttempt(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			GenericProvider gp = (GenericProvider)o;
			FreeConnection conn = gp.getCallMgr().getLazyConnection(id, address);
			conn.toAlerting(cause);

				// notify each CallListenerAdapter we find
			CallListener[] cls = conn.getCall().getCallListeners();
			int size = cls.length;
			for (int i = 0; i < size; i++) {
				if (cls[i] instanceof ConnListenerAdapter) {
					((ConnListenerAdapter)cls[i]).connectionAuthorizeCallAttempt(conn, cause);
				}
			}
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * connectionCallDelivery method comment.
 */
public void connectionCallDelivery(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			GenericProvider gp = (GenericProvider)o;
			FreeConnection conn = gp.getCallMgr().getLazyConnection(id, address);
			conn.toAlerting(cause);

				// notify each CallListenerAdapter we find
			CallListener[] cls = conn.getCall().getCallListeners();
			int size = cls.length;
			for (int i = 0; i < size; i++) {
				if (cls[i] instanceof ConnListenerAdapter) {
					((ConnListenerAdapter)cls[i]).connectionCallDelivery(conn, cause);
				}
			}
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Connection Connected notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void connectionConnected(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			((GenericProvider)o).getCallMgr().getLazyConnection(id, address).toConnected(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Connection Disconnected notification event.
 * If the call or connection does not exist in the framework, don't create it...
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void connectionDisconnected(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// find, but don't create the call
			FreeConnection conn = ((GenericProvider)o).getCallMgr().getCachedConnection(id, address);
			if (conn != null)
				conn.toDisconnected(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Connection Failed notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void connectionFailed(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			FreeConnection conn = ((GenericProvider)o).getCallMgr().getCachedConnection(id, address);
			if (conn != null)
				conn.toFailed(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Connection In Progress notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void connectionInProgress(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			((GenericProvider)o).getCallMgr().getLazyConnection(id, address).toInProgress(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * connectionSuspended method comment.
 */
public void connectionSuspended(final CallId id, final String address, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the connection and move it to alerting
			GenericProvider gp = (GenericProvider)o;
			FreeConnection conn = gp.getCallMgr().getLazyConnection(id, address);
			conn.toAlerting(cause);

				// notify each CallListenerAdapter we find
			CallListener[] cls = conn.getCall().getCallListeners();
			int size = cls.length;
			for (int i = 0; i < size; i++) {
				if (cls[i] instanceof ConnListenerAdapter) {
					((ConnListenerAdapter)cls[i]).connectionSuspended(conn, cause);
				}
			}
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Accept a FreeCallEvent and dispatch off to registered clients.
 * Creation date: (2000-05-02 12:48:36)
 * @author: Richard Deadman
 * @param ev The event to dispatch
 */
void dispatch(FreeCallEvent ev) {
    final ClientNotifier notifier = new ClientNotifier(ev);
    this.getDispatchPool().put(notifier);
}
/**
 * The pool for dispatching events to client applications.
 * Creation date: (2000-04-25 14:14:22)
 * @author: Richard Deadman
 * @return A block manager that accepts EventHandler (event dispatchers) and schedules the event delivery for later.
 */
BlockManager getDispatchPool() {
	return dispatchPool;
}
/**
 * Internal accessor
 * Creation date: (2000-02-25 7:57:26)
 * @author: Richard Deadman
 * @return The internal pool for managing events and invoking threads to handle the events
 */
private BlockManager getEventPool() {
	return eventPool;
}
/**
 * mediaPlayPause method comment.
 */
public void mediaPlayPause(final String terminal, final int index, final int offset, final javax.telephony.media.Symbol trigger) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			GenericProvider provider = (GenericProvider)o;
			final MediaServiceHolder lms = provider.getMediaMgr().findForTerminal(terminal);

			// Create the common event
			final GenericPlayerEvent ev = new GenericPlayerEvent(PlayerConstants.ev_Pause,
				lms.getMediaService(),
				null, null, trigger, null,
				index, offset);
			
			// create a block to send these events out to the MediaService's listeners
			getDispatchPool().put(new EventHandler() {
				public void process(Object o) {	// ignore o -- will be null
					Iterator<MediaListener> it = lms.getListeners();
					while (it.hasNext()) {
						MediaListener l = it.next();
						if (l instanceof PlayerListener)
							((PlayerListener)l).onPause(ev);
					}
				}
			});
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Handle the Media play is resumed asynchronous notification.
 */
public void mediaPlayResume(final String terminal, final javax.telephony.media.Symbol trigger) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			GenericProvider provider = (GenericProvider)o;
			final MediaServiceHolder lms = provider.getMediaMgr().findForTerminal(terminal);

			// Create the common event
			final GenericPlayerEvent ev = new GenericPlayerEvent(PlayerConstants.ev_Resume,
				lms.getMediaService(),
				null, null, trigger, null,
				0, 0);
			
			// create a block to send these events out to the MediaService's listeners
			getDispatchPool().put(new EventHandler() {
				public void process(Object o) {	// ignore o -- will be null
					Iterator<MediaListener> it = lms.getListeners();
					while (it.hasNext()) {
						MediaListener l = it.next();
						if (l instanceof PlayerListener)
							((PlayerListener)l).onResume(ev);
					}
				}
			});
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * mediaRecorderPause method comment.
 */
public void mediaRecorderPause(final String terminal, final int duration, final javax.telephony.media.Symbol trigger) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			GenericProvider provider = (GenericProvider)o;
			final MediaServiceHolder lms = provider.getMediaMgr().findForTerminal(terminal);

			// Create the common event
			final GenericRecorderEvent ev = new GenericRecorderEvent(RecorderConstants.ev_Pause,
				lms.getMediaService(),
				null, null, trigger,
				duration);
			
			// create a block to send these events out to the MediaService's listeners
			getDispatchPool().put(new EventHandler() {
				public void process(Object o) {	// ignore o -- will be null
					Iterator<MediaListener> it = lms.getListeners();
					while (it.hasNext()) {
						MediaListener l = it.next();
						if (l instanceof RecorderListener)
							((RecorderListener)l).onPause(ev);
					}
				}
			});
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * mediaRecorderResume method comment.
 */
public void mediaRecorderResume(final String terminal, final javax.telephony.media.Symbol trigger) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			GenericProvider provider = (GenericProvider)o;
			final MediaServiceHolder lms = provider.getMediaMgr().findForTerminal(terminal);

			// Create the common event
			final GenericRecorderEvent ev = new GenericRecorderEvent(RecorderConstants.ev_Resume,
				lms.getMediaService(),
				null, null, trigger,
				0);
			
			// create a block to send these events out to the MediaService's listeners
			getDispatchPool().put(new EventHandler() {
				public void process(Object o) {	// ignore o -- will be null
					Iterator<MediaListener> it = lms.getListeners();
					while (it.hasNext()) {
						MediaListener l = it.next();
						if (l instanceof RecorderListener)
							((RecorderListener)l).onResume(ev);
					}
				}
			});
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * mediaSignalDetectorDetected method comment.
 */
public void mediaSignalDetectorDetected(final String terminal, final Symbol[] sigs) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			GenericProvider provider = (GenericProvider)o;
			final MediaServiceHolder lms = provider.getMediaMgr().findForTerminal(terminal);

			// Create the common event
			final GenericSignalDetectorEvent ev = new GenericSignalDetectorEvent(SignalDetectorConstants.ev_SignalDetected,
				lms.getMediaService(),
				null, null, null,
				-1, sigs);
			
			// create a block to send these events out to the MediaService's listeners
			getDispatchPool().put(new EventHandler() {
				public void process(Object o) {	// ignore o -- will be null
					Iterator<MediaListener> it = lms.getListeners();
					while (it.hasNext()) {
						MediaListener l = it.next();
						if (l instanceof SignalDetectorListener)
							((SignalDetectorListener)l).onSignalDetected(ev);
					}
				}
			});
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * mediaSignalDetectorOverflow method comment.
 */
public void mediaSignalDetectorOverflow(final String terminal, final Symbol[] sigs) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			GenericProvider provider = (GenericProvider)o;
			final MediaServiceHolder lms = provider.getMediaMgr().findForTerminal(terminal);

			// Create the common event
			final GenericSignalDetectorEvent ev = new GenericSignalDetectorEvent(SignalDetectorConstants.ev_Overflow,
				lms.getMediaService(),
				null, null, null,
				-1, sigs);
			
			// create a block to send these events out to the MediaService's listeners
			getDispatchPool().put(new EventHandler() {
				public void process(Object o) {	// ignore o -- will be null
					Iterator<MediaListener> it = lms.getListeners();
					while (it.hasNext()) {
						MediaListener l = it.next();
						if (l instanceof SignalDetectorListener)
							((SignalDetectorListener)l).onOverflow(ev);
					}
				}
			});
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * mediaSignalDetectorPatternMatched method comment.
 */
public void mediaSignalDetectorPatternMatched(final String terminal, final Symbol[] sigs, final int index) {

	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Fetch or create the call
			GenericProvider provider = (GenericProvider)o;
			GenericMediaService ms = (GenericMediaService)provider.getMediaMgr().findForTerminal(terminal);

			// Create the common event
			final GenericSignalDetectorEvent ev = new GenericSignalDetectorEvent(SignalDetectorConstants.ev_Pattern[index],
				ms,
				null, null, null,
				index, sigs);
			
			// create a block to send these events out to the MediaService's listeners
			getDispatchPool().put(new EventHandler() {
				public void process(Object o) {	// ignore o -- will be null
					Iterator<MediaListener> it = ((GenericMediaService)ev.getMediaService()).getListeners();
					while (it.hasNext()) {
						MediaListener l = it.next();
						if (l instanceof SignalDetectorListener)
							((SignalDetectorListener)l).onPatternMatched(ev);
					}
				}
			});
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Dispatch private data to all Provider observers who are registered for it.
 * @Author Richard Deadman
 * @paramdata The data that is to be asynchronously delivered.
 * @param cause The Ev cause code indicating what caused the event.
 */
public void providerPrivateData(final Serializable data, final int cause) {
	// define action block for doing the event processing in
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			GenericProvider prov = (GenericProvider)o;
			
			// Create the event
			GenPrivateProvEv ppe = new GenPrivateProvEv(prov, cause, data);

			// dispatch the event
			prov.sendToObservers(ppe);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Terminal Connection Created notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param terminal The logical name of the device holding the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void terminalConnectionCreated(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// Force the creation of the Terminal Connection
		    final GenericProvider provider = (GenericProvider) o;
		    final CallMgr callMgr = provider.getCallMgr();
		    callMgr.getLazyTermConn(id, address, terminal);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Terminal Connection Dropped notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param terminal The logical name of the device holding the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void terminalConnectionDropped(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			// find, but don't create the call
			FreeTerminalConnection tc = ((GenericProvider)o).getCallMgr().getCachedTermConn(id, address, terminal);
			if (tc != null)
				tc.toDropped(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Terminal Connection Held notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param terminal The logical name of the device holding the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void terminalConnectionHeld(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			((GenericProvider)o).getCallMgr().getLazyTermConn(id, address, terminal).toHeld(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Terminal Connection Ringing notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param terminal The logical name of the device holding the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void terminalConnectionRinging(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
		    // Dispatch off to Terminal Connection
		    final GenericProvider provider = (GenericProvider) o;
		    final FreeTerminalConnection connection =
		        provider.getCallMgr().getLazyTermConn(id, address,
		                terminal);
		    connection.toRinging(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Receive and queue up a Terminal Connection Talking notification event.
 * @Author Richard Deadman
 * @param id The id for the call that holds the connection.
 * @param address The logical end of the connection.
 * @param terminal The logical name of the device holding the connection.
 * @param cause The Event cause id that details why the state changed.
 */
public void terminalConnectionTalking(final CallId id, final String address, final String terminal, final int cause) {
	// define action block
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			((GenericProvider)o).getCallMgr().getLazyTermConn(id, address, terminal).toTalking(cause);
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
/**
 * Dispatch private data to all Terminal observers who are registered for it.
 * @Author Richard Deadman
 * @param terminal The name of the Terminal that the data is associated with.
 * @paramdata The data that is to be asynchronously delivered.
 * @param cause The Ev cause code indicating what caused the event.
 */
public void terminalPrivateData(final String terminal, final Serializable data, final int cause) {
	// define action block for doing the event processing in
	EventHandler eh = new EventHandler() {
		public void process(Object o) {
			try {
				// Find the Terminal
				FreeTerminal term = (FreeTerminal)((GenericProvider)o).getTerminal(terminal);
				
				// Create the event
				GenPrivateTermEv pte = new GenPrivateTermEv(term, cause, data);

				// dispatch the event
				term.sendToObservers(pte);

			} catch (InvalidArgumentException iae) {
				// terminal is unknown -- eat this event
			}
		}
	};

	// dispatch for processing
	this.getEventPool().put(eh);
}
}
