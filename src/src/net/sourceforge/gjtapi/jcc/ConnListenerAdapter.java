package net.sourceforge.gjtapi.jcc;

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
import net.sourceforge.gjtapi.FreeConnection;
import javax.jain.services.jcc.*;
import javax.telephony.*;
import javax.telephony.callcontrol.*;
/**
 * This is a simple adapter that listens for JTAPI Connection events and translates
 * them into JccConnection events.
 * Creation date: (2000-10-11 15:09:31)
 * @author: Richard Deadman
 */
public class ConnListenerAdapter extends CallListenerAdapter implements ConnectionListener {
	private JccConnectionListener realConnectionListener = null;
	private EventFilter connFilter = null;
/**
 * EventConnectionAdapter constructor comment.
 */
public ConnListenerAdapter(Provider prov,
		JccConnectionListener listener,
		EventFilter filter) {
	super(prov, listener);

	this.setRealConnectionListener(listener);
	this.setConnFilter(filter);
}
/**
 * Send an Address_Analyze event off to the listener, and have the listener update the Jcc
 * state flag.
 * Creation date: (2000-11-15 13:53:10)
 * @param conn net.sourceforge.gjtapi.FreeConnection
 * @param cause int
 */
public void connectionAddressAnalyse(net.sourceforge.gjtapi.FreeConnection conn, int cause) {
	GenConnection gc = this.getProv().findConnection(conn);
	if (gc != null)
		gc.setJccState(JccConnection.ADDRESS_ANALYZE);

	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new ExtraConnEvent(gc,
									JccConnectionEvent.CONNECTION_ADDRESS_ANALYZE,
									cause);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD) {
		this.getRealConnectionListener().connectionAddressAnalyze(ce);
	}
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * Send an Address_Collect event off to the listener, and have the listener update the Jcc
 * state flag.
 * Creation date: (2000-11-15 13:53:10)
 * @param conn net.sourceforge.gjtapi.FreeConnection
 * @param cause int
 */
public void connectionAddressCollect(net.sourceforge.gjtapi.FreeConnection conn, int cause) {
	GenConnection gc = this.getProv().findConnection(conn);
	if (gc != null)
		gc.setJccState(JccConnection.ADDRESS_COLLECT);

	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new ExtraConnEvent(gc,
									JccConnectionEvent.CONNECTION_ADDRESS_COLLECT,
									cause);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD) {
		this.getRealConnectionListener().connectionAddressCollect(ce);
	}
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * connectionAlerting method comment.
 */
public void connectionAlerting(ConnectionEvent event) {
	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new GenConnEvent(this.getProv(), event);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD)
		this.getRealConnectionListener().connectionAlerting(ce);
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * Send an Address_Analyze event off to the listener, and have the listener update the Jcc
 * state flag.
 * Creation date: (2000-11-15 13:53:10)
 * @param conn net.sourceforge.gjtapi.FreeConnection
 * @param cause int
 */
public void connectionAuthorizeCallAttempt(net.sourceforge.gjtapi.FreeConnection conn, int cause) {
	GenConnection gc = this.getProv().findConnection(conn);
	if (gc != null)
		gc.setJccState(JccConnection.AUTHORIZE_CALL_ATTEMPT);

	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new ExtraConnEvent(gc,
									JccConnectionEvent.CONNECTION_AUTHORIZE_CALL_ATTEMPT,
									cause);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD) {
		this.getRealConnectionListener().connectionAuthorizeCallAttempt(ce);
	}
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * Send an Address_Analyze event off to the listener, and have the listener update the Jcc
 * state flag.
 * Creation date: (2000-11-15 13:53:10)
 * @param conn net.sourceforge.gjtapi.FreeConnection
 * @param cause int
 */
public void connectionCallDelivery(net.sourceforge.gjtapi.FreeConnection conn, int cause) {
	GenConnection gc = this.getProv().findConnection(conn);
	if (gc != null)
		gc.setJccState(JccConnection.CALL_DELIVERY);

	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new ExtraConnEvent(gc,
									JccConnectionEvent.CONNECTION_CALL_DELIVERY,
									cause);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD) {
		this.getRealConnectionListener().connectionCallDelivery(ce);
	}
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * connectionAlerting method comment.
 */
public void connectionConnected(ConnectionEvent event) {
	// update the override state
	GenConnection gc = this.getProv().findConnection((FreeConnection)event.getConnection());
	if (gc != null)
		gc.setJccState(JccConnection.CONNECTED);

	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new GenConnEvent(this.getProv(), event);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD)
		this.getRealConnectionListener().connectionConnected(ce);
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * connectionAlerting method comment.
 */
public void connectionCreated(ConnectionEvent event) {
	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new GenConnEvent(this.getProv(), event);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD)
		this.getRealConnectionListener().connectionCreated(ce);
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * connectionAlerting method comment.
 */
public void connectionDisconnected(ConnectionEvent event) {
	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new GenConnEvent(this.getProv(), event);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD)
		this.getRealConnectionListener().connectionDisconnected(ce);
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * connectionAlerting method comment.
 */
public void connectionFailed(ConnectionEvent event) {
	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new GenConnEvent(this.getProv(), event);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD)
		this.getRealConnectionListener().connectionFailed(ce);
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * connectionAlerting method comment.
 */
public void connectionInProgress(ConnectionEvent event) {
	// update the override state
	GenConnection gc = this.getProv().findConnection((FreeConnection)event.getConnection());
	if (gc != null)
		gc.setJccState(JccConnection.INPROGRESS);

	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new GenConnEvent(this.getProv(), event);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD)
		this.getRealConnectionListener().connectionInProgress(ce);
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * Send an Address_Analyze event off to the listener, and have the listener update the Jcc
 * state flag.
 * Creation date: (2000-11-15 13:53:10)
 * @param conn net.sourceforge.gjtapi.FreeConnection
 * @param cause int
 */
public void connectionSuspended(net.sourceforge.gjtapi.FreeConnection conn, int cause) {
	GenConnection gc = this.getProv().findConnection(conn);
	if (gc != null)
		gc.setJccState(JccConnection.SUSPENDED);

	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new ExtraConnEvent(gc,
									JccConnectionEvent.CONNECTION_SUSPENDED,
									cause);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD) {
		this.getRealConnectionListener().connectionSuspended(ce);
	}
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * connectionAlerting method comment.
 */
public void connectionUnknown(ConnectionEvent event) {
	EventFilter filter = this.getConnFilter();
	JccConnectionEvent ce = new GenConnEvent(this.getProv(), event);
	int disposition = EventFilter.EVENT_NOTIFY;
	if (filter != null)
		disposition = filter.getEventDisposition(ce);
	if (disposition != EventFilter.EVENT_DISCARD)
		this.getRealConnectionListener().connectionUnknown(ce);
	if (disposition == EventFilter.EVENT_BLOCK)
		((GenConnection)ce.getConnection()).setBlocked(true);
}
/**
 * Get the filter that determines event disposition.
 * Creation date: (2000-10-30 10:24:26)
 * @return jain.application.services.jcc.JccConnectionEventFilter
 */
private EventFilter getConnFilter() {
	return connFilter;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:24:26)
 * @return jain.application.services.jcc.JccConnectionListener
 */
private JccConnectionListener getRealConnectionListener() {
	return realConnectionListener;
}
/**
 * Allow my filter to be updated at runtime.
 * Creation date: (2000-10-30 10:24:26)
 * @param newConnFilter jain.application.services.jcc.EventFilter
 */
private void setConnFilter(EventFilter newConnFilter) {
	connFilter = newConnFilter;
}
/**
 * Allow my filter to be updated at runtime.
 * Creation date: (2000-10-30 10:24:26)
 * @param newConnFilter jain.application.services.jcc.EventFilter
 */
void setFilter(EventFilter newConnFilter) {
	this.setConnFilter(newConnFilter);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-10-30 10:24:26)
 * @param newRealConnectionListener jain.application.services.jcp.JcpConnectionListener
 */
private void setRealConnectionListener(JccConnectionListener newRealConnectionListener) {
	realConnectionListener = newRealConnectionListener;
}
/**
 * Returns a String that represents the value of this object.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Listener adapter for Jcc Connection Listener: " + this.getRealConnectionListener();
}
}
