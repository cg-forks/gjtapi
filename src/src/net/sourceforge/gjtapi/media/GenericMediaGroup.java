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
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import javax.telephony.Terminal;
import javax.telephony.media.ConfigSpec;
import javax.telephony.media.MediaConfigException;
import javax.telephony.media.MediaResourceException;
import javax.telephony.media.MediaService;
import javax.telephony.media.Player;
import javax.telephony.media.PlayerConstants;
import javax.telephony.media.PlayerEvent;
import javax.telephony.media.RTC;
import javax.telephony.media.Recorder;
import javax.telephony.media.RecorderConstants;
import javax.telephony.media.RecorderEvent;
import javax.telephony.media.ResourceEvent;
import javax.telephony.media.ResourceSpec;
import javax.telephony.media.SignalDetector;
import javax.telephony.media.SignalDetectorConstants;
import javax.telephony.media.SignalDetectorEvent;
import javax.telephony.media.SignalGenerator;
import javax.telephony.media.SignalGeneratorConstants;
import javax.telephony.media.SignalGeneratorEvent;
import javax.telephony.media.Symbol;

import net.sourceforge.gjtapi.GenericProvider;
import net.sourceforge.gjtapi.RawSigDetectEvent;
import net.sourceforge.gjtapi.TelephonyProvider;
/**
 * A package-protected media group encapsualation.
 * This is an early simple architecture that will change or be replaced as the ECTF architecture matures.
 * Creation date: (2000-03-10 16:58:24)
 * @author: Richard Deadman
 */
class GenericMediaGroup implements Player, Recorder, SignalDetector, SignalGenerator {
	private GenericProvider prov = null;
	private javax.telephony.media.ConfigSpec configSpec = null;
	private final Set<ResourceSpec> resourceSet = new HashSet<ResourceSpec>();
	@SuppressWarnings("unchecked")
	private Dictionary dictionary = null;
	@SuppressWarnings("unchecked")
	private Dictionary parameters = null;
	private javax.telephony.Terminal terminal = null;
	/** 
	  * The MediaGroup is virtually bound in the raw provider.  We hold a terminal name
	  * and a possibly null handle to the holding MediaService. 
	  */
	private int resourceTypes = TelephonyProvider.MEDIA_RES_NONE;
	private javax.telephony.media.MediaService mediaService = null;
/**
 * Create a MediaGroup for a terminal with a set of resources as specified in the ConfigSpec.
 * Creation date: (2000-03-14 8:49:44)
 * @author: Richard Deadman
 * @param spec A collection of configuration parameters for this media group.
 * @param terminal The media terminal to bind the group to.
 */
public GenericMediaGroup(ConfigSpec spec, Terminal terminal, MediaService ms) throws MediaConfigException {
	super();

	this.setTerminal(terminal);
	this.setMediaService(ms);
	this.setProv((GenericProvider)terminal.getProvider());

	if (spec != null) {
		this.setConfigSpec(spec);
		this.allocate(spec.getResourceSpecs());
	} else {
		// define a default configuration spec resource set
		ResourceSpec[] rs = {new ResourceSpec(Player.class, null, null),
							new ResourceSpec(Recorder.class, null, null),
							new ResourceSpec(SignalGenerator.class, null, null),
							new ResourceSpec(SignalDetector.class, null, null)};
		this.allocate(rs);
	}
		
}
/**
 * Allocate the appropriate resources for the MediaGroup
 * Creation date: (2000-03-30 13:02:12)
 * @author: Richard Deadman
 * @param rs The set of new Resources to allocate
 */
@SuppressWarnings("unchecked")
public void allocate(ResourceSpec[] rs) throws MediaConfigException {
	if (rs == null)
		return;
	int newRes = 0;
	Set<ResourceSpec> resSet = this.getResources();
	for (int i = 0; i < rs.length; i++) {
	    Class cl = rs[i].getResourceClass();
	    if (Player.class.isAssignableFrom(cl))
	    	newRes &= TelephonyProvider.MEDIA_RES_PLAYER;
	    if (Recorder.class.isAssignableFrom(cl))
	    	newRes &= TelephonyProvider.MEDIA_RES_RECORDER;
	    if (SignalGenerator.class.isAssignableFrom(cl))
	    	newRes &= TelephonyProvider.MEDIA_RES_GENERATOR;
	    if (SignalDetector.class.isAssignableFrom(cl))
	    	newRes &= TelephonyProvider.MEDIA_RES_DETECTOR;

	    resSet.add(rs[i]);
	}
	// ask raw provider to allocate new resources, if necessary
	GenericProvider prov = this.getProv();
	Dictionary dict = this.getParameters();
	if (prov.getRawCapabilities().allocateMedia || dict != null) {
		int oldRes = this.getResourceTypes();
		prov.getRaw().allocateMedia(this.getTerminalName(), newRes | oldRes, dict);
		this.setResourceTypes(newRes & oldRes);
	}
}
/**
 * Called when the object is no longer held onto
 * Creation date: (2000-03-29 14:44:08)
 * @author: Richard Deadman
 */
@Override
public void finalize() {
	try {
		this.free();
	} catch (NullPointerException npe) {
		// NOP
	}
}
/**
 * Tell the signal detector to flush all signals so far collected
 */
public SignalDetectorEvent flushBuffer() throws javax.telephony.media.MediaResourceException {
	Dictionary<Symbol, Integer> dict = new Hashtable<Symbol, Integer>();
	dict.put(SignalDetectorConstants.p_Duration, new Integer(0));
	return this.retrieveSignals(Integer.MAX_VALUE, null, null, dict);
}
/**
 * Tell the MediaGroup to free its resources
 * Creation date: (2000-03-29 14:33:21)
 * @author: Richard Deadman
 */
void free() {
	String term = this.getTerminalName();
	if (term != null) {
		GenericProvider prov = this.getProv();
		if (prov.getRawCapabilities().allocateMedia) {
			this.getProv().getRaw().freeMedia(this.getTerminalName(), this.getResourceTypes());
			this.setResourceTypes(TelephonyProvider.MEDIA_RES_NONE);
		}
		this.setTerminal(null);
	}
	this.freeService();
}
/**
 * Tell the MediaGroup to free its MediaService
 * Creation date: (2000-03-29 14:33:21)
 * @author: Richard Deadman
 */
void freeService() {
	this.setMediaService(null);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-13 10:13:22)
 * @author: 
 * @return javax.telephony.media.ConfigSpec
 */
public javax.telephony.media.ConfigSpec getConfigSpec() {
	ConfigSpec orig = this.configSpec;
	ResourceSpec[] rs = (ResourceSpec[])this.getResources().toArray(new ResourceSpec[0]);

	return new ConfigSpec(rs,
				orig.getTimeout(),
				orig.getAttributes(),
				orig.getParameters(),
				orig.getRTC());
}
/**
 * Get the set of key-value pairs associated with this Media Group (call)
 * Creation date: (2000-03-13 10:14:47)
 * @author: Richard Deadman
 * @return The key-value collection.
 */
@SuppressWarnings("unchecked")
public Dictionary getDictionary() {
	if (this.dictionary == null)
		this.dictionary = new Hashtable();
	return this.dictionary;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-30 10:21:51)
 * @author: 
 * @return javax.telephony.media.MediaService
 */
public javax.telephony.media.MediaService getMediaService() {
	return mediaService;
}
/**
 * Get the set of key-value parameter pairs associated with this Media Group (call)
 * Creation date: (2000-03-13 10:14:47)
 * @author: Richard Deadman
 * @return The key-value collection.
 */
@SuppressWarnings("unchecked")
public Dictionary getParameters() {
	if (this.parameters == null)
		this.parameters = new Hashtable();
	return this.parameters;
}
/**
 * Internal accessor
 * Creation date: (2000-03-14 7:19:02)
 * @author: Richard Deadman
 * @return The framework manager.
 */
private net.sourceforge.gjtapi.GenericProvider getProv() {
	return prov;
}
/**
 * Internal accessor of a Set of Resources allocated for the MediaGroup
 * Creation date: (2000-03-30 13:35:23)
 * @author: Richard Deadman
 * @return java.util.Set
 */
private Set<ResourceSpec> getResources() {
	return this.resourceSet;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-29 14:36:35)
 * @author: 
 * @return int
 */
private int getResourceTypes() {
	return resourceTypes;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-13 10:16:23)
 * @author: 
 * @return javax.telephony.Terminal
 */
javax.telephony.Terminal getTerminal() {
	return terminal;
}
/**
 * Convenience method to safely retrieve the name of the terminal
 * @return name of the terminal, <code>null</code> if there is no terminal
 */
String getTerminalName() {
    Terminal term = this.getTerminal();
    if (term == null) {
        return null;
    } else {
        return term.getName();
    }
}
	boolean isDone() {return true;}
/**
 * play method comment.
 */
@SuppressWarnings("unchecked")
public PlayerEvent play(String[] streamIDs, int offset, RTC[] rtc, Dictionary optargs) throws MediaResourceException {
	int len = streamIDs.length;
	// test for no stream ids
	if (len != 0) {
		// test if the streamIds mix fax and non-fax URL
		String prefix = "fax:";
		boolean fax = false;
		boolean nonFax = false;
		for (int i = 0; i < len; i++) {
			String id = streamIDs[i];
			if (id != null) {
				if (id.toLowerCase().startsWith(prefix)) {
					fax = true;
				} else {
					nonFax = true;
				}
				if (fax && nonFax)
					throw new MediaResourceException("Mixed Fax and non-Fax streamId URLs");
			}
		}
	
		try {
		    final TelephonyProvider raw = getProv().getRaw();
		    raw.play(this.getTerminalName(), streamIDs, offset, rtc, optargs);
		} catch (MediaResourceException mre) {
		    // morph and rethrow the event
			ResourceEvent re = mre.getResourceEvent();
			if (re != null && re instanceof GenericResourceEvent) {
				((GenericResourceEvent)re).morph(this.getProv());
			}
			throw mre;
		}
	}
	return new GenericPlayerEvent(PlayerConstants.ev_Play,
						this.getMediaService(),
						null, null, null, null, 0, 0);
}
/**
 * play method comment.
 */
@SuppressWarnings("unchecked")
public PlayerEvent play(String streamID, int offset, RTC[] rtc, Dictionary optargs) throws MediaResourceException {
	String[] strs = {streamID};
	return this.play(strs, offset, rtc, optargs);
}
/**
 * record method comment.
 */
@SuppressWarnings("unchecked")
public RecorderEvent record(String streamID, RTC[] rtc, Dictionary optargs) throws MediaResourceException {
	try {
		this.getProv().getRaw().record(this.getTerminalName(), streamID, rtc, optargs);
	} catch (MediaResourceException mre) {
			// morph and rethrow the event
		ResourceEvent re = mre.getResourceEvent();
		if (re != null && re instanceof GenericResourceEvent) {
			((GenericResourceEvent)re).morph(this.getProv());
		}
		throw mre;
	}

	return new GenericRecorderEvent(RecorderConstants.ev_Record,
				this.getMediaService(),
				null, null, null, 0);
}

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
	public SignalDetectorEvent retrieveSignals(int numSignals,
            Symbol[] patterns, RTC[] rtc, Dictionary optargs)
            throws MediaResourceException {
        GenericProvider provider = getProv();
        try {
            TelephonyProvider rawProvider = provider.getRaw();
            RawSigDetectEvent event = rawProvider.retrieveSignals(
                    this.getTerminalName(), numSignals, patterns, rtc, optargs);
            if (event == null) {
                return null;
            }
            return event.buildEvent(provider);
        } catch (MediaResourceException mre) {
            // morph and rethrow the event
            ResourceEvent re = mre.getResourceEvent();
            if (re != null && re instanceof GenericResourceEvent) {
                ((GenericResourceEvent) re).morph(provider);
            }
            throw mre;
        }

    }

    /**
 * sendSignals method comment.
 */
@SuppressWarnings("unchecked")
public SignalGeneratorEvent sendSignals(Symbol[] signals, RTC[] rtc, Dictionary optargs) throws MediaResourceException {
	try {
		this.getProv().getRaw().sendSignals(this.getTerminalName(), signals, rtc, optargs);
	} catch (MediaResourceException mre) {
			// morph and rethrow the event
		ResourceEvent re = mre.getResourceEvent();
		if (re != null && re instanceof GenericResourceEvent) {
			((GenericResourceEvent)re).morph(this.getProv());
		}
		throw mre;
	}
	SignalGeneratorEvent sge = new GenericSignalGeneratorEvent(SignalGeneratorConstants.ev_SendSignals,
								this.getMediaService(),
								null, null, null);
	return sge;
}
/**
 * sendSignals method comment.
 */
@SuppressWarnings("unchecked")
public SignalGeneratorEvent sendSignals(String signals, RTC[] rtc, Dictionary optargs) throws MediaResourceException {
	Symbol[] syms = SymbolConvertor.convert(signals);
	return this.sendSignals(syms, rtc, optargs);
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-13 10:13:22)
 * @author: 
 * @param newConfigSpec javax.telephony.media.ConfigSpec
 */
protected void setConfigSpec(javax.telephony.media.ConfigSpec newConfigSpec) {
	configSpec = newConfigSpec;

	this.setParameters(newConfigSpec.getParameters());
}
/**
 * Set the dictionary of key-value pairs associated with a Media Group
 * Creation date: (2000-03-13 10:14:47)
 * @author: Richard Deadman
 * @param newDictionary The replacement set of values
 */
@SuppressWarnings("unchecked")
public void setDictionary(Dictionary newDictionary) {
	if (newDictionary == null)
		dictionary = new Hashtable();
	else
		dictionary = newDictionary;
}
/**
 * Set a MediaService for this group
 * Creation date: (2000-03-30 10:21:51)
 * @author: Richard Deadman
 * @param newMediaService The MediaService that now controls the Media Group.
 */
void setMediaService(javax.telephony.media.MediaService newMediaService) {
	mediaService = newMediaService;
}
/**
 * Set the dictionary of key-value parameter pairs associated with a Media Group.
 * This is passsed onto the raw provider if it is different from the existing paramter set.
 * Creation date: (2000-03-13 10:14:47)
 * @author: Richard Deadman
 * @param newDictionary The replacement set of values
 */
@SuppressWarnings("unchecked")
public void setParameters(Dictionary newParams) {
	Dictionary oldParams = this.getDictionary();
	
	if (!oldParams.equals(newParams)) {
		parameters = newParams;

		this.getProv().getRaw().allocateMedia(this.getTerminalName(), this.getResourceTypes(), newParams);
	}

}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-14 7:19:02)
 * @author: 
 * @param newProv net.sourceforge.gjtapi.GenericProvider
 */
private void setProv(net.sourceforge.gjtapi.GenericProvider newProv) {
	prov = newProv;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-29 14:36:35)
 * @author: 
 * @param newResourceTypes int
 */
private void setResourceTypes(int newResourceTypes) {
	resourceTypes = newResourceTypes;
}
/**
 * Insert the method's description here.
 * Creation date: (2000-03-13 10:16:23)
 * @author: 
 * @param newTerminal javax.telephony.Terminal
 */
private void setTerminal(javax.telephony.Terminal newTerminal) {
	terminal = newTerminal;
}
}
