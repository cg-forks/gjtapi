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
import javax.telephony.media.*;
import net.sourceforge.gjtapi.media.*;
/**
 * This is a convenience class to allow RawProviders to create mobile SignalDetectorEvents synchronous
 * return values using convenience static methods.
 * <P>An attempt has been made in the RawProvider to keep the interface as small and simple as possible,
 * while still offering as many features as possible.  The SignalDetector::retreiveSignals(...) JTAPI
 * call, however, provides for a complex semantics as to when signal retreival should return.  The method
 * must relay the myriad possible reasons back to the Generic JTAPI framework.  This factory class
 * is used to simplify the creation of these result containers by a RawProvider.
 * Creation date: (2000-05-03 10:45:27)
 * @author: Richard Deadman
 */
public class RawSigDetectEvent implements java.io.Serializable {
	final static long serialVersionUID = 5595693053225567189L;
	
		// SignalDetectorEvent info
	private int patternIndex = -1;
	private SymbolHolder[] sigs = null;
		// index to look up MediaService
	private String terminal = null;
		// ResourceEvent info
	private SymbolHolder err = null;
	private SymbolHolder qualifier = null;
	private SymbolHolder trigger = null;
/**
 * Default Constructor.
 * Creation date: (2000-06-07 10:25:44)
 * @author: Richard Deadman
 */
public RawSigDetectEvent() {}
/**
 * Telling this event factory to build our event.
 * Called by a factory method in GenericSignalDetectorEvent.
 * Creation date: (2000-05-03 11:02:49)
 * @author: Richard Deadman
 * @param The provider holding the system resolution information to hook up the event to the JTAPI Generic Framework objects.
 * @return The new SignalDetectorEvent
 */
public GenericSignalDetectorEvent buildEvent(GenericProvider prov) {
	Symbol id;
	if (this.patternIndex != -1) {
		id = SignalDetectorConstants.ev_Pattern[patternIndex];
	} else {
		id = SignalDetectorConstants.ev_RetrieveSignals;
	}

	Symbol q;
	if (this.qualifier == null)
		q = SignalDetectorConstants.q_Standard;
	else
		q = this.qualifier.getSymbol();

	return new GenericSignalDetectorEvent(id,	// ECTF event type, such as ev_RetrieveSignals
				prov.getMediaMgr().findForTerminal(this.terminal).getMediaService(),	// attached media service
				null,						// Symbol representing error, or null
				q,							// Additional information about why an event occured, such as q_Duration.
				this.decode(this.trigger),	// The RTC trigger that caused the event, or null
				this.patternIndex,			// The index into a pattern array if a pattern caused the event to be triggered, or -1.
				SymbolHolder.decode(this.sigs));		// Array of Symbols representing the received signals.
}
/**
 * Convert an array of Symbol ids to an array of Symbols
 * Creation date: (2000-08-24 9:42:44)
 * @author: Richard Deadman
 * @return javax.telephony.media.Symbol[]
 * @param sigs int[]
 */
private static Symbol[] buildSignalBuffer(int[] sigs) {
	int size = sigs.length;
	Symbol[] sb = new Symbol[size];
	for (int i = 0; i < size; i++) {
		sb[i] = Symbol.getSymbol(sigs[i]);
	}
	return sb;
}
/**
 * Constructor from basic information
 * Creation date: (2000-08-24 10:25:44)
 * @author: Richard Deadman
 */
public static RawSigDetectEvent create(String term, int qualId, int[] sigBuf, int patIndex, int rtcTrig, int err) {
	RawSigDetectEvent ev = null;
	Symbol qualifier = Symbol.getSymbol(qualId);
	if (qualifier.equals(SignalDetectorEvent.p_InitialTimeout)) {
		ev = initialTimeout(term);
	} else {
		if (qualifier.equals(SignalDetectorEvent.p_InterSigTimeout)) {
			ev = interSigTimeout(term, RawSigDetectEvent.buildSignalBuffer(sigBuf));
		} else
			if (qualifier.equals(SignalDetectorEvent.q_NumSignals)) {
				ev = maxDetected(term, RawSigDetectEvent.buildSignalBuffer(sigBuf));
			} else
				if (qualifier == null) {
					ev = patternMatched(term, patIndex, RawSigDetectEvent.buildSignalBuffer(sigBuf));
				} else
					if (qualifier.equals(SignalDetectorEvent.q_RTC)) {
						ev = rtcStopped(term, Symbol.getSymbol(rtcTrig), RawSigDetectEvent.buildSignalBuffer(sigBuf));
					} else
						if (qualifier.equals(SignalDetectorConstants.q_Duration)) {
							ev = timeout(term, RawSigDetectEvent.buildSignalBuffer(sigBuf));
						}
	}
	if (ev == null) {
		// No matches
		ev = new RawSigDetectEvent();
		ev.qualifier = new SymbolHolder(qualifier);
		ev.patternIndex = patIndex;
		ev.err = new SymbolHolder(Symbol.getSymbol(err));
		ev.sigs = SymbolHolder.create(RawSigDetectEvent.buildSignalBuffer(sigBuf));
		ev.terminal = term;
		ev.trigger = new SymbolHolder(Symbol.getSymbol(rtcTrig));
	}
	return ev;
}
/**
 * Construct from a SignalGeneratorEvent.
 * Creation date: (2000-06-07 10:25:44)
 * @author: Richard Deadman
 */
public static RawSigDetectEvent create(SignalDetectorEvent sigEvent) {
	RawSigDetectEvent ev = null;
	String term = sigEvent.getMediaService().getTerminal().getName();
	Symbol qualifier = sigEvent.getQualifier();
	if (qualifier.equals(SignalDetectorEvent.p_InitialTimeout)) {
		ev = initialTimeout(term);
	} else
		if (qualifier.equals(SignalDetectorEvent.p_InterSigTimeout)) {
			ev = interSigTimeout(term, sigEvent.getSignalBuffer());
		} else
			if (qualifier.equals(SignalDetectorEvent.q_NumSignals)) {
				ev = maxDetected(term, sigEvent.getSignalBuffer());
			} else
				if (qualifier == null) {
					ev = patternMatched(term, sigEvent.getPatternIndex(), sigEvent.getSignalBuffer());
				} else
					if (qualifier.equals(SignalDetectorEvent.q_RTC)) {
						ev = rtcStopped(term, sigEvent.getRTCTrigger(), sigEvent.getSignalBuffer());
					} else
						if (qualifier.equals(SignalDetectorConstants.q_Duration)) {
							ev = timeout(term, sigEvent.getSignalBuffer());
						}
	if (ev == null) {
		// No matches
		ev = new RawSigDetectEvent();
		ev.qualifier = new SymbolHolder(sigEvent.getQualifier());
		ev.patternIndex = sigEvent.getPatternIndex();
		ev.err = new SymbolHolder(sigEvent.getError());
		ev.sigs = SymbolHolder.create(sigEvent.getSignalBuffer());
		ev.terminal = term;
		ev.trigger = new SymbolHolder(sigEvent.getRTCTrigger());
	}
	return ev;
}
/**
 * Decode the SymbolHolder or return null if the holder is null.
 * Creation date: (2000-05-03 11:31:18)
 * @author: Richard Deadman
 * @return The decoded Symbol or null
 */
private Symbol decode(SymbolHolder holder) {
	if (holder == null)
		return null;
	else
		return holder.getSymbol();
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-25 1:09:33)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.media.SymbolHolder
 */
public net.sourceforge.gjtapi.media.SymbolHolder getErr() {
	return err;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-25 1:09:33)
 * @author: Richard Deadman
 * @return int
 */
public int getPatternIndex() {
	return patternIndex;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-25 1:09:33)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.media.SymbolHolder
 */
public net.sourceforge.gjtapi.media.SymbolHolder getQualifier() {
	return qualifier;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-08-25 1:09:33)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.media.SymbolHolder[]
 */
public net.sourceforge.gjtapi.media.SymbolHolder[] getSigs() {
	return sigs;
}
/**
 * Get the name of the terminal that this signal was detected on.
 * Creation date: (2000-08-25 1:09:33)
 * @author: Richard Deadman
 * @return java.lang.String
 */
public String getTerminal() {
	return terminal;
}
/**
 * Get a holder for the RTC symbol that triggered the
 * signal detection event.
 * Creation date: (2000-08-25 1:09:33)
 * @author: Richard Deadman
 * @return net.sourceforge.gjtapi.media.SymbolHolder
 */
public net.sourceforge.gjtapi.media.SymbolHolder getTrigger() {
	return trigger;
}
/**
 * Factory method for when a SignalDetector initial timeout happens.  No signals have been detected at all.
 * RawProviders may call this to create an appropriate return value for the synchronous retreiveSignals
 * call.
 * Creation date: (2000-05-03 11:38:02)
 * @author: Richard Deadman
 * @return A serializable RawSigDetectEvent that may be returned from RawProvider::retreiveSignals(..).
 * @param term The name of the terminal the detector is attached to.
 * @param sigs Set of detected signals.
 */
public static RawSigDetectEvent initialTimeout(String term) {
	RawSigDetectEvent ev = new RawSigDetectEvent();
	ev.terminal = term;
	ev.qualifier = new SymbolHolder(SignalDetectorConstants.q_InitialTimeout);

	return ev;
}
/**
 * Factory method for when a SignalDetector inter signal timeout happens.  The space between signals was too long.
 * RawProviders may call this to create an appropriate return value for the synchronous retreiveSignals
 * call.
 * Creation date: (2000-05-03 11:38:02)
 * @author: Richard Deadman
 * @return A serializable RawSigDetectEvent that may be returned from RawProvider::retreiveSignals(..).
 * @param term The name of the terminal the detector is attached to.
 * @param sigs Set of detected signals.
 */
public static RawSigDetectEvent interSigTimeout(String term, Symbol[] sigs) {
	RawSigDetectEvent ev = new RawSigDetectEvent();
	ev.terminal = term;
	ev.qualifier = new SymbolHolder(SignalDetectorConstants.q_InterSigTimeout);
	ev.sigs = SymbolHolder.create(sigs);

	return ev;
}
/**
 * Factory method for when a SignalDetector maximum number is reached.
 * RawProviders may call this to create an appropriate return value for the synchronous retreiveSignals
 * call.
 * Creation date: (2000-05-03 11:38:02)
 * @author: Richard Deadman
 * @return A serializable RawSigDetectEvent that may be returned from RawProvider::retreiveSignals(..).
 * @param term The name of the terminal the detector is attached to.
 * @param sigs Set of detected signals.
 */
public static RawSigDetectEvent maxDetected(String term, Symbol[] sigs) {
	RawSigDetectEvent ev = new RawSigDetectEvent();
	ev.terminal = term;
	ev.qualifier = new SymbolHolder(SignalDetectorConstants.q_NumSignals);
	ev.sigs = SymbolHolder.create(sigs);

	return ev;
}
/**
 * Factory method for when a SignalDetector pattern is matched.
 * RawProviders may call this to create an appropriate return value for the synchronous retreiveSignals
 * call.
 * Creation date: (2000-05-03 11:38:02)
 * @author: Richard Deadman
 * @return A serializable RawSigDetectEvent that may be returned from RawProvider::retreiveSignals(..).
 * @param term The name of the terminal the detector is attached to.
 * @param index The pattern index that was matched.
 * @param sigs Set of detected signals.
 */
public static RawSigDetectEvent patternMatched(String term, int index, Symbol[] sigs) {
	RawSigDetectEvent ev = new RawSigDetectEvent();
	ev.terminal = term;
	ev.patternIndex = index;
	ev.sigs = SymbolHolder.create(sigs);

	return ev;
}
/**
 * Factory method for when a SignalDetector total synchronous timout happened.
 * RawProviders may call this to create an appropriate return value for the synchronous retreiveSignals
 * call.
 * Creation date: (2000-05-03 11:38:02)
 * @author: Richard Deadman
 * @return A serializable RawSigDetectEvent that may be returned from RawProvider::retreiveSignals(..).
 * @param term The name of the terminal the detector is attached to.
 * @param sigs Set of detected signals.
 */
public static RawSigDetectEvent rtcStopped(String term, Symbol trigger, Symbol[] sigs) {
	RawSigDetectEvent ev = new RawSigDetectEvent();
	ev.terminal = term;
	ev.qualifier = new SymbolHolder(SignalDetectorConstants.q_RTC);
	ev.trigger = new SymbolHolder(trigger);
	ev.sigs = SymbolHolder.create(sigs);

	return ev;
}
/**
 * Factory method for when a SignalDetector total synchronous timout happened.
 * RawProviders may call this to create an appropriate return value for the synchronous retreiveSignals
 * call.
 * Creation date: (2000-05-03 11:38:02)
 * @author: Richard Deadman
 * @return A serializable RawSigDetectEvent that may be returned from RawProvider::retreiveSignals(..).
 * @param term The name of the terminal the detector is attached to.
 * @param sigs Set of detected signals.
 */
public static RawSigDetectEvent timeout(String term, Symbol[] sigs) {
	RawSigDetectEvent ev = new RawSigDetectEvent();
	ev.terminal = term;
	ev.qualifier = new SymbolHolder(SignalDetectorConstants.q_Duration);
	ev.sigs = SymbolHolder.create(sigs);

	return ev;
}
}
