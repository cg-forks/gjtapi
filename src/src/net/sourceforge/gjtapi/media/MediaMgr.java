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
import java.lang.ref.WeakReference;
import java.util.*;
import javax.telephony.media.*;
/**
 * Manager of MediaServices -- mapping services to MediaTerminals and to bound service names.
 * Creation date: (2000-03-29 11:24:54)
 * @author: Richard Deadman
 */
public class MediaMgr {
	private Dictionary activeServices = new Hashtable();	// terminalName -> WeakReference(MediaServiceHolder)
	private Hashtable waitingServices = new Hashtable();	// MediaService -> serviceName
		// The following is a time-bound set of Call-tagged MediaGroups waiting to be passed to
		// a MediaService.
	private Dictionary waitingMediaGroups = new Hashtable();	// serviceName -> Set(MediaGroups)
/**
 * Assign a MediaService to a MediaGroup that is waiting for a Media Service with a certain serviceName
 * Creation date: (2000-03-30 10:26:35)
 * @author: Richard Deadman
 * @param serviceName The name of the service (MediaService) that is now available to take a Media Group
 * @return true if the MediaService was assigned to a Media Group.
 */
private boolean assignWaitingMediaGroup(String serviceName, MediaService ms) {
	Dictionary dict = this.getWaitingMediaGroups();

	synchronized (dict) {
		// find the set of already waiting media groups
		Object o = dict.get(serviceName);
		if (o != null && o instanceof Set) {
			Set groups = (Set)o;
			if (!groups.isEmpty()) {
				Iterator it = groups.iterator();
				GenericMediaGroup mg = (GenericMediaGroup)it.next();

				// assign my service to the MediaGroup
				mg.setMediaService(ms);

				// notify the method waiting on this assignment
				synchronized (mg) {
					mg.notify();
				}

				return true;
			}
		}
	}
	return false;
}
/**
 * Add a Terminal to MediaService entry.
 * This notes that the given terminal is currently bound to the MediaService
 * Creation date: (2000-03-08 12:42:45)
 * @author: Richard Deadman
 * @param term The terminal the MediaService is assigned to.
 * @param msh The pseudo media service which is allocated to the terminal.
 */
public void bind(String term, MediaServiceHolder msh) {
	this.getActiveServices().put(term, new WeakReference(msh));
}
/**
 * Find the first MediaService that is registered for a service name.
 * Creation date: (2000-03-08 12:42:45)
 * @author: Richard Deadman
 * @return A MediaService, or null if none registered.
 * @param serviceName The serviceName the service is currently in limbo waiting to serve.
 */
public MediaService findForService(String serviceName) {
	Map.Entry entry = this.privateFindForName(serviceName);
	if (entry != null)
		return (MediaService)entry.getKey();
	return null;
}
/**
 * Find the MediaService that is registered for this terminal.
 * This involves looking for a WeakReference for the MediaService bound to the terminal and dereferencing it.
 * Creation date: (2000-03-08 12:42:45)
 * @author: Richard Deadman
 * @return A MediaService connected to the named terminal
 * @param term The name of a terminal we want to find the MediaService for
 */
public MediaServiceHolder findForTerminal(String term) {
	WeakReference ref = (WeakReference)this.getActiveServices().get(term);
	if (ref != null)
		return (MediaServiceHolder)ref.get();
	else
		return null;
}
/**
 * Internal accessor
 * Creation date: (2000-03-08 12:45:35)
 * @author: Richard Deadman
 * @return A Dictionary of terminal name to MediaService mappings.
 */
private java.util.Dictionary getActiveServices() {
	return this.activeServices;
}
/**
 * Internal accessor for MediaGroups waiting to have a MediaService made available for them.
 * When a MediaService releases a call to another MediaService by name (MediaService.releaseToService()),
 * there may not be an available MediaService for that call yet.  In that case, the call's MediaGroup
 * can be temporarily placed in a waiting area, with a timer set to remove it if a MediaService does not
 * appear in x milliseconds.
 * Creation date: (2000-03-08 12:45:35)
 * @author: Richard Deadman
 * @return A Hashtable of serviceName to sets of waiting MediaGroups
 */
private Dictionary getWaitingMediaGroups() {
	return this.waitingMediaGroups;
}
/**
 * Internal accessor for MediaServices bound to a name.
 * When a call is delivered to a service name (possibly by another MediaService), the MediaMgr looks up
 * a MediaService bound to that name so that the call's MediaGroup can be assigned to the MediaService
 * and the MediaService may restart from it held state.
 * Creation date: (2000-03-08 12:45:35)
 * @author: Richard Deadman
 * @return A Hashtable of MediaService to serviceName entries
 */
private Hashtable getWaitingServices() {
	return this.waitingServices;
}
/**
 * Find the first MediaService that is registered for a service name.
 * This is an internal entry access method shared by other methods.
 * Creation date: (2000-03-08 12:42:45)
 * @author: Richard Deadman
 * @return A Map.Entry for the first found entry, or null
 * @param serviceName The serviceName the service is currently in limbo waiting to serve.
 */
private Map.Entry privateFindForName(String serviceName) {
	Set entries = this.getWaitingServices().entrySet();
	Iterator it = entries.iterator();
	while (it.hasNext()) {
		Map.Entry ent = (Map.Entry)it.next();
		if (ent.getValue().equals(serviceName))
			return ent;
	}
	return null;
}
/**
 * This puts a MediaGroup into the waiting queue.
 * Creation date: (2000-03-30 10:26:35)
 * @author: Richard Deadman
 * @param serviceName The name of the service (MediaService) the media group is waiting for.
 * @param group The Media Group temporarily waiting
 */
private void putWaitingMediaGroup(String serviceName, GenericMediaGroup group) {
	Dictionary dict = this.getWaitingMediaGroups();

	synchronized (dict) {
		// find the set of already waiting media groups
		Object o = dict.get(serviceName);
		Set groups = null;
		boolean newSet = false;
		if (o != null && o instanceof Set) {
			groups = (Set)o;
		} else {
			groups = new HashSet();
			newSet = true;
		}

		// add the new group and add to dictionary if necessary
		groups.add(group);
		if (newSet)
			dict.put(serviceName, groups);
	}
}
/**
 * Assign a MediaService to a serviceName.
 * Calls that are directed to that service name will be bound the a waiting media service waiting.
 * Note that the service is the key in the table, since more than one service can be bound to the
 * same service name.  MediaServices should implement "equals()" and "hashCode()" correctly.
 * Creation date: (2000-03-08 12:42:45)
 * @author: Richard Deadman
 * @param serviceName The name that the MediaService is registered against.
 * @param ms The media service which is registered.
 */
public void register(String serviceName, MediaService ms) {
	// first see if we can assign it to a waiting MediaGroup
	if (!assignWaitingMediaGroup(serviceName, ms))
		this.getWaitingServices().put(ms, serviceName);
}
/**
 * Unassign a Terminal to MediaService entry
 * Creation date: (2000-03-08 12:42:45)
 * @author: Richard Deadman
 * @param term The terminal the MediaService is no longer assigned to.
 * @return true if the terminal had the MediaService assigned to it.
 */
public boolean release(String term) {
	return (this.getActiveServices().remove(term) != null);
}
/**
 * This removes a MediaGroup from the waiting queue.
 * Creation date: (2000-03-30 10:26:35)
 * @author: Richard Deadman
 * @param serviceName The name of the service (MediaService) the media group was waiting for.
 * @param group The Media Group to remove
 */
private void removeWaitingMediaGroup(String serviceName, GenericMediaGroup group) {
	Dictionary dict = this.getWaitingMediaGroups();

	synchronized (dict) {
		// find the set of already waiting media groups
		Object o = dict.get(serviceName);
		if (o != null && o instanceof Set) {
			Set groups = (Set)o;
			groups.remove(group);
		}
	}
}
/**
 * Describe myself.
 * @return a string representation of the receiver
 */
public String toString() {
	return "Media Manager with " + this.getActiveServices().size() + " active and " + this.getWaitingServices().size() + " bound services.";
}
/**
 * Find the first MediaService registered to a serviceName, remove it and return it.
 * Calls that are directed to that service name will be bound the a waiting media service waiting.
 * Note that the service is the key in the table, since more than one service can be bound to the
 * same service name.  MediaServices should implement "equals()" and "hashCode()" correctly.
 * Creation date: (2000-03-08 12:42:45)
 * @author: Richard Deadman
 * @param serviceName The service name to find a MediaService for.
 * @return The unregistered MediaService, or null if none found.
 */
public MediaService unRegister(String serviceName) {
	Map.Entry entry = this.privateFindForName(serviceName);
	if (entry != null) {
		MediaService ms = (MediaService)entry.getKey();
		this.getWaitingServices().remove(ms);
		return ms;
	}
	return null;
}
/**
 * Register a MediaGroup to wait "timeout" milliseconds for a MediaService to be registered for
 * a serviceName.
 * Creation date: (2000-03-30 10:10:48)
 * @author: Richard Deadman
 * @return The MediaService that is available to take over processing of the call
 * @param group net.sourceforge.gjtapi.media.GenericMediaGroup
 * @param serviceName The service name the MediaService is registered under
 * @param timeout The number of milliseconds to wait for a MediaService to take over.
 */
public MediaService waitForMediaService(GenericMediaGroup group, String serviceName, int timeout)
	throws NoServiceReadyException {
	this.putWaitingMediaGroup(serviceName, group);
	synchronized (group) {
		try {
			group.wait(timeout);
		} catch (InterruptedException ie) {
			// drop through
		}
	}
	// See if we were allocated a MediaService
	MediaService ms = group.getMediaService();
	if (ms == null) {
		this.removeWaitingMediaGroup(serviceName, group);
		throw new NoServiceReadyException("timed out");
	}
	return ms;
}
}
