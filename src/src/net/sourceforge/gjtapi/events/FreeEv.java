package net.sourceforge.gjtapi.events;

/*
	Copyright (c) 1999,2002 Westhawk Ltd (www.westhawk.co.uk) 
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
import javax.telephony.events.*;
import javax.telephony.Event;
/**
 * A base abstract class for all the Generic JTAPI events.
 */
public abstract class FreeEv implements Ev, Event {
	private int _cause;
	private int _metaCode;
	private boolean _isNewMetaEvent;
	protected transient Object[] _Objects;
	private javax.telephony.MetaEvent metaEvent = null;
/**
 * FreeEv constructor comment.
 */
public FreeEv() {
	super();
	_cause = Ev.CAUSE_UNKNOWN;
	_metaCode = Ev.META_UNKNOWN;
	_isNewMetaEvent = false;
}
/**
 * This method was created in VisualAge.
 * @param id int
 * @param cause int
 * @param metaCode int
 * @param isNewMetaEvent boolean
 */
public FreeEv(int cause, int metaCode, boolean isNewMetaEvent) {
	super();
	_cause = cause;
	_metaCode = metaCode;
	_isNewMetaEvent = isNewMetaEvent;
}
/**
 * given a new-style Event cause, translate to Ev style cause.
 * This is currently not used, but since the events causes have the same values,
 * it is not needed.  Using it would require that our combined events morph themselves
 * depending on if they go to a Listener or an Observer.
 */
public static int causeMap(int eventCause){
  int ret = Ev.CAUSE_UNKNOWN;
  switch (eventCause) {
	case Event.CAUSE_CALL_CANCELLED:
	  ret= Ev.CAUSE_CALL_CANCELLED;
	  break;
	case Event.CAUSE_DEST_NOT_OBTAINABLE:
	  ret= Ev.CAUSE_DEST_NOT_OBTAINABLE;
	  break;
	case Event.CAUSE_INCOMPATIBLE_DESTINATION:
	  ret= Ev.CAUSE_INCOMPATIBLE_DESTINATION;
	  break;
	case Event.CAUSE_LOCKOUT:
	  ret= Ev.CAUSE_LOCKOUT;
	  break;
	case Event.CAUSE_NETWORK_CONGESTION:
	  ret= Ev.CAUSE_NETWORK_CONGESTION;
	  break;
	case Event.CAUSE_NETWORK_NOT_OBTAINABLE:
	  ret= Ev.CAUSE_NETWORK_NOT_OBTAINABLE;
	  break;
	case Event.CAUSE_NEW_CALL:
	  ret= Ev.CAUSE_NEW_CALL;
	  break;
	case Event.CAUSE_NORMAL:
	  ret= Ev.CAUSE_NORMAL;
	  break;
	case Event.CAUSE_RESOURCES_NOT_AVAILABLE:
	  ret= Ev.CAUSE_RESOURCES_NOT_AVAILABLE;
	  break;
	case Event.CAUSE_SNAPSHOT:
	  ret= Ev.CAUSE_SNAPSHOT;
	  break;
	case Event.CAUSE_UNKNOWN:
	default:
	  ret= Ev.CAUSE_UNKNOWN;
	  break;
  }
  return ret;
}
/**
 * getCause method comment.
 */
public int getCause() {
	return _cause;
}
/**
 * Get the observer switch id for the event.
 * Concrete subclasses implement this.
 */
public abstract int getID();
/**
 * getMetaCode method comment.
 */
public int getMetaCode() {
	return _metaCode;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-04-18 10:21:12)
 * @author: 
 * @return javax.telephony.MetaEvent
 */
public javax.telephony.MetaEvent getMetaEvent() {
	return metaEvent;
}
  /**
   * Returns the object that is being observed.
   * <p>
   * <STRONG>Note:</STRONG>Implementation need no longer supply this
   * information. The <CODE>CallObsevationEndedEv.getObservedObject()</CODE>
   * method has been added which returns related information. This method may
   * return null in JTAPI v1.2 and later.
   * <p>
   * @deprecated Since JTAPI v1.2 This interface no longer needs to supply this
   * information and may return null.
   * @return The object that is being observed.
   */
public Object getObserved() {
	return null;
}
/**
 * isNewMetaEvent method comment.
 */
public boolean isNewMetaEvent() {
	return _isNewMetaEvent;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-04-18 10:21:12)
 * @author: 
 * @param newMetaEvent javax.telephony.MetaEvent
 */
public void setMetaEvent(javax.telephony.MetaEvent newMetaEvent) {
	metaEvent = newMetaEvent;
}
}
